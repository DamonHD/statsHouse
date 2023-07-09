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

package org.hd.d.statsHouse.midi;

import org.hd.d.statsHouse.data.DataUtils;
import org.hd.d.statsHouse.data.EOUDataCSV;

/**Discovered bounds for input data.
 *
 * @param streams  number of usable streams in the data;
 *     non-negative and no larger than MAX_DATA_STREAMS
 * @param mainDataStream  1-based index of 'main' data stream (often with most data points);
 *     0 for no such main stream, 1+ for stream, possibly larger than streams
 * @param maxVal  maximum (non-zero) value in the data; non-negative.
 */
public record DataBounds(int streams, int mainDataStream, float maxVal)
	{
    public DataBounds
	    {
	    if(streams < 0) { throw new IllegalArgumentException(); }
	    if(streams > MAX_DATA_STREAMS) { throw new IllegalArgumentException(); }
	    if(mainDataStream < 0) { throw new IllegalArgumentException(); }
	    if(maxVal < 0) { throw new IllegalArgumentException(); }
	    }

    /**Maximum number of data streams allowed.
     * The highest number in any one file as of 2023-06 is 3.
     * <p>
     * Keeping this below GM1_PERCUSSION_CHANNEL
     * allows channels below that to be used for data melody,
     * and above for other things such as bass line.
     */
    public static final int MAX_DATA_STREAMS = 4;

    /**Construct and instance from the raw data. */
    public DataBounds(final EOUDataCSV data)
	    {
	    this(Math.min(DataUtils.countDataStreamsQuick(data), MAX_DATA_STREAMS),
	    		DataUtils.maxNVal(data),
	    		DataUtils.maxVal(data));
	    }

    /**True if the specified stream is the main stream (of homogeneous data).
     * Convenience method to avoid some lambda boilerplate!
     */
    public boolean isMainDataStream(final int s) { return(s == mainDataStream); }
	}
