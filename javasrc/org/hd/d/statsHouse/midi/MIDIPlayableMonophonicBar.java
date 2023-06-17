package org.hd.d.statsHouse.midi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.hd.d.statsHouse.DataProtoBar;
import org.hd.d.statsHouse.NoteAndVelocity;

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
 * This object is immutable if the List is.
 *
 * @param dpr  data proto bar; null if not backed by source data
 * @param stream  which data stream this is from in a non-null dpr;
 *     must be zero if there is no dpr
 * @param notes  notes from the above stream in neutral MIDI-like form; null slots are empty/silent;
 *     never null, same length as the dpr
 */
public record MIDIPlayableMonophonicBar(int dataNotesPerBar, DataProtoBar dpr, int stream, List<NoteAndVelocity> notes)
    {
    public MIDIPlayableMonophonicBar
	    {
		if(dataNotesPerBar < 1) { throw new IllegalArgumentException(); }
		if(dataNotesPerBar != notes.size()) { throw new IllegalArgumentException(); }
	    if((null != dpr) && (dpr.dataNotesPerBar() != dataNotesPerBar)) { throw new IllegalArgumentException(); }
	    if((null != dpr) && (stream < 1)) { throw new IllegalArgumentException(); }
	    if((null == dpr) && (0 != stream)) { throw new IllegalArgumentException(); }
	    Objects.requireNonNull(notes);
	    if(dpr.dataNotesPerBar() != notes.size()) { throw new IllegalArgumentException(); }
	    }

    /**Make an immutable copy/close suitable for safe sharing. */
    public MIDIPlayableMonophonicBar getImmutableClone()
	    {
    	return(new MIDIPlayableMonophonicBar(dataNotesPerBar, dpr, stream,
            Collections.unmodifiableList(new ArrayList<>(notes))));
	    }

    /**Make an immutable copy/clone with one note changed/cleared.
     *
     * @param index  index of note to set, zero-based; [0,notes.length-1]
     * @param note  new note for given index or null for no note
     * @return  immutable clone of original with the specified change
     */
    public MIDIPlayableMonophonicBar cloneAndSet(final int index, final NoteAndVelocity note)
	    {
    	final List<NoteAndVelocity> l = new ArrayList<>(notes);
    	l.set(index, note);
    	return(new MIDIPlayableMonophonicBar(dataNotesPerBar, dpr, stream,
            Collections.unmodifiableList(new ArrayList<>(l))));
	    }
    }
