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
	public static final String sample_FeedStatus_byHour_record =
			"539 2295559 200:304:406:429:SH 90 81 0 367 539 00";
	public static final String sample_FeedStatus_empty_UA_record =
		"477 632084 200:304:406:429:SH 22 0 75 380 173 \"-\"";
	public static final String sample_FeedStatus_spaced_UA_record =
		"1701 3248489 200:304:406:429:SH 183 0 0 1518 421 \"Podbean/FeedUpdate 2.1\"";

	/**Test construction of a FeedStatus record, eg that it does not throw. */
	public static void testConstructionOfSampleFeedStatus()
		{
		new FeedStatus(12857, 71404021, "200:304:406:429:SH", List.of(2987, 1993, 359, 7476, 5129), "ALL");
		}

	/**Test parse of an "ALL" FeedStatus record, eg that it does not throw. */
	public static void testParseOfSampleALLFeedStatus()
		{
		final FeedStatus fs = FeedStatus.parseRecord(sample_FeedStatus_ALL_record);
        assertNotNull(fs);
        assertEquals(12857, fs.hits());
        assertEquals(71404021, fs.bytes());
        assertEquals("200:304:406:429:SH", fs.colTypes());
        // TODO: cols content
        assertEquals("ALL", fs.index());
        assertFalse(fs.isUA());
        assertNull(fs.extractUA());
		}

	/**Test parse of by-hour FeedStatus record, eg that it does not throw. */
	public static void testParseOfSampleByHourFeedStatus()
		{
		final FeedStatus fs = FeedStatus.parseRecord(sample_FeedStatus_byHour_record);
        assertNotNull(fs);
        assertEquals(539, fs.hits());
        assertEquals(2295559, fs.bytes());
        assertEquals("200:304:406:429:SH", fs.colTypes());
        // TODO: cols content
        assertEquals("00", fs.index());
        assertFalse(fs.isUA());
        assertNull(fs.extractUA());
		}

	/**Test parse of an empty UA FeedStatus record, eg that it does not throw. */
	public static void testParseOfSampleEmptyUAFeedStatus()
		{
		final FeedStatus fs = FeedStatus.parseRecord(sample_FeedStatus_empty_UA_record);
        assertNotNull(fs);
        assertEquals(477, fs.hits());
        assertEquals(632084, fs.bytes());
        assertEquals("200:304:406:429:SH", fs.colTypes());
        // TODO: cols content
        assertEquals("\"-\"", fs.index());
        assertTrue(fs.isUA());
        assertEquals("", fs.extractUA());
		}

	/**Test parse of an UA-with-space FeedStatus record, eg that it does not throw. */
	public static void testParseOfSampleUAWithSpaceFeedStatus()
		{
		final FeedStatus fs = FeedStatus.parseRecord(sample_FeedStatus_spaced_UA_record);
        assertNotNull(fs);
        assertEquals(1701, fs.hits());
        assertEquals(3248489, fs.bytes());
        assertEquals("200:304:406:429:SH", fs.colTypes());
        // TODO: cols content
        assertEquals("\"Podbean/FeedUpdate 2.1\"", fs.index());
        assertTrue(fs.isUA());
        assertEquals("Podbean/FeedUpdate 2.1", fs.extractUA());
		}
   }
