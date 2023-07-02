package org.hd.d.statsHouse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**Basic musical scales as semitone steps for one octave.
 * The semitones in one scale add up to 12.
 * <p>
 * Is immutable.
 * <p>
 * See <a href="https://www.guitarland.com/Music10/FGA/LectureMIDIscales.html">Scales using MIDI note numbers</a>.
 */
public enum Scale
	{
	MAJOR(2,2,1,2,2,2,1),

	/**Equivalent to no scale at all, ie all notes in the octave. */
	NO_SCALE(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);


	/**Semitones for this scale; non-null, all entries strictly positive. */
	public final List<Integer> semitones;

	/**Construct from variable length list. */
	private Scale(final Integer... semitones)
		{
		assert(null != semitones);
		assert(Arrays.stream(semitones).mapToInt(Integer::intValue).noneMatch(i -> i <= 0));
		assert(12 == Arrays.stream(semitones).mapToInt(Integer::intValue).sum());
		this.semitones = Collections.unmodifiableList(Arrays.asList(semitones));
		}
	}
