package org.hd.d.statsHouse.midi;

import java.util.List;
import java.util.Objects;

// TODO: add other non-data-melody tracks.

/**A representation of a full MIDI 'tune' created from data.
 * Is immutable if dataMelody is.
 */
public record MIDITune(List<MIDIMelodyTrack> dataMelody)
    {
    public MIDITune
	    {
	    Objects.requireNonNull(dataMelody);
	    }
    }
