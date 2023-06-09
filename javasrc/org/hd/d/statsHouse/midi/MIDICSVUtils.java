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

package org.hd.d.statsHouse.midi;

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

    /**MIDI format 1 default tempo track number. */
    public static final byte DEFAULT_TEMPO_TRACK_NUMBER = 1;


    /**Format template for MIDICSV file header row: track count, clocks per quarter note. */
    public static final String TEMPLATE_FILE_HEADER = "0, 0, Header, 1, %d, %d\n";

    /**Append MIDI format 1 file header to supplied Writer.
     * @throws IOException
     */
    public static void writeF1Header(final Writer w,
    		final short totalTrackCount, final int clksPQtr)
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
    public static void writeF1Footer(final Writer w) throws IOException
	    { w.append("0, 0, End_of_file\n"); }

    /**Format template for MIDICSV track start row: track number. */
    public static final String TEMPLATE_START_TRACK = "%d, 0, Start_track\n";

    /**Append track start to supplied Writer.
     * @throws IOException
     */
    public static void writeF1TrackStart(final Writer w, final byte track)
    	throws IOException
	    { w.append(String.format(TEMPLATE_START_TRACK, track)); }

    /**Format template for MIDICSV track end row: track number, clock. */
    public static final String TEMPLATE_END_TRACK = "%d, %d, End_track\n";

    /**Append track end to supplied Writer.
     * @throws IOException
     */
    public static void writeF1TrackEnd(final Writer w, final byte track, final int clock)
    	throws IOException
	    { w.append(String.format(TEMPLATE_END_TRACK, track, clock)); }

    /**Format template for MIDICSV tempo row: microseconds per quarter note. */
    public static final String TEMPLATE_TEMPO = "1, 0, Tempo, %d\n";

    /**Append (near) minimal (first, MIDI format 1, 4/4, C Major) tempo track to supplied Writer.
     * Always track 1 (default).
     */
    public static void writeF1MinimalTempoTrack(final Writer w,
    		final int tempo)
		throws IOException
	    {
	    if(null == w) { throw new IllegalArgumentException(); }
	    if(tempo < 1) { throw new IllegalArgumentException(); }
	    writeF1TrackStart(w, DEFAULT_TEMPO_TRACK_NUMBER);
        w.append(String.format(TEMPLATE_TEMPO, tempo));
	    w.append("1, 0, Time_signature, 4, 2, 24, 8\n");
	    writeF1TrackEnd(w, DEFAULT_TEMPO_TRACK_NUMBER, 0);
	    }

    /**Format template for MIDICSV program change row: track, clock, channel, instrument number. */
    public static final String TEMPLATE_PROGRAM_C = "%d, %d, Program_c, %d, %d\n";

    /**Append program change (voice/instrument selection) to supplied Writer.
     * @throws IOException
     */
    public static void writeF1ProgramC(final Writer w, final byte track, final int clock,
    		final byte channel, final byte instrument)
    	throws IOException
	    { w.append(String.format(TEMPLATE_PROGRAM_C, track, clock, channel, instrument)); }

    /**Format template for MIDICSV note-on row: track, clock, channel, note, velocity. */
    public static final String TEMPLATE_NOTE_ON = "%d, %d, Note_on_c, %d, %d, %d\n";

    /**Append note-on to supplied Writer.
     * @throws IOException
     */
    public static void writeF1NoteOn(final Writer w, final byte track, final int clock,
    		final byte channel, final byte note, final byte velocity)
    	throws IOException
	    { w.append(String.format(TEMPLATE_NOTE_ON, track, clock, channel, note, velocity)); }

    /**Format template for MIDICSV note-off row: track, clock, channel, note. */
    public static final String TEMPLATE_NOTE_OFF = "%d, %d, Note_off_c, %d, %d, 0\n";

    /**Append note-off to supplied Writer.
     * @throws IOException
     */
    public static void writeF1NoteOff(final Writer w, final byte track, final int clock,
    		final byte channel, final byte note)
    	throws IOException
	    { w.append(String.format(TEMPLATE_NOTE_OFF, track, clock, channel, note)); }
    }
