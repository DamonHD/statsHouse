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

package org.hd.d.statsHouse;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;


/**Data utilities.
 * Handles the 'consolidated' style of EOU home data CSV.
 */
public final class DataUtils
    {
    /**Prevent creation of an instance. */
    private DataUtils() { }


    /**Immutable regex pattern used to split CSV lines; never null.
     * This is basically just a simple ","
     * which with split() should preserve empty fields.
     */
    public static final Pattern delimCSV = Pattern.compile(",");

    /**If true, attempt to minimise memory consumption when parsing and loading EOUDATACSV data. */
    private static final boolean OPTIMISE_MEMORY_IN_EOUDATACSV_PARSE = true;

    /**Charset for EOU consolidated data CSV format (ASCII 7-bit). */
    public static final Charset EOUDATACSV_CHARSET = StandardCharsets.US_ASCII;


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

    /**Counts the number of data streams in the data set using a quick method.
     * This only looks at the first row of the data.
     * @param data  data set; never null
     * @return count of data streams; non-negative
     */
    public static int countDataStreamsQuick(final EOUDataCSV data)
	    {
	    if(null == data) { throw new IllegalArgumentException(); }
	    if(data.data().isEmpty()) { return(0); }
	    return((data.data().get(0).size() - 1) / 3);
	    }

    /**Extract maximum data (positive) value from entire data set.
     * This examines the data value for all streams in each row.
     * <p>
     * If there is no data, or all data-values are non-positive,
     * then this will return 0.
     * <p>
     * This ignores coverage levels, etc.
     * <p>
     * This ignored data values not parseable as float.
     *
     * @param data  data set; never null
     * @return return highest positive data value; non-negative
     */
    public static float maxVal(final EOUDataCSV data)
	    {
	    if(null == data) { throw new IllegalArgumentException(); }
	    float result = 0;
	    for(final List<String> row : data.data())
		    {
		    // 2008-02,,,,meter,1,4,SunnyBeam,0.142857,3.54
	    	for(int j = 3; j < row.size(); j += 3)
		    	{
		    	try {
		    		final float v = Float.parseFloat(row.get(j));
		    		if(v > result) { result = v; }
		    		}
		    	catch(final NumberFormatException e) { /* Ignore */ }
		    	}
		    }
	    return(result);
	    }

    /**Return the index of the data stream with the most non-empty values, 1-based.
     * If there is no data then this will return 0.
     * <p>
     * If multiple streams have the same number of non-empty values,
     * then the lowest-numbered index amongst them is returned.
     * <p>
     * This ignores coverage levels, etc.
     * <p>
     * The first stream is 1.
     *
     * @param data  data set; never null
     * @return return stream number with most data points; non-negative
     */
    public static int maxNVal(final EOUDataCSV data)
	    {
	    if(null == data) { throw new IllegalArgumentException(); }
	    final SortedMap<Integer,Integer> counts = new TreeMap<>();
	    for(final List<String> row : data.data())
		    {
		    // 2008-02,,,,meter,1,4,SunnyBeam,0.142857,3.54
	    	for(int j = 3; j < row.size(); j += 3)
		    	{
	    		final boolean isEmpty = row.get(j).isEmpty();
	    		if(isEmpty) { continue; }
	    		final int stream = j / 3;
	    		final Integer old = counts.getOrDefault(stream, 0);
	    		counts.put(stream, old + 1);
		    	}
		    }
	    int highestCount = 0;
	    int busiestStream = 0;
	    for(final Integer stream : counts.keySet())
		    {
		    final int count = counts.get(stream);
		    if(count > highestCount)
			    {
		    	highestCount = count;
		    	busiestStream = stream;
			    }
		    }
	    return(busiestStream);
	    }

    /**Extracts the cadence of the data set using a quick method.
     * This only looks at the first row of the data.
     * <p>
     * If the date us of the form YYYY then the cadence is yearly;
     * YYYY-MM is monthly; YYYY-MM-DD is daily; otherwise an error.
     * <p>
     * This assumes that dates are correctly formated,
     * and the same format for all rows.
     * <p>
     * If there is no data this returns an arbitrary cadence,
     * and does not throw an exception.
     *
     * @param data  data set; never null
     * @return cadence of data streams; non-null
     * @throws IllegalArgumentException  if the cadence cannot be deduced
     */
    public static DataCadence extractDataCadenceQuick(final EOUDataCSV data)
	    {
	    if(null == data) { throw new IllegalArgumentException(); }
	    if(data.data().isEmpty()) { return(DataCadence.Y); }
	    final String firstDate = data.data().get(0).get(0);
	    if(4 == firstDate.length()) { return(DataCadence.Y); }
	    if(7 == firstDate.length()) { return(DataCadence.M); }
	    if(10 == firstDate.length()) { return(DataCadence.D); }
	    throw new IllegalArgumentException();
	    }



    /**Chop data into proto bars with no alignment or padding; never null, may be empty.
     * The final bar is likely to be incomplete (padded with nulls).
     * <p>
     * This does not look at the content of the data at all.
     */
    public static List<DataProtoBar> chopDataIntoProtoBars(final int dataNotesPerBar, final EOUDataCSV data)
	    {
	    if(dataNotesPerBar < 1) { throw new IllegalArgumentException(); }
	    if(null == data) { throw new IllegalArgumentException(); }

	    final int size = data.data().size();
	    final ArrayList<DataProtoBar> result = new ArrayList<>(1 + (size/dataNotesPerBar));

		for(int i = 0; i < size; i += dataNotesPerBar)
		    {
		    final List<List<String>> out = new ArrayList<>(dataNotesPerBar);
		    // FIXME: wrap leaf List if not already Unmodifiable.
		    for(int j = i; (j - i < dataNotesPerBar) && (j < size); ++j)
			    { out.add(data.data().get(j)); }
		    // Pad the final partial bar if necessary.
		    while(out.size() < dataNotesPerBar) { out.add(null); }
		    result.add(new DataProtoBar(dataNotesPerBar, new EOUDataCSV(Collections.unmodifiableList(out))));
		    }

		result.trimToSize();
		return(Collections.unmodifiableList(result));
	    }


    /**Prefix used on temporary files, eg while doing atomic replacements.
     * This used to be in GlobalParams but we may even need it
     * while loading GlobalParams.
     */
    public static final String F_tmpPrefix = ".tmp.";

    /**Private moderate pseudo-random-number source for replacePublishedFile(); not null. */
    private static final Random rnd = new Random();

    /**Replaces an existing published file with a new one (see 3-arg version).
     * Is verbose when it replaces the file.
     */
    public static boolean replacePublishedFile(final String name, final byte data[])
        throws IOException
        { return(replacePublishedFile(name, data, false)); }

    /**Replaces an existing published file with a new one.
     * This replaces (atomically if possible) the existing file (if any)
     * of the given name, ensuring the correct permissions for
     * a file to be published with a Web server (ie basically
     * global read permissions), provided the following
     * conditions are met:
     * <p>
     * <ul>
     * <li>The filename extension is acceptable (not checked yet).
     * <li>The data array is non-null and not zero-length.
     * <li>The content of the data array is different to the file.
     * <li>All the required permissions are available.
     * </ul>
     * <p>
     * If the file is successfully replaced, true is returned.
     * <p>
     * If the file does not need replacing, false is returned.
     * <p>
     * If an error occurs, eg in the input data or during file
     * operations, an IOException is thrown.
     * <p>
     * This routine enforces locking so that only one such
     * operation may be performed at any one time.  This does
     * not avoid the possibility of externally-generated races.
     * <p>
     * The final file, once replaced, will be globally readable,
     * and writable by us.
     * <p>
     * (If the final component of the file starts with ".",
     * then the file will be accessible only by us.)
     *
     * @param quiet     if true then only error messages will be output
     */
    public static boolean replacePublishedFile(final String name, final byte data[],
                                               final boolean quiet)
        throws IOException
        {
        if((name == null) || (name.length() == 0))
            { throw new IOException("inappropriate file name"); }
        if((data == null) || (data.length == 0))
            { throw new IOException("inappropriate file content"); }

        final File extant = new File(name);

        // Lock the critical external bits against read and write updates.
        rPF_rwlock.writeLock().lock();
        try
            {
            // Use a temporary file in the same directory (and thus the same filesystem)
            // to avoid unexpectedly truncating the file when copying/moving it.
            File tempFile;
            for( ; ; )
                {
                tempFile = new File(extant.getParent(),
                    F_tmpPrefix +
                    Long.toString((rnd.nextLong() >>> 1),
                        Character.MAX_RADIX) /* +
                    "." +
                    extant.getName() */ ); // Avoid making very long names...
                if(tempFile.exists())
                    {
                    System.err.println("WARNING: FileTools.replacePublishedFile(): "+
                        "temporary file " + tempFile.getPath() +
                        " exists, looping...");
                    continue;
                    }
                break;
                }

            // Get extant file's length.
            final long oldLength = extant.length();
            // Should we overwrite it?
            boolean overwrite = (oldLength < 1); // Missing or zero length.

            // If length has changed, we should overwrite.
            if(data.length != oldLength) { overwrite = true; }

            // Now, if we haven't already decided to overwrite the file,
            // check the content.
            if(!overwrite)
                {
                try
                    {
                    final InputStream is = new BufferedInputStream(
                        new FileInputStream(extant));
                    try (is)
                        {
                        final int l = data.length;
                        for(int i = 0; i < l; ++i)
                            {
                            if(data[i] != is.read())
                                { overwrite = true; break; }
                            }
                        }
                    }
                catch(final FileNotFoundException e) { overwrite = true; }
                }

            // OK, we don't want to overwrite, so return.
            if(!overwrite) { return(false); }


            // OVERWRITE OLD FILE WITH NEW...

            try {
                // Write new temp file...
                // (Allow any IOException to terminate the function.)
                OutputStream os = new FileOutputStream(tempFile);
                os.write(data);
                // os.flush(); // Possibly avoid unnecessary premature disc flush here.
                os.close();
                os = null; // Help GC.
                if(tempFile.length() != data.length)
                    { new IOException("temp file not written correctly"); }

                final boolean globalRead = !extant.getName().startsWith(".");

                // Ensure that the temp file has the correct read permissions.
                tempFile.setReadable(true, !globalRead);
                tempFile.setWritable(true, true);

                // Warn if target does not have write perms, and try to add them.
                // This should allow us to replace it with the new file.
                final boolean alreadyExists = extant.exists();
                if(alreadyExists && !extant.canWrite())
                    {
                    System.err.println("FileTools.replacePublishedFile(): "+
                        "WARNING: " + name + " not writable.");
                    extant.setWritable(true, true);
                    if(!extant.canWrite())
                        {
                        throw new IOException("can't make target writable");
                        }
                    }

                // (Atomically) move tempFile to extant file.
                // Note that renameTo() may not be atomic
                // and we may have to remove the target file first.
                if(!tempFile.renameTo(extant))
                    {
                    // If the target already exists,
                    // then be prepared to explicitly delete it.
                    if(!alreadyExists || !extant.delete() || !tempFile.renameTo(extant))
                        { throw new IOException("renameTo/update of "+name+" failed"); }
                    if(!quiet) { System.err.println("[WARNING: atomic replacement not possible for: " + name + ": used explicit delete.]"); }
                    }

                if(extant.length() != data.length)
                    { new IOException("update of "+name+" failed"); }
                extant.setReadable(true, !globalRead);
                extant.setWritable(true, true);
                if(!quiet) { System.err.println("["+(alreadyExists?"Updated":"Created")+" " + name + "]"); }
                return(true); // All seems OK.
                }
            finally // Tidy up...
                {
                tempFile.delete(); // Remove the temp file.
//                Thread.yield(); // That was probably expensive; give up the CPU...
                }
            }
        finally { rPF_rwlock.writeLock().unlock(); }

        // Can't get here...
        }

    /**Private lock for replacePublishedFile().
     * We use a read/write lock to improve available concurrency.
     * <p>
     * TODO: We could extend this to a lock per distinct directory or filesystem.
     */
    private static final ReentrantReadWriteLock rPF_rwlock = new ReentrantReadWriteLock();
    }
