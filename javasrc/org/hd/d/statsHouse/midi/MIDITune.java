package org.hd.d.statsHouse.midi;

import java.util.List;
import java.util.Objects;

import org.hd.d.statsHouse.TuneSectionPlan;

// TODO: add other non-data-melody tracks.

/**A representation of a full MIDI 'tune' created from data.
 * Is immutable if dataMelody is.
 *
 * @param dataMelody  the data melody parts of the final tune;
 *     non-null but may be empty
 * @param plan  the section plan which should cover the whole melody at least if present;
 *     may be null
 *
 * The section plan may in part be folded into markers etc in a MIDI tempo track.
 */
public record MIDITune(List<MIDIMelodyTrack> dataMelody, TuneSectionPlan plan)
    {
    public MIDITune
	    {
	    Objects.requireNonNull(dataMelody);
	    }

    /**Bare melody. */
    public MIDITune(final List<MIDIMelodyTrack> dataMelody) { this(dataMelody, null); }
    }
