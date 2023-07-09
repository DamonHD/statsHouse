package localtest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hd.d.statsHouse.GenerationParameters;
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
			new ExternalFile("gen-D.csv", 5611, DataCadence.D),
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
    		for(final Style style : Style.values())
	    		{
    			for(final int introBars : new int[] {-1, 0, 4, 12, 16})
	    			{
        			for(final int seed : new int[] {-1, 0, 1})
	        			{
	    				final GenerationParameters params =
							new GenerationParameters(seed, style, introBars, false, sample.name());
	    				final MIDITune result = MIDIGen.genMelody(params, data);
	    		    	MIDIGen.validateMIDITune(result);
	    		    	assertFalse(result.dataMelody().isEmpty());
	    		    	assertTrue("expect support tracks unless 'plain' style / no production", (ProductionLevel.None == params.style().level) || !result.supportTracks().isEmpty());
	        			}
	    			}
	    		}
	    	}
	    }
	}
