package org.hd.d.statsHouse.midi;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.hd.d.statsHouse.TuneSectionPlan;

/**A representation of a full MIDI 'tune' created from data.
 * Is immutable if the bar List items are.
 * <p>
 * The section plan may in part be folded into markers etc in a MIDI tempo track.
 *
 * @param dataMelody  the data melody parts of the final tune;
 *     non-null but may be empty
 * @param supportTracks  the non-data tracks of the final tune;
 *     non-null but may be empty
 * @param plan  the section plan which should cover the whole melody at least if present;
 *     may be null
 */
public record MIDITune(List<MIDIDataMelodyTrack> dataMelody, List<MIDISupportTrack> supportTracks, TuneSectionPlan plan)
    {
    public MIDITune
	    {
	    Objects.requireNonNull(dataMelody);
	    Objects.requireNonNull(supportTracks);
	    }

    /**Data melody and support track, no plan. */
    public MIDITune(final List<MIDIDataMelodyTrack> dataMelody, final List<MIDISupportTrack> supportTracks) { this(dataMelody, supportTracks, null); }

    /**Bare data melody, no support nor plan. */
    public MIDITune(final List<MIDIDataMelodyTrack> dataMelody) { this(dataMelody, Collections.emptyList()); }
    }
