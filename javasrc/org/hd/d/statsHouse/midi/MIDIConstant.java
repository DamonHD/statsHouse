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
	}
