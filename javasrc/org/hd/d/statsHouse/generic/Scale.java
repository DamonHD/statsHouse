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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**Basic musical scale as semitone steps for one octave.
 * The semitones in one scale add up to 12.
 * <p>
 * Is immutable.
 * <p>
 * Some common scales are provided as static values.
 * <p>
 * See <a href="https://www.guitarland.com/Music10/FGA/LectureMIDIscales.html">Scales using MIDI note numbers</a>.
 */
public final class Scale
	{
	/**Major scale. */
	public static final Scale MAJOR = new Scale(2,2,1,2,2,2,1);

	/**Equivalent to no scale at all, ie all notes in the octave. */
	public static final Scale NO_SCALE = new Scale(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);


	/**Semitones for this scale; non-null, non-empty, all entries in range [1,12]. */
	public final List<Integer> semitones;

	/**Construct from variable length list of semitone steps.
	 * Defensively copies then validates.
	 * @param semitones  non-null, non-empty list of strictly-positive semitone intervals summing to 12
	 */
	public Scale(final Integer... semitones)
		{
		Objects.requireNonNull(semitones);
		final List<Integer> copied = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(semitones)));
		if(!copied.stream().mapToInt(Integer::intValue).noneMatch(i -> i <= 0)) { throw new IllegalArgumentException(); }
		if(12 != copied.stream().mapToInt(Integer::intValue).sum()) { throw new IllegalArgumentException(); }
		this.semitones = copied;
		}
	}
