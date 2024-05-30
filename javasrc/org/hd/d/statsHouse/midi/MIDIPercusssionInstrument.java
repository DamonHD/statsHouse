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

package org.hd.d.statsHouse.midi;

/**GM 1 channel 10 percussion instruments, for percussion used in this code.
 * 0-based (non-negative) numbers ready to use directly in MIDI messages.
 * <p>
 * See:
 * <ul>
 * <li><a href="http://www.music.mcgill.ca/~ich/classes/mumt306/StandardMIDIfileformat.html">http://www.music.mcgill.ca/~ich/classes/mumt306/StandardMIDIfileformat.html</a>
 * though note that the numbers given in the table are 1-based.</li>
 * <li><a href="https://en.wikipedia.org/wiki/General_MIDI">General MIDI</a>.</li>
 * </ul>
 */
public enum MIDIPercusssionInstrument
	{
	ACOUSTIC_BASE_DRUM(35),
	ELECTRIC_BASE_DRUM(36),

	HAND_CLAP(39),

	CLOSED_HI_HAT(42),

	OPEN_HI_HAT(46),

	LOW_MID_TOM(47),
	HI_MID_TOM(48);

	/**Raw zero-based instrument number. */
	public final byte instrument0;

	private MIDIPercusssionInstrument(final int inst)
		{ this.instrument0 = (byte) inst; }
	}
