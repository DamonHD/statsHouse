package org.hd.d.statsHouse.midi;

/**Various MIDI constants. */
public final class MIDIConstant
	{
	/**Prevent creation of an instance. */
    private MIDIConstant() { }

    /**GM1 generators must support at least 16 melodic voices at once. */
    public static final int GM1_MIN_MELODIC_VOICES = 16;
    /**GM1 generators must support at least 16 percussive voices at once. */
    public static final int GM1_MIN_PERCUSSIVE_VOICES = 8;

    /**GM1 generators must support at least 16 channels at once. */
    public static final int GM1_MIN_CHANNELS = 16;

    /**GM1 reserved percussion channel (1-based). */
    public static final byte GM1_PERCUSSION_CHANNEL = 10;

    /**Preferred MIDI filestype to write. */
    public static final int PREFERRED_MIDI_FILETYPE = 1;

    /**MIDI track title meta message number. */
    public static final byte METAMESSAGE_TITLE = 3;

    /**The default is centred, ie 64. */
	public static final byte DEFAULT_PAN = 64;

    /**The default volume is a little below max for some headroom; GM 2 apparently has 100.
     * Setting to a non-default value may imply setting expression (CC 11) also.
     */
    public static final byte DEFAULT_VOLUME = 100;
    /**The default expression is max to leave overall volume unchanged.
     * GM Level 1 Developer Guidelines and the DLS specification use L(dB) = 40 log (volume × expression / 127).
     * See: https://sound.stackexchange.com/questions/41549/is-midi-volume-scale-logarithmic
     */
    public static final byte DEFAULT_EXPRESSION = 127;
	}
