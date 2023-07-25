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
public final class Progression
	{
	/**Generation parameters for entire tune generation; never null. */
	public final GenerationParameters params;

	/**Unique(ish) ID/seed for this set of progression values.
	 * Can be derived from a unique String value instead.
	 */
	public final int uniqueID;

	public Progression(final GenerationParameters params, final int uniqueID)
		{
		Objects.requireNonNull(params);
		this.params = params;
		this.uniqueID = uniqueID;
		}

	public Progression(final GenerationParameters params, final String uniqueID)
		{ this(params, (null == uniqueID) ? 0 : uniqueID.hashCode()); }

//	/**Trivial fake PRNG that always returns 0; may break some things eg cause some routines to hang! */
//	private static final RandomGenerator ALWAYS_ZERO = () -> (0);

	/**Munge components together to make good 64-bit seed.
	 * This depends on any per-run randomness,
	 * the unique name/ID of this progression group,
	 * and any progression numbers fed in.
	 * <p>
	 * Only the lower 48 bits may be used, eg for Random(seed).
	 */
	private long makeSeed(final Integer ...progression)
		{
		final long seed = params.derivedSeed() ^ (((long)params.derivedSeed()) << 13) ^
			(((long) uniqueID) << 3) ^ (((long)uniqueID) << 32) ^
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
