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

import javax.sound.midi.InvalidMidiDataException;

import org.hd.d.statsHouse.GenerationParameters;
import org.hd.d.statsHouse.data.DataProtoBar;
import org.hd.d.statsHouse.data.EOUDataCSV;
import org.hd.d.statsHouse.generic.TuneSection;
import org.hd.d.statsHouse.midi.MIDIGen;

import junit.framework.TestCase;

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

    	final List<DataProtoBar> result1 = MIDIGen.splitAndAlignData(TuneSection.verse, new GenerationParameters(), EOUDataCSV.parseEOUDataCSV(new StringReader(CSVTestDataSamples.minimal_sample_Y)));
	    assertNotNull(result1);
	    assertFalse(result1.isEmpty());
	    assertEquals("1 bars of 4 notes", 1, result1.size());
	    assertEquals("1 bars of 4 notes", 4, result1.get(0).dataNotesPerBar());

    	final List<DataProtoBar> result2 = MIDIGen.splitAndAlignData(TuneSection.verse, new GenerationParameters(), EOUDataCSV.parseEOUDataCSV(new StringReader(CSVTestDataSamples.sample_gen_Y)));
	    assertNotNull(result2);
	    assertFalse(result2.isEmpty());
	    assertEquals("4 bars of 4 notes", 4, result2.size());
	    assertEquals("4 bars of 4 notes", 4, result2.get(0).dataNotesPerBar());

    	final List<DataProtoBar> result3 = MIDIGen.splitAndAlignData(TuneSection.verse, new GenerationParameters(), EOUDataCSV.parseEOUDataCSV(new StringReader(CSVTestDataSamples.sample_gen_M)));
	    assertNotNull(result3);
	    assertFalse(result3.isEmpty());
	    assertEquals("1 bar of 6/12 notes", 1, result3.size());
	    assertEquals("1 bar of 6/12 notes", 12, result3.get(0).dataNotesPerBar());
	    }
    }
