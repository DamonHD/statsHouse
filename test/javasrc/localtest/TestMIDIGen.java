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

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;

import org.hd.d.statsHouse.GenerationParameters;
import org.hd.d.statsHouse.data.EOUDataCSV;
import org.hd.d.statsHouse.generic.Style;
import org.hd.d.statsHouse.midi.MIDIConstant;
import org.hd.d.statsHouse.midi.MIDIGen;
import org.hd.d.statsHouse.midi.MIDITune;

import junit.framework.TestCase;
import localtest.support.BuiltInCSVDataSamples;

/**Test MIDIGen. */
public final class TestMIDIGen extends TestCase
    {
	/**Test makeDerivedSeed() behaviour. */
    public static void testMakeDerivedSeed()
	    {
    	assertTrue(GenerationParameters.RANDOMNESS__MAX > 0);
    	assertTrue(GenerationParameters.RANDOMNESS__MAX >= GenerationParameters.RANDOMNESS_NONE);
    	assertTrue(GenerationParameters.RANDOMNESS__MAX >= GenerationParameters.RANDOMNESS_UNIQUE);
    	assertTrue(GenerationParameters.RANDOMNESS__MAX >= GenerationParameters.RANDOMNESS_NAME);

    	assertEquals(0, GenerationParameters.RANDOMNESS_NONE);
        assertEquals(0, GenerationParameters.makeDerivedSeed(GenerationParameters.RANDOMNESS_NONE, null));
        assertEquals(0, GenerationParameters.makeDerivedSeed(GenerationParameters.RANDOMNESS_NONE, "some-name"));

        assertEquals(-1, GenerationParameters.RANDOMNESS_UNIQUE);
        assertTrue(GenerationParameters.makeDerivedSeed(GenerationParameters.RANDOMNESS_UNIQUE, null) > GenerationParameters.RANDOMNESS__MAX);
        assertTrue(GenerationParameters.makeDerivedSeed(GenerationParameters.RANDOMNESS_UNIQUE, "otherName") > GenerationParameters.RANDOMNESS__MAX);

    	assertEquals(1, GenerationParameters.RANDOMNESS_NAME);
        assertEquals(GenerationParameters.RANDOMNESS_NAME, GenerationParameters.makeDerivedSeed(GenerationParameters.RANDOMNESS_NAME, null));
        assertTrue(GenerationParameters.makeDerivedSeed(GenerationParameters.RANDOMNESS_NAME, "some-name") > GenerationParameters.RANDOMNESS__MAX);
        assertEquals("expect two RANDOMNESS_NAME with the same name to be equal",
    		GenerationParameters.makeDerivedSeed(GenerationParameters.RANDOMNESS_NAME, "some-name"),
    		GenerationParameters.makeDerivedSeed(GenerationParameters.RANDOMNESS_NAME, "some-name"));
        assertFalse("expect results of different names for RANDOMNESS_NAME to differ (usually)",
    		GenerationParameters.makeDerivedSeed(GenerationParameters.RANDOMNESS_NAME, "some-name") ==
    		GenerationParameters.makeDerivedSeed(GenerationParameters.RANDOMNESS_NAME, "other-name"));

        final int customVal1 = 12345;
        assertEquals("expect custom +ve value to remain unchanged", customVal1, GenerationParameters.makeDerivedSeed(customVal1, null));
        assertEquals("expect custom +ve value to remain unchanged", customVal1, GenerationParameters.makeDerivedSeed(customVal1, "other"));
	    }

    /**Test minimal data MIDICSV and Sequence generation.
     * @throws InvalidMidiDataException
     */
    public static void testGenMinimalMelodyMIDISCV() throws IOException, InvalidMidiDataException
	    {
        final EOUDataCSV csv1 = EOUDataCSV.parseEOUDataCSV(new StringReader(BuiltInCSVDataSamples.sample_gen_Y));
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
    	final MIDITune result1 = MIDIGen.genTune(new GenerationParameters(), new EOUDataCSV(Collections.emptyList()));
	    assertTrue(result1.dataMelody().isEmpty());
	    }

	/**Test generation of minimal plain MIDITune.
     * @throws IOException
     * @throws InvalidMidiDataException
     */
    public static void testGenMelodyMinimalPlainMIDITune() throws IOException
	    {
    	final EOUDataCSV data = EOUDataCSV.parseEOUDataCSV(new StringReader(BuiltInCSVDataSamples.minimal_sample_Y));
        assertEquals("expect exactly 1 data row", 1, data.data().size());
		final MIDITune result1 = MIDIGen.genTune(new GenerationParameters(0, Style.plain, 0, false, null), data);
    	MIDIGen.validateMIDITune(result1);
    	assertFalse(result1.dataMelody().isEmpty());
        assertEquals("expect exactly 1 melody track", 1, result1.dataMelody().size());
        assertNotNull(result1.dataMelody().get(0).bars());
        assertEquals("expect exactly 1 melody bar", 1, result1.dataMelody().get(0).bars().size());
        assertNotNull("expect notes non-null", result1.dataMelody().get(0).bars().get(0).notes());
        assertNotNull("expect 1st note non-null", result1.dataMelody().get(0).bars().get(0).notes().get(0));
        assertNull("expect 2st note null", result1.dataMelody().get(0).bars().get(0).notes().get(1));
        assertTrue("do not expect any support percussion track",
    		result1.supportTracks().stream().noneMatch(st -> st.setup().channel() == MIDIConstant.GM1_MIN_PERCUSSIVE_VOICES-1));
	    }

    /**Test generation of minimal plain Sequence.
     * @throws InvalidMidiDataException
     */
    public static void testGenMelodyMinimalPlainSequence() throws IOException, InvalidMidiDataException
	    {
    	final MIDITune mt1 = MIDIGen.genTune(new GenerationParameters(0, Style.plain, 0, false, null), EOUDataCSV.parseEOUDataCSV(new StringReader(BuiltInCSVDataSamples.minimal_sample_Y)));
    	MIDIGen.validateMIDITune(mt1);
        assertTrue("do not expect any percussion track",
    		mt1.supportTracks().stream().noneMatch(st -> st.setup().channel() == MIDIConstant.GM1_MIN_PERCUSSIVE_VOICES-1));
    	final Sequence result1 = MIDIGen.genFromTuneSequence(mt1, null, null);
	    assertNotNull(result1);
        assertEquals(500_000, result1.getMicrosecondLength(), 10_000);
        assertTrue(result1.getTracks().length > 0);
	    }

    /**Test generation of minimal gentle MIDITune.
     * @throws IOException
     * @throws InvalidMidiDataException
     */
    public static void testGenMelodyMinimalGentleMIDITune() throws IOException
	    {
    	final MIDITune result1 = MIDIGen.genTune(new GenerationParameters(0, Style.gentle, 0, false, null), EOUDataCSV.parseEOUDataCSV(new StringReader(BuiltInCSVDataSamples.minimal_sample_Y)));
    	MIDIGen.validateMIDITune(result1);
    	assertFalse(result1.dataMelody().isEmpty());
        assertEquals("expect exactly 1 melody track", 1, result1.dataMelody().size());
        assertNotNull(result1.dataMelody().get(0).bars());
        assertEquals("expect exactly 1 melody bar", 1, result1.dataMelody().get(0).bars().size());
        assertNotNull("expect notes non-null", result1.dataMelody().get(0).bars().get(0).notes());
        assertNotNull("expect 1st note non-null", result1.dataMelody().get(0).bars().get(0).notes().get(0));
        assertNull("expect 2nd note null", result1.dataMelody().get(0).bars().get(0).notes().get(1));
        assertTrue("expect a support track", result1.supportTracks().size() > 0);
        assertTrue("expect a percussion support track",
    		result1.supportTracks().stream().anyMatch(st -> (st.setup().channel() == (MIDIConstant.GM1_PERCUSSION_CHANNEL-1))));
	    assertTrue("expect persussion note(s)",
    		result1.supportTracks().stream().anyMatch(st -> !st.bars().isEmpty()));
	    }

    /**Test generation of minimal house MIDITune.
     * @throws IOException
     * @throws InvalidMidiDataException
     */
    public static void testGenMelodyMinimalHouseMIDITune() throws IOException
	    {
    	final MIDITune result1 = MIDIGen.genTune(new GenerationParameters(0, Style.house, 0, false, null), EOUDataCSV.parseEOUDataCSV(new StringReader(BuiltInCSVDataSamples.minimal_sample_Y)));
    	MIDIGen.validateMIDITune(result1);
    	assertFalse(result1.dataMelody().isEmpty());
        assertEquals("expect exactly 1 melody track", 1, result1.dataMelody().size());
        assertNotNull(result1.dataMelody().get(0).bars());
        assertFalse("expect bars non-empty", result1.dataMelody().get(0).bars().isEmpty());
        assertNotNull("expect notes non-null", result1.dataMelody().get(0).bars().get(0).notes());
        assertTrue("expect at least 8 melody bars (section=4b, v,c)", 2*MIDIGen.DEFAULT_MIN_SECTION_BARS <= result1.dataMelody().get(0).bars().size());
        assertNotNull("expect 1st note non-null", result1.dataMelody().get(0).bars().get(0).notes().get(0));
        assertNull("expect 2nd note null", result1.dataMelody().get(0).bars().get(0).notes().get(1));
        assertTrue("expect a support track", result1.supportTracks().size() > 0);
        assertTrue("expect a percussion support track",
    		result1.supportTracks().stream().anyMatch(st -> (st.setup().channel() == (MIDIConstant.GM1_PERCUSSION_CHANNEL-1))));
	    assertTrue("expect persussion note(s)",
    		result1.supportTracks().stream().anyMatch(st -> !st.bars().isEmpty()));
	    }

    /**Test generation of non-trivial house MIDITune, including intro, outro, verse and chorus sections.
     * @throws IOException
     * @throws InvalidMidiDataException
     */
    public static void testGenMelodylHouseMIDITune() throws IOException
	    {
    	final EOUDataCSV data = EOUDataCSV.parseEOUDataCSV(new StringReader(BuiltInCSVDataSamples.conexDHW_M_to_202305));
        assertEquals("expect exactly 185 data rows", 185, data.data().size());
		final MIDITune result1 = MIDIGen.genTune(new GenerationParameters(0, Style.house, 4, false, null), data);
    	MIDIGen.validateMIDITune(result1);
    	assertFalse(result1.dataMelody().isEmpty());
        assertEquals("expect exactly 1 melody track", 1, result1.dataMelody().size());
        assertNotNull(result1.dataMelody().get(0).bars());
        assertFalse("expect bars non-empty", result1.dataMelody().get(0).bars().isEmpty());
        assertNotNull("expect notes non-null", result1.dataMelody().get(0).bars().get(0).notes());
        assertTrue("expect at least 40 melody bars (section=4b, i,v,c,...,v,c,o)", 40 <= result1.dataMelody().get(0).bars().size());
        assertTrue("expect a support track", result1.supportTracks().size() > 0);
        assertTrue("expect a percussion support track",
    		result1.supportTracks().stream().anyMatch(st -> (st.setup().channel() == (MIDIConstant.GM1_PERCUSSION_CHANNEL-1))));
	    assertTrue("expect persussion note(s)",
    		result1.supportTracks().stream().anyMatch(st -> !st.bars().isEmpty()));
	    }
    }
