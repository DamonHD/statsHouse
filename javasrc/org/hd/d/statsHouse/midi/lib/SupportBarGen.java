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

package org.hd.d.statsHouse.midi.lib;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.random.RandomGenerator;

import org.hd.d.statsHouse.generic.NoteAndVelocity;
import org.hd.d.statsHouse.generic.PickOne;
import org.hd.d.statsHouse.generic.ProgressionGroup;
import org.hd.d.statsHouse.generic.TuneSection;
import org.hd.d.statsHouse.midi.MIDIGen;
import org.hd.d.statsHouse.midi.MIDIPercusssionInstrument;
import org.hd.d.statsHouse.midi.MIDIPlayableBar;

/**Simple support-track bar generation, eg bass and percussion. */
public final class SupportBarGen
	{
    /**Prevent creation of an instance. */
    private SupportBarGen() { }


	/**Create a fixed gentle percussion bar: one hand clap at the start. */
	public static MIDIPlayableBar makeBasicGentlePercussionBar() {
		final MIDIPlayableBar.StartNoteVelocityDuration snvd =
			new MIDIPlayableBar.StartNoteVelocityDuration(0,
				new NoteAndVelocity(MIDIPercusssionInstrument.HAND_CLAP.instrument0,
									(byte) ((2*MIDIGen.DEFAULT_MELODY_VELOCITY)/3)),
				MIDIGen.DEFAULT_CLOCKS_PER_BAR / 16);
		final MIDIPlayableBar bar = new MIDIPlayableBar(
			Collections.unmodifiableSortedSet(new TreeSet<>(Arrays.asList(snvd))));
		return bar;
	}

	/**Create a basic house percussion bar: four on the floor.
	 * @param prog   progression control; never null
	 * @param finalBar  if true, is the final bar of a (usually longish) section
	 * @return one bar; never null
	 */
	public static MIDIPlayableBar makeBasicHousePercussionBar(
			final ProgressionGroup prog, final boolean finalBar)
		{
		Objects.requireNonNull(prog);

		final RandomGenerator prng = prog.getPRNG(finalBar ? 1 : 42);

		final SortedSet<MIDIPlayableBar.StartNoteVelocityDuration> notes = new TreeSet<>();

		final byte DRUM = ProgressionGroup.pickOne(prng, PickOne.SQUARE, Arrays.asList(
				MIDIPercusssionInstrument.ACOUSTIC_BASE_DRUM,
				MIDIPercusssionInstrument.ELECTRIC_BASE_DRUM)).instrument0;
		final byte vDRUM = MIDIGen.DEFAULT_MAX_MELODY_VELOCITY;
		final byte HAT = ProgressionGroup.pickOne(prng, PickOne.SQUARE, Arrays.asList(
				MIDIPercusssionInstrument.CLOSED_HI_HAT,
				MIDIPercusssionInstrument.OPEN_HI_HAT)).instrument0;
		final byte vHAT = MIDIGen.DEFAULT_MAX_MELODY_VELOCITY;
		final byte CLAP = MIDIPercusssionInstrument.HAND_CLAP.instrument0;
		final byte vCLAP = MIDIGen.DEFAULT_MAX_MELODY_VELOCITY;

		// Beat 1: drum
		notes.add(new MIDIPlayableBar.StartNoteVelocityDuration(
			0,
				new NoteAndVelocity(DRUM, vDRUM),
				MIDIGen.DEFAULT_CLKSPQTR/2-1));
		// Beat 1 off: hi-hat
		notes.add(new MIDIPlayableBar.StartNoteVelocityDuration(
			0 + MIDIGen.DEFAULT_CLKSPQTR/2,
					new NoteAndVelocity(HAT, vHAT),
					MIDIGen.DEFAULT_CLKSPQTR/2));
		// Beat 1 final: extra kick usually.
		if(finalBar == (prng.nextBoolean() || prng.nextBoolean()))
			{
			notes.add(new MIDIPlayableBar.StartNoteVelocityDuration(
				0 + MIDIGen.DEFAULT_CLKSPQTR/2,
					new NoteAndVelocity(DRUM, vDRUM),
					MIDIGen.DEFAULT_CLKSPQTR/2));
			}

		// Beat 2: drum, clap - hat
		notes.add(new MIDIPlayableBar.StartNoteVelocityDuration(
			1 * MIDIGen.DEFAULT_CLKSPQTR,
					new NoteAndVelocity(DRUM, vDRUM),
					MIDIGen.DEFAULT_CLKSPQTR/2-1));
		notes.add(new MIDIPlayableBar.StartNoteVelocityDuration(
			1 * MIDIGen.DEFAULT_CLKSPQTR,
					new NoteAndVelocity(CLAP, vCLAP),
					MIDIGen.DEFAULT_CLKSPQTR/2));
		// Beat 2 off: hi-hat
		notes.add(new MIDIPlayableBar.StartNoteVelocityDuration(
			1 * MIDIGen.DEFAULT_CLKSPQTR + MIDIGen.DEFAULT_CLKSPQTR/2,
					new NoteAndVelocity(HAT, vHAT),
					MIDIGen.DEFAULT_CLKSPQTR/2));

		// Beat 3: drum
		notes.add(new MIDIPlayableBar.StartNoteVelocityDuration(
			2 * MIDIGen.DEFAULT_CLKSPQTR,
					new NoteAndVelocity(DRUM, vDRUM),
					MIDIGen.DEFAULT_CLKSPQTR/2-1));
		// Beat 3 off: hi-hat
		notes.add(new MIDIPlayableBar.StartNoteVelocityDuration(
			2 * MIDIGen.DEFAULT_CLKSPQTR + MIDIGen.DEFAULT_CLKSPQTR/2,
					new NoteAndVelocity(HAT, vHAT),
					MIDIGen.DEFAULT_CLKSPQTR/2));

		// Beat 4: drum, clap - hat
		notes.add(new MIDIPlayableBar.StartNoteVelocityDuration(
			3 * MIDIGen.DEFAULT_CLKSPQTR,
					new NoteAndVelocity(DRUM, vDRUM),
					MIDIGen.DEFAULT_CLKSPQTR/2-1));
		notes.add(new MIDIPlayableBar.StartNoteVelocityDuration(
			3 * MIDIGen.DEFAULT_CLKSPQTR,
					new NoteAndVelocity(CLAP, vCLAP),
					MIDIGen.DEFAULT_CLKSPQTR/2));
		// Beat 4 off: hi-hat
		notes.add(new MIDIPlayableBar.StartNoteVelocityDuration(
			3 * MIDIGen.DEFAULT_CLKSPQTR + MIDIGen.DEFAULT_CLKSPQTR/2,
					new NoteAndVelocity(HAT, finalBar ? 127 : vHAT), // Max volume at end of final bar.
					MIDIGen.DEFAULT_CLKSPQTR/2));

		final MIDIPlayableBar bar = new MIDIPlayableBar(Collections.unmodifiableSortedSet(notes));
		return(bar);
		}

	/**Create a basic house bass bar.
	 * Modulate slightly based on (eg) section type.
	 * @return single bar to be used throughout the given section
	 */
	public static MIDIPlayableBar makeBasicHouseBassBar(
			final TuneSection section,
			final ProgressionGroup prog,
			final int sectionNumber)
		{
		// The bass line can show some progression...
		final RandomGenerator prng = prog.getPRNG(sectionNumber, section.ordinal());

		final SortedSet<MIDIPlayableBar.StartNoteVelocityDuration> notes = new TreeSet<>();

		final byte vBASS = MIDIGen.DEFAULT_MAX_MELODY_VELOCITY;

		final int offset = 2 * 12; // Alt: vary eg 1 or 2 octaves down from root.

		// Sometimes mess around with first note each chorus bar.
		final byte defaultNote = (byte) (MIDIGen.DEFAULT_ROOT_NOTE-offset);
		final byte firstNote = (TuneSection.chorus != section) ? defaultNote :
			ProgressionGroup.pickOne(prng, PickOne.SQUARE, Arrays.asList(
				(byte) (MIDIGen.DEFAULT_ROOT_NOTE-offset+12),
				defaultNote));

		// Delay of bass note from start of beat.
		final int delay = ProgressionGroup.pickOne(prng, PickOne.SQUARE, Arrays.asList(
			MIDIGen.DEFAULT_CLKSPQTR/8,
			0,
			MIDIGen.DEFAULT_CLKSPQTR/4));

		// Duration of bass note.
		final int duration = ProgressionGroup.pickOne(prng, PickOne.SQUARE, Arrays.asList(
			MIDIGen.DEFAULT_CLKSPQTR/2,
			MIDIGen.DEFAULT_CLKSPQTR/4));

		// Beat 1, 2, 3, 4.
		notes.add(new MIDIPlayableBar.StartNoteVelocityDuration(
			0 + delay,
				new NoteAndVelocity(firstNote, vBASS),
				duration));
		notes.add(new MIDIPlayableBar.StartNoteVelocityDuration(
			1 * MIDIGen.DEFAULT_CLKSPQTR + delay,
				new NoteAndVelocity(defaultNote, vBASS),
				duration));
		notes.add(new MIDIPlayableBar.StartNoteVelocityDuration(
			2 * MIDIGen.DEFAULT_CLKSPQTR + delay,
				new NoteAndVelocity(defaultNote, vBASS),
				duration));
		notes.add(new MIDIPlayableBar.StartNoteVelocityDuration(
			3 * MIDIGen.DEFAULT_CLKSPQTR + delay,
				new NoteAndVelocity(defaultNote, vBASS),
				duration));

		final MIDIPlayableBar bar = new MIDIPlayableBar(Collections.unmodifiableSortedSet(notes));
		return(bar);
		}
	}
