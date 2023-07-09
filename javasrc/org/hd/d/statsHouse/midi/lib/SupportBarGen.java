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
import java.util.SortedSet;
import java.util.TreeSet;

import org.hd.d.statsHouse.GenerationParameters;
import org.hd.d.statsHouse.generic.NoteAndVelocity;
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
	 * @param finalBar  if true, is the final bar of a (usually longish) section
	 * @return one bar; never null
	 */
	public static MIDIPlayableBar makeBasicHousePercussionBar(final boolean finalBar)
		{
		final SortedSet<MIDIPlayableBar.StartNoteVelocityDuration> notes = new TreeSet<>();

		final byte DRUM = MIDIPercusssionInstrument.ACOUSTIC_BASE_DRUM.instrument0; // Alt: ELECTRIC_BASE_DRUM
		final byte vDRUM = MIDIGen.DEFAULT_MELODY_VELOCITY;
		final byte HAT = MIDIPercusssionInstrument.CLOSED_HI_HAT.instrument0; // OPEN_HI_HAT: OPEN_HI_HAT
		final byte vHAT = MIDIGen.DEFAULT_MELODY_VELOCITY;
		final byte CLAP = MIDIPercusssionInstrument.HAND_CLAP.instrument0;
		final byte vCLAP = MIDIGen.DEFAULT_MELODY_VELOCITY;

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
		// Beat 1 final: extra kick
		if(finalBar)
			{
			notes.add(new MIDIPlayableBar.StartNoteVelocityDuration(
				0 + MIDIGen.DEFAULT_CLKSPQTR/2,
					new NoteAndVelocity(DRUM, vDRUM),
					MIDIGen.DEFAULT_CLKSPQTR/2));
			}

		// Beat 2: drum, clap
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

		// Beat 4: drum, clap
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
	 * @return
	 */
	public static MIDIPlayableBar makeBasicHouseBassBar(
			final GenerationParameters params, final TuneSection section)
		{
		final SortedSet<MIDIPlayableBar.StartNoteVelocityDuration> notes = new TreeSet<>();

//		final byte BASS = MIDIInstrument.ELECTRIC_BASE_FINGER.instrument0;
		final byte vBASS = MIDIGen.DEFAULT_MELODY_VELOCITY;

		// Much around with first note each chorus bar.
		final byte defaultNote = (byte) (MIDIGen.DEFAULT_ROOT_NOTE-12);
		final byte firstNote = (TuneSection.chorus != section) ? defaultNote :
			(byte) (MIDIGen.DEFAULT_ROOT_NOTE);

		// Beat 1, 2, 3, 4.
		notes.add(new MIDIPlayableBar.StartNoteVelocityDuration(
			0,
				new NoteAndVelocity(firstNote, vBASS),
				MIDIGen.DEFAULT_CLKSPQTR/4));
		notes.add(new MIDIPlayableBar.StartNoteVelocityDuration(
			1 * MIDIGen.DEFAULT_CLKSPQTR,
				new NoteAndVelocity(defaultNote, vBASS),
				MIDIGen.DEFAULT_CLKSPQTR/4));
		notes.add(new MIDIPlayableBar.StartNoteVelocityDuration(
			2 * MIDIGen.DEFAULT_CLKSPQTR,
				new NoteAndVelocity(defaultNote, vBASS),
				MIDIGen.DEFAULT_CLKSPQTR/4));
		notes.add(new MIDIPlayableBar.StartNoteVelocityDuration(
			3 * MIDIGen.DEFAULT_CLKSPQTR,
				new NoteAndVelocity(defaultNote, vBASS),
				MIDIGen.DEFAULT_CLKSPQTR/4));

		final MIDIPlayableBar bar = new MIDIPlayableBar(Collections.unmodifiableSortedSet(notes));
		return(bar);
		}
	}
