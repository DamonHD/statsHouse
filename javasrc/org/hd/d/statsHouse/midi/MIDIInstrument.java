package org.hd.d.statsHouse.midi;

/**GM instrument patch map, for instruments used in this code.
 * 0-based (non-negative) numbers ready to use in MIDI messages.
 * <p>
 * See
 * <a href="http://www.music.mcgill.ca/~ich/classes/mumt306/StandardMIDIfileformat.html">http://www.music.mcgill.ca/~ich/classes/mumt306/StandardMIDIfileformat.html</a>
 * though note that the numbers given in the table are 1-based.
 */
public enum MIDIInstrument
	{
	OCARINA(79), // Aka flute.
	LEAD_1_SQUARE_WAVE(80);


	/**Production level for this style. */
	public final byte inst;

	private MIDIInstrument(final int inst)
		{ this.inst = (byte) inst; }

	}
