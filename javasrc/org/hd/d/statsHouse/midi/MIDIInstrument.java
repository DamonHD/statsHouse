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
	/**Aka dream voice (GB).  Use as house main data stream. */
	SYNTH_VOICE(55),

	/**Aka bright synth brass (GB).  Use for house secondary data stream. */
	SYNTHBRASS1(62),

	TENOR_SAX(67),

	/**Aka flute solo (GB).  Use as plain/gentle data stream voice. */
	OCARINA(79),

	LEAD_1_SQUARE_WAVE(80),

	/**Aka soft saw lead (GB). */
	LEAD_2_SAWTOOTH_WAVE(81);


	/**Production level for this style. */
	public final byte inst;

	private MIDIInstrument(final int inst)
		{ this.inst = (byte) inst; }

	}
