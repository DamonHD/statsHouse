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

import org.hd.d.statsHouse.GenerationParameters;

/**Source of (reproducible) progression/randomisation for music. */
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


	}
