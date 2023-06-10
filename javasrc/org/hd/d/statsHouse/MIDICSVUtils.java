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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/**MIDICSV utilities.
 * Generation of CSV output that the MIDICSV utility can convert to MIDI binary.
 */
public final class MIDICSVUtils
    {
    /**Prevent creation of an instance. */
    private MIDICSVUtils() { }


    /**Charset for MIDICSV CSV format (ASCII 7-bit). */
    public static final Charset MIDICSVCSV_CHARSET = StandardCharsets.US_ASCII;

    /**Default 120bpm (500uS per quarter note / beat). */
    public static final int DEFAULT_TEMPO  = 500000;
    /**Default clock pulses per quarter note. */
    public static final int DEFAULT_CLKSPQTR = 480;
    /**Default beats / quarter notes per bar (4/4 time). */
    public static final int DEFAULT_BeatsPerBar = 4;

    /**MIDI format 1 default tempo track number. */
    public static final int DEFAULT_TEMPO_TRACK_NUMBER = 1;

    /**Format template for MIDICSV file header row: track count, clocks per quarter note. */
    public static final String TEMPLATE_FILE_HEADER = "0, 0, Header, 1, %d, %d\n";

    /**Format template for MIDICSV tempo row: microseconds per quarter note. */
    public static final String TEMPLATE_TEMPO = "1, 0, Tempo, %d\n";

    /**Append MIDI format 1 file header to supplied Writer.
     * @throws IOException
     */
    public static void writeF1Header(final Writer w,
    		final int totalTrackCount, final int clksPQtr)
		throws IOException
	    {
	    if(null == w) { throw new IllegalArgumentException(); }
	    if(totalTrackCount < 1) { throw new IllegalArgumentException(); }
	    if(clksPQtr < 1) { throw new IllegalArgumentException(); }

	    // "0, 0, Header, 1, "TRACKS",", CLKSPQTR;
	    w.append(String.format(TEMPLATE_FILE_HEADER, totalTrackCount, clksPQtr));
	    }

    /**Append MIDI format 1 file footer to supplied Writer.
     * @throws IOException
     */
    public static void writeF1Footer(final Writer w)
		throws IOException
	    {
	    w.append("0, 0, End_of_file\n");
	    }

    /**Append (near) minimal (first, MIDI format 1, 4/4, C Major) tempo track to supplied Writer.
     * Always track 1.
     */
    public static void writeF1MinimalTempoTrack(final Writer w,
    		final int tempo)
		throws IOException
	    {
	    if(null == w) { throw new IllegalArgumentException(); }
	    if(tempo < 1) { throw new IllegalArgumentException(); }
        w.append("1, 0, Start_track\n");
        w.append(String.format(TEMPLATE_TEMPO, tempo));
	    w.append("1, 0, Time_signature, 4, 2, 24, 8\n");
	    w.append("1, 0, End_track\n");
	    }
    }
