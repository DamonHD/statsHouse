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

import java.util.List;
import java.util.Objects;

/**Plan of song sections.
 * Is immutable if sections is.
 *
 * @param sections  in-order list of sections; non-null and non-empty
 *
 */
public record TuneSectionPlan(List <TuneSectionMetadata> sections)
	{
	public TuneSectionPlan
		{
		Objects.requireNonNull(sections);
		if(sections.isEmpty()) { throw new IllegalArgumentException(); }
		}
	}
