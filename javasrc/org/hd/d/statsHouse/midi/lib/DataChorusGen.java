package org.hd.d.statsHouse.midi.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.hd.d.statsHouse.ChorusStyleFromData;
import org.hd.d.statsHouse.DataProtoBar;
import org.hd.d.statsHouse.Datum;
import org.hd.d.statsHouse.EOUDataCSV;
import org.hd.d.statsHouse.GenerationParameters;
import org.hd.d.statsHouse.NoteAndVelocity;
import org.hd.d.statsHouse.TuneSection;
import org.hd.d.statsHouse.TuneSectionMetadata;
import org.hd.d.statsHouse.midi.DataBounds;
import org.hd.d.statsHouse.midi.MIDIGen;
import org.hd.d.statsHouse.midi.MIDIPlayableMonophonicDataBar;

/**Simple data-melody-track bar(s) generation. */
public final class DataChorusGen
	{
    /**Prevent creation of an instance. */
    private DataChorusGen() { }

	/**Generate a house chorus data melody track section.
	 *
	 * @param chorusIndex  chorus index, first is 1; strictly positive
	 * @param stream  data stream index, first is 1; strictly positive
	 * @param ts  tune section; never null
	 * @param params  generation parameters; never null
	 * @param db  data bounds; never null
	 * @param data  full input data set; never null
	 * @return  chorus section data melody track segment of correct section length; never null
	 */
	public static List<MIDIPlayableMonophonicDataBar> makeHouseDataChorusBars(
			final int chorusCount,
			final int stream,
			final TuneSectionMetadata ts,
			final GenerationParameters params, final DataBounds db, final EOUDataCSV data)
		{
		if(chorusCount < 1) { throw new IllegalArgumentException(); }
		if(stream < 1) { throw new IllegalArgumentException(); }
		Objects.requireNonNull(ts);
		Objects.requireNonNull(params);
		Objects.requireNonNull(db);
		Objects.requireNonNull(data);

		// Skip any secondary data stream by returning empty bars.
		if(!db.isMainDataStream(stream) && !params.hetro())
		    { return(Collections.nCopies(ts.bars(), MIDIPlayableMonophonicDataBar.EMPTY_1_NOTE_BAR)); }

		final ChorusStyleFromData style = ChorusStyleFromData.FirstDataBar;

		// Parameterisation of melody play without scales.
		final int octaves = MIDIGen.DEFAULT_RANGE_OCTAVES; // Math.max(1, DEFAULT_RANGE_OCTAVES/2);
		final int range = 12 * octaves;
		final float multScaling = (db.maxVal() > 0) ? ((range-1)/db.maxVal()) : 1;
		final List<DataProtoBar> verseProtoBars = MIDIGen.splitAndAlignData(TuneSection.verse, params, data);
		// If there are no data bars, return empty section.
		if(verseProtoBars.isEmpty())
	    	{ return(Collections.nCopies(ts.bars(), MIDIPlayableMonophonicDataBar.EMPTY_1_NOTE_BAR)); }

	    switch(style) {
	        default:
	    	case FirstDataBar:
	        final DataProtoBar dbp = verseProtoBars.get(0);
			final List<List<String>> rows = dbp.dataRows().data();
			final int dnpb = dbp.dataNotesPerBar();
			final List<NoteAndVelocity> notes = new ArrayList<>(dnpb);
			for(final List<String> row : rows)
				{
				final Datum d = Datum.extractDatum(stream, row);
				// Rest/silence for missing stream or value,
				// or where coverage is not strictly positive.
				final NoteAndVelocity n = MIDIGen.datumToNoteAndVelocityNoScale(
						d,
						false, // isNotSecondaryDataStream: only do this for primary stream!
						multScaling);
				notes.add(n);
				}

			// Construct repeated MIDI-playable bar for this stream.
			final MIDIPlayableMonophonicDataBar mpmb = new MIDIPlayableMonophonicDataBar(
					dnpb, dbp, stream, Collections.unmodifiableList(notes));
			return(Collections.nCopies(ts.bars(), mpmb));
	        }
		}



	}
