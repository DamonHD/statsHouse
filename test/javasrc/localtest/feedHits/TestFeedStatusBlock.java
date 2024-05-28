/*
Copyright (c) 2024, Damon Hart-Davis

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

package localtest.feedHits;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;

import org.hd.d.statsHouse.feedHits.data.FeedStatusBlock;

import junit.framework.TestCase;

/**Test reading of feed data files.
 * Also some limited subsequent processing of raw data files.
 */
public final class TestFeedStatusBlock extends TestCase
    {
	/**Test construction of a minimal FeedStatusBlock, eg that it does not throw. */
	public static void testConstructionOfMinimalFeedStatusBlock() throws IOException
		{
		new FeedStatusBlock(1, Collections.emptyList());
		}

	/**Test parse of a single "ALL" FeedStatus record, eg that it does not throw. */
	public static void testParseOfSingleLineFeedStatusBlock() throws IOException
		{
		final FeedStatusBlock fsb = FeedStatusBlock.parseRecords(
				8, new StringReader(BuiltInFeedHitsDataSamples.sample_FeedStatus_ALL_record));
        assertNotNull(fsb);
        assertEquals(8, fsb.nDays());
        assertNotNull(fsb.records());
        assertEquals(1, fsb.records().size());
        assertEquals(12857, fsb.records().get(0).hits());
        assertEquals(71404021, fsb.records().get(0).bytes());
        assertEquals("200:304:406:429:SH", fsb.records().get(0).colTypes());
        // ...
		}

	/**Test parse of a full by-hour data block. */
	public static void testParseOfByHourFeedStatusBlock() throws IOException
		{
		final FeedStatusBlock fsb = FeedStatusBlock.parseRecords(
				8, new StringReader(BuiltInFeedHitsDataSamples.feedStatusByHour_20240527));
	    assertNotNull(fsb);
	    assertEquals(8, fsb.nDays());
	    assertNotNull(fsb.records());
	    assertEquals(25, fsb.records().size());
	    assertEquals(539, fsb.records().get(0).hits());
	    assertEquals(2295559, fsb.records().get(0).bytes());
	    assertEquals("200:304:406:429:SH", fsb.records().get(0).colTypes());
	    assertEquals(90, fsb.records().get(0).getColsMap().get("200").intValue());
	    // ...
	    assertEquals("00", fsb.records().get(0).index());
	    // ...
	    assertEquals("12", fsb.records().get(12).index());
	    // ...
	    assertEquals("ALL", fsb.records().get(24).index());
		}
    }
