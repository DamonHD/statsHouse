package org.hd.d.statsHouse.midi;

import java.util.Collections;
import java.util.Objects;
import java.util.SortedMap;

import org.hd.d.statsHouse.NoteAndVelocity;
import org.hd.d.statsHouse.midi.MIDIPlayableBar.NoteVelocityDuration;

/**One bar, playable as MIDI, eg by conversion to MIDICSV or adding to a Track within a Sequence.
 * Represents non-data melody or other sound for one playable bar.
 * <p>
 * May be polyphonic.
 * <p>
 * (Individual note slots can be null to be empty and ignored,
 * though this is not recommended and may become forbidden.)
 * <p>
 * This does not contain an absolute time nor bar number
 * to allow it to be used more than once in the same track,
 * for example in a chorus or loop.
 * <p>
 * This may be the output of a chain of transformations.
 * <p>
 * This object is immutable if the Map is.
 *
 * @param notes  time ordered by from non-negative clocks offset from start of bar,
 *     to note and velocity and note-on/note-off duration in clocks;
 *     may be empty but not null
 */
public record MIDIPlayableBar(SortedMap<Integer, NoteVelocityDuration> notes, int clocks)
    {
    public MIDIPlayableBar
	    {
    	Objects.requireNonNull(notes);
    	if(clocks <= 0) { throw new IllegalArgumentException(); }
		}

    /**Supplied notes, with default clocks per bar. */
    public MIDIPlayableBar(final SortedMap<Integer, NoteVelocityDuration> notes)
    	{ this(notes, MIDIGen.DEFAULT_CLOCKS_PER_BAR); }

    /**Note and velocity, and +ve duration in clocks (MIDI note-on to note-off).
     * Immutable.
     */
    public record NoteVelocityDuration(NoteAndVelocity note, int clocks)
	    {
    	public NoteVelocityDuration
	    	{
        	Objects.requireNonNull(note);
        	if(clocks <= 0) { throw new IllegalArgumentException(); }
	    	}
	    }

    /**Empty bar with default number of clocks. */
    public static final MIDIPlayableBar EMPTY_DEFAULT_CLOCKS = new MIDIPlayableBar(Collections.emptySortedMap());
    }
