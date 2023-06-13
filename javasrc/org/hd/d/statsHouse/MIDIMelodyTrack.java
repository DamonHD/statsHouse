import java.util.List;
import java.util.Objects;

import org.hd.d.statsHouse.MIDIPlayableMonophonicBar;
import org.hd.d.statsHouse.TrackSetup;

/**A single data stream melody track that is playable as MIDI.
 * It immutable if 'bars' is.
 * <p>
 * Melody bars are assumed to be a constant duration (eg ticks)
 * throughout a track and across melody (and other) tracks
 * in one piece.
 *
 * @param setup  setup for the whole track; never null
 * @param bars   zero or more bars of melody; never null
 */
public record MIDIMelodyTrack(TrackSetup setup, List<MIDIPlayableMonophonicBar> bars)
	{
    public MIDIMelodyTrack
	    {
	    Objects.nonNull(setup);
	    Objects.nonNull(bars);
	    }
	}
