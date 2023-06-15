package org.hd.d.statsHouse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**Wraps input CSV data to make it clear what it is; data cannot be null.  */
public record EOUDataCSV(List<List<String>> data)
    {
	public EOUDataCSV { Objects.requireNonNull(data); }

	/**If true, attempt to minimise memory consumption when parsing and loading EOUDATACSV data. */
	private static final boolean OPTIMISE_MEMORY_IN_EOUDATACSV_PARSE = true;

	/**Charset for EOU consolidated data CSV format (ASCII 7-bit). */
	public static final Charset EOUDATACSV_CHARSET = StandardCharsets.US_ASCII;

	/**Immutable regex pattern used to split CSV lines; never null.
	 * This is basically just a simple ","
	 * which with split() should preserve empty fields.
	 */
	public static final Pattern delimCSV = Pattern.compile(",");

	/**Parse EOU consolidated data CSV file/stream; never null but may be empty.
	     * Parses CSV as List (by row) of List (of String fields),
	     * omitting empty and comment (starting with '#') rows.
	     * <p>
	     * This <em>does not</em> validate the content.
	     * </p>
	     * The outer and inner Lists implement RandomAccess.
	     * <p>
	     * This buffers its input for efficiency if not already a BufferedReader.
	     *
	     * @param r  stream to read from, not closed by this routine; never null
	     * @return a non-null but possibly-empty in-order immutable List of rows,
	     *    each of which is a non-null but possibly-empty in-order List of fields
	     * @throws IOException  if there is an I/O problem or the data is malformed
	     */
	    public static EOUDataCSV parseEOUDataCSV(final Reader r)
	        throws IOException
	        {
	        if(null == r) { throw new IllegalArgumentException(); }

	        // Wrap a buffered reader around the input if not already so.
	        final BufferedReader br = (r instanceof BufferedReader) ? (BufferedReader)r :
	        	new BufferedReader(r, 8192);

	        // Initially-empty result...
	        // As of 2023-06-08, largest non-daily-cadence data CSV is 203 lines.
	        final ArrayList<List<String>> result = new ArrayList<>(256);

	        String row;
	        while(null != (row = br.readLine()))
	            {
	        	// Skip comments.
	        	if(row.startsWith("#")) { continue; }
	        	// Skip empty rows.
	        	if("".equals(row)) { continue; }
	            final String fields[] = delimCSV.split(row);
	            if(fields.length < 1) { continue; }

	            if(fields[0].isEmpty())
	                { throw new IOException("unexpected empty date"); }

	            // Memory micro-optimisation.
	            // Where possible, share duplicate values from the previous row,
	            // or common values with a constant "" or "1".
	            // Costs maybe ~10% of parse execution time doing this extra work,
	            // but may save more than that in avoided GC on small JVM instance.
	            //
	            // DHD20230615: "0" is not common and mainly in successive records in a few files.
	            if(OPTIMISE_MEMORY_IN_EOUDATACSV_PARSE && !result.isEmpty())
		            {
		            final List<String> prevRow = result.get(result.size() - 1);
		            if(fields.length == prevRow.size())
			            {
			            for(int i = fields.length; --i >= 0; )
				            {
			            	final String fi = fields[i];
			            	switch(fi)
				            	{
			            		// Deduplicate values by using an implicitly intern()ed constant.
				            	case "": fields[i] = ""; continue;
	//			            	case "0": fields[i] = "0"; continue; // Not actually very common!
				            	case "1": fields[i] = "1"; continue;
				            	}
	                        // Else if this matches the item from the previous row, reuse it.
				            final String pi = prevRow.get(i);
							if(fi.equals(pi)) { fields[i] = pi; }
				            }
			            }
		            }

	            // Package up row data (and make it unmodifiable).
	            result.add(Collections.unmodifiableList(Arrays.asList(fields)));
	            }

	        result.trimToSize(); // Free resources...
	        return(new EOUDataCSV(Collections.unmodifiableList(result))); // Make outer list unmodifiable...
	        }

	/**Load from file EOU consolidated data in a form that parseEOUDataCSV() can read; never null but may be empty.
	 * @throws IOException  if file not present or unreadable/unparseable.
	 */
	public static EOUDataCSV loadEOUDataCSV(final File longStoreFile)
	    throws IOException
	    {
		if(null == longStoreFile) { throw new IllegalArgumentException(); }
		try(final Reader r = new FileReader(longStoreFile, EOUDATACSV_CHARSET))
		    { return(parseEOUDataCSV(r)); }
	    }
	}
