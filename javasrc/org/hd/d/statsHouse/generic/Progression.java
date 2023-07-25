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

	/**PRNG that always returns 0; may break some things eg cause some routines to hang! */
	private static final RandomGenerator ALWAYS_ZERO = () -> (0);

	public RandomGenerator getPRNGNoProgression()
		{
		// Where there should be no randomness,
		// always pick the first element of any array of alternatives
		// by ensuring that nextInt(int bound) is always zero.
		if(params.randomnessNone())
            { return(ALWAYS_ZERO); }

		// Return PRNG starting at fixed position for this progression group.
		return(new Random(params.derivedSeed() ^ (uniqueID << 31)));
		}

	public RandomGenerator getPRNG(final Integer ...progression)
		{
		// Hash over all progression arguments, else zero if null.
		final int progHash = Arrays.hashCode(progression);

		// Where there should be no randomness,
		// always pick the first element of any array of alternatives
		// by ensuring that nextInt(int bound) is always zero.
		if(params.randomnessNone())
            { return(ALWAYS_ZERO); }

		// Return PRNG starting at fixed position for this progression group.
		return(new Random(params.derivedSeed() ^ (uniqueID << 31)));
		}

	}
