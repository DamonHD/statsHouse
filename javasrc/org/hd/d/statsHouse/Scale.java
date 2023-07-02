package org.hd.d.statsHouse;

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
public class Scale
	{
	/**Major scale. */
	public static final Scale MAJOR = new Scale(2,2,1,2,2,2,1);

	/**Equivalent to no scale at all, ie all notes in the octave. */
	public static final Scale NO_SCALE = new Scale(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);


	/**Semitones for this scale; non-null, non-empty, all entries sin range[1,12]. */
	public final List<Integer> semitones;

	/**Construct from variable length list. */
	public Scale(final Integer... semitones)
		{
		Objects.requireNonNull(semitones);
		if(!Arrays.stream(semitones).mapToInt(Integer::intValue).noneMatch(i -> i <= 0)) { throw new IllegalArgumentException(); }
		if(12 != Arrays.stream(semitones).mapToInt(Integer::intValue).sum()) { throw new IllegalArgumentException(); }
		this.semitones = Collections.unmodifiableList(Arrays.asList(semitones));
		}
	}
