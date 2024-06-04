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

import java.util.List;

/**Data Visualisation for a data point per simple (typically 4/4) beat.
 *
 * @param dataLabels  column labels for dataRendered (not containing nulls or commas);
 *     may be null
 * @param dataRendered  the set of key data as rendered in the tune,
 *     with the outer List usually one item per bar beat,
 *     and the inner list an ordered list of the key items rendered in that beat maybe normalised;
 *     may be null
 */
public record DataVizBeatPoint(
        List<String> dataLabels,
        List<List<Float>> dataRendered
		)
	{
    public DataVizBeatPoint
	    {
	    if(null != dataLabels) { dataLabels = List.copyOf(dataLabels); } // Defensive copy.
	    }
	}
