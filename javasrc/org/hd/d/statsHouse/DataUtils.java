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
import java.util.Objects;
import java.util.Random;
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

    /**Wraps the CSV data to make it clear what it is; data cannot be null.  */
    public record EOUDataCSV(List<List<String>> data)
	    { public EOUDataCSV { Objects.requireNonNull(data); } }

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
        final ArrayList<List<String>> result = new ArrayList<List<String>>(256);

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
            // or with a constant "", "0", or "1".
            // Costs maybe ~10% of parse execution time doing this extra work,
            // but may save more than that in avoided GC on small JVM instance.
            if(OPTIMISE_MEMORY_IN_EOUDATACSV_PARSE && !result.isEmpty())
	            {
	            final List<String> prevRow = result.get(result.size() - 1);	
	            if(fields.length == prevRow.size())
		            {
		            for(int i = fields.length; --i >= 0; )
			            {
		            	final String fi = fields[i];
			            // Deduplicate "" values by using an implicitly intern()ed constant.
						if("".equals(fi)) { fields[i] = ""; continue; }
			            // Deduplicate "0" values by using an implicitly intern()ed constant.
						if("0".equals(fi)) { fields[i] = "0"; continue; }
			            // Deduplicate "1" values by using an implicitly intern()ed constant.
						if("1".equals(fi)) { fields[i] = "1"; continue; }
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

    /**Counts the number of data streams using a quick method.
     * This just looks at the first row.
     * @param data  full data set; never null
     * @return count of data streams; non-negative
     */
    public static int countDataStreamsQuick(final EOUDataCSV data)
	    {
	    if(null == data) { throw new IllegalArgumentException(); }
	    if(data.data().isEmpty()) { return(0); }
	    return((data.data().get(0).size() - 1) / 3);
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
	    
	    int size = data.data.size();
	    final ArrayList<DataProtoBar> result = new ArrayList<DataProtoBar>(1 + (size/dataNotesPerBar));
	    
		for(int i = 0; i < size; i += dataNotesPerBar)
		    {
		    final List<List<String>> out = new ArrayList<List<String>>(dataNotesPerBar);
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
                    try
                        {
                        final int l = data.length;
                        for(int i = 0; i < l; ++i)
                            {
                            if(data[i] != is.read())
                                { overwrite = true; break; }
                            }
                        }
                    finally
                        { is.close(); }
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
