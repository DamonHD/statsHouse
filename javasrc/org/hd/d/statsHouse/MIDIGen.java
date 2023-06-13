/*
Copyright (c) 2023, Damon Hart-Davis

Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package org.hd.d.statsHouse;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**MIDI generation in various forms and with varying levels of sophistication.
 */
public final class MIDIGen
    {
	/**Prevent creation of an instance. */
    private MIDIGen() { }


    /**Default clock pulses per quarter note. */
	public static final int DEFAULT_CLKSPQTR = 480;
	/**Default 120bpm (0.5s per quarter note / beat). */
	public static final int DEFAULT_TEMPO  = 500_000;
	/**Default beats / quarter notes per bar (4/4 time). */
	public static final int DEFAULT_BEATS_PER_BAR = 4;


    /**Framework MIDI generation from data; never null.
     * Does not include creating of GenerationParameters
     * nor final conversion to final MIDI output form.
     */
    public static MIDITune genMelody(final GenerationParameters params, final EOUDataCSV data)
	    {
    	if(null == params) { throw new IllegalArgumentException(); }
    	if(null == data) { throw new IllegalArgumentException(); }

    	// Return empty tune if no data points.
    	if(data.data().isEmpty()) { return(new MIDITune(Collections.emptyList())); }

    	return(new MIDITune(Collections.emptyList())); // FIXME
	    }

    /**Generate a MIDI Sequence from a tune.
     * @throws InvalidMidiDataException
     */
    public static Sequence genFromTuneSequence(final MIDITune tune)
		throws InvalidMidiDataException
	    {
    	if(null == tune) { throw new IllegalArgumentException(); }

		final int barClocks = DEFAULT_CLKSPQTR * DEFAULT_BEATS_PER_BAR;
		final Sequence sequence = new Sequence(Sequence.PPQ, DEFAULT_CLKSPQTR);

		// TODO: tempo track
		// TODO: percussion (etc) tracks.

		// Add data melody tracks.
		for(final MIDIMelodyTrack mt : tune.dataMelody())
			{
			final Track trackMelody = sequence.createTrack();
			final MIDITrackSetup ts = mt.setup();
			final byte channel = ts.channel();
			final byte instrument = ts.instrument();

			final ShortMessage pc = new ShortMessage();
			pc.setMessage(ShortMessage.PROGRAM_CHANGE, channel, instrument, 0);
			trackMelody.add(new MidiEvent(pc, 0));
			// TODO: volume
			// TODO: pan (if not default)

			int clock = 0;
			for(final MIDIPlayableMonophonicBar b : mt.bars())
				{
				final int noteCount = b.notes().size();
				final int clocksPerNote = barClocks / noteCount;
				int subClock = clock;
				for(final NoteAndVelocity nv : b.notes())
					{
					// Rest for null/missing note.
					if(null != nv)
						{
						// Add a note-on event to the track.
					    final ShortMessage noteOn = new ShortMessage();
					    noteOn.setMessage(ShortMessage.NOTE_ON, channel, nv.note(), nv.velocity());
					    final MidiEvent noteOnEvent = new MidiEvent(noteOn, clock);
					    trackMelody.add(noteOnEvent);
					    // Add a note-off event to the track.
					    final ShortMessage noteOff = new ShortMessage();
					    noteOff.setMessage(ShortMessage.NOTE_OFF, channel, nv.note(), 0);
					    final MidiEvent noteOffEvent = new MidiEvent(noteOff, clock+clocksPerNote-1);
					    trackMelody.add(noteOffEvent);
						}
					subClock += clocksPerNote;
					}
				clock += barClocks; // Ensure correct clocks per bar.
				}
			}
		return(sequence);
	    }

    /**Generate a MIDICSV stream from a tune. */
    public static void genFromTuneMIDICSV(final Writer w, final MIDITune tune)
	    {
    	if(null == w) { throw new IllegalArgumentException(); }
    	if(null == tune) { throw new IllegalArgumentException(); }
throw new UnsupportedOperationException("NOT IMPLEMENTED YET"); // FIXME
	    }


    /**Minimal MIDICSV generation from main data source to supplied Writer, and generate a matching Sequence.
     * Picks the main/busiest data channel and turns that into
     * a minimal tempo track and a single data melody track.
     * <p>
     * This does no alignment nor filling in of empty notes.
     * <p>
     * This uses 4 notes per bar.
     *
     * @throws IOException
     * @throws InvalidMidiDataException
     */
	public static Sequence genMinimalMelodyMIDISCV(final Writer w, final EOUDataCSV data)
			throws IOException, InvalidMidiDataException
		{
		if(null == w) { throw new IllegalArgumentException(); }
		if(null == data) { throw new IllegalArgumentException(); }

		// 0 if no data.
		// 1 if left-most data stream is main one.
		final int mainDataStream = DataUtils.maxNVal(data);
		final float maxVal = DataUtils.maxVal(data);

		// Divide data into bars.
		final int notesPerBar = 4;
		final List<DataProtoBar> protoBars = DataUtils.chopDataIntoProtoBars(notesPerBar, data);

		// Paying homage to textToMIDIv4-consolidated.sh and friends.
		final byte melodyTrack = 2;
		final byte channel = 2;
		final byte instrument = 80; // Lead 1 (square wave).
		final byte noteVelocity = 63;
		final int clksPQtr = MIDIGen.DEFAULT_CLKSPQTR;
		final int noteDeltaTime = clksPQtr;
		final int octaves = 2;
		final byte range = 12 * octaves;
		final byte offset = 60;
		final float multScaling = (maxVal > 0) ? ((range-1)/maxVal) : 1;

		final Sequence sequence = new Sequence(Sequence.PPQ, clksPQtr);

		// Start MIDI file.
		MIDICSVUtils.writeF1Header(w, (short) 2, clksPQtr);

		// First track is just tempo.
		MIDICSVUtils.writeF1MinimalTempoTrack(w, MIDIGen.DEFAULT_TEMPO);
        // MIDIX
		// Create a new tempo Track in the Sequence.
	    final Track trackTempo = sequence.createTrack();
	    // TODO: trackTempo:
	    //    http://www.java2s.com/example/java/javax.sound.midi/create-a-set-tempo-meta-event-for-midi.html
        //    https://spencerpark.github.io/MellowD/build/docs/docco/src/main/java/cas/cs4tb3/mellowd/TimingEnvironment.html

		// MIDI clocks since start.
		int clock = 0;

		// Start melody track.
		MIDICSVUtils.writeF1TrackStart(w, melodyTrack);
        // MIDIX
	    final Track trackMelody = sequence.createTrack();

		// Select instrument.
		MIDICSVUtils.writeF1ProgramC(w, melodyTrack, clock, channel, instrument);
        // MIDIX
		final ShortMessage pc = new ShortMessage();
		pc.setMessage(ShortMessage.PROGRAM_CHANGE, channel, instrument, 0);
		trackMelody.add(new MidiEvent(pc, 0));

		// Make sweet music...
		// First iterate over the proto bars...
		clock -= noteDeltaTime; // Pre-decrement to allow first note to be at zero.
		for(final DataProtoBar dbp : protoBars)
			{
			if(mainDataStream < 1) { break; } // No data!
			// ... then the data rows inside them.

			for(final var dp : dbp.dataRows().data())
				{
				// Allow time for note, played or not.
				clock += noteDeltaTime;

                // Pick out the raw data field for the main stream.
				final int field = (mainDataStream * 3);
				// Skip empty or unparseable fields.
				if(field >= dp.size()) { continue; }
				final String d = dp.get(field);
				if(d.isEmpty()) { continue; }
				final float dataValue;
				try { dataValue = Float.parseFloat(d); } catch(final NumberFormatException e) { continue; }
				// Skip unusable values.
				if(!Float.isFinite(dataValue)) { continue; }
				final float fnote = (offset + (dataValue * multScaling));
				if((fnote < 0) || (fnote > 127)) { continue; }
				final byte note = (byte)(fnote);
				if((note < 0) || (note > 127)) { continue; }

				// Play data melody note.
				MIDICSVUtils.writeF1NoteOn(w, melodyTrack, clock, channel, note, noteVelocity);
				MIDICSVUtils.writeF1NoteOff(w, melodyTrack, clock+noteDeltaTime-1, channel, note);
                // MIDIX
				// Add a note-on event to the track.
			    final ShortMessage noteOn = new ShortMessage();
			    noteOn.setMessage(ShortMessage.NOTE_ON, channel, note, noteVelocity);
			    final MidiEvent noteOnEvent = new MidiEvent(noteOn, clock);
			    trackMelody.add(noteOnEvent);
			    // Add a note-off event to the track.
			    final ShortMessage noteOff = new ShortMessage();
			    noteOff.setMessage(ShortMessage.NOTE_OFF, channel, note, 0);
			    final MidiEvent noteOffEvent = new MidiEvent(noteOff, clock+noteDeltaTime-1);
			    trackMelody.add(noteOffEvent);
				}
			}

		// End melody track.
		clock += noteDeltaTime;
		MIDICSVUtils.writeF1TrackEnd(w, melodyTrack, clock);

		// End MIDI file.
		MIDICSVUtils.writeF1Footer(w);

		return(sequence);
		}
    }
