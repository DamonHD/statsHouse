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

import java.util.Objects;

/**A prototype music bar made from (a subset of) raw EOUDataCSV records; immutable.
 *
 * Entries in dataRows may be null (padding at either end)
 * or contiguous in-date-order EOUDataCSV rows.
 * <p>
 * This can be used as an intermediate data representation
 * between the raw data coming in and the music bars.
 * <p>
 * For full immutability, this relies on the incoming
 * leaf List values being immutable.  They must also
 * be well-formed, eg non-null, in order, consistent date cadence.
 *
 * @param dataNotesPerBar number of data/note slots; strictly positive
 * @param dataRows must be exactly dataNotesPerBar long; never null
 */
public record DataProtoBar(int dataNotesPerBar, EOUDataCSV dataRows)
    {
	public DataProtoBar
		{
		Objects.requireNonNull(dataRows);
		Objects.requireNonNull(dataRows.data());
		if(dataNotesPerBar < 1) { throw new IllegalArgumentException(); }
		if(dataNotesPerBar != dataRows.data().size()) { throw new IllegalArgumentException(); }
		}
    }
