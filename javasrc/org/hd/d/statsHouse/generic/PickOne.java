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

import java.util.random.RandomGenerator;

/**Picks an integer randomly between 0 and n-1 inclusive, usually weighted towards 0.
 * Intended to be used to pick amongst a number of alternatives
 * from an n-element array, typically favouring lower-index values,
 * with a variety of possible distributions.
 */
public interface PickOne
	{
    int pickOne(RandomGenerator prng, int n);
	}
