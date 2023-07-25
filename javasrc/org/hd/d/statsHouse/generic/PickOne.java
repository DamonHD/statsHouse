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

/**Picks an integer randomly between 0 and n-1 inclusive, often weighted towards lower values.
 * Intended to be used to pick amongst a number of alternatives
 * from an n-element array, typically favouring lower-index values,
 * with a variety of possible distributions.
 * <p>
 * The array/selection is always expected to have at least 1 item,
 * so 0 is always a valid result.
 */
public interface PickOne
	{
	/**Pick a value in the range [0,n-1] using a random number generator.
	 * Parameters may not be validated for simplicity and speed.
	 * <p>
	 * Should be thread safe if the prng is.
	 *
	 * @param prng  (pseudo) random number source; usually must not be null.
	 * @param n  length of array to pick from (ie number of choices); strictly positive.
	 * @return  value in the range 0 to n-1 inclusive
	 */
    int pickOne(RandomGenerator prng, int n);

    /**Always pick element zero; the PRNG is not used so can be null. */
    public static final PickOne ZERO = (prng, n) -> (0);

    /**Pick with equal/uniform weighting. */
    public static final PickOne UNIFORM = (prng, n) -> (prng.nextInt(n));

    /**Pick weighted towards lower values with a square law. */
    public static final PickOne SQUARE = (prng, n) -> ((int) Math.floor(prng.nextDouble() * prng.nextDouble() * n));
	}
