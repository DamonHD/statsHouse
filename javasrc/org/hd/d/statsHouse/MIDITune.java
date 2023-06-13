package org.hd.d.statsHouse;

import java.util.List;
import java.util.Objects;

/**An representation of a full MIDI 'tune' created from data. */
public record MIDITune(List<MIDIMelodyTrack> dataMelody)
    {
    public MIDITune
	    {
	    Objects.nonNull(dataMelody);
	    }
    }
