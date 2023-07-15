/*
Copyright (c) 2023, Damon Hart-Davis

Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package org.hd.d.statsHouse.midi.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.hd.d.statsHouse.GenerationParameters;
import org.hd.d.statsHouse.data.DataBounds;
import org.hd.d.statsHouse.data.DataProtoBar;
import org.hd.d.statsHouse.data.Datum;
import org.hd.d.statsHouse.data.EOUDataCSV;
import org.hd.d.statsHouse.generic.ChorusStyleFromData;
import org.hd.d.statsHouse.generic.NoteAndVelocity;
import org.hd.d.statsHouse.generic.Scale;
import org.hd.d.statsHouse.generic.TuneSection;
import org.hd.d.statsHouse.generic.TuneSectionMetadata;
import org.hd.d.statsHouse.midi.MIDIGen;
import org.hd.d.statsHouse.midi.MIDIPlayableMonophonicDataBar;

/**Simple data-melody-track bar(s) generation. */
public final class DataChorusGen
	{
    /**Prevent creation of an instance. */
    private DataChorusGen() { }


	/**Generate a house chorus data melody track section.
	 * TODO: implement other types
	 * @param stream  data stream index, first is 1; strictly positive
	 * @param ts  tune section; never null
	 * @param params  generation parameters; never null
	 * @param db  data bounds; never null
	 * @param data  full input data set; never null
	 * @param scale scale to use; never null
	 * @param chorusIndex  chorus index, first is 1; strictly positive
	 *
	 * @return chorus section data melody track segment of correct section length; never null
	 */
	public static List<MIDIPlayableMonophonicDataBar> makeHouseDataChorusBars(
			final ChorusStyleFromData style,
			final int chorusCount,
			final int stream,
			final TuneSectionMetadata ts,
			final GenerationParameters params, final DataBounds db, final EOUDataCSV data,
			final Scale scale)
		{
		if(chorusCount < 1) { throw new IllegalArgumentException(); }
		if(stream < 1) { throw new IllegalArgumentException(); }
		Objects.requireNonNull(ts);
		Objects.requireNonNull(params);
		Objects.requireNonNull(db);
		Objects.requireNonNull(data);
		Objects.requireNonNull(scale);

		// Skip any secondary data stream by returning empty bars.
		if(!db.isMainDataStream(stream) && !params.hetero())
		    { return(Collections.nCopies(ts.bars(), MIDIPlayableMonophonicDataBar.EMPTY_1_NOTE_BAR)); }

		// Parameterisation of melody play with scales.
		final int octaves = MIDIGen.DEFAULT_RANGE_OCTAVES; // Math.max(1, DEFAULT_RANGE_OCTAVES/2);
		final List<DataProtoBar> verseProtoBars = MIDIGen.splitAndAlignData(TuneSection.verse, params, data);
		// If there are no data bars, return empty section.
		if(verseProtoBars.isEmpty())
	    	{ return(Collections.nCopies(ts.bars(), MIDIPlayableMonophonicDataBar.EMPTY_1_NOTE_BAR)); }

	    switch(style) {
	        default:
	        case FirstFullDataBar:
	        nextBar: for(final DataProtoBar dbp : verseProtoBars)
		        {
				final int dnpb = dbp.dataNotesPerBar();
				final List<List<String>> rows = dbp.dataRows().data();
                if(rows.isEmpty()) { continue; }
                final List<NoteAndVelocity> notes = new ArrayList<>(dnpb);
    			for(final List<String> row : rows)
    				{
    				// Skip this bar for missing (or no-coverage) data.
    				final Datum d = Datum.extractDatum(stream, row);
    				if(null == d.value()) { continue nextBar; }
					if(null == d.coverage()) { continue nextBar; }
					if(0 == d.coverage()) { continue nextBar; }
    				// Skip this bar if the note is silent.
        			final NoteAndVelocity n = MIDIGen.datumToNoteAndVelocity(
    					d,
    					true, // isNotSecondaryDataStream,
    					scale,
    					octaves,
    					db.maxVal());
        			if(null == n) { continue nextBar; }
        			if(0 == n.velocity()) { continue nextBar; }
    				notes.add(n);
    				}

    			// A full bar has been located.
    			// Return repeated MIDI-playable bar for this stream.
    			final MIDIPlayableMonophonicDataBar mpmb = new MIDIPlayableMonophonicDataBar(
    					dnpb, dbp, stream, Collections.unmodifiableList(notes));
    			return(Collections.nCopies(ts.bars(), mpmb));
		        }
        	// Fall back to FirstDataBar if no full bar found.

	    	case FirstDataBar:
	        final DataProtoBar dbp = verseProtoBars.get(0);
			final List<List<String>> rows = dbp.dataRows().data();
			final int dnpb = dbp.dataNotesPerBar();
//assert(dnpb == rows.size());
			final List<NoteAndVelocity> notes = new ArrayList<>(dnpb);
			for(final List<String> row : rows)
				{
				final Datum d = Datum.extractDatum(stream, row);
				// Rest/silence for missing stream or value,
				// or where coverage is not strictly positive.
    			final NoteAndVelocity n = MIDIGen.datumToNoteAndVelocity(
					d,
					true, // isNotSecondaryDataStream,
					scale,
					octaves,
					db.maxVal());
				notes.add(n);
				}

			// Return repeated MIDI-playable bar for this stream.
			final MIDIPlayableMonophonicDataBar mpmb = new MIDIPlayableMonophonicDataBar(
					dnpb, dbp, stream, Collections.unmodifiableList(notes));
			return(Collections.nCopies(ts.bars(), mpmb));
	        }
		}
	}
