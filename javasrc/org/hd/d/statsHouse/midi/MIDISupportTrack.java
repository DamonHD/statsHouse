package org.hd.d.statsHouse.midi;
import java.util.List;
import java.util.Objects;

/**A single (non-data-stream) track that is playable as MIDI.
 * Is immutable if 'bars' is.
 * <p>
 * Melody bars are assumed to be a constant duration (eg ticks)
 * throughout a track and across melody (and other) tracks
 * in one piece.
 *
 * @param setup  setup for the whole track; never null
 * @param bars   zero or more bars of melody; never null
 */
public record MIDISupportTrack(MIDITrackSetup setup, List<MIDIPlayableBar> bars)
	{
	public MIDISupportTrack
	    {
	    Objects.requireNonNull(setup);
	    Objects.requireNonNull(bars);
	    }
	}
