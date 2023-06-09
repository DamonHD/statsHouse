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

package localtest;

import java.io.IOException;
import java.io.StringWriter;

import org.hd.d.statsHouse.MIDICSVUtils;

import junit.framework.TestCase;

/**Test MIDICSVUtils.
 */
public final class TestMIDICSVUtils extends TestCase
    {
    /**Test MIDICSV file header generation.. */
    public static void testWriteF1Header()
        throws IOException
	    {
        final StringWriter sw1 = new StringWriter();
        MIDICSVUtils.writeF1Header(sw1, 2, 480);
        final String expected1 = """
0, 0, Header, 1, 2, 480
        		""";
        assertEquals(expected1, sw1.toString());
	    }

    /**Test MIDICSV file footer generation.. */
    public static void testWriteF1Footer()
        throws IOException
	    {
        final StringWriter sw1 = new StringWriter();
        MIDICSVUtils.writeF1Footer(sw1);
        final String expected1 = """
0, 0, End_of_file
        		""";
        assertEquals(expected1, sw1.toString());
	    }

    /**Test MIDICSV file minimal tempo track generation.. */
    public static void testWriteF1MinimalTempoTrack()
        throws IOException
	    {
        final StringWriter sw1 = new StringWriter();
        MIDICSVUtils.writeF1MinimalTempoTrack(sw1, 500000);
        final String expected1 = """
1, 0, Start_track
1, 0, Tempo, 500000
1, 0, Time_signature, 4, 2, 24, 8
1, 0, End_track
        		""";
        assertEquals(expected1, sw1.toString());
	    }
    }
