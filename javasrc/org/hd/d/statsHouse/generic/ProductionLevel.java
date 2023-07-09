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

/**How 'produced' the output is as (danceable) music.
 * Low ordinal values such as for None retain more fidelity to the source,
 * and should be most useful for science purposes.
 * <p>
 * High ordinal values such as for Danceable have more stylistic transformations applied,
 * and should be most useful for entertainment.
 */
public enum ProductionLevel
    {
    None,
    Gentle,
    Danceable;
    }