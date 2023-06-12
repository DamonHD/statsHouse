package org.hd.d.statsHouse;

/**Simple MIDI-style note with note number (60 = middle C) and velocity. */
public record NoteAndVelocity(byte note, byte velocity)
	{
	public NoteAndVelocity
		{
		if(note < 0) { throw new IllegalArgumentException(); }
		if(velocity < 0) { throw new IllegalArgumentException(); }
		}
	}