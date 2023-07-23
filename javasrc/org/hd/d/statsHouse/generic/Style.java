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

/**Music/percussion style.
 * Implies a level of production/'art' also.
 */
public enum Style
    {
    plain(ProductionLevel.None),
    gentle(ProductionLevel.Gentle),
    house(ProductionLevel.Danceable);

//	// TODO: trance, drumAndBase, ...
//    trance(ProductionLevel.Danceable), // psytrance
//    dnb(ProductionLevel.Danceable); // liquid d&b...

	/**Production level for this style. */
	public final ProductionLevel level;

	private Style(final ProductionLevel level)
		{ this.level = level; }
    }