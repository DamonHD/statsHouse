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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hd.d.statsHouse.GenerationParameters;
import org.hd.d.statsHouse.Main;
import org.hd.d.statsHouse.data.DataUtils;
import org.hd.d.statsHouse.data.EOUDataCSV;
import org.hd.d.statsHouse.generic.DataCadence;
import org.hd.d.statsHouse.generic.ProductionLevel;
import org.hd.d.statsHouse.generic.Style;
import org.hd.d.statsHouse.midi.MIDIGen;
import org.hd.d.statsHouse.midi.MIDITune;

import junit.framework.TestCase;
import localtest.support.ExternalFile;

/**Significant built-in test and external CSV data, shareable across all test cases. */
public final class TestCSVDataSamples extends TestCase
	{
	/**Main external data samples that are available; non-null, non-empty and immutable. */
	public static List<ExternalFile> mainFileDataSamples()
		{
		return(Collections.unmodifiableList(Arrays.asList(
			new ExternalFile("imp-M.csv", 163, DataCadence.M),
			new ExternalFile("gen-D.csv", 5617, DataCadence.D),
			new ExternalFile("gen-M.csv", 186, DataCadence.M),
			new ExternalFile("gen-Y.csv", 16, DataCadence.Y)
			)));
		}

	/**Validate external data files for presence and that they match simple expected metrics.
	 * Mainly a sanity check of the test system,
	 * given the relative fragility of using data from the file system.
	 *
	 * @throws IOException
	 */
    public static void testExternalCSVDataSamplesAreIntact() throws IOException
		{
    	final List<ExternalFile> samples = TestCSVDataSamples.mainFileDataSamples();
    	assertNotNull(samples);
    	assertFalse(samples.isEmpty());
    	for(final ExternalFile sample : samples)
	    	{
    		assertTrue(sample.getFullPath().canRead());
    		final EOUDataCSV data = sample.loadEOUDataCSV();
    		assertEquals("expecting listed number of data records for "+sample.name(), sample.recordsExpected(), data.data().size());
    		assertEquals("expecting listed cadence for "+sample.name(), sample.cadenceExpected(), DataUtils.extractDataCadenceQuick(data));
	    	}
		}

	/**Check that external data files can be built with a variety of generation parameters without failing.
	 *
	 * @throws IOException
	 */
    public static void testAllExternalCSVDataSamplesCanMakeTunes() throws IOException
	    {
    	final List<ExternalFile> samples = TestCSVDataSamples.mainFileDataSamples();
    	assertNotNull(samples);
    	assertFalse(samples.isEmpty());
    	for(final ExternalFile sample : samples)
	    	{
    		final EOUDataCSV data = sample.loadEOUDataCSV();
			for(final int introBars : new int[] {-1, 0, 1, 4, 12, 13, 16})
	    		{
    			// Testing against conventional and strange intro bar (and thus section bar) counts.
    			for(final int seed : new int[] {-1, 0, 42})
	    			{
    	    		for(final Style style : Style.values())
	        			{
	    				final GenerationParameters params =
							new GenerationParameters(seed, style, introBars, false, sample.name());
	    				final MIDITune result = MIDIGen.genTune(params, data);
	    		    	MIDIGen.validateMIDITune(result);
	    		    	assertFalse(result.dataMelody().isEmpty());
	    		    	assertTrue("expect support tracks unless 'plain' style / no production", (ProductionLevel.None == params.style().level) || !result.supportTracks().isEmpty());
	        			}
	    			}
	    		}
	    	}
	    }

    /**Write MIDI files for core versions of main samples.
     * Ensure that no error occurs, files are updated, etc.
     * <p>
     * Uses the command runner in Main.
     * <p>
     * These files can also be listened to,
     * to check that all styles and cadences are reasonably handled,
     * so are not deleted after running the test.
     */
    public static void testKeyMIDIFilesGeneration() throws Exception
	    {
		assertTrue("expecting test output directoty to be present", ExternalFile.TEST_OUTPUT_DIR.isDirectory());
    	final List<ExternalFile> samples = TestCSVDataSamples.mainFileDataSamples();
    	assertNotNull(samples);
    	assertFalse(samples.isEmpty());
    	for(final ExternalFile sample : samples)
	    	{
    		assertTrue(sample.getFullPath().canRead());
    		final File outputPath = new File(ExternalFile.TEST_OUTPUT_DIR, sample.name());
    		for(final Style style : Style.values())
    			{
    			final String styleName = style.name();
    			final File outputName = new File(ExternalFile.TEST_OUTPUT_DIR,
					sample.name() + "-" + styleName + ".mid");
    			final long oldTimestamp = outputName.lastModified();
    			final List<String> cmd = Arrays.asList(
    					sample.getFullPath().toString(),
    					outputName.toString(),
    					"-seed", "-1",
    					"-intro", "auto",
    					"-style", styleName
    					);
    			Main.runCommands(Collections.singletonList(cmd), true);
    			final long newTimestamp = outputName.lastModified();
    			assertTrue("output file timestamp muct have updated", newTimestamp > oldTimestamp);
    			assertTrue("output file must not be empty", outputName.length() > 0);
    			}
	    	}
	    }
	}
