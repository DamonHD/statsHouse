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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.sound.midi.InvalidMidiDataException;

import org.hd.d.statsHouse.GenerationParameters;
import org.hd.d.statsHouse.data.DataProtoBar;
import org.hd.d.statsHouse.data.EOUDataCSV;
import org.hd.d.statsHouse.generic.DataCadence;
import org.hd.d.statsHouse.generic.Style;
import org.hd.d.statsHouse.generic.TuneSection;
import org.hd.d.statsHouse.midi.MIDIGen;

import junit.framework.TestCase;
import localtest.support.BuiltInCSVDataSamples;

/**Test split and align of input data into (proto) bars.
 */
public final class TestSplitAndAlign extends TestCase
    {
    /**Test splitting of sample data.
     * @throws IOException
     * @throws InvalidMidiDataException
     */
    public static void testSplitAndAlignData() throws IOException
	    {
    	final List<DataProtoBar> result0 = MIDIGen.splitAndAlignData(TuneSection.verse, new GenerationParameters(), new EOUDataCSV(Collections.emptyList()));
	    assertNotNull(result0);
	    assertTrue(result0.isEmpty());

    	final List<DataProtoBar> result1 = MIDIGen.splitAndAlignData(TuneSection.verse, new GenerationParameters(), EOUDataCSV.parseEOUDataCSV(new StringReader(BuiltInCSVDataSamples.minimal_sample_Y)));
	    assertNotNull(result1);
	    assertFalse(result1.isEmpty());
	    assertEquals("1 bars of 4 notes", 1, result1.size());
	    assertEquals("1 bars of 4 notes", 4, result1.get(0).dataNotesPerBar());

    	final List<DataProtoBar> result2 = MIDIGen.splitAndAlignData(TuneSection.verse, new GenerationParameters(), EOUDataCSV.parseEOUDataCSV(new StringReader(BuiltInCSVDataSamples.sample_gen_Y)));
	    assertNotNull(result2);
	    assertFalse(result2.isEmpty());
	    assertEquals("4 bars of 4 notes", 4, result2.size());
	    assertEquals("4 bars of 4 notes", 4, result2.get(0).dataNotesPerBar());

	    // Testing short M sample with non-aligned data in a plain style, so should stay unaligned.
    	final List<DataProtoBar> result3 = MIDIGen.splitAndAlignData(TuneSection.verse,
			new GenerationParameters(GenerationParameters.RANDOMNESS_NONE, Style.plain, 0, false, null),
			EOUDataCSV.parseEOUDataCSV(new StringReader(BuiltInCSVDataSamples.sample_gen_M)));
	    assertNotNull(result3);
	    assertFalse(result3.isEmpty());
	    assertEquals("1 bar of 6/12 notes", 1, result3.size());
	    assertEquals("1 bar of 6/12 notes", 6, result3.get(0).dataRows().data().stream().filter(Objects::nonNull).count());
	    assertEquals("1 bar of 6/12 notes", 12, result3.get(0).dataNotesPerBar());
	    assertNotNull("first note in unaligned bar should not be null", result3.get(0).dataRows().data().get(0));
	    assertNull("last note in bar should be null", result3.get(0).dataRows().data().get(11));

	    // Testing short M sample with non-aligned data in a gentle style, so should be aligned.
    	final List<DataProtoBar> result4 = MIDIGen.splitAndAlignData(TuneSection.verse,
			new GenerationParameters(GenerationParameters.RANDOMNESS_NONE, Style.gentle, 0, false, null),
			EOUDataCSV.parseEOUDataCSV(new StringReader(BuiltInCSVDataSamples.sample_gen_M)));
	    assertNotNull(result4);
	    assertFalse(result4.isEmpty());
	    assertEquals("1 bar of 6/12 notes", 1, result4.size());
	    assertEquals("1 bar of 6/12 notes", 6, result4.get(0).dataRows().data().stream().filter(Objects::nonNull).count());
	    assertEquals("1 bar of 6/12 notes", 12, result4.get(0).dataNotesPerBar());
	    assertNull("first note in unaligned bar should be null", result4.get(0).dataRows().data().get(0));
	    assertNull("last note in bar should be null", result4.get(0).dataRows().data().get(11));

	    // Testing short M sample with non-aligned data in a house style, so should be aligned.
    	final List<DataProtoBar> result5 = MIDIGen.splitAndAlignData(TuneSection.verse,
			new GenerationParameters(GenerationParameters.RANDOMNESS_NONE, Style.gentle, 0, false, null),
			EOUDataCSV.parseEOUDataCSV(new StringReader(BuiltInCSVDataSamples.sample_gen_M)));
	    assertNotNull(result5);
	    assertFalse(result5.isEmpty());
	    assertEquals("1 bar of 6/12 notes", 1, result5.size());
	    assertEquals("1 bar of 6/12 notes", 6, result5.get(0).dataRows().data().stream().filter(Objects::nonNull).count());
	    assertEquals("1 bar of 6/12 notes", 12, result5.get(0).dataNotesPerBar());
	    assertNull("first note in unaligned bar should be null", result5.get(0).dataRows().data().get(0));
	    assertNull("last note in bar should be null", result5.get(0).dataRows().data().get(11));

	    // Testing long M sample with aligned data in a gentle style, so should stay aligned.
	    final EOUDataCSV conexDHW = EOUDataCSV.parseEOUDataCSV(new StringReader(BuiltInCSVDataSamples.conexDHW_M_to_202305));
    	final List<DataProtoBar> result6 = MIDIGen.splitAndAlignData(TuneSection.verse,
			new GenerationParameters(GenerationParameters.RANDOMNESS_NONE, Style.gentle, 0, false, null),
			conexDHW);
	    assertNotNull(result6);
	    assertFalse(result6.isEmpty());
	    assertEquals("16 bars", 16, result6.size());
	    assertEquals("first bar should have nominal 12 notes", DataCadence.M.defaultPerBar, result6.get(0).dataNotesPerBar());
	    assertEquals("first bar should be full", 12, result6.get(0).dataRows().data().stream().filter(Objects::nonNull).count());
	    assertNotNull("first note in unaligned first bar should not be null", result6.get(0).dataRows().data().get(0));
	    assertNull("last note in last bar should be null", result6.get(15).dataRows().data().get(11));

	    // Take long M sample with aligned data and remove first sample to make it non-aligned.
	    // Use gentle style to force alignment.
	    final EOUDataCSV conexDHWNonAligned = new EOUDataCSV(conexDHW.data().subList(1, conexDHW.data().size()));
    	final List<DataProtoBar> result7 = MIDIGen.splitAndAlignData(TuneSection.verse,
			new GenerationParameters(GenerationParameters.RANDOMNESS_NONE, Style.gentle, 0, false, null),
			conexDHWNonAligned);
	    assertNotNull(result7);
	    assertFalse(result7.isEmpty());
	    assertEquals("16 bars", 16, result7.size());
	    assertEquals("first bar should have nominal 12 notes", DataCadence.M.defaultPerBar, result7.get(0).dataNotesPerBar());
	    assertEquals("first bar should not be full", 11, result7.get(0).dataRows().data().stream().filter(Objects::nonNull).count());
	    assertNull("first note in unaligned first bar should be null", result7.get(0).dataRows().data().get(0));
	    assertNull("last note in last bar should be null", result7.get(15).dataRows().data().get(11));
	    }
    }
