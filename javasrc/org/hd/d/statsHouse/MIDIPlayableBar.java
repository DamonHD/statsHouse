package org.hd.d.statsHouse;

/**Immutable bar, playable as MIDI, eg by conversion to MIDICSV or adding to a Track within a Sequence.
 * Represents data melody and other content such as percussion.
 * <p>
 * Covers one voice/track, though can be polyphonic, eg for a percussion track.
 * <p>
 * Refers to source DataProtoBar and data stream number if data melody.
 * <p>
 * This can accommodate some MIDI meta events such as CC.
 * <p>
 * This does not contain an absolute time or bar number
 * to allow it to be used more than once in the same track,
 * for example in a chorus or loop.
 */
public final class MIDIPlayableBar
    {

    }
