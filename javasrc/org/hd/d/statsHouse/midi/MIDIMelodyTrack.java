package org.hd.d.statsHouse.midi;
import java.util.List;
import java.util.Objects;

/**A single data stream melody track that is playable as MIDI.
 * Is immutable if 'bars' is.
 * <p>
 * Melody bars are assumed to be a constant duration (eg ticks)
 * throughout a track and across melody (and other) tracks
 * in one piece.
 *
 * @param setup  setup for the whole track; never null
 * @param bars   zero or more bars of melody; never null
 */
public record MIDIMelodyTrack(MIDITrackSetup setup, List<MIDIPlayableMonophonicBar> bars)
	{
    public static final byte DEFAULT_VOLUME = 0;

	public MIDIMelodyTrack
	    {
	    Objects.requireNonNull(setup);
	    Objects.requireNonNull(bars);
	    }
	}
