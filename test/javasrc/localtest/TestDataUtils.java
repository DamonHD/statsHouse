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

import org.hd.d.statsHouse.DataUtils;
import org.hd.d.statsHouse.DataUtils.EOUDataCSV;

import junit.framework.TestCase;

/**Test DataUtils.
 */
public final class TestDataUtils extends TestCase
    {
    /**Test data stream counting. 
     * @throws IOException
     */
    public static void testCountDataStreamsQuick()
		throws IOException
    	{
        final EOUDataCSV result1 = DataUtils.parseEOUDataCSV(new StringReader(TestDataCSVRead.sample_gen_M));
        assertEquals(3, DataUtils.countDataStreamsQuick(result1));
	    }
    
    /**Test maximum data value extraction. 
     * @throws IOException
     */
    public static void testMaxVal()
		throws IOException
    	{
        final EOUDataCSV result1 = DataUtils.parseEOUDataCSV(new StringReader(TestDataCSVRead.sample_gen_M));
        assertEquals(161f, DataUtils.maxVal(result1), 0.1f);
	    }
    }
