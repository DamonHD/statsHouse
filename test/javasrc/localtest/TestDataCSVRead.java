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

import org.hd.d.statsHouse.data.DataProtoBar;
import org.hd.d.statsHouse.data.DataUtils;
import org.hd.d.statsHouse.data.EOUDataCSV;

import junit.framework.TestCase;
import localtest.support.BuiltInCSVDataSamples;

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

    /**Verify that parse of real-life sample CSV works. */
    public static void testReadSample() throws IOException
	    {
        final EOUDataCSV result1 = EOUDataCSV.parseEOUDataCSV(new StringReader(BuiltInCSVDataSamples.sample_gen_M));
        assertEquals("Sample should have 6 data rows", 6, result1.data().size());

        // Sample some fields.
        assertEquals("meter", result1.data().get(3).get(4));
        assertEquals("1", result1.data().get(2).get(5));
        assertEquals("", result1.data().get(4).get(2));
        assertEquals("2008-07", result1.data().get(5).get(0));

        final EOUDataCSV result2 = EOUDataCSV.parseEOUDataCSV(new StringReader(BuiltInCSVDataSamples.sample_gen_Y));
        assertEquals("Sample should have 16 data rows", 16, result2.data().size());
	    }

    /**Check simple splitting into DataProtoBar.
     * @throws IOException
     */
    public static void testChopDataIntoProtoBars() throws IOException
	    {
    	// Expecting 2 bars, the first of 4 notes, the second of 2 notes plus 2 nulls.
        final EOUDataCSV edc1 = EOUDataCSV.parseEOUDataCSV(new StringReader(BuiltInCSVDataSamples.sample_gen_M));
        final List<DataProtoBar> result1 = DataUtils.chopDataIntoProtoBarsSimple(4, edc1);
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

        final EOUDataCSV edc2 = EOUDataCSV.parseEOUDataCSV(new StringReader(BuiltInCSVDataSamples.sample_gen_Y));
        final List<DataProtoBar> result2 = DataUtils.chopDataIntoProtoBarsSimple(4, edc2);
        assertEquals(4, result2.size());
	    }
    }
