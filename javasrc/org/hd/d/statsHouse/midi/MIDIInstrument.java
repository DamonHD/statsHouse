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

/**GM 1 instrument patch map, for instruments used in this code.
 * 0-based (non-negative) numbers ready to use in MIDI messages.
 * <p>
 * See:
 * <ul>
 * <li><a href="http://www.music.mcgill.ca/~ich/classes/mumt306/StandardMIDIfileformat.html">http://www.music.mcgill.ca/~ich/classes/mumt306/StandardMIDIfileformat.html</a>
 * though note that the numbers given in the table are 1-based.</li>
 * <li><a href="https://en.wikipedia.org/wiki/General_MIDI">General MIDI</a>.</li>
 * </ul>
 */
public enum MIDIInstrument
	{
	MARIMBA(13),

	// Bass
	ACOUSTIC_BASE(32),
	ELECTRIC_BASE_FINGER(33),
	ELECTRIC_BASE_PICKED(34),
	ELECTRIC_BASE_FRETLESS(35),
	SLAP_BASE_1(36),
	SLAP_BASE_2(37),
	SYNTH_BASE_1(38),
	SYNTH_BASE_2(39),

	/**Aka dream voice (GB).  Use as house main data stream. */
	SYNTH_VOICE(55),

	/**Aka bright synth brass (GB).  Use for house secondary data stream. */
	SYNTH_BRASS_1(62),

	TENOR_SAX(67),

	/**Aka flute solo (GB).  Use as plain/gentle data stream voice. */
	OCARINA(79),

	LEAD_1_SQUARE_WAVE(80),

	/**Aka soft saw lead (GB). */
	LEAD_2_SAWTOOTH_WAVE(81),

	/**Aka boutique 808 (GB). */
	SYNTH_DRUM(120);


	/**Raw zero-based instrument number. */
	public final byte instrument0;

	private MIDIInstrument(final int inst)
		{ this.instrument0 = (byte) inst; }
	}
