package org.hd.d.statsHouse;

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
