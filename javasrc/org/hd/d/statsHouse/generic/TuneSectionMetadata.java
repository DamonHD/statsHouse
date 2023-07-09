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

import java.util.Objects;

/**Details of metadata for one section of a tune.
 * TODO: extend to tempo, volume, etc, changes, and transitions.
 *
 * @param sectionType  type of section; default is verse, eg for 'plain' style
 * @param bars  count of bars in this section; strictly positive, usually even
 */
public record TuneSectionMetadata(int bars, TuneSection sectionType)
	{
    public TuneSectionMetadata
	    {
	    if(bars <= 0) { throw new IllegalArgumentException(); }
	    Objects.requireNonNull(sectionType);
	    }

    /**Default (verse) section.
     * @param bars  count of bars in this section; strictly positive, usually even
     */
    public TuneSectionMetadata(final int bars) { this(bars, TuneSection.verse); }
	}
