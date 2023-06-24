package org.hd.d.statsHouse.midi;

import java.util.Collections;
import java.util.Objects;
import java.util.SortedSet;

import org.hd.d.statsHouse.NoteAndVelocity;

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
 * This object is immutable if the Set is.
 * <p>
 * A note should not run beyond the end of its bar, in general.
 * <p>
 * TODO: unit tests
 *
 * @param notes  time ordered by from non-negative clocks offset from start of bar,
 *     to note and velocity and note-on/note-off duration in clocks;
 *     may be empty but not null
 */
public record MIDIPlayableBar(SortedSet<MIDIPlayableBar.StartNoteVelocityDuration> notes, int clocks)
    {
    public MIDIPlayableBar
	    {
    	Objects.requireNonNull(notes);
    	if(clocks <= 0) { throw new IllegalArgumentException(); }
		}

    /**Supplied notes, with default clocks per bar. */
    public MIDIPlayableBar(final SortedSet<StartNoteVelocityDuration> notes)
    	{ this(notes, MIDIGen.DEFAULT_CLOCKS_PER_BAR); }

    /**Note and velocity, and +ve duration in clocks (MIDI note-on to note-off).
     * Immutable.
     * <p>
     * Totally ordered, first by start, to enforce time ordering.
     */
    public record StartNoteVelocityDuration(int start, NoteAndVelocity note, int duration)
    	implements Comparable<StartNoteVelocityDuration>
	    {
    	public StartNoteVelocityDuration
	    	{
        	if(start < 0) { throw new IllegalArgumentException(); }
        	Objects.requireNonNull(note);
        	if(duration <= 0) { throw new IllegalArgumentException(); }
	    	}

        /**Total order: sort first by start clock offset. */
    	@Override
    	public int compareTo(final StartNoteVelocityDuration o)
    		{
    		Objects.requireNonNull(o);

    		final int startDiff = start - o.start;
    		if(0 != startDiff) { return(startDiff); }
    		final int durationDiff = duration - o.duration;
    		if(0 != durationDiff) { return(durationDiff); }
    		final int noteDiff = note.note() - o.note.note();
    		if(0 != noteDiff) { return(noteDiff); }
    		final int velocityDiff = note.velocity() - o.note.velocity();
    		if(0 != velocityDiff) { return(velocityDiff); }
    		assert(o.equals(this));
    		return(0);
    		}
	    }

    /**Empty bar with default number of clocks. */
    public static final MIDIPlayableBar EMPTY_DEFAULT_CLOCKS = new MIDIPlayableBar(Collections.emptySortedSet());
    }
