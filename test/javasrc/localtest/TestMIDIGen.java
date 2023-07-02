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
import org.hd.d.statsHouse.Datum;
import org.hd.d.statsHouse.EOUDataCSV;
import org.hd.d.statsHouse.GenerationParameters;
import org.hd.d.statsHouse.NoteAndVelocity;
import org.hd.d.statsHouse.Scale;
import org.hd.d.statsHouse.Style;
import org.hd.d.statsHouse.TuneSection;
import org.hd.d.statsHouse.midi.MIDIConstant;
import org.hd.d.statsHouse.midi.MIDIGen;
import org.hd.d.statsHouse.midi.MIDITune;

import junit.framework.TestCase;

/**Test MIDIGen.
 */
public final class TestMIDIGen extends TestCase
    {
    /**Test generation of notes on Scale. */
    public static void testDatumToNoteAndVelocity()
	    {
    	final NoteAndVelocity result1 = MIDIGen.datumToNoteAndVelocity(
    			new Datum(null, 1.0f, 0f),
    			true, // isNotSecondaryDataStream,
    			Scale.NO_SCALE,
    			1,
    			0);
    	assertNotNull("should generate a note", result1);
    	assertEquals("should generate a root note", MIDIGen.DEFAULT_ROOT_NOTE, result1.note());
    	assertTrue("should generate a non-slient note", result1.velocity() > 0);
    	final NoteAndVelocity result2 = MIDIGen.datumToNoteAndVelocity(
    			new Datum(null, 1.0f, 1f),
    			true, // isNotSecondaryDataStream,
    			Scale.NO_SCALE,
    			1,
    			1);
    	assertNotNull("should generate a note", result2);
    	assertEquals("should generate a root+12 note", MIDIGen.DEFAULT_ROOT_NOTE+12, result2.note());
    	assertTrue("should generate a non-slient note", result2.velocity() > 0);
    	final NoteAndVelocity result3 = MIDIGen.datumToNoteAndVelocity(
    			new Datum(null, 1.0f, 1f),
    			true, // isNotSecondaryDataStream,
    			Scale.NO_SCALE,
    			2,
    			1);
    	assertNotNull("should generate a note", result3);
    	assertEquals("should generate a root+24 note", MIDIGen.DEFAULT_ROOT_NOTE+24, result3.note());
    	assertTrue("should generate a non-slient note", result3.velocity() > 0);
    	final NoteAndVelocity result4 = MIDIGen.datumToNoteAndVelocity(
    			new Datum(null, 1.0f, 1f),
    			true, // isNotSecondaryDataStream,
    			Scale.MAJOR,
    			2,
    			1);
    	assertNotNull("should generate a note", result4);
    	assertEquals("should generate a root+24 note", MIDIGen.DEFAULT_ROOT_NOTE+24, result4.note());
    	assertTrue("should generate a non-slient note", result4.velocity() > 0);


// TODO


	    }


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
    	final List<DataProtoBar> result0 = MIDIGen.splitAndAlignData(TuneSection.verse, new GenerationParameters(), new EOUDataCSV(Collections.emptyList()));
	    assertNotNull(result0);
	    assertTrue(result0.isEmpty());

    	final List<DataProtoBar> result1 = MIDIGen.splitAndAlignData(TuneSection.verse, new GenerationParameters(), EOUDataCSV.parseEOUDataCSV(new StringReader(TestDataCSVRead.sample_gen_Y)));
	    assertNotNull(result1);
	    assertFalse(result1.isEmpty());
	    assertEquals("4 bars of 4 notes", 4, result1.size());
	    assertEquals("4 bars of 4 notes", 4, result1.get(0).dataNotesPerBar());

    	final List<DataProtoBar> result2 = MIDIGen.splitAndAlignData(TuneSection.verse, new GenerationParameters(), EOUDataCSV.parseEOUDataCSV(new StringReader(TestDataCSVRead.sample_gen_M)));
	    assertNotNull(result2);
	    assertFalse(result2.isEmpty());
	    assertEquals("1 bar of 6/12 notes", 1, result2.size());
	    assertEquals("1 bar of 6/12 notes", 12, result2.get(0).dataNotesPerBar());
	    }

	/**Single data point, yearly cadence. */
	private static final String minimal_sample_Y = """
#YYYY,device,coverage,gen
2009,meter,1,2956.1
			""";

    /**Test generation of minimal plain MIDITune.
     * @throws IOException
     * @throws InvalidMidiDataException
     */
    public static void testGenMelodyMinimalPlainMIDITune() throws IOException
	    {
    	final MIDITune result1 = MIDIGen.genMelody(new GenerationParameters(0, Style.plain, 0, false, null), EOUDataCSV.parseEOUDataCSV(new StringReader(minimal_sample_Y)));
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
    	final MIDITune mt1 = MIDIGen.genMelody(new GenerationParameters(0, Style.plain, 0, false, null), EOUDataCSV.parseEOUDataCSV(new StringReader(minimal_sample_Y)));
    	MIDIGen.validateMIDITune(mt1);
        assertTrue("do not expect any percussion track",
    		mt1.supportTracks().stream().noneMatch(st -> st.setup().channel() == MIDIConstant.GM1_MIN_PERCUSSIVE_VOICES-1));
    	final Sequence result1 = MIDIGen.genFromTuneSequence(mt1);
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
    	final MIDITune result1 = MIDIGen.genMelody(new GenerationParameters(0, Style.gentle, 0, false, null), EOUDataCSV.parseEOUDataCSV(new StringReader(minimal_sample_Y)));
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
    	final MIDITune result1 = MIDIGen.genMelody(new GenerationParameters(0, Style.house, 0, false, null), EOUDataCSV.parseEOUDataCSV(new StringReader(minimal_sample_Y)));
    	MIDIGen.validateMIDITune(result1);
    	assertFalse(result1.dataMelody().isEmpty());
    	assertFalse(result1.dataMelody().isEmpty());
        assertEquals("expect exactly 1 melody track", 1, result1.dataMelody().size());
        assertNotNull(result1.dataMelody().get(0).bars());
        assertFalse("expect bars non-empty", result1.dataMelody().get(0).bars().isEmpty());
        assertNotNull("expect notes non-null", result1.dataMelody().get(0).bars().get(0).notes());
        assertEquals("expect exactly 16 melody bars", 16, result1.dataMelody().get(0).bars().size());
        assertNotNull("expect 1st note non-null", result1.dataMelody().get(0).bars().get(0).notes().get(0));
        assertNull("expect 2nd note null", result1.dataMelody().get(0).bars().get(0).notes().get(1));
        assertTrue("expect a support track", result1.supportTracks().size() > 0);
        assertTrue("expect a percussion support track",
    		result1.supportTracks().stream().anyMatch(st -> (st.setup().channel() == (MIDIConstant.GM1_PERCUSSION_CHANNEL-1))));
	    assertTrue("expect persussion note(s)",
    		result1.supportTracks().stream().anyMatch(st -> !st.bars().isEmpty()));
	    }

    /**Single-source multi-year monthly-cadence sample (electricity consumption minus DHW) to mid-2023. */
    private static final String conexDHW_M_to_202305 = """
#YYYY-MM,device,coverage,conexDHW
#synth,"meter.con-Eddi.h1"
2008-01,synth,1,214
2008-02,synth,1,195
2008-03,synth,1,187
2008-04,synth,1,165
2008-05,synth,1,146
2008-06,synth,1,146
2008-07,synth,1,139
2008-08,synth,1,147
2008-09,synth,1,152
2008-10,synth,1,160
2008-11,synth,1,191
2008-12,synth,1,210
2009-01,synth,1,214
2009-02,synth,1,178
2009-03,synth,1,177
2009-04,synth,1,149
2009-05,synth,1,147
2009-06,synth,1,130
2009-07,synth,1,163
2009-08,synth,1,143
2009-09,synth,1,154
2009-10,synth,1,155
2009-11,synth,1,161
2009-12,synth,1,169
2010-01,synth,1,181
2010-02,synth,1,141
2010-03,synth,1,154
2010-04,synth,1,126
2010-05,synth,1,129
2010-06,synth,1,132
2010-07,synth,1,112
2010-08,synth,1,124
2010-09,synth,1,141
2010-10,synth,1,135
2010-11,synth,1,163
2010-12,synth,1,181
2011-01,synth,1,159
2011-02,synth,1,132
2011-03,synth,1,158
2011-04,synth,1,122
2011-05,synth,1,124
2011-06,synth,1,120
2011-07,synth,1,122
2011-08,synth,1,103
2011-09,synth,1,123
2011-10,synth,1,134
2011-11,synth,1,149
2011-12,synth,1,136
2012-01,synth,1,174
2012-02,synth,1,153
2012-03,synth,1,139
2012-04,synth,1,117
2012-05,synth,1,128
2012-06,synth,1,103
2012-07,synth,1,99
2012-08,synth,1,93
2012-09,synth,1,126
2012-10,synth,1,119
2012-11,synth,1,149
2012-12,synth,1,142
2013-01,synth,1,155
2013-02,synth,1,124
2013-03,synth,1,147
2013-04,synth,1,127
2013-05,synth,1,122
2013-06,synth,1,143
2013-07,synth,1,106
2013-08,synth,1,106
2013-09,synth,1,151
2013-10,synth,1,141
2013-11,synth,1,167
2013-12,synth,1,170
2014-01,synth,1,190
2014-02,synth,1,131
2014-03,synth,1,162
2014-04,synth,1,123
2014-05,synth,1,136
2014-06,synth,1,135
2014-07,synth,1,100
2014-08,synth,1,114
2014-09,synth,1,130
2014-10,synth,1,143
2014-11,synth,1,155
2014-12,synth,1,159
2015-01,synth,1,181
2015-02,synth,1,155
2015-03,synth,1,161
2015-04,synth,1,143
2015-05,synth,1,148
2015-06,synth,1,140
2015-07,synth,1,129
2015-08,synth,1,114
2015-09,synth,1,147
2015-10,synth,1,140
2015-11,synth,1,168
2015-12,synth,1,168
2016-01,synth,1,196
2016-02,synth,1,160
2016-03,synth,1,180
2016-04,synth,1,143
2016-05,synth,1,147
2016-06,synth,1,132
2016-07,synth,1,135
2016-08,synth,1,102
2016-09,synth,1,132
2016-10,synth,1,146
2016-11,synth,1,179
2016-12,synth,1,180
2017-01,synth,1,207
2017-02,synth,1,163
2017-03,synth,1,166
2017-04,synth,1,143
2017-05,synth,1,149
2017-06,synth,1,139
2017-07,synth,1,147
2017-08,synth,1,97
2017-09,synth,1,143
2017-10,synth,1,150
2017-11,synth,1,179
2017-12,synth,1,178
2018-01,synth,1,214
2018-02,synth,1,186
2018-03,synth,1,190
2018-04,synth,1,93
2018-05,synth,1,160
2018-06,synth,1,137
2018-07,synth,1,139
2018-08,synth,1,107
2018-09,synth,1,159
2018-10,synth,1,172
2018-11,synth,1,182
2018-12,synth,1,194
2019-01,synth,1,198
2019-02,synth,1,164
2019-03,synth,1,196
2019-04,synth,1,150
2019-05,synth,1,171
2019-06,synth,1,161
2019-07,synth,1,150
2019-08,synth,1,100
2019-09,synth,1,172
2019-10,synth,1,173
2019-11,synth,1,187
2019-12,synth,1,179
2020-01,synth,1,230
2020-02,synth,1,197
2020-03,synth,1,213
2020-04,synth,1,218
2020-05,synth,1,217
2020-06,synth,1,196
2020-07,synth,1,189
2020-08,synth,1,151
2020-09,synth,1,157
2020-10,synth,1,192
2020-11,synth,1,210
2020-12,synth,1,195
2021-01,synth,1,243
2021-02,synth,1,226
2021-03,synth,1,165
2021-04,synth,1,168
2021-05,synth,1,166
2021-06,synth,1,152
2021-07,synth,1,136
2021-08,synth,1,138
2021-09,synth,1,168
2021-10,synth,1,174
2021-11,synth,1,210
2021-12,synth,1,199
2022-01,synth,1,225
2022-02,synth,0.21131,189
2022-03,synth,1,212.689
2022-04,synth,1,163.041
2022-05,synth,1,193.308
2022-06,synth,1,172.756
2022-07,synth,1,149.657
2022-08,synth,1,120.93
2022-09,synth,1,174.28
2022-10,synth,1,174.573
2022-11,synth,1,194.081
2022-12,synth,1,186.007
2023-01,synth,1,214.285
2023-02,synth,1,194.503
2023-03,synth,0.998656,210.209
2023-04,synth,1,171.44
2023-05,synth,1,191.661
    		""";

    /**Test generation of non-trivial house MIDITune, including intro, outro, verse and chorus sections.
     * @throws IOException
     * @throws InvalidMidiDataException
     */
    public static void testGenMelodylHouseMIDITune() throws IOException
	    {
    	final MIDITune result1 = MIDIGen.genMelody(new GenerationParameters(0, Style.house, 4, false, null), EOUDataCSV.parseEOUDataCSV(new StringReader(conexDHW_M_to_202305)));
    	MIDIGen.validateMIDITune(result1);
    	assertFalse(result1.dataMelody().isEmpty());
    	assertFalse(result1.dataMelody().isEmpty());
        assertEquals("expect exactly 1 melody track", 1, result1.dataMelody().size());
        assertNotNull(result1.dataMelody().get(0).bars());
        assertFalse("expect bars non-empty", result1.dataMelody().get(0).bars().isEmpty());
        assertNotNull("expect notes non-null", result1.dataMelody().get(0).bars().get(0).notes());
        assertEquals("expect exactly 36 melody bars (section=4b, i,v,c,v,c,v,c,v,o)", 36, result1.dataMelody().get(0).bars().size());
        assertTrue("expect a support track", result1.supportTracks().size() > 0);
        assertTrue("expect a percussion support track",
    		result1.supportTracks().stream().anyMatch(st -> (st.setup().channel() == (MIDIConstant.GM1_PERCUSSION_CHANNEL-1))));
	    assertTrue("expect persussion note(s)",
    		result1.supportTracks().stream().anyMatch(st -> !st.bars().isEmpty()));
	    }
    }
