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

import org.hd.d.statsHouse.DataUtils;
import org.hd.d.statsHouse.DataUtils.EOUDataCSV;

import junit.framework.TestCase;

/**Test reading of EOU consolidated data CSVs.
 */
public final class TestDataCSVRead extends TestCase
    {
    /**Verify that read of empty CSV works. */
    public static void testReadEmpty() throws IOException
	    {
        final EOUDataCSV result1 = DataUtils.parseEOUDataCSV(new StringReader(""));
        assertTrue("0 bytes should be empty", result1.data().isEmpty());
        
        final EOUDataCSV result2 = DataUtils.parseEOUDataCSV(new StringReader("\r\n"));
        assertTrue("CRLF should be empty", result2.data().isEmpty());

        final EOUDataCSV result3 = DataUtils.parseEOUDataCSV(new StringReader("#comment\r\n"));
        assertTrue("Comment should be empty", result3.data().isEmpty());
        

        final EOUDataCSV result4 = DataUtils.parseEOUDataCSV(new StringReader("\r\n#comment\r\n#moar comment"));
        assertTrue("Multi-comment should be empty", result4.data().isEmpty()); 
	    }

    /**Verify that parse of real-life sample CSV works.
     * Sample from:
     * <pre>
% head data/consolidated/energy/std/gen/M/gen-M.csv
     * </pre>
     * @throws IOException
     */
    public static void testReadSample() throws IOException
	    {
        final String sample = """
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

        final EOUDataCSV result1 = DataUtils.parseEOUDataCSV(new StringReader(sample));
        assertEquals("Sample should have 6 data rows", 6, result1.data().size());

        // Sample some fields.
        assertEquals("meter", result1.data().get(3).get(4));
        assertEquals("1", result1.data().get(2).get(5));
        assertEquals("", result1.data().get(4).get(2));
        assertEquals("2008-07", result1.data().get(5).get(0));
	    }
    }
