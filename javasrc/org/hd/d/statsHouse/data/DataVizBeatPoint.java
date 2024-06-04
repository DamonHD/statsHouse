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

package org.hd.d.statsHouse.data;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**Data Visualisation for a data point vector per simple (typically 4/4) beat.
 * TODO: will be immutable by defensive copying.
 * <p>
 *
 * @param dataLabels  column labels for dataRendered;
 *     may be empty, contain nulls, may be null (but if not null size must == nColumns)
 * @param dataRendered  the set of key data as rendered in the tune,
 *     with the outer List usually one item per bar beat,
 *     and the inner list an ordered list of the key items rendered in that beat, possibly normalised;
 *     never null but may be empty
 */
public record DataVizBeatPoint(
		int nBeats,
		int nColumns,
        List<String> dataLabels,
        List<List<Float>> dataRendered
		)
	{
    public DataVizBeatPoint
	    {
    	if(nBeats < 0) { throw new IllegalArgumentException(); }
    	if(nColumns < 0) { throw new IllegalArgumentException(); }
    	Objects.requireNonNull(dataRendered);
    	if((null != dataLabels) && (nColumns != dataLabels.size())) { throw new IllegalArgumentException(); }
	    if(null != dataLabels) { dataLabels = Collections.unmodifiableList(new ArrayList<>(dataLabels)); } // Defensive copy.
	    }

    /**Output placeholder in place of empty label or value for write(). */
    public static final String PlaceholderValue = "?";

    /**Output placeholder character in place of potential separator for write(). */
    public static final char PlaceholderChar = '_';

    /**Write dataset to stream for other tools such as gnuplot to visualise/render.
     * Each beat's vector of points is written to a record in a single line.
     * <p>
     * The first line may optionally be labels if dataLabels is not null.
     * <p>
     * Values in a record may be separated by spaces or commas.
     * <p>
     * Finishes with a flush() to avoid surprises!
     */
    public void write(final Writer w, final boolean commaSep) throws IOException
	    {
        Objects.requireNonNull(w);

        // Write data column labels, if any.
        if(null != dataLabels)
	        {
	        // Write column headings.
        	// Convert any commas and spaces to underscores to avoid ambiguity.
        	// A null label becomes a single non-empty place-holder value.
        	for(int c = 0; c < nColumns; ++c)
	        	{
	        	String label = dataLabels.get(c);
	        	if(null == label) { label = PlaceholderValue; }
	        	else { label = label.replace(',', PlaceholderChar).replace(' ', PlaceholderChar); }
                if(c > 0) { w.write(commaSep ? ',' : ' '); }
	        	w.append(label);
	        	}
        	w.write('\n');
	        }

        // Write beat vector data rows.
        for(final List<Float> b : dataRendered)
        	{
        	if(null != b)
	        	{
		    	for(int c = 0; c < nColumns; ++c)
			    	{
		    		final Float rawValue = b.get(c);
	                if(c > 0) { w.write(commaSep ? ',' : ' '); }
		    		w.append((null == rawValue)? PlaceholderValue : rawValue.toString());
			    	}
	        	}
        	w.write('\n');
		    }

        w.flush();
	    }
	}
