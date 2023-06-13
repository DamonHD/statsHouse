package org.hd.d.statsHouse;

/**Simple MIDI tack setup, in particular including the default channel and instrument. */
public record MIDITrackSetup(byte channel, byte instrument, byte volume, byte pan)
	{
    public MIDITrackSetup
	    {
	    if(channel < 0) { throw new IllegalArgumentException(); }
	    if(channel > 15) { throw new IllegalArgumentException(); }
	    if(instrument < 0) { throw new IllegalArgumentException(); }
	    if(volume < 0) { throw new IllegalArgumentException(); }
	    if(pan < 0) { throw new IllegalArgumentException(); }
	    }

    public static final byte DEFAULT_VOLUME = 127;
    public static final byte DEFAULT_PAN = 64;

    /**The pan is set to the default centre position. */
    public MIDITrackSetup(final byte channel, final byte instrument, final byte volume)
    	{ this(channel, instrument, volume, DEFAULT_PAN); }
    /**The volume is set to its default and pan is set to the default centre position. */
    public MIDITrackSetup(final byte channel, final byte instrument)
		{ this(channel, instrument, DEFAULT_VOLUME); }
	}
