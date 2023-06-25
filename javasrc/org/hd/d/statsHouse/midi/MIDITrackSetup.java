package org.hd.d.statsHouse.midi;

/**Simple MIDI tack setup, in particular including the default channel and instrument.
 * For data melody the 1-based channel number will be the 1-based data stream number.
 * (This the actual raw channel number in messages will be one less.)
 *
 * @param name  track/source name; may be null
 */
public record MIDITrackSetup(byte channel, byte instrument, byte volume, byte pan, String name)
	{
    public MIDITrackSetup
	    {
	    if(channel < 0) { throw new IllegalArgumentException(); }
	    if(channel > 15) { throw new IllegalArgumentException(); }
	    if(instrument < 0) { throw new IllegalArgumentException(); }
	    if(volume < 0) { throw new IllegalArgumentException(); }
	    if(pan < 0) { throw new IllegalArgumentException(); }
	    }

    /**The default volume is a little below max for some headroom; GM 2 apparently has 100. */
    public static final byte DEFAULT_VOLUME = 100;
    /**The default is centred, ie 64. */
    public static final byte DEFAULT_PAN = 64;

    /**The name is null. */
    public MIDITrackSetup(final byte channel, final byte instrument, final byte volume, final byte pan)
    	{ this(channel, instrument, volume, pan, null); }
    /**The pan is set to the default centre position. */
    public MIDITrackSetup(final byte channel, final byte instrument, final byte volume)
    	{ this(channel, instrument, volume, DEFAULT_PAN); }
    /**The volume is set to its default and pan is set to the default centre position. */
    public MIDITrackSetup(final byte channel, final byte instrument)
		{ this(channel, instrument, DEFAULT_VOLUME); }
	}
