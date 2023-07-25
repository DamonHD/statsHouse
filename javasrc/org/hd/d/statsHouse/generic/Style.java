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

import java.util.Arrays;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**Music/percussion style/genre.
 * Implies a level of production/'art' also.
 */
public enum Style
    {
    plain(ProductionLevel.None, "none"),
    gentle(ProductionLevel.Gentle),
    house(ProductionLevel.Danceable);

//	// TODO: trance, drumAndBase, ...
//    trance(ProductionLevel.Danceable, "psytrance"), // psytrance
//    dnb(ProductionLevel.Danceable, "liquid"); // liquid d&b...

	/**Production level for this style. */
	public final ProductionLevel level;

	/**Immutable SortedSet of available sub-style/genre names. */
	public final SortedSet<String> subStyles;

	/**Production level only. */
	private Style(final ProductionLevel level)
		{ this.level = level; subStyles = Collections.emptySortedSet(); }

	/**Production level and optional Set of sub-genres. */
	private Style(final ProductionLevel level, final String ... subStyles)
		{
		this.level = level;
		this.subStyles = Collections.unmodifiableSortedSet(new TreeSet<>(Arrays.asList(subStyles)));
		}
    }