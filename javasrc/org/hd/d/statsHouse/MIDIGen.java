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
     * @throws IOException
     */
	public static void minMIDISCVGet(final Writer w, final EOUDataCSV data) throws IOException
		{
		if(null == w) { throw new IllegalArgumentException(); }
		if(null == data) { throw new IllegalArgumentException(); }

		MIDICSVUtils.writeF1Header(w, 2, MIDICSVUtils.DEFAULT_CLKSPQTR);
		MIDICSVUtils.writeF1MinimalTempoTrack(w, MIDICSVUtils.DEFAULT_TEMPO);

		// TODO

		MIDICSVUtils.writeF1Footer(w);
		}

    }
