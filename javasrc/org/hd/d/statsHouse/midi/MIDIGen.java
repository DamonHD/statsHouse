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

package org.hd.d.statsHouse.midi;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.hd.d.statsHouse.DataCadence;
import org.hd.d.statsHouse.DataProtoBar;
import org.hd.d.statsHouse.DataUtils;
import org.hd.d.statsHouse.Datum;
import org.hd.d.statsHouse.EOUDataCSV;
import org.hd.d.statsHouse.GenerationParameters;
import org.hd.d.statsHouse.NoteAndVelocity;
import org.hd.d.statsHouse.ProductionLevel;
import org.hd.d.statsHouse.TuneSection;
import org.hd.d.statsHouse.TuneSectionMetadata;
import org.hd.d.statsHouse.TuneSectionPlan;

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
	/**Default clock pulses per bar, eg for empty bars. */
	public static final int DEFAULT_CLOCKS_PER_BAR = DEFAULT_CLKSPQTR * 4;
    /**Standard-ish section (eg intro/outro, chorus) length for house and related EDM. */
    public static final int DEFAULT_SECTION_BARS = 16;
    /**Default note velocity. */
    public static final byte DEFAULT_MELODY_VELOCITY = 63;
    /**Default root/lowest melody note (middle C). */
    public static final byte DEFAULT_ROOT_NOTE = 60;

    /**Default max verse sections of ~30s each.
     * This is intended to keep the full tune length to a few minutes
     * even when interspersed with other sections such as chorus.
     * <p>
     * This is entirely a stylistic 'limit' guideline!
     */
    public static final int DEFAULT_MAX_VERSE_SECTIONS = 4;


    /**Framework MIDI generation from data; never null.
     * Does not include parsing (etc) of GenerationParameters,
     * nor conversion to final MIDI output form.
     * <p>
     * Does include generation of additional tracks such as percussion.
     * <p>
     * Possible processing stages:
     * <p>
     * <ul>
     * <li>(Parse/collect generation parameters already parsed and collected.)
     * <li>Align (ls part of date to position in bar; may involve inserting nulls/padding).
     * <li>Populate with basic notes from data.
     * <li>Extend to full bars, phrases, sections.
     * <li>Add filler notes on data melody tracks.
     * <li>Add decoration notes on data melody tracks.
     * <li>Add dependent aux tracks such as pad notes.
     * <li>Add independent aux tracks (though maybe changes at sections): percussion, bass.
     * <li>(Save as .csv/.mid/.wav/etc to stdout or file, or play, from the returned MIDITune.)
     * </ul>
     */
    public static MIDITune genMelody(final GenerationParameters params, final EOUDataCSV data)
	    {
    	if(null == params) { throw new IllegalArgumentException(); }
    	if(null == data) { throw new IllegalArgumentException(); }

    	// Return empty tune if no data points.
    	if(data.data().isEmpty()) { return(new MIDITune(Collections.emptyList())); }

    	// Compute data bounds, including capping number of data streams.
		final DataBounds db = new DataBounds(data);

    	// Initial partitioning/alignment/padding for main data melody verse.
    	final List<DataProtoBar> verseProtoBars = splitAndAlignData(TuneSection.verse, params, data);

    	// Return empty tune if no bars (though in principle cannot happen).
    	if(verseProtoBars.isEmpty()) { return(new MIDITune(Collections.emptyList())); }

    	final List<TuneSectionMetadata> protoPlan = new ArrayList<>();

    	// For plain/gentle style the data is used as-is as a single verse section.
        switch(params.style())
	        {
	        case plain, gentle:
	        	{
	        	protoPlan.add(new TuneSectionMetadata(verseProtoBars.size(), TuneSection.verse));
	            break;
	        	}

	        default:
throw new UnsupportedOperationException("NOT IMPLEMENTED YET"); // FIXME
	        }

        // Top and tail with intro/outro if specified, eg to be mix-friendly.
    	final boolean hasIntroOutro = (params.introBars() > 0);
        if(hasIntroOutro)
	        {
	        protoPlan.add(0, new TuneSectionMetadata(params.introBars(), TuneSection.intro));
	        protoPlan.add(new TuneSectionMetadata(params.introBars(), TuneSection.outro));
	        }

    	// For plain/gentle style the data is used as-is as a single verse section.
		return switch (params.style()) {
		case plain -> (_genPlainMIDIMelodyTune(params, db, verseProtoBars, new TuneSectionPlan(protoPlan)));
default -> throw new UnsupportedOperationException("NOT IMPLEMENTED YET"); // FIXME
		};


//throw new UnsupportedOperationException("NOT IMPLEMENTED YET"); // FIXME
	    }

    /**Create a plain melody from the data; never null
     * Will insert a copy of the data melody at each verse in the plan
     * (the number of bars allowed must be large enough)
     * and will rest through all other sections.
     * <p>
     * All melody tracks in the result have the same number of bars.
     *
     * @param params  generation parameters; never null
     * @param verseProtoBars  data split into proto bars; never null
     * @param plan  section plan,
     *     preferably containing exactly one verse section and possible intro/outro;
     *     never null
     * @return   data melody, one or more tracks; never null
     */
    private static MIDITune _genPlainMIDIMelodyTune(
    		final GenerationParameters params,
    		final DataBounds db,
    		final List<DataProtoBar> verseProtoBars,
			final TuneSectionPlan plan)
        {
    	Objects.requireNonNull(params);
    	Objects.requireNonNull(db);
    	Objects.requireNonNull(verseProtoBars);
    	Objects.requireNonNull(plan);

// FIXME: work correctly with het data, eg no primary.
if(params.hetro()) { throw new UnsupportedOperationException("NOT IMPLEMENTED YET"); }

    	// Create tracks with deliberately- mutable/extendable (by us) bars.
    	final int streams = db.streams();
    	final int mainDataStream = db.mainDataStream();
    	final MIDIMelodyTrack tracks[] = new MIDIMelodyTrack[streams];
    	Arrays.setAll(tracks,
			i -> new MIDIMelodyTrack(new MIDITrackSetup(
					// Use separate channels for each data stream, starting from 0.
					(byte) i,
					// Use ocarina for main data stream, synth brass 1 for remainder.
					db.isMainDataStream(i+1) ? MIDIInstrument.OCARINA.instrument0 : MIDIInstrument.SYNTH_BRASS_1.instrument0,
					// Use default volume for main data stream, with the rest quieter.
					db.isMainDataStream(i+1) ? MIDITrackSetup.DEFAULT_VOLUME : (byte)(MIDITrackSetup.DEFAULT_VOLUME/2)),
				new ArrayList<>()));

    	// Parameterisation of play.
		final int octaves = 2;
		final byte range = 12 * octaves;
		final byte offset = DEFAULT_ROOT_NOTE;
		final float multScaling = (db.maxVal() > 0) ? ((range-1)/db.maxVal()) : 1;

    	// Run through all the sections,
    	// inserting the full data melody in any 'verse' section(s).
    	for(final TuneSectionMetadata ts : plan.sections())
	    	{
            if(ts.sectionType() != TuneSection.verse)
	            {
                // Skip over this section silently,
            	// inserting empty bars for all streams.
            	for(int s = 1; s <= streams; ++s)
	            	{ tracks[s - 1].bars().add(MIDIPlayableMonophonicBar.EMPTY_1_NOTE_BAR); }
            	continue;
	            }

            // Verify that section size is correct.
            if(ts.bars() != verseProtoBars.size())
        		{ throw new IllegalArgumentException(); }

            for(final DataProtoBar dbp : verseProtoBars)
            	{
            	for(int s = 1; s <= streams; ++s)
            		{
            		final boolean isMainDataStream = db.isMainDataStream(s);

            		final List<List<String>> rows = dbp.dataRows().data();
            		final int dnpb = dbp.dataNotesPerBar();
            		final List<NoteAndVelocity> notes = new ArrayList<>(dnpb);
            		for(final List<String> row : rows)
            			{
            			final Datum d = Datum.extractDatum(s, row);
            			// Rest/silence for missing stream or value,
            			// or where coverage is not strictly positive.
            			if(// d.isEmpty() ||
        					(null == d.value()) ||
        					(null == d.coverage()) || (d.coverage() <= 0))
            			    { notes.add(null); }
            			else
            				{
            				final byte note = (byte) Math.max(1, Math.min(127,
            						offset + (d.value() * multScaling)));
                            byte velocity = isMainDataStream ?
                        		DEFAULT_MELODY_VELOCITY : (DEFAULT_MELODY_VELOCITY/2);
                            if(d.coverage() < 1)
                                {
                            	// Reduce volume for low coverage / low certainty.
                            	velocity = (byte) Math.max(1, Math.min(127,
                            		velocity * d.coverage()));
                                }
							final NoteAndVelocity nv = new NoteAndVelocity(note, velocity);
							notes.add(nv);
            				}
            			}

            		// Construct MIDI-playable bar for this stream.
            		final MIDIPlayableMonophonicBar mpmb = new MIDIPlayableMonophonicBar(
            				dnpb, dbp, s, Collections.unmodifiableList(notes));
            		tracks[s - 1].bars().add(mpmb);
            		}
            	}
	    	}

    	// Return unmodifiable compact version.
    	for(int i = tracks.length; --i >= 0; )
	    	{
	    	tracks[i] = new MIDIMelodyTrack(tracks[i].setup(),
    			Collections.unmodifiableList(new ArrayList<>(tracks[i].bars())));
	    	}
    	return(new MIDITune(Arrays.asList(tracks), plan));
	    }


	/**Do initial splitting of data into whole proto melody bars for given section, including any alignment; never null.
     * The verse output is the one most reflective of the input data,
     * and should be the only one used for plain style for example.
     *
     * @param section  which song section this is for; never null
     * @param params  generation parameters; never null
     * @param data  the entire ingested data set; never null
     */
    public static List<DataProtoBar> splitAndAlignData(final TuneSection section, final GenerationParameters params, final EOUDataCSV data)
	    {
    	if(null == section) { throw new IllegalArgumentException(); }
    	if(null == params) { throw new IllegalArgumentException(); }
    	if(null == data) { throw new IllegalArgumentException(); }

    	// Data notes per bar is determined by the cadence.
    	final DataCadence cadence = DataUtils.extractDataCadenceQuick(data);
    	final int dataNotesPerBar = switch(cadence)
			{
			case Y -> 4;
			case M -> 12;
			case D -> 32;
			};

    	// We may choose not to align in the most produced music for some seeds.
    	final boolean canAlign = switch(cadence)
			{
			case Y -> false;
			case M -> true;
			case D -> true;
			};
		final boolean doAlign = canAlign &&
			(params.style().level == ProductionLevel.Gentle) ||
			(params.style().level == ProductionLevel.Danceable); // TODO: randomness
		// FIXME: do alignment where appropriate.


	    final int size = data.data().size(); // TODO: allow for alignment
	    final ArrayList<DataProtoBar> result = new ArrayList<>(1 + (size/dataNotesPerBar));

		for(int i = 0; i < size; i += dataNotesPerBar)
		    {
		    final List<List<String>> out = new ArrayList<>(dataNotesPerBar);
		    // FIXME: wrap leaf List if not already Unmodifiable.
		    for(int j = i; (j - i < dataNotesPerBar) && (j < size); ++j)
			    { out.add(data.data().get(j)); }
		    // Pad the final partial bar if necessary.
		    while(out.size() < dataNotesPerBar) { out.add(null); }
		    result.add(new DataProtoBar(dataNotesPerBar, new EOUDataCSV(Collections.unmodifiableList(out))));
		    }

		result.trimToSize();
		return(Collections.unmodifiableList(result));
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

			// Program change (setting the instrument).
			final ShortMessage pc = new ShortMessage();
			pc.setMessage(ShortMessage.PROGRAM_CHANGE, channel, instrument, 0);
			trackMelody.add(new MidiEvent(pc, 0));
			// Volume setting.; done rely on a consistent defualt.
			final ShortMessage vol = new ShortMessage();
			vol.setMessage(ShortMessage.CONTROL_CHANGE, channel, 7, ts.volume());
			trackMelody.add(new MidiEvent(vol, 0));
			// Pan (if not default).
			if(MIDITrackSetup.DEFAULT_PAN != ts.pan())
				{
				final ShortMessage pan = new ShortMessage();
				pan.setMessage(ShortMessage.CONTROL_CHANGE, channel, 10, ts.pan());
				trackMelody.add(new MidiEvent(pan, 0));
				}

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
					    final MidiEvent noteOnEvent = new MidiEvent(noteOn, subClock);
					    trackMelody.add(noteOnEvent);
					    // Add a note-off event to the track.
					    final ShortMessage noteOff = new ShortMessage();
					    noteOff.setMessage(ShortMessage.NOTE_OFF, channel, nv.note(), 0);
					    final MidiEvent noteOffEvent = new MidiEvent(noteOff, subClock+clocksPerNote-1);
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
		final List<DataProtoBar> protoBars = DataUtils.chopDataIntoProtoBarsSimple(notesPerBar, data);

		// Paying homage to textToMIDIv4-consolidated.sh and friends.
		final byte melodyTrack = 2;
		final byte channel = 2;
		final byte instrument = MIDIInstrument.LEAD_1_SQUARE_WAVE.instrument0; // Lead 1 (square wave).
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
