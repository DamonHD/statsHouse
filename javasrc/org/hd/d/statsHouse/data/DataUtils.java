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

package org.hd.d.statsHouse.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.hd.d.statsHouse.generic.DataCadence;


/**Data utilities.
 * Handles the 'consolidated' style of EOU home data CSV.
 */
public final class DataUtils
    {
    /**Prevent creation of an instance. */
    private DataUtils() { }


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
     * @return  stream number with most data points; non-negative
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

    /**Extract source name for given 1-based stream; non-empty or null.
     * If there is no data or no such stream, then this will return null.
     * <p>
     * The first stream is 1.
     * <p>
     * This finds the first non-empty source name for the stream, if any.
     *
     * @param data  data set; never null
     * @return  non-"" source name if any, else null
     */
    public static String extractSourceName(final EOUDataCSV data, final int stream)
	    {
	    if(null == data) { throw new IllegalArgumentException(); }
	    if(stream < 1) { throw new IllegalArgumentException(); }

	    // Position of field containing specified stream's source name.
// 2008-02,,,,meter,1,4,SunnyBeam,0.142857,3.54
	    final int fieldNumber = (stream * 3) - 2;

	    for(final List<String> row : data.data())
		    {
	    	if(fieldNumber >= row.size()) { continue; }
            final String field = row.get(fieldNumber);
            if(!field.isEmpty()) { return(field); }
		    }

	    // Not found.
	    return(null);
	    }

    /**Extracts the cadence of the data set using a quick method; never null.
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

    /**Chop data into proto bars with no alignment nor padding; never null, may be empty.
     * The final bar is likely to be incomplete (padded with nulls).
     * <p>
     * This does not look at the content of the data at all.
     */
    public static List<DataProtoBar> chopDataIntoProtoBarsSimple(final int dataNotesPerBar, final EOUDataCSV data)
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
    }
