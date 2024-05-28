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

package org.hd.d.statsHouse.feedHits.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**Duration in days and ordered List of FeedStatus records, immutable.
 * The day count must be strictly positive.
 * <p>
 * This makes a defensive immutable copy of the list to ensure record immutability.
 */
public record FeedStatusBlock(int nDays, List<FeedStatus> records)
    {
	public FeedStatusBlock
	    {
		if(nDays <= 0) { throw new IllegalArgumentException(); }
		Objects.nonNull(records);
		records = List.copyOf(records); // Defensive copy to enforce immutability.
	    }

	/**Read a sequence of FeedStatus records from a stream, and add the day count; never null.
	 * @throws IOException
	 */
	public static FeedStatusBlock parseRecords(final int nDays, final Reader r) throws IOException
		{
        if(nDays <= 0) { throw new IllegalArgumentException(); }
        Objects.requireNonNull(r);

        final List<FeedStatus> l = new ArrayList<>();

        // Wrap a buffered reader around the input if not already so.
        final BufferedReader br = (r instanceof BufferedReader) ? (BufferedReader)r :
        	new BufferedReader(r, 8192);

        String row;
        while(null != (row = br.readLine()))
            {
        	// Skip empty line.
        	if("".equals(row)) { continue; }
            l.add(FeedStatus.parseRecord(row));
            }

        return(new FeedStatusBlock(nDays, l));
		}
	}
