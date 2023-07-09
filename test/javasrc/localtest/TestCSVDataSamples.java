package localtest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hd.d.statsHouse.data.DataUtils;
import org.hd.d.statsHouse.data.EOUDataCSV;
import org.hd.d.statsHouse.generic.DataCadence;

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
    public static void testThatExternalCSVDataSamplesAreIntact() throws IOException
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
	}
