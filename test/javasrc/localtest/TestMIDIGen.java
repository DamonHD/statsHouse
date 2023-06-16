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
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;

import org.hd.d.statsHouse.DataProtoBar;
import org.hd.d.statsHouse.EOUDataCSV;
import org.hd.d.statsHouse.GenerationParameters;
import org.hd.d.statsHouse.MIDIGen;
import org.hd.d.statsHouse.MIDITune;

import junit.framework.TestCase;

/**Test MIDIGen.
 */
public final class TestMIDIGen extends TestCase
    {
    /**Test minimal data MIDICSV and Sequence generation.
     * @throws InvalidMidiDataException */
    public static void testGenMinimalMelodyMIDISCV() throws IOException, InvalidMidiDataException
	    {
        final EOUDataCSV csv1 = EOUDataCSV.parseEOUDataCSV(new StringReader(TestDataCSVRead.sample_gen_Y));
        final StringWriter sw1 = new StringWriter();
        final Sequence s = MIDIGen.genMinimalMelodyMIDISCV(sw1, csv1);
//System.err.print(sw1.toString());
        final String expected1 = """
0, 0, Header, 1, 2, 480
1, 0, Start_track
1, 0, Tempo, 500000
1, 0, Time_signature, 4, 2, 24, 8
1, 0, End_track
2, 0, Start_track
2, 0, Program_c, 2, 80
2, 0, Note_on_c, 2, 65, 63
2, 479, Note_off_c, 2, 65, 0
2, 480, Note_on_c, 2, 76, 63
2, 959, Note_off_c, 2, 76, 0
2, 960, Note_on_c, 2, 79, 63
2, 1439, Note_off_c, 2, 79, 0
2, 1440, Note_on_c, 2, 82, 63
2, 1919, Note_off_c, 2, 82, 0
2, 1920, Note_on_c, 2, 81, 63
2, 2399, Note_off_c, 2, 81, 0
2, 2400, Note_on_c, 2, 81, 63
2, 2879, Note_off_c, 2, 81, 0
2, 2880, Note_on_c, 2, 82, 63
2, 3359, Note_off_c, 2, 82, 0
2, 3360, Note_on_c, 2, 81, 63
2, 3839, Note_off_c, 2, 81, 0
2, 3840, Note_on_c, 2, 80, 63
2, 4319, Note_off_c, 2, 80, 0
2, 4320, Note_on_c, 2, 81, 63
2, 4799, Note_off_c, 2, 81, 0
2, 4800, Note_on_c, 2, 82, 63
2, 5279, Note_off_c, 2, 82, 0
2, 5280, Note_on_c, 2, 81, 63
2, 5759, Note_off_c, 2, 81, 0
2, 5760, Note_on_c, 2, 82, 63
2, 6239, Note_off_c, 2, 82, 0
2, 6240, Note_on_c, 2, 79, 63
2, 6719, Note_off_c, 2, 79, 0
2, 6720, Note_on_c, 2, 82, 63
2, 7199, Note_off_c, 2, 82, 0
2, 7200, Note_on_c, 2, 67, 63
2, 7679, Note_off_c, 2, 67, 0
2, 7680, End_track
0, 0, End_of_file
        		""";
        assertEquals(expected1, sw1.toString());

        // Test the javax...midi MIDI.
        assertEquals(8_000_000, s.getMicrosecondLength(), 10_000);
        assertEquals(2, s.getTracks().length);
	    }

    /**Test zero-data generation of empty tune.
     * @throws InvalidMidiDataException
     */
    public static void testGenMelodyEmpty()
	    {
    	final MIDITune result1 = MIDIGen.genMelody(new GenerationParameters(), new EOUDataCSV(Collections.emptyList()));
	    assertTrue(result1.dataMelody().isEmpty());
	    }

    /**Test splitting of sample data.
     * @throws IOException
     * @throws InvalidMidiDataException
     */
    public static void testSplitAndAlignData() throws IOException
	    {
    	final List<DataProtoBar> result0 = MIDIGen.splitAndAlignData(new GenerationParameters(), new EOUDataCSV(Collections.emptyList()));
	    assertNotNull(result0);
	    assertTrue(result0.isEmpty());

    	final List<DataProtoBar> result1 = MIDIGen.splitAndAlignData(new GenerationParameters(), EOUDataCSV.parseEOUDataCSV(new StringReader(TestDataCSVRead.sample_gen_Y)));
	    assertNotNull(result1);
	    assertFalse(result1.isEmpty());
	    assertEquals("4 bars of 4 notes", 4, result1.size());
	    assertEquals("4 bars of 4 notes", 4, result1.get(0).dataNotesPerBar());

    	final List<DataProtoBar> result2 = MIDIGen.splitAndAlignData(new GenerationParameters(), EOUDataCSV.parseEOUDataCSV(new StringReader(TestDataCSVRead.sample_gen_M)));
	    assertNotNull(result2);
	    assertFalse(result2.isEmpty());
	    assertEquals("1 bar of 6/12 notes", 1, result2.size());
	    assertEquals("1 bar of 6/12 notes", 12, result2.get(0).dataNotesPerBar());


	    }
    }
