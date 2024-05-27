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
import java.util.List;
import java.util.Objects;

/**Single feed status record, for by-hour or by-User-Agent forms.
 * Input records are of the form:
<pre>
539 2295559 200:304:406:429:SH 90 81 0 367 539 00
</pre>
 * or
<pre>
12857 71404021 200:304:406:429:SH 2987 1993 359 7476 5129 ALL
</pre>
 or
<pre>
1701 3248489 200:304:406:429:SH 183 0 0 1518 421 "Podbean/FeedUpdate 2.1"
</pre>
 *
 * Where the third column enumerates the following columns before the index.
 * The index can be unquoted numeric 00 to 23, or
 * unquoted <code>ALL</code> summary/total, or
 * quoted <code>User-Agent</code> string, with <code>"-"</code> indicating no/empty UA.
 * <p>
 * All integer values are non-negative, all Strings are non-null, cols is non-null,
 * and the record will be immutable if the cols List is.
 */
public record FeedStatus(int hits, int bytes, String colTypes, List<Integer> cols, String index)
    {
	public FeedStatus
	    {
		if(hits < 0) { throw new IllegalArgumentException(); }
		if(bytes < 0) { throw new IllegalArgumentException(); }
		Objects.nonNull(colTypes);
		Objects.nonNull(cols);
		Objects.nonNull(index);
	    }

	/**Charset for feed status data (ASCII 7-bit). */
	public static final Charset CHARSET = StandardCharsets.US_ASCII;

//	/**Parse EOU consolidated data CSV file/stream; never null but may be empty.
//     * Parses CSV as List (by row) of List (of String fields),
//     * omitting empty and comment (starting with '#') rows.
//     * <p>
//     * This <em>does not</em> validate the content.
//     * </p>
//     * The outer and inner Lists implement RandomAccess.
//     * <p>
//     * This buffers its input for efficiency if not already a BufferedReader.
//     *
//     * @param r  stream to read from, not closed by this routine; never null
//     * @return a non-null but possibly-empty in-order immutable List of rows,
//     *    each of which is a non-null but possibly-empty in-order List of fields
//     * @throws IOException  if there is an I/O problem or the data is malformed
//     */
//    public static FeedStatus parseEOUDataCSV(final Reader r)
//        throws IOException
//        {
//        if(null == r) { throw new IllegalArgumentException(); }
//
//        // Wrap a buffered reader around the input if not already so.
//        final BufferedReader br = (r instanceof BufferedReader) ? (BufferedReader)r :
//        	new BufferedReader(r, 8192);
//
//        // Initially-empty result...
//        // As of 2023-06-08, largest non-daily-cadence data CSV is 203 lines.
//        final ArrayList<List<String>> result = new ArrayList<>(256);
//
//        String row;
//        while(null != (row = br.readLine()))
//            {
//        	// Skip empty rows.
//        	if("".equals(row)) { continue; }
//        	// Skip comments.
//        	if(row.startsWith("#")) { continue; }
//            final String fields[] = delimCSV.split(row);
//            if(fields.length < 1) { continue; }
//
//            if(fields[0].isEmpty())
//                { throw new IOException("unexpected empty date"); }
//
//            // Memory micro-optimisation.
//            // Where possible, share duplicate values from the previous row,
//            // or common values with a constant "" or "1".
//            // Costs maybe ~10% of parse execution time doing this extra work,
//            // but may save more than that in avoided GC on small JVM instance.
//            //
//            // DHD20230615: "0" is not common and mainly in successive records in a few files.
//            if(OPTIMISE_MEMORY_IN_EOUDATACSV_PARSE && !result.isEmpty())
//	            {
//	            final List<String> prevRow = result.get(result.size() - 1);
//	            if(fields.length == prevRow.size())
//		            {
//		            for(int i = fields.length; --i >= 0; )
//			            {
//		            	final String fi = fields[i];
//		            	switch(fi)
//			            	{
//		            		// Deduplicate values by using an implicitly intern()ed constant.
//			            	case "": fields[i] = ""; continue;
////			            	case "0": fields[i] = "0"; continue; // Not actually very common!
//			            	case "1": fields[i] = "1"; continue;
//			            	}
//                        // Else if this matches the item from the previous row, reuse it.
//			            final String pi = prevRow.get(i);
//						if(fi.equals(pi)) { fields[i] = pi; }
//			            }
//		            }
//	            }
//
//            // Package up row data (and make it unmodifiable).
//            result.add(Collections.unmodifiableList(Arrays.asList(fields)));
//            }
//
//        result.trimToSize(); // Free resources...
//        return(new FeedStatus(Collections.unmodifiableList(result))); // Make outer list unmodifiable...
//        }

//	/**Load from file EOU consolidated data in a form that parseEOUDataCSV() can read; never null but may be empty.
//	 * @throws IOException  if file not present or unreadable/unparseable.
//	 */
//	public static FeedStatus loadEOUDataCSV(final File dataCSVFile)
//	    throws IOException
//	    {
//		if(null == dataCSVFile) { throw new IllegalArgumentException(); }
//		try(final Reader r = new FileReader(dataCSVFile, EOUDATACSV_CHARSET))
//		    { return(parseEOUDataCSV(r)); }
//	    }
	}
