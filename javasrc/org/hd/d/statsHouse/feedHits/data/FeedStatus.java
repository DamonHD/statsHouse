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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**Single feed status record, for by-hour or by-User-Agent forms, immutable.
 * All integer values are non-negative, colTypes and other Strings are non-null, cols is non-null.
 * <p>
 * The colTypes element count must match cols.
 * <p>
 * This makes a defensive immutable copy of the cols data to ensure record immutability.
 */
public record FeedStatus(int hits, int bytes, String colTypes, List<Integer> cols, String index)
    {
	public FeedStatus
	    {
		if(hits < 0) { throw new IllegalArgumentException(); }
		if(bytes < 0) { throw new IllegalArgumentException(); }
		Objects.nonNull(colTypes);
		Objects.nonNull(cols);
		if(cols.size() != colTypes.split(":").length) { throw new IllegalArgumentException("colTypes element count must match cols"); }
		for(final int c : cols) { if(c < 0) { throw new IllegalArgumentException(); } }
		cols = List.copyOf(cols); // Defensive copy to enforce immutability.
		Objects.nonNull(index);
	    }

	/**Charset for feed status data (ASCII 7-bit). */
	public static final Charset CHARSET = StandardCharsets.US_ASCII;

	/**Parse a since line/record.
	 * Input records are of the form (for a by-hour record):
<pre>
539 2295559 200:304:406:429:SH 90 81 0 367 539 00
</pre>
     * or an "ALL" total record for by-hour or by-UA:
<pre>
12857 71404021 200:304:406:429:SH 2987 1993 359 7476 5129 ALL
</pre>
     * or for a by-UA record with a <code>User-Agent</code>:
<pre>
1701 3248489 200:304:406:429:SH 183 0 0 1518 421 "Podbean/FeedUpdate 2.1"
</pre>
     * or for a by-UA record with an empty <code>User-Agent</code>:
<pre>
477 632084 200:304:406:429:SH 22 0 75 380 173 "-"
</pre>
	 * The third column enumerates the following columns before the index.
	 * The index can be unquoted numeric 00 to 23, or
	 * unquoted <code>ALL</code> summary/total, or
	 * quoted <code>User-Agent</code> string, with <code>"-"</code> indicating no/empty UA.
	 * <p>
	 * All integer values are base-10 and non-negative, all Strings are non-null, cols is non-null.
	 * <p>
	 * The colTypes element count must match cols.
	 * <p>
	 * The record is trim()med of excess leading and trailing whitespace before parsing.
	 * <p>
	 * Content validation is deferred to the constructor.
	 */
	public static FeedStatus parseRecord(final String line)
		{
		Objects.nonNull(line);
		final String trimmed = line.trim();
		// Initial parse with spaces for leading columns.
		final String[] rawFields = trimmed.split(" ");
		if(rawFields.length < 4) { throw new IllegalArgumentException("too few fields"); }
        final int hits = Integer.parseInt(rawFields[0], 10);
        final int bytes = Integer.parseInt(rawFields[1], 10);
        final String colTypes = rawFields[2];
        final String[] colTypeArray = colTypes.split(":");
        final int nCols = colTypeArray.length;
        // Reparse to capture index field in final slot.
        final int expectedFieldCount = 3 + nCols + 1;
		final String[] rawFields2 = trimmed.split(" ", expectedFieldCount);
		if(rawFields2.length != expectedFieldCount) { throw new IllegalArgumentException("too few cols"); }
        final List<Integer> colArray = new ArrayList<>(nCols);
        for(int c = 0; c < nCols; ++c) { colArray.add(Integer.parseInt(rawFields[3 + c], 10)); }
        final String index = rawFields2[rawFields2.length -1];
        // Validate that any index that starts with a " ends with one too.
        if(index.startsWith("\"") && !index.endsWith("\"")) { throw new IllegalArgumentException("index UA not correctly quoted"); }
        return(new FeedStatus(hits, bytes, colTypes, colArray, index));
		}

	/**Returns true if the index is a <code>User-Agent</code> (index starts with <code>"</code>). */
	public boolean isUA() { return(index.startsWith("\"")); }

	/**Extracts the <code>User-Agent</code> from the index; null if not a <code>User-Agent</code>.
	 * This returns null if <code>!isUA()</code>,
	 * and strips the quotes from the index value,
	 * and treats <code>"-"</code> as special, returning the empty string.
	 */
	public String extractUA()
	    {
		if(!isUA()) { return(null); }
		if("\"-\"".equals(index)) { return(""); }
		return(index.substring(1, index.length()-1));
	    }

	/**Return cols values as a Map from the keys in <code>colTypes</code> to (non-negative) Integer values; never null.
	 * The key order is the same as in <code>colTypes</code>.
	 * <p>
	 * The return value is immutable.
	 * @return
	 */
	public Map<String, Integer> getColsMap()
		{
		final int nCols = cols.size();
        final LinkedHashMap<String, Integer> m = new LinkedHashMap<>(nCols);
        final String[] keys = colTypes.split(":");
        for(int i = 0; i < nCols; ++i)
	        { m.put(keys[i], cols.get(i)); }
        return(Collections.unmodifiableMap(m));
		}
	}
