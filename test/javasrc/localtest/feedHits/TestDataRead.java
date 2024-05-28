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

import java.util.List;

import org.hd.d.statsHouse.feedHits.data.FeedStatus;

import junit.framework.TestCase;

/**Test reading of feed data files.
 * Also some limited subsequent processing of raw data files.
 */
public final class TestDataRead extends TestCase
    {
	public static final String sample_FeedStatus_ALL_record =
		"12857 71404021 200:304:406:429:SH 2987 1993 359 7476 5129 ALL";

	/**Test construction of a FeedStatus record, eg that it does not throw. */
	public static void testConstructionOfSampleFeedStatus()
		{
		new FeedStatus(12857, 71404021, "200:304:406:429:SH", List.of(2987, 1993, 359, 7476, 5129), "ALL");
		}

	/**Test parse of a FeedStatus record, eg that it does not throw. */
	public static void testParseOfSampleFeedStatus()
		{
		final FeedStatus fs = FeedStatus.parseRecord(sample_FeedStatus_ALL_record);
        assertNotNull(fs);
        assertEquals(12857, fs.hits());
        assertEquals(71404021, fs.bytes());
        assertEquals("200:304:406:429:SH", fs.colTypes());
        // TODO
        assertEquals("ALL", fs.index());
		}
    }
