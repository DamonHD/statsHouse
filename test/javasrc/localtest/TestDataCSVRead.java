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
import java.util.List;

import org.hd.d.statsHouse.DataProtoBar;
import org.hd.d.statsHouse.DataUtils;
import org.hd.d.statsHouse.EOUDataCSV;

import junit.framework.TestCase;

/**Test reading of EOU consolidated data CSVs.
 * Also some limited subsequent processing of raw data CSVs.
 */
public final class TestDataCSVRead extends TestCase
    {
    /**Verify that read of empty CSV works. */
    public static void testReadEmpty() throws IOException
	    {
        final EOUDataCSV result1 = EOUDataCSV.parseEOUDataCSV(new StringReader(""));
        assertTrue("0 bytes should be empty", result1.data().isEmpty());

        final EOUDataCSV result2 = EOUDataCSV.parseEOUDataCSV(new StringReader("\r\n"));
        assertTrue("CRLF should be empty", result2.data().isEmpty());

        final EOUDataCSV result3 = EOUDataCSV.parseEOUDataCSV(new StringReader("#comment\r\n"));
        assertTrue("Comment should be empty", result3.data().isEmpty());

        final EOUDataCSV result4 = EOUDataCSV.parseEOUDataCSV(new StringReader("\r\n#comment\r\n#moar comment"));
        assertTrue("Multi-comment should be empty", result4.data().isEmpty());
	    }

    /**First 10 lines of monthly-cadence PV generation data CSV, including comment rows.
     * Sample from:
     * <pre>
% head data/consolidated/energy/std/gen/M/gen-M.csv
     * </pre>
     * <p>
     * Is public so as to be usable from other test cases.
     */
	public static final String sample_gen_M = """
#YYYY-MM,device,coverage,gen,device,coverage,gen,device,coverage,gen
#input,"data/consolidated/energy/std/gen/M/Enphase/gen-M-Enphase.csv"
#input,"data/consolidated/energy/std/gen/M/meter/gen-M-meter.csv"
#input,"data/consolidated/energy/std/gen/M/SunnyBeam/gen-M-SunnyBeam.csv"
2008-02,,,,meter,1,4,SunnyBeam,0.142857,3.54
2008-03,,,,meter,1,70,SunnyBeam,1,68.55
2008-04,,,,meter,1,108,SunnyBeam,1,106.13
2008-05,,,,meter,1,135,SunnyBeam,1,134.03
2008-06,,,,meter,1,160,SunnyBeam,1,158.1
2008-07,,,,meter,1,161,SunnyBeam,1,146.12
""";

	/**Full yearly-cadence PV generation data CSV to partial 2023, including comment rows.
	 * Sample from:
	 * <pre>
% cat data/consolidated/energy/std/gen/Y/gen-Y.csv
     * </pre>
     * <p>
     * Is public so as to be usable from other test cases.
	 */
	public static final String sample_gen_Y = """
#YYYY,device,coverage,gen,device,coverage,gen,device,coverage,gen
#input,"data/consolidated/energy/std/gen/Y/Enphase/gen-Y-Enphase.csv"
#input,"data/consolidated/energy/std/gen/Y/meter/gen-Y-meter.csv"
#input,"data/consolidated/energy/std/gen/Y/SunnyBeam/gen-Y-SunnyBeam.csv"
2008,,,,meter,0.916667,915,SunnyBeam,0.845238,889.93
2009,,,,meter,1,2956.1,SunnyBeam,1,2907.15
2010,,,,meter,1,3546.9,SunnyBeam,1,3482.76
2011,,,,meter,1,3988.1,SunnyBeam,1,3922.27
2012,,,,meter,1,3777.8,SunnyBeam,1,3712.68
2013,,,,meter,1,3749.7,SunnyBeam,1,3687.79
2014,,,,meter,1,3944,SunnyBeam,1,3881.99
2015,,,,meter,1,3828.6,SunnyBeam,1,3766.9
2016,,,,meter,1,3703.2,SunnyBeam,1,3676.54
2017,,,,meter,1,3794.4,SunnyBeam,1,3736.89
2018,Enphase,0.410714,1069.29,meter,1,3927.8,SunnyBeam,1,3931.44
2019,Enphase,0.999888,3870.89,meter,1,3855.5,SunnyBeam,1,3800.95
2020,Enphase,0.999888,4084.42,meter,1,4069.9,SunnyBeam,1,4020.86
2021,Enphase,0.999888,3514.19,meter,1,3500.8,SunnyBeam,1,3448.6
2022,Enphase,0.999888,3943.38,meter,1,3925.1,SunnyBeam,1,3865.5
2023,Enphase,0.416555,1415.92,meter,0.416667,1411,SunnyBeam,0.440476,1554.63
			""";

    /**Verify that parse of real-life sample CSV works. */
    public static void testReadSample() throws IOException
	    {
        final EOUDataCSV result1 = EOUDataCSV.parseEOUDataCSV(new StringReader(sample_gen_M));
        assertEquals("Sample should have 6 data rows", 6, result1.data().size());

        // Sample some fields.
        assertEquals("meter", result1.data().get(3).get(4));
        assertEquals("1", result1.data().get(2).get(5));
        assertEquals("", result1.data().get(4).get(2));
        assertEquals("2008-07", result1.data().get(5).get(0));

        final EOUDataCSV result2 = EOUDataCSV.parseEOUDataCSV(new StringReader(sample_gen_Y));
        assertEquals("Sample should have 16 data rows", 16, result2.data().size());
	    }

    /**Check simple splitting into DataProtoBar.
     * @throws IOException
     */
    public static void testChopDataIntoProtoBars() throws IOException
	    {
    	// Expecting 2 bars, the first of 4 notes, the second of 2 notes plus 2 nulls.
        final EOUDataCSV edc1 = EOUDataCSV.parseEOUDataCSV(new StringReader(sample_gen_M));
        final List<DataProtoBar> result1 = DataUtils.chopDataIntoProtoBars(4, edc1);
        assertNotNull(result1);
        assertEquals(2, result1.size());
        assertEquals(4, result1.get(0).dataNotesPerBar());
        assertEquals(4, result1.get(0).dataRows().data().size());
        assertEquals("2008-02", result1.get(0).dataRows().data().get(0).get(0));
        assertEquals("2008-05", result1.get(0).dataRows().data().get(3).get(0));
        assertEquals(4, result1.get(1).dataNotesPerBar());
        assertEquals(4, result1.get(1).dataRows().data().size());
        assertEquals("2008-06", result1.get(1).dataRows().data().get(0).get(0));
        assertEquals("2008-07", result1.get(1).dataRows().data().get(1).get(0));
        assertNull(result1.get(1).dataRows().data().get(2));
        assertNull(result1.get(1).dataRows().data().get(3));

        final EOUDataCSV edc2 = EOUDataCSV.parseEOUDataCSV(new StringReader(sample_gen_Y));
        final List<DataProtoBar> result2 = DataUtils.chopDataIntoProtoBars(4, edc2);
        assertEquals(4, result2.size());
	    }
    }
