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
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.random.RandomGenerator;

import org.hd.d.statsHouse.GenerationParameters;

/**Source of (reproducible) progression/randomisation for music.
 * Behaviour is driven both by whole-tune GenerationParameters
 * and the unique(-ish) identifier for a particular progression group
 * so that different parts of the tune can diverge in different ways
 * for the same seeds.
 */
public final class ProgressionGroup
	{
	/**Generation parameters for entire tune generation; never null. */
	public final GenerationParameters params;

	/**Unique(ish) ID/seed for this set of progression values.
	 * Can be derived from a unique String value instead.
	 */
	public final int uniqueID;

	public ProgressionGroup(final GenerationParameters params, final int uniqueID)
		{
		Objects.requireNonNull(params);
		this.params = params;
		this.uniqueID = uniqueID;
		}

	public ProgressionGroup(final GenerationParameters params, final String uniqueID)
		{ this(params, (null == uniqueID) ? 0 : uniqueID.hashCode()); }

	/**Pick one of an array of objects, most likely first. */
	public static Object pickOneOf(final RandomGenerator prng, final Object ...values)
		{
		Objects.requireNonNull(prng);
		Objects.requireNonNull(values);
		if(0 == values.length) { throw new IllegalArgumentException(); }

		// If only one value then always return it.
		if(1 == values.length)
			{ return(values[0]); }


		return(null);
		}

	/**Pick one value without progression.
	 * If 'no randomness' is selected then always returns the first (best) item.
	 * (This also avoids any potentially-expensive PRNG creation/use.)
	 * <p>
	 * Does not take any progression parameters,
	 * so is constant for a given input choice set
	 * throughout the life of a ProgressionGroup instance.
	 * <p>
	 * The choices should not be null, nor zero length, nor usually include nulls.
	 * <p>
	 * May be expensive so remember the result
	 * rather that regenerating where possible.
	 */
	public final <T> T pickOneNoProgression(final PickOne distribution, final List<T> choices)
		{
        Objects.requireNonNull(distribution);
        Objects.requireNonNull(choices);
        final int length = choices.size();
		if(length < 1) { throw new IllegalArgumentException(); }

        // If no randomness then always (cheaply) return the first choice.
        if(params.randomnessNone()) { return(choices.get(0)); }

        // Use our PRNG creation as an elaborate hash.
        // Fold the choices array length in as extra randomness.
        final RandomGenerator prng = getPRNG(length);
        final int choice = distribution.pickOne(prng, length);
//System.err.println(String.format("choice %d/%d", choice, length));
		return(choices.get(choice));
		}

//	/**Trivial fake PRNG that always returns 0; may break some things eg cause some routines to hang! */
//	private static final RandomGenerator ALWAYS_ZERO = () -> (0);

	/**Fold components together to try to make a good 64-bit seed.
	 * This depends on any per-run randomness,
	 * the unique name/ID of this progression group,
	 * and any progression numbers fed in.
	 * <p>
	 * Only the lower 48 bits may end up being used, eg for Random(seed).
	 */
	private long makeSeed(final Integer ...progression)
		{
		final long seed = params.derivedSeed() ^ (((long)params.derivedSeed()) << 13) ^
			(((long)uniqueID) << 3) ^ (((long)uniqueID) << 32) ^
			(((long)Arrays.hashCode(progression)) << 17);
		return(seed);
		}

	/**Creates a simple PRNG seeded from per-run randomness, progression group ID and values; never null. */
	public RandomGenerator getPRNG(final Integer ...progressionValues)
		{
		// Return PRNG starting at fixed position for this progression group.
		return(new Random(makeSeed(progressionValues)));
		}
	}
