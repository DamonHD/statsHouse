package org.hd.d.statsHouse;

import java.util.List;
import java.util.Objects;

/**One bar, playable as MIDI, eg by conversion to MIDICSV or adding to a Track within a Sequence.
 * Represents data melody from one data stream for one playable bar.
 * <p>
 * Covers one voice/track, can cannot be polyphonic.
 * <p>
 * Individual note slots can be null to be empty.
 * <p>
 * Refers to source DataProtoBar and data stream number.
 * The number of (equal-length) notes in the bar
 * is that of the DataProtoBar.
 * <p>
 * This does not contain an absolute time nor bar number
 * to allow it to be used more than once in the same track,
 * for example in a chorus or loop.
 * <p>
 * This may be the output of a chain of transformations.
 * <p>
 * This object is immutable if the list is.
 */
public record MIDIPlayableMonophonicBar(DataProtoBar dpr, int stream, List<NoteAndVelocity> notes)
    {
    public MIDIPlayableMonophonicBar
	    {
	    Objects.nonNull(dpr);
	    if(stream < 1) { throw new IllegalArgumentException(); }
	    Objects.nonNull(notes);
	    if(dpr.dataNotesPerBar() != notes.size()) { throw new IllegalArgumentException(); }
	    }
    }
