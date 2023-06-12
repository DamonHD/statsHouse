package org.hd.d.statsHouse;

/**Immutable bar, playable as MIDI, eg by conversion to MIDICSV or adding to a Track within a Sequence.
 * Represents data melody from one data stream for one playable bar.
 * <p>
 * Covers one voice/track, can cannot be polyphonic.
 * <p>
 * Refers to source DataProtoBar and data stream number if data melody.
 * <p>
 * This does not contain an absolute time or bar number
 * to allow it to be used more than once in the same track,
 * for example in a chorus or loop.
 * <p>
 * This may be the output of a chain of transformations.
 */
public final class MIDIPlayableMonophonicBar
    {

    }
