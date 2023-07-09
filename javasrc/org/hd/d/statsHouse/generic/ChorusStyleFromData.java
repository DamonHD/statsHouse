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

/**This enumerates some possible algorithmic styles of data melody chorus generation. */
public enum ChorusStyleFromData
	{
	/**Use the first data stream bar as-is; may be partly or entirely empty. */
	FirstDataBar,
	/**More robust for aligned data and/or on non-primary stream; such a full bar may not exist so will need fallback. */
    FirstFullDataBar,
    /**Use the first bar with the most notes; more robust for sparser data; may be partially or entirely empty. */
    FirstFullestDataBar,

    /**Can capture means and max/min or 90/10 percentile points; hopes for periodicity on ls date component. */
    SyntheticRepresentativeDataBar,
    SyntheticRepresentativeDataBarPlusCounterpoint,

    /**Downsampling in at least one bar, then synthetic style; hopes for periodicity on ls date component. */
    MeansPlusSyntheticRepresentativeDataBar,
    MeansPlusSyntheticRepresentativeDataBarPlusCounterpoint;
	}
