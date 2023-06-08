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

import junit.framework.TestCase;

/**Test reading of EOU consolidated data CSVs.
 */
public final class TestDataCSVRead extends TestCase
    {
    /**Verify that read of empty CSV works. */
    public static void testReadEmpty() throws IOException
	    {
        // Parse without checking the header text (ie data type)...
        final List<List<String>> result1 = DataUtils.parseEOUDataCSV(new StringReader(""));
        assertTrue("0 bytes should be empty", result1.isEmpty());
        
        final List<List<String>> result2 = DataUtils.parseEOUDataCSV(new StringReader("\r\n"));
        assertTrue("CRLF should be empty", result2.isEmpty());

        final List<List<String>> result3 = DataUtils.parseEOUDataCSV(new StringReader("#comment\r\n"));
        assertTrue("Comment should be empty", result3.isEmpty());
	    }

    }
