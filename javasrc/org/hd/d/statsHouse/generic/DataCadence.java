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

package org.hd.d.statsHouse.generic;

/**Cadence of input data: daily, monthly, yearly. */
public enum DataCadence
    {
    D(32, 32, 12), // Also 7 days, or 28 days with a cycle of 13 plus some kinda 1 or 2 day fudge...
    M(12, 12, 0),
    Y(4, 0, 0);

	/**Default data points per bar; strictly positive. */
	public final int defaultPerBar;
	/**Default +ve cycle in the data (possibly after padding/alignment), or zero if none. */
	public final int defaultCycle;
	/**Default +ve higher cycle in bars, or zero if none. */
	public final int defaultBarsCycle;

	/**True if can align (which requires there to be a default cycle. */
	public boolean canAlign() { return(0 != defaultCycle); }

	private DataCadence(final int defaultPerBar, final int defaultCycle, final int defaultBarsCycle)
		{
		this.defaultPerBar = defaultPerBar;
		this.defaultCycle = defaultCycle;
		this.defaultBarsCycle = defaultBarsCycle;
		}
    }
