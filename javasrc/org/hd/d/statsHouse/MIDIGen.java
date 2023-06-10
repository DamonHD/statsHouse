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

package org.hd.d.statsHouse;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**MIDI generation.
 * Initially will generate CSV suitable as input for the MIDICSV utility.
 * May also generate MIDI and/or audio output directly with the javax.midi package.
 */
public final class MIDIGen
    {
    /**Prevent creation of an instance. */
    private MIDIGen() { }


    /**Minimal MIDICSV generation from main data source to supplied Writer.
     * Picks the main/busiest data channel and turns that into
     * a minimal tempo track and a single (flute) data melody track.
     * <p>
     * This does no alignment nor filling in of empty notes.
     * <p>
     * This uses 4 notes per bar.
     *
     * @throws IOException
     */
	public static void genMinimalMelodyMIDISCV(final Writer w, final EOUDataCSV data) throws IOException
		{
		if(null == w) { throw new IllegalArgumentException(); }
		if(null == data) { throw new IllegalArgumentException(); }

		// 0 if no data.
		// 1 if left-most data stream is main one.
		final int mainDataStream = DataUtils.maxNVal(data);
		final float maxVal = DataUtils.maxVal(data);

		// Divide data into bars.
		final int notesPerBar = 4;
		final List<DataProtoBar> protoBars = DataUtils.chopDataIntoProtoBars(notesPerBar, data);

		// Paying homage to textToMIDIv4-consolidated.sh and friends.
		final short melodyTrack = 2;
		final short instrument = 80; // Flute solo (GB).
		final short noteVelocity = 63;
		final int clksPQtr = MIDICSVUtils.DEFAULT_CLKSPQTR;
		final int noteDeltaTime = clksPQtr;
		final int octaves = 2;
		final short range = 12 * octaves;
		final short offset = 60;
		final float multScaling = (maxVal > 0) ? ((range-1)/maxVal) : 1;

		// Start MIDI file.
		MIDICSVUtils.writeF1Header(w, (short) 2, clksPQtr);
		// First track is just tempo.
		MIDICSVUtils.writeF1MinimalTempoTrack(w, MIDICSVUtils.DEFAULT_TEMPO);

// TODO: start melody track

		// MIDI clocks since start.
		int clock = 0;

		// Select instrument.
		MIDICSVUtils.writeF1ProgramC(w, melodyTrack, clock, instrument);

		// Make sweet music...
		// First iterate over the proto bars...
		for(final DataProtoBar dbp : protoBars)
			{
			if(mainDataStream < 1) { break; } // No data!
			// ... then the data rows inside them.
			for(final var dp : dbp.dataRows().data())
				{
                // Pick out the raw data field for the main stream.
				final int field = (mainDataStream * 3);
				// Skip empty or unparseable fields.
				if(field >= dp.size()) { continue; }
				final String d = dp.get(field);
				if(d.isEmpty()) { continue; }
				final float dataValue;
				try { dataValue = Float.parseFloat(d); } catch(final NumberFormatException e) { continue; }
				// Skip unusable values.
				if(!Float.isFinite(offset)) { continue; }
				final short scaled = (short)(offset + (dataValue * multScaling));
				if((scaled < 0) || (scaled > 127)) { continue; }

// TODO: play data melody note

				}

			// Allow time for note, played or not.
			clock += noteDeltaTime;
			}

// TODO: end melody track

		// End MIDI file.
		MIDICSVUtils.writeF1Footer(w);
		}

    }
