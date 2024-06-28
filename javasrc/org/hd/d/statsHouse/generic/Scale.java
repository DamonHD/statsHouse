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
	public static final Scale MAJOR = new Scale(2, 2, 1, 2, 2, 2, 1);

	public static final Scale NATURAL_MINOR = new Scale(2, 1, 2, 2, 1, 2, 2);
	public static final Scale HARMONIC_MINOR = new Scale(2, 1, 2, 2, 1, 3, 1);
	public static final Scale MELODIC_MINOR = new Scale(2, 1, 2, 2, 2, 2, 1);

	public static final Scale DORIAN = new Scale(2, 1, 2, 2, 2, 1, 2);

	public static final Scale MINOR_PENTATONIC = new Scale(3, 2, 2, 3, 2);
	public static final Scale MAJOR_PENTATONIC = new Scale(2, 2, 3, 2, 3);

	/**Equivalent to no scale at all, ie all notes in the octave. */
	public static final Scale CHROMATIC = new Scale(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);


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
		if(!copied.stream().mapToInt(Integer::intValue).noneMatch(i -> i <= 0)) { throw new IllegalArgumentException("non-positive interval"); }
		if(12 != copied.stream().mapToInt(Integer::intValue).sum()) { throw new IllegalArgumentException("intervals do not add to octave"); }
		this.semitones = copied;
		}

	/**Given Scale and offset (+ve or -ve), compute MIDI note offset. */
	public byte noteOffset(final byte scaleNoteOffset)
		{
		final int s = semitones.size();
		// Whole octaves offset.
		final int octaves = (scaleNoteOffset >= 0) ?
			(scaleNoteOffset / s) :
			((scaleNoteOffset - s + 1) / semitones.size());
        final int residue = semitones.subList(0, (((scaleNoteOffset % s) + s) % s)).stream().mapToInt(Integer::intValue).sum();
        final int result = 12*octaves + residue;
        if((result < Byte.MIN_VALUE) || (result > Byte.MAX_VALUE)) { throw new IllegalArgumentException(); }
        return((byte) result);
		}
	}
