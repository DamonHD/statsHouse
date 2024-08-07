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
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.hd.d.statsHouse.GenerationParameters;
import org.hd.d.statsHouse.Main;
import org.hd.d.statsHouse.data.DataBounds;
import org.hd.d.statsHouse.data.DataProtoBar;
import org.hd.d.statsHouse.data.DataUtils;
import org.hd.d.statsHouse.data.Datum;
import org.hd.d.statsHouse.data.EOUDataCSV;
import org.hd.d.statsHouse.generic.ChorusStyleFromData;
import org.hd.d.statsHouse.generic.DataCadence;
import org.hd.d.statsHouse.generic.NoteAndVelocity;
import org.hd.d.statsHouse.generic.PickOne;
import org.hd.d.statsHouse.generic.ProductionLevel;
import org.hd.d.statsHouse.generic.ProgressionGroup;
import org.hd.d.statsHouse.generic.Scale;
import org.hd.d.statsHouse.generic.Style;
import org.hd.d.statsHouse.generic.TuneSection;
import org.hd.d.statsHouse.generic.TuneSectionMetadata;
import org.hd.d.statsHouse.generic.TuneSectionPlan;
import org.hd.d.statsHouse.midi.lib.DataChorusGen;
import org.hd.d.statsHouse.midi.lib.SupportBarGen;

/**MIDI generation in various forms and with varying levels of sophistication.
 */
public final class MIDIGen
    {
	/**Prevent creation of an instance. */
    private MIDIGen() { }


    /**Default clock pulses per quarter note. */
	public static final int DEFAULT_CLKSPQTR = 480;
	/**Default 120bpm (0.5s per quarter note / beat). */
	public static final int DEFAULT_TEMPO = 500_000;
	/**Default beats / quarter notes per bar (4/4 time). */
	public static final int DEFAULT_BEATS_PER_BAR = 4;
	/**Default clock pulses per bar, eg for empty bars. */
	public static final int DEFAULT_CLOCKS_PER_BAR = DEFAULT_CLKSPQTR * 4;
    /**Standard-ish section (eg intro/outro, chorus) length for house and related EDM.
     * Note that other section lengths may make sense,
     * eg 12 bars with a bar per month of data.
     */
    public static final int DEFAULT_SECTION_BARS = 16;
    /**Default minimum section length in bars. */
    public static final int DEFAULT_MIN_SECTION_BARS = 8; // 4
    /**Default note velocity. */
    public static final byte DEFAULT_MELODY_VELOCITY = 63;
    /**Default maximum note velocity to avoid clipping. */
    public static final byte DEFAULT_MAX_MELODY_VELOCITY = 100;
    /**Default root/lowest melody note (middle C). */
    public static final byte DEFAULT_ROOT_NOTE = 60;
    /**Default range of a data melody in octaves. */
    public static final int DEFAULT_RANGE_OCTAVES = 2;

    /**Default maximum number of verse sections of ~30s each.
     * This is intended to keep the full danceable tune length to a few minutes
     * even when interspersed with other sections such as chorus.
     * <p>
     * This is entirely a stylistic 'limit' guideline!
     */
    public static final int DEFAULT_MAX_VERSE_SECTIONS = 4;

    /**Default minimum number of verse sections
     * This is intended to allow at least one danceable chorus repeat.
     * <p>
     * This is entirely a stylistic 'limit' guideline!
     */
    public static final int DEFAULT_MIN_VERSE_SECTIONS = 2;

    /**Default target tune (~120bpm) bars for 'radio' version (no DJ intro/outro).
     * This is intended to keep the full tune length to a ~4 minutes
     * (ie ~4*32 bars).
     * <p>
     * This is entirely a stylistic 'limit' guideline!
     */
    public static final int DEFAULT_TYPICAL_RADIO_TUNE_BARS = DEFAULT_MAX_VERSE_SECTIONS * 32;

    /**Default scale to use for house tracks; minor is common for most sub-genres. */
    public static final Scale DEFAULT_HOUSE_SCALE = Scale.NATURAL_MINOR; // Scale.MAJOR;


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
    public static MIDITune genTune(final GenerationParameters params, final EOUDataCSV data)
	    {
    	if(null == params) { throw new IllegalArgumentException(); }
    	if(null == data) { throw new IllegalArgumentException(); }

    	// Return empty tune if no data points.
    	if(data.data().isEmpty()) { return(new MIDITune(Collections.emptyList())); }

    	// Compute data bounds, including capping number of data streams.
		final DataBounds db = new DataBounds(data);

    	// For plain/gentle style the data is used as-is as a single verse section.
		return switch (params.style()) {
		case plain, gentle -> _genPlainGentleMIDITune(params, db, data);
		case house -> _genHouseMIDITune(params, db, data);
default -> throw new UnsupportedOperationException("NOT IMPLEMENTED YET"); // FIXME
		};
	    }

    /**Create a house tune from data; never null though may be empty.
     *
     * @param params  generation parameters; never null
     * @param db  data bounds; never null
     * @return data melody, one or more tracks; never null
     */
    private static MIDITune _genHouseMIDITune(
    		final GenerationParameters params,
    		final DataBounds db,
			final EOUDataCSV data)
    	{
    	Objects.requireNonNull(params);
    	switch(params.style()) {
			case house: break;
			default: throw new IllegalArgumentException("unsupported style");
			}
    	Objects.requireNonNull(db);
    	Objects.requireNonNull(data);

    	// Progression group for whole house.
    	// Any once-per-tune selections can use this.
    	final ProgressionGroup prog = new ProgressionGroup(params, "house");

    	// Initial partitioning/alignment/padding for main data melody verse.
    	final List<DataProtoBar> verseProtoBars = splitAndAlignData(TuneSection.verse, params, data);

    	// Return empty tune if no bars (though in principle cannot happen).
    	if(verseProtoBars.isEmpty()) { return(new MIDITune()); }

    	// TODO: drive various items explicitly from cadence, eg voice choice, section length.
    	final DataCadence cadence = DataUtils.extractDataCadenceQuick(data);

        // Construct tune plan/structure...
    	final List<TuneSectionMetadata> plan = new ArrayList<>();

    	// Parameterisation of melody play with scales.
    	// Have a more muted tonal range for house, to let percussion/base stand out.
		final int octaves = Math.max(1, DEFAULT_RANGE_OCTAVES/2);
//        final Scale scale = MIDIGen.DEFAULT_HOUSE_SCALE;
        final Scale scale = prog.pickOneNoProgression(PickOne.SQUARE, Arrays.asList(
    		Scale.NATURAL_MINOR,
    		Scale.HARMONIC_MINOR,
    		Scale.DORIAN,
    		Scale.MAJOR,
    		Scale.CHROMATIC
    		));

    	// Establish how many sections worth of 'verse' data there is.
    	// Always at least one.
    	// Prepared to stretch a bit (25%) else truncate as needed.
		// If intro bar count is specified then it wins.
		// TODO: allow verse repeats when only original material for one.
		// TODO: auto-select section length for some cadences, eg 12 for bar-per-month daily cadence.
    	final int sectionBars =
			(params.introRequestedFixedLength() ? params.introBars() :
				((0 != cadence.defaultBarsCycle) ? cadence.defaultBarsCycle :
				    ((verseProtoBars.size() >= MIDIGen.DEFAULT_SECTION_BARS)) ?
			    		MIDIGen.DEFAULT_SECTION_BARS : MIDIGen.DEFAULT_MIN_SECTION_BARS));
        final int availableVerseSections =
        	Math.max(1, (verseProtoBars.size() + (sectionBars/4)) / sectionBars);
        final int verseSectionCount = Math.min(DEFAULT_MAX_VERSE_SECTIONS, availableVerseSections);

        // Section plan based on available data and song structure...
        // TODO: breakdown, drop, etc.
        do  {
	        for(int i = 0; i < verseSectionCount; ++i)
		        {
	        	plan.add(new TuneSectionMetadata(sectionBars, TuneSection.verse));
	        	// Chorus after every verse.
	        	plan.add(new TuneSectionMetadata(sectionBars, TuneSection.chorus));
		        }
    		}
	        // Repeat verse/chorus as needed to bring core tune
	        // up to at least half target length.
	        while(plan.size() * sectionBars < DEFAULT_TYPICAL_RADIO_TUNE_BARS/2);

        // Top and tail with intro/outro if specified, eg to be DJ mix-friendly.
    	final boolean hasIntroOutro = (params.introRequested());
        if(hasIntroOutro)
	        {
        	plan.add(0, new TuneSectionMetadata(sectionBars, TuneSection.intro));
        	plan.add(new TuneSectionMetadata(sectionBars, TuneSection.outro));
	        }

    	// Create melody tracks with extendable (within this method) bars.
    	final int streams = db.streams();
    	final MIDIDataMelodyTrack tracks[] = new MIDIDataMelodyTrack[streams];
    	Arrays.setAll(tracks,
			i -> new MIDIDataMelodyTrack(
					genMIDITrackSetupGeneric(i+1, params, db,
						((null==data)?null:("source: "+DataUtils.extractSourceName(data, i+1)))),
				new ArrayList<>()));

    	// Create support tracks with extendable (within this method) bars.
    	final MIDISupportTrack percTrack =
			new MIDISupportTrack(
    			new MIDITrackSetup((byte)(MIDIConstant.GM1_PERCUSSION_CHANNEL-1),
    					(byte) 0,
    					MIDIConstant.DEFAULT_VOLUME,
    					MIDIConstant.DEFAULT_PAN,
    					"percussion: house"),
    			new ArrayList<>());
    	final MIDISupportTrack bassTrack =
			new MIDISupportTrack(
    			new MIDITrackSetup((MIDIConstant.GM1_PERCUSSION_CHANNEL), // Use channel one after percussion.
    					prog.pickOneNoProgression(PickOne.SQUARE, Arrays.asList(
							MIDIInstrument.SYNTH_BASE_2,
							MIDIInstrument.SYNTH_BASE_1,
							MIDIInstrument.ELECTRIC_BASE_FINGER)).instrument0,
    					(byte) (2*(MIDIConstant.DEFAULT_VOLUME/3)),
    					(byte) (MIDIConstant.DEFAULT_PAN+1), // Slightly off to side.
    					"bass: house"),
    			new ArrayList<>());
    	final MIDISupportTrack support[] = { percTrack, bassTrack };

    	// Run through all the sections,
    	// inserting the full data melody and support as needed.
    	int verseCount = 0;
    	int chorusCount = 0;
    	for(int sectionNumber = 0; sectionNumber < plan.size(); ++sectionNumber)
	    	{
    		final TuneSectionMetadata ts = plan.get(sectionNumber);
    		// Previous section (null if none) to help with transitions.
    		final TuneSectionMetadata tsPrev = (sectionNumber > 0) ? plan.get(sectionNumber-1) : null;
    		// Next section (null if none) to help with transitions.
    		final TuneSectionMetadata tsNext = (sectionNumber+1 < plan.size()) ? plan.get(sectionNumber+1) : null;

            // Verify that section size is correct.
            assert(ts.bars() == sectionBars);

    		_generateHousePercussionBySection(percTrack, ts, prog);

            // Fade in and out first and last verse/chorus.
            // Also fade in/out each non-primary instrument verse?
    		// Maybe do not want this type of logic hard-wired in.
    		// TODO: Alt: sometimes omit (some) fades depending on the seed.
    		// If the previous section is intro (or there is no previous section)
    		// then set this section to fade in.
    		final boolean fadeIn = (null == tsPrev) || (TuneSection.intro == tsPrev.sectionType());
    		// If the following section is outro (or there is no further section)
    		// then set this section to fade out.
    		final boolean fadeOut = (null == tsNext) || (TuneSection.outro == tsNext.sectionType());

    		// If this verse is not getting any other fade
    		// and it is followed by a drop or a (higher-energy) chorus
    		// then adjust the expression to build up to the drop/chorus.
    		final boolean followedByDrop = (null != tsNext) &&
				(switch(tsNext.sectionType()) {
				case drop, chorus -> true;
				default -> false;});

            _generateHouseBassBySection(bassTrack, ts,
            		fadeIn, fadeOut, followedByDrop,
            		prog, sectionNumber);

            // Inject relatively-vanilla data melody for verse.
            switch(ts.sectionType())
	        	{
	        	case verse:
	        		{
	        		// Extract and pad to exactly section size for last one if needed.
	        	    // If truncating (ie not using some bars)
	        		// then on alternate repeats discard late bars rather than early ones
	        		// for a little taste of progression.
	        		// This also means that the latest data is heard on the first verse set.
	        		final boolean discardEarlyBars = (0 == ((verseCount / verseSectionCount) & 1));
	        		final int excessBars = verseProtoBars.size() - (verseSectionCount * sectionBars);
	        		final int startOffset = (discardEarlyBars ? Math.max(0, excessBars) : 0);
	        		final List<DataProtoBar> sectionProtoBars = new ArrayList<>(sectionBars);
	        		final int startRow = ((verseCount % verseSectionCount) * sectionBars) + startOffset;
	        		final int endRow = startRow + sectionBars;
//System.err.println(String.format("protobars=%d, verseCount=%d, excessBars=%d, startRow=%d",verseProtoBars.size(), verseCount, excessBars, startRow ));
        			final DataProtoBar dpplast = verseProtoBars.get(verseProtoBars.size() - 1);
	        		for(int dr = startRow; dr < endRow; ++dr)
		        		{
	        			final DataProtoBar dbp = (dr < verseProtoBars.size()) ? verseProtoBars.get(dr) :
	        				(new DataProtoBar(dpplast.dataNotesPerBar(),
        						new EOUDataCSV(Collections.nCopies(dpplast.dataNotesPerBar(), Collections.emptyList()))));
	        			sectionProtoBars.add(dbp);
		        		}

	        		// Generate notes from data.
        			for(int s = 1; s <= streams; ++s)
	                	{
                		final boolean isNotSecondaryDataStream = params.hetero() || db.isMainDataStream(s);

                		// Collect all the bars for this stream (for this section).
                		final List<MIDIPlayableMonophonicDataBar> mpmBars =
            				new ArrayList<>(sectionBars);

                		for(final DataProtoBar dbp : sectionProtoBars)
	                		{
	                		final List<List<String>> rows = dbp.dataRows().data();
	                		final int dnpb = dbp.dataNotesPerBar();
	                		final List<NoteAndVelocity> notes = new ArrayList<>(dnpb);
	                		for(final List<String> row : rows)
	                			{
	                			final Datum d = Datum.extractDatum(s, row);
	                			// Rest/silence for missing stream or value,
	                			// or where coverage is not strictly positive.
	                			final NoteAndVelocity n = datumToNoteAndVelocity(
                					d,
                					isNotSecondaryDataStream,
                					scale,
                					octaves,
                					db.maxVal());
	                			notes.add(n);
	                			}

	                		// Construct MIDI-playable bar for this stream.
	                		final MIDIPlayableMonophonicDataBar mpmb = new MIDIPlayableMonophonicDataBar(
	                				dnpb, dbp, s, Collections.unmodifiableList(notes));
	                		mpmBars.add(mpmb);
	                		}

    	        		// Fill in missing notes for each section (for each stream).
                		fillInMissingNotes(params, isNotSecondaryDataStream, mpmBars);

                		// TODO: other transformations

                		// TODO: construct padding track?

                		final List<MIDIPlayableMonophonicDataBar> newBars;
                		if(isNotSecondaryDataStream && !fadeIn && !fadeOut && followedByDrop)
	                		{
                			// Warm up to drop...
                			newBars = warmUpToDrop(mpmBars, MIDIPlayableMonophonicDataBar.class);
	                		}
                		else
	                		{
                			// Fade in and/or out for start/finish.
                    		newBars = optionalFadeInOut(mpmBars, MIDIPlayableMonophonicDataBar.class,
                				fadeIn || !isNotSecondaryDataStream,
                				fadeOut || !isNotSecondaryDataStream);
	                		}

						tracks[s - 1].bars().addAll(newBars);
	                	}

        			++verseCount;
	                break;
	                }

	        	case chorus:
	        		{
        			++chorusCount;
	            	for(int s = 1; s <= streams; ++s)
	            		{
        				// Make the bars!
	            		final List<MIDIPlayableMonophonicDataBar> mpmBars =
    						DataChorusGen.makeHouseDataChorusBars(
    							ChorusStyleFromData.SyntheticRepresentativeDataBar, // Alt: randomise
								chorusCount, s, ts, params, db, data,
								scale);
//	        			assert(mpmBars.size() == ts.bars());
	            		tracks[s - 1].bars().addAll(optionalFadeInOut(mpmBars, MIDIPlayableMonophonicDataBar.class, fadeIn, fadeOut));
	            		}
	        		break;
	        		}

	        	default:
	                // Skip over this section silently,
	            	// inserting empty bars for all streams.
	            	for(int s = 1; s <= streams; ++s)
		            	{
	            		tracks[s - 1].bars().addAll(
	        				Collections.nCopies(ts.bars(), MIDIPlayableMonophonicDataBar.EMPTY_1_NOTE_BAR));
	            		}
	            	break;
	        	}
	    	}

		// Return unmodifiable compact version.
		for(int i = tracks.length; --i >= 0; )
			{
			tracks[i] = new MIDIDataMelodyTrack(tracks[i].setup(),
				Collections.unmodifiableList(new ArrayList<>(tracks[i].bars())));
			}
		for(int i = support.length; --i >= 0; )
			{
			support[i] = new MIDISupportTrack(support[i].setup(),
				Collections.unmodifiableList(new ArrayList<>(support[i].bars())));
			}
		return(new MIDITune(Arrays.asList(tracks), Arrays.asList(support), new TuneSectionPlan(plan)));
    	}

    /**Warm up for a following drop (or high-energy section) with a slow fall then sudden rise in expression; never null.
     * Start at maximum, fade out a little until the last bar, then fade back up to max.
     *
     * @param bars  unfaded bars; never null
     * @return  same-length List of bars with new expression set; never null
     */
    private static <T extends MIDIBarExpression> List<T> warmUpToDrop(final List<T> bars, final Class<T> type)
    	{
		Objects.requireNonNull(bars);
		Objects.requireNonNull(type);

		final int barCount = bars.size();
		if(0 == barCount) { return(bars); }

        // Number of bars to partly fade out over.
		// If only one bar input then there will be no fade out.
        final int fadeBarCount = barCount - 1;
        final byte totalFade = (MIDIConstant.DEFAULT_EXPRESSION + 1) / 3;
        final int fadePerBar = (byte) (totalFade / Math.max(1, fadeBarCount));

        final ArrayList<T> updatedBars = new ArrayList<>(barCount);

        // Bars before final one slowly fade out a little.
        byte expression = MIDIConstant.DEFAULT_EXPRESSION;
        for(int i = 0; i < fadeBarCount; ++i)
	        {
        	final byte newExpression = (byte) (expression - fadePerBar);
            updatedBars.add(type.cast(bars.get(i).cloneAndSetExpression(expression, newExpression)));
            expression = newExpression;
	        }
//        assert(expression > 0); // Should not be fading all the way out!

        // Fade back up on final bar.
        updatedBars.add(type.cast(bars.get(barCount-1).cloneAndSetExpression(expression, MIDIConstant.DEFAULT_EXPRESSION)));

//		assert(barCount == updatedBars.size());
		updatedBars.trimToSize(); // Should be a no-op.
        return(Collections.unmodifiableList(updatedBars));
        }

	/**Apply optional fade-in/fade-out to the List of bars, to hit silent at the end.
     * The default behaviour is to return the input List unchanged.
     * <p>
     * This assumes that the incoming bars are all at default (maximum) expression.
     * <p>
     * The default behaviour is to fade in/out over
     * the first/last quarter of the section or ~4 bars,
     * whichever is shorter.
     * <p>
     * An empty list of bars is left unchanged.
     * <p>
     * For a single bar, only fade-out will happen if both are requested.
     *
     * @param bars  unfaded bars; never null
     * @param fadeIn  if true, request a fade in
     * @param fadeOut  if true, request a fade out
     * @return  same-length List of bars with new expression set; never null
     */
	private static <T extends MIDIBarExpression> List<T> optionalFadeInOut(
			final List<T> bars, final Class<T> type,
			final boolean fadeIn, final boolean fadeOut)
		{
		Objects.requireNonNull(bars);
		Objects.requireNonNull(type);

		final int barCount = bars.size();
		if(0 == barCount) { return(bars); }

		// Forbid both fade in and fade out on a single bar section.
		// Block any fade in for this case.
		final boolean blockFadeIn = fadeOut && (1 == barCount);
		final boolean doFadeIn = fadeIn && !blockFadeIn;
		final boolean doFadeOut = fadeOut;

        final int fadeBarCount = Math.max(1, Math.min(4, barCount/4));
        final int postFadeInBarIndex = doFadeIn ? fadeBarCount : 0;
        final int firstFadeOutBarIndex = doFadeOut ? (barCount - fadeBarCount) : barCount;
        final int fadePerBar = (MIDIConstant.DEFAULT_EXPRESSION + 1) / fadeBarCount;

        final ArrayList<T> updatedBars = new ArrayList<>(barCount);

		// Raise expression on the lead bars, ending at max/default.
        if(doFadeIn)
	        {
			byte expression = 0;
	        for(int i = 0; i < postFadeInBarIndex; ++i)
	            {
	        	final boolean isFinalFadeInBar = (i == postFadeInBarIndex - 1);
	        	final byte newExpression = (byte)
	    			(isFinalFadeInBar ? MIDIConstant.DEFAULT_EXPRESSION : (expression + fadePerBar));
	            updatedBars.add(type.cast(bars.get(i).cloneAndSetExpression(expression, newExpression)));
//	            assert(expression < MIDIConstant.DEFAULT_EXPRESSION); // No note (even last) at max.
	            expression = newExpression;
	            }
//	        assert(MIDIConstant.DEFAULT_EXPRESSION == expression);
	        }

        // Preserve any middle unfaded portion.
		updatedBars.addAll(bars.subList(postFadeInBarIndex, firstFadeOutBarIndex));

		// Reduce expression on the tail bars, ending at 0.
		if(doFadeOut)
			{
			byte expression = MIDIConstant.DEFAULT_EXPRESSION;
	        for(int i = firstFadeOutBarIndex; i < barCount; ++i)
	            {
	        	final boolean isFinalFadeOutBar = (i == barCount - 1);
	        	final byte newExpression = (byte)
	    			(isFinalFadeOutBar ? 0 : (expression - fadePerBar));
	            updatedBars.add(type.cast(bars.get(i).cloneAndSetExpression(expression, newExpression)));
//	            assert(expression > 0); // No note (even last) totally silent.
	            expression = newExpression;
	            }
//	        assert(0 == expression);
			}

//		assert(barCount == updatedBars.size());
		updatedBars.trimToSize(); // Should be a no-op.
        return(Collections.unmodifiableList(updatedBars));
		}

	/**Generate a single house tune section of bass. */
	public static void _generateHouseBassBySection(
			final MIDISupportTrack bassTrack,
			final TuneSectionMetadata ts,
			final boolean fadeIn, final boolean fadeOut, final boolean followedByDrop,
			final ProgressionGroup prog,
			final int sectionNumber)
		{
		// Inject normal bass for verse and chorus only,
		// though possibly varying between types and instances.
		switch(ts.sectionType())
			{
			case verse, chorus:
				{
				final MIDIPlayableBar bar = SupportBarGen.makeBasicHouseBassBar(
					ts.sectionType(), prog, sectionNumber);
				final int barCount = ts.bars();
				final List<MIDIPlayableBar> oldBars = Collections.nCopies(barCount, bar);

        		final List<MIDIPlayableBar> newBars;
        		if(!fadeIn && !fadeOut && followedByDrop)
            		{
        			// Warm up to drop...
        			newBars = warmUpToDrop(oldBars, MIDIPlayableBar.class);
            		}
        		else
            		{
        			// Fade in and/or out for start/finish.
            		newBars = optionalFadeInOut(oldBars, MIDIPlayableBar.class, fadeIn, fadeOut);
            		}

				bassTrack.bars().addAll(newBars);
		        break;
		        }
			default:
				// Skip this section type for bass.
				bassTrack.bars().addAll(
					Collections.nCopies(ts.bars(), MIDIPlayableBar.EMPTY_DEFAULT_CLOCKS));
		    	break;
			}
		}

	/**Generate a single house tune section of percussion. */
	public static void _generateHousePercussionBySection(
			final MIDISupportTrack percTrack,
			final TuneSectionMetadata ts,
			final ProgressionGroup prog)
	    {
		// Inject percussion for most section types,
		// though possibly varying between types and instances.
		switch(ts.sectionType())
		    {
		    case drop, breakdown:
		    	// Skip this section type for percussion.
				percTrack.bars().addAll(
					Collections.nCopies(ts.bars(), MIDIPlayableBar.EMPTY_DEFAULT_CLOCKS));
		    	break;
		    // Default simple floor-to-the-floor.
		    default:
		        {
				final MIDIPlayableBar bar = SupportBarGen.makeBasicHousePercussionBar(prog, false);
				final MIDIPlayableBar barFinal = SupportBarGen.makeBasicHousePercussionBar(prog, true);
				final int barCount = ts.bars();
				if(barCount > 1) { percTrack.bars().addAll(Collections.nCopies(barCount-1, bar)); }
				percTrack.bars().add(barFinal);
		        break;
		        }
		    }
	    }

    /**Convert datum to note/velocity without a scale; may be null.
     *
     * @param d  datum; never null
     * @param isNotSecondaryDataStream  true unless a known secondary stream
     * @param multScaling  +ve multiplier to file data value to note range; usually ]0,1]
     * @return  note, or null for a rest ie (no note)
     */
	public static NoteAndVelocity datumToNoteAndVelocityNoScale(
			final Datum d,
			final boolean isNotSecondaryDataStream,
			final float multScaling)
		{
		Objects.requireNonNull(d);
		if(!Float.isFinite(multScaling)) { throw new IllegalArgumentException(); }
		if(multScaling <= 0) { throw new IllegalArgumentException(); }

		final NoteAndVelocity n;
		if(// d.isEmpty() ||
			(null == d.value()) ||
			(null == d.coverage()) || (d.coverage() <= 0))
		    { n = null; }
		else
			{
			// Simple linear scaling of data value to MIDI note.
			final byte note = (byte) Math.max(0, Math.min(127,
					DEFAULT_ROOT_NOTE + (d.value() * multScaling)));
			// Velocity/volume lowered for secondary streams and low coverage.
		    byte velocity = isNotSecondaryDataStream ?
				DEFAULT_MELODY_VELOCITY : ((2*DEFAULT_MELODY_VELOCITY)/3);
		    if(d.coverage() < 1)
		        {
		    	// Reduce volume for low coverage / low certainty.
		    	velocity = (byte) Math.max(1, Math.min(127,
		    		velocity * d.coverage()));
		        }
			final NoteAndVelocity nv = new NoteAndVelocity(note, velocity);
			n = nv;
			}
		return(n);
		}

    /**Convert datum to note/velocity with a scale; may be null.
     * Maximum data value will be exactly 'octaves' above root,
     *
     * @param d  datum; never null
     * @param isNotSecondaryDataStream  true unless a known secondary stream
     * @param scale  never null
     * @param octaves  number of octaves to range over; strictly positive
     * @param maxVal  maximum data value across all relevant streams; finite, non-negative
     * @return  note, or null for a rest ie (no note)
     */
	public static NoteAndVelocity datumToNoteAndVelocity(
			final Datum d,
			final boolean isNotSecondaryDataStream,
			final Scale scale, final int octaves, final float maxVal)
		{
		Objects.requireNonNull(d);
		Objects.requireNonNull(scale);
		if(octaves < 1) { throw new IllegalArgumentException(); }
		if(!Float.isFinite(maxVal)) { throw new IllegalArgumentException(); }
		if(maxVal < 0) { throw new IllegalArgumentException(); }

		final NoteAndVelocity n;
		if(// d.isEmpty() ||
			(null == d.value()) ||
			(d.value() < 0) || // FIXME: allow some -ve values.
			(null == d.coverage()) || (d.coverage() <= 0))
		    { n = null; }
		else
			{
			final int notesPerOctave = scale.semitones.size();
			final int stepsRange = octaves * notesPerOctave;
			final float multScaling = stepsRange / ((maxVal > 0) ? maxVal : 1);
			final int scaledNote = Math.max(0, Math.round(d.value() * multScaling));
			final int octave = scaledNote / notesPerOctave;
			final int residualIntervals =  scaledNote % notesPerOctave;
			final int rawMIDINote = DEFAULT_ROOT_NOTE +
				(12 * octave) +
				scale.semitones.subList(0, residualIntervals).stream().mapToInt(Integer::intValue).sum();
			// Coerce into valid range of MIDI note.
			final byte note = (byte) Math.max(0, Math.min(127, rawMIDINote));
			// Velocity/volume lowered for secondary streams and low coverage.
		    byte velocity = isNotSecondaryDataStream ?
	    		DEFAULT_MAX_MELODY_VELOCITY : ((2*DEFAULT_MAX_MELODY_VELOCITY)/3);
		    if(d.coverage() < 1)
		        {
		    	// Reduce volume for low coverage / low certainty.
		    	velocity = (byte) Math.max(1, Math.min(127,
		    		velocity * d.coverage()));
		        }
			final NoteAndVelocity nv = new NoteAndVelocity(note, velocity);
			n = nv;
			}
		return(n);
		}


    /**Fill in missing notes for each section (for each stream).
     * (Possibly generalisable with general transformation/plugin.)
     * <p>
     * TODO: unit tests
     *
     * @param params  generation parameters; never null
     * @param isNotSecondaryDataStream  true unless the primary data stream
     * @param mpmBars  mutable entire section of data bars possibly with some null notes;
     *     mpmBars should not be null not should any of the bars,
     *     and all bars should have the correct number of notes slots
     */
	private static void fillInMissingNotes(
			final GenerationParameters params,
			final boolean isNotSecondaryDataStream,
			final List<MIDIPlayableMonophonicDataBar> mpmBars)
		{
		Objects.requireNonNull(params);
		Objects.requireNonNull(mpmBars);

		// For data with a cycle per bar, eg typically monthly-cadence, one year per bar...
		//
		// For each note in each bar if null:
		//   * copy (at low velocity) the latest note in this slot from a previous bar, else
		//   * copy (at low velocity) the earliest note in this slot from a following bar.
		// So echo this note position in the recent past, else foreshadow the near future.

		// Maximum velocity of filled-in ghost note.
		final byte ghostVelocity = DEFAULT_MELODY_VELOCITY / 2;

		final int bars = mpmBars.size();
		for(int i = 0; i < bars; ++i)
			{
//            assert(null != mpmBars.get(i));

            nextNote:
        	for(int noteIndexInBar = mpmBars.get(i).dataNotesPerBar(); --noteIndexInBar >= 0; )
	            {
                if(null != mpmBars.get(i).notes().get(noteIndexInBar)) { continue; }

                // Have found a missing note...

                // Look backwards first.
                for(int k = i; --k >= 0; )
	                {
                	final MIDIPlayableMonophonicDataBar otherBar = mpmBars.get(k);
                	final NoteAndVelocity oldNote = otherBar.notes().get(noteIndexInBar);
                	if(null == oldNote) { continue; }
                    // Have found a non-null note to borrow from.
                	final NoteAndVelocity newNote = new NoteAndVelocity(oldNote.note(), (byte) Math.min(ghostVelocity, oldNote.velocity()));
                	mpmBars.set(i, mpmBars.get(i).cloneAndSetNote(noteIndexInBar, newNote));
//                	assert(null != mpmBars.get(i).notes().get(noteIndexInBar)) ;
                	continue nextNote;
	                }
                // Look forwards.
                for(int k = i; ++k < bars; )
	                {
                	final MIDIPlayableMonophonicDataBar otherBar = mpmBars.get(k);
                	final NoteAndVelocity oldNote = otherBar.notes().get(noteIndexInBar);
                	if(null == oldNote) { continue; }
                    // Have found a non-null note to borrow from.
                	final NoteAndVelocity newNote = new NoteAndVelocity(oldNote.note(), (byte) Math.min(ghostVelocity, oldNote.velocity()));
                	mpmBars.set(i, mpmBars.get(i).cloneAndSetNote(noteIndexInBar, newNote));
//                	assert(null != mpmBars.get(i).notes().get(noteIndexInBar)) ;
                	continue nextNote;
	                }
	            }
			}
		}

	/**Create a plain (or gentle) melody from data; never null.
     * All melody tracks in the result have the same number of bars.
     * <p>
     * The 'gentle' output is the same as plain but with two extras:
     * <ul>
     * <li>The data melody may be 'aligned' if appropriate,
     *     eg to year boundaries for monthly-cadence data</li>
     * <li>There is a simple percussion track added,
     *     with a sound at the start of each bar,
     *     eg to help emphasise alignment</li>
     * </ul>
     *
     * @param params  generation parameters; never null
     * @param db  data bounds; never null
     * @return data melody, one or more tracks; never null
     */
    private static MIDITune _genPlainGentleMIDITune(
    		final GenerationParameters params,
    		final DataBounds db,
			final EOUDataCSV data)
        {
    	Objects.requireNonNull(params);
    	switch(params.style()) {
			case plain, gentle: break;
			default: throw new IllegalArgumentException("unsupported style");
			}
    	Objects.requireNonNull(db);
    	Objects.requireNonNull(data);

    	// Initial partitioning/alignment/padding for main data melody verse.
    	final List<DataProtoBar> verseProtoBars = splitAndAlignData(TuneSection.verse, params, data);

    	// Return empty tune if no bars (though in principle cannot happen).
    	if(verseProtoBars.isEmpty()) { return(new MIDITune()); }

    	final List<TuneSectionMetadata> plan = new ArrayList<>();
    	plan.add(new TuneSectionMetadata(verseProtoBars.size(), TuneSection.verse));

        // Top and tail with intro/outro if specified, eg to be mix-friendly.
    	final boolean hasIntroOutro = (params.introBars() > 0);
        if(hasIntroOutro)
	        {
        	plan.add(0, new TuneSectionMetadata(params.introBars(), TuneSection.intro));
        	plan.add(new TuneSectionMetadata(params.introBars(), TuneSection.outro));
	        }

    	// Create tracks with extendable (within this method) bars.
    	final int streams = db.streams();
    	final MIDIDataMelodyTrack tracks[] = new MIDIDataMelodyTrack[streams];
    	Arrays.setAll(tracks,
			i -> new MIDIDataMelodyTrack(
					genMIDITrackSetupGeneric(i+1, params, db,
						((null==data)?null:("source: "+DataUtils.extractSourceName(data, i+1)))),
				new ArrayList<>()));

    	// At most one percussion track, not for "plain".
    	final MIDISupportTrack percTrack = (Style.plain == params.style()) ? null :
			new MIDISupportTrack(
    			new MIDITrackSetup((byte)(MIDIConstant.GM1_PERCUSSION_CHANNEL-1),
    					(byte) 0,
    					MIDIConstant.DEFAULT_VOLUME,
    					MIDIConstant.DEFAULT_PAN,
    					"percussion: gentle"),
    			new ArrayList<>());

    	if(null != percTrack)
    		{
    		// Get the fixed gentle percussion bar: one hand clap at the start.
    		final MIDIPlayableBar bar = SupportBarGen.makeBasicGentlePercussionBar();

        	// Fill all bars of all sections with same percussion bar...
        	for(final TuneSectionMetadata ts : plan)
	        	{
        		final int barCount = ts.bars();
        		percTrack.bars().addAll(Collections.nCopies(barCount, bar));
	        	}
    		}

    	// Parameterisation of melody play.
		final byte range = 12 * DEFAULT_RANGE_OCTAVES;
		final float multScaling = (db.maxVal() > 0) ? ((range-1)/db.maxVal()) : 1;

    	// Run through all the sections,
    	// inserting the full data melody in the 'verse' section.
    	for(final TuneSectionMetadata ts : plan)
	    	{
            if(ts.sectionType() != TuneSection.verse)
	            {
                // Skip over this section silently,
            	// inserting empty bars for all streams.
            	for(int s = 1; s <= streams; ++s)
	            	{
            		tracks[s - 1].bars().addAll(
        				Collections.nCopies(ts.bars(), MIDIPlayableMonophonicDataBar.EMPTY_1_NOTE_BAR));
            		}
            	continue;
	            }

            // Verify that section size is correct.
            if(ts.bars() != verseProtoBars.size())
        		{ throw new IllegalArgumentException(); }

            for(final DataProtoBar dbp : verseProtoBars)
            	{
            	for(int s = 1; s <= streams; ++s)
            		{
            		final boolean isNotSecondaryDataStream = params.hetero() || db.isMainDataStream(s);

            		final List<List<String>> rows = dbp.dataRows().data(); // Notes in bar.
            		final int dnpb = dbp.dataNotesPerBar();
            		final List<NoteAndVelocity> notes = new ArrayList<>(dnpb);
            		for(final List<String> row : rows)
            			{
            			final Datum d = Datum.extractDatum(s, row);
            			final NoteAndVelocity n = datumToNoteAndVelocityNoScale(
            					d,
								isNotSecondaryDataStream,
								multScaling);
            			notes.add(n);
            			}

            		// Construct MIDI-playable bar for this stream.
            		final MIDIPlayableMonophonicDataBar mpmb = new MIDIPlayableMonophonicDataBar(
            				dnpb, dbp, s, Collections.unmodifiableList(notes));
            		tracks[s - 1].bars().add(mpmb);
            		}
            	}
	    	}

    	// Return unmodifiable compact version.
    	for(int i = tracks.length; --i >= 0; )
	    	{
	    	tracks[i] = new MIDIDataMelodyTrack(tracks[i].setup(),
    			Collections.unmodifiableList(new ArrayList<>(tracks[i].bars())));
	    	}
    	final List<MIDISupportTrack> support = (null == percTrack) ? Collections.emptyList() :
			Collections.singletonList(new MIDISupportTrack(percTrack.setup(),
				Collections.unmodifiableList(percTrack.bars())));
    	return(new MIDITune(Arrays.asList(tracks), support, new TuneSectionPlan(plan)));
	    }

	/**Generate MIDITrackSetup for a given stream (1-based); never null.
     * This knows about instrument choices, relative volumes, etc.
     * <p>
     * TODO: incorporate seed-based randomness when appropriate
     * <p>
     * TODO: unit tests
     *
     * @param stream  stream number (first stream is 1);
     *     out of bounds value indicates not a (primary) data stream
     * @return  track setup, with channel number 1 less than stream number
     */
    public static MIDITrackSetup genMIDITrackSetupGeneric(
    		final int stream,
    		final GenerationParameters params,
    		final DataBounds db,
    		final String name)
	    {
    	Objects.requireNonNull(params);
    	Objects.requireNonNull(db);

    	// True if a major/main stream, else a minor/secondary stream.
    	final boolean isMajorStream = params.hetero() || db.isMainDataStream(stream);

		// None/gentle: tenor sax for main data stream, ocarina for remainder.  (Alternative: ocarina / synth brass 1.)
		// House: saw lead for main data stream, synth brass for remainder.
    	final MIDIInstrument instrument = isMajorStream ?
			((Style.house == params.style()) ? MIDIInstrument.LEAD_2_SAWTOOTH_WAVE : MIDIInstrument.TENOR_SAX) :
			((Style.house == params.style()) ? MIDIInstrument.SYNTH_BRASS_1 : MIDIInstrument.OCARINA);

		// Use default volume for main data stream, with the rest quieter.
    	// If the style is Danceable then turn down all the data melody volume!
		final byte rawVolume = isMajorStream ? MIDIConstant.DEFAULT_VOLUME : (byte) ((2*MIDIConstant.DEFAULT_VOLUME)/3);
		final byte volume = (ProductionLevel.Danceable == params.style().level) ?
				((byte) ((2*rawVolume) / 3)) : rawVolume;

		final byte pan;
		if(params.hetero())
			{
			// Spread heterogeneous-and-equal streams around the L-R axis.
			pan = (byte) ((127 / Math.max(1, db.streams() - 1)) * (stream - 1));
			}
		else
			{
			if(db.isMainDataStream(stream))
				{
				// Put main stream slightly to one side.
				pan = 80; // In V4.x was 70;
				}
			else
				{
				// Spread non-major streams out on other side.
				pan = (byte) ((63 / Math.max(1, db.streams() - 1)) * (stream - 1));
				}
			}

    	return(new MIDITrackSetup((byte) Math.max(0, stream - 1), instrument.instrument0, volume, pan, name));
	    }

	/**Do initial splitting of data into whole proto melody bars for the given section type, including any alignment; never null.
     * The verse output is the one most reflective of the input data,
     * and should be the only one used for plain style for example.
     * <p>
     * May only support a few section types, including verse.
     *
     * @param section  which song section type this is for; never null
	 * @param params  generation parameters; never null
	 * @param data  the entire ingested data set; never null
     */
    public static List<DataProtoBar> splitAndAlignData(
    		final TuneSection section,
    		final GenerationParameters params,
    		final EOUDataCSV data)
	    {
    	Objects.requireNonNull(section);
    	switch(section) {
    	    case verse: break;
	    	default: throw new IllegalArgumentException("unsupported section type");
	    	}
    	Objects.requireNonNull(params);
    	Objects.requireNonNull(data);

    	final ProgressionGroup prog = new ProgressionGroup(params, "split");
    	final RandomGenerator prng = prog.getPRNG(data.data().size());

    	// Data notes per bar is determined by the cadence.
    	final DataCadence cadence = DataUtils.extractDataCadenceQuick(data);
    	final int dataNotesPerBar = cadence.defaultPerBar;
    	// We may choose not to align in the most produced music for some seeds.
    	final boolean canAlign = cadence.canAlign();
		final boolean doAlign = canAlign &&
			(
			(params.style().level == ProductionLevel.Gentle) ||
			((params.style().level == ProductionLevel.Danceable) &&
					(params.randomnessNone() || prng.nextBoolean()))
			);
		// Be prepared to omit partial starting and ending bars,
		// for Danceable tunes
		// unless there is very little data left!
		final boolean maybeOmitPartialStartEndBars = // doAlign &&
			((params.style().level == ProductionLevel.Danceable) &&
					(params.randomnessNone() || prng.nextBoolean()));

	    final int size = data.data().size();
	    final ArrayList<DataProtoBar> result = new ArrayList<>(2 + (size/dataNotesPerBar));

		// Do alignment where appropriate.
	    if(doAlign)
		    {
	    	// Do alignment.
	    	// Assumes all input data is well-formed, well-ordered and dense (no gaps).
	    	// Any item with least-significant date (lsd) section N should be on beat N
	    	// (treating both schemes as 1-based).
	    	//     * If a datum with lsd less than the current beat is encountered
	    	//       then insert empty notes to pad the current bar to the end.
	    	//       (Nominally the lsd should be 1 (or "01") if no gaps,
	    	//       but we could drop through to the next rule to cope with some gaps.)
	    	//     * If a datum with lsd greater than the current beat is encountered
	    	//       then insert empty notes to get to beat N.
		    final List<List<String>> bar = new ArrayList<>(dataNotesPerBar);
	    	for(int i = 0; i < size; ++i)
		    	{
	    		final int currentBeatNumber = bar.size() + 1;

	    		final List<String> row = data.data().get(i);
	    		// TODO: optimisation: avoid parsing the data for each stream.
	    		final String date = row.get(0);
	    		final int lastDash = date.lastIndexOf('-');
	    		if(lastDash < 0) { throw new DateTimeException("malformed date (missing '-'): " + date); }
	    		final String lsdRaw = date.substring(lastDash + 1);
	    		final int lsd = Integer.parseInt(lsdRaw, 10);
	    		if(lsd <= 0) { throw new DateTimeException("malformed date (lsd <= 0): " + date); }
	    		if(lsd > dataNotesPerBar) { throw new DateTimeException("malformed date (lsd too high): " + date); }

                if(lsd < currentBeatNumber)
	                {
                	if(1 != lsd) { throw new DateTimeException("malformed date or missing datum: " + date + "; lsd="+lsd+", currentBeatNumber="+currentBeatNumber); }
                    // Pad bar to end, push it out...
        		    while(bar.size() < dataNotesPerBar) { bar.add(null); }
        		    result.add(new DataProtoBar(dataNotesPerBar, new EOUDataCSV(Collections.unmodifiableList(new ArrayList<>(bar)))));
                	// ... and be ready to start new bar with this note.
        		    bar.clear();
	                }
                else if(lsd > currentBeatNumber)
	                {
	                // Insert empty notes to get to the right place.
                	// Should only happen on a partial first bar if data is dense/complete.
        		    while(bar.size()+1 < lsd) { bar.add(null); }
	                }

                // Add this note.
    		    bar.add(row);

    		    assert(bar.size() <= dataNotesPerBar);
    		    if(dataNotesPerBar == bar.size())
	    		    {
        		    result.add(new DataProtoBar(dataNotesPerBar, new EOUDataCSV(Collections.unmodifiableList(new ArrayList<>(bar)))));
                	// Start new bar with this note.
        		    bar.clear();
	    		    }
		    	}

		    // Pad the final possibly-partial bar if necessary.
		    while(bar.size() < dataNotesPerBar) { bar.add(null); }
		    result.add(new DataProtoBar(dataNotesPerBar, new EOUDataCSV(Collections.unmodifiableList(new ArrayList<>(bar)))));
		    }
	    else
		    {
	    	// No alignment
			for(int i = 0; i < size; i += dataNotesPerBar)
			    {
			    final List<List<String>> bar = new ArrayList<>(dataNotesPerBar);
			    // FIXME: wrap leaf List if not already Unmodifiable.
			    for(int j = i; (j - i < dataNotesPerBar) && (j < size); ++j)
				    { bar.add(data.data().get(j)); }
			    // Pad the final possibly-partial bar if necessary.
			    while(bar.size() < dataNotesPerBar) { bar.add(null); }
			    result.add(new DataProtoBar(dataNotesPerBar, new EOUDataCSV(Collections.unmodifiableList(bar))));
			    }
		    }

	    // Be prepared to discard partial start/end bars
	    // for Danceable tunes
	    // if there is plenty of remaining data.
	    // Npte that some bars will always be missing notes,
	    // eg daily cadence 32 per bar.
	    if(maybeOmitPartialStartEndBars && (result.size() > DEFAULT_MIN_SECTION_BARS+1))
		    {
	    	final int maxMissing = dataNotesPerBar/4; // Alt: vary
            final DataProtoBar last = result.get(result.size()-1);
            final long lastNulls = last.dataRows().data().stream().filter(Objects::isNull).count();
            if(lastNulls > maxMissing) { result.remove(result.size()-1); }
            final DataProtoBar first = result.get(0);
            final long firstNulls = first.dataRows().data().stream().filter(Objects::isNull).count();
            if(firstNulls > maxMissing) { result.remove(0); }
		    }

	    assert(data.data().isEmpty() == result.isEmpty());
		result.trimToSize();
		return(Collections.unmodifiableList(result));
	    }


    /**Validates the supplied MIDITune across various features.
     * Throws exception (mainly InvalidArgumentException)
     * in case of badness.
     * <p>
     * Checks that:
     * </p>
     * <ul>
     * <li>The tune is not null.</li>
     * <li>No data melody track is using the reserved (10) percussion channel.</li>
     * <li>No two tracks are using the same channel (at least in their setup).</li>
     * <li>All tracks have the same length in bars (except possibly a stub tempo track).</li>
     * </ul>
     * <p>
     * TODO: other checks, eg:
     * <ul>
     * <li>Bar lengths in clock ticks match across tracks.</li>
     * </ul>
     * <p>
     * TODO: unit tests
     *
     * @param tune  for validation; never null
     *
     */
    public static void validateMIDITune(final MIDITune tune)
		{
    	Objects.requireNonNull(tune);

    	// Check that no data melody is using the percussion channel.
    	for(final MIDIDataMelodyTrack t : tune.dataMelody())
	    	{
	    	if(MIDIConstant.GM1_PERCUSSION_CHANNEL-1 == t.setup().channel())
	    		{ throw new IllegalArgumentException("data melody track using percussion channel"); }
	    	}

    	// Check that no channel (0-based) is being used from more than one track.
    	final BitSet channelsInUse = new BitSet();
    	for(final MIDIDataMelodyTrack t : tune.dataMelody())
	    	{
    		final int c = t.setup().channel();
            if(channelsInUse.get(c))
    			{ throw new IllegalArgumentException("channel in use more than once (melody): "+c); }
            channelsInUse.set(c);
	    	}
    	for(final MIDISupportTrack t : tune.supportTracks())
	    	{
			final int c = t.setup().channel();
	        if(channelsInUse.get(c))
				{ throw new IllegalArgumentException("channel in use more than once (support): "+c); }
	        channelsInUse.set(c);
	    	}

    	// Check tracks for same number of bars.
    	// Putative length of all tracks.
    	final int lengthBars =
			(!tune.dataMelody().isEmpty()) ? tune.dataMelody().get(0).bars().size() :
				((!tune.supportTracks().isEmpty()) ? tune.supportTracks().get(0).bars().size() :
					0);
    	for(final MIDIDataMelodyTrack t : tune.dataMelody())
	    	{
	    	if(t.bars().size() != lengthBars)
				{ throw new IllegalArgumentException("tracks not all equal length in bars (melody)"); }
	    	}
    	for(final MIDISupportTrack t : tune.supportTracks())
    		{
	    	if(t.bars().size() != lengthBars)
				{ throw new IllegalArgumentException("tracks not all equal length in bars (support)"); }
	    	}

    	// TODO: more checks

		}

    /**Set up a fresh Track.
     *
     * @param trackMelody  never null
     * @param ts  track setup parameters; never null
     * @throws InvalidMidiDataException
     */
	private static void _setupMIDITrack(final Track trackMelody, final MIDITrackSetup ts)
		throws InvalidMidiDataException
	    {
		Objects.requireNonNull(trackMelody);
		Objects.requireNonNull(ts);

		final byte channel = ts.channel();
		final byte instrument = ts.instrument();

		// Set the track name, if available.
		if((null != ts.name()) && !ts.name().isBlank())
			{
			final byte[] text = ts.name().getBytes(StandardCharsets.US_ASCII);
            final MetaMessage mm = new MetaMessage(MIDIConstant.METAMESSAGE_TITLE, text, text.length);
            trackMelody.add(new MidiEvent(mm, 0));
			}
		// Set the track comment, if available.
		if((null != ts.comment()) && !ts.name().isBlank())
			{
			final byte[] text = ("comment: " + ts.comment()).getBytes(StandardCharsets.US_ASCII);
            final MetaMessage mm = new MetaMessage(MIDIConstant.METAMESSAGE_TEXT, text, text.length);
            trackMelody.add(new MidiEvent(mm, 0));
			}

		// Program change (setting the instrument).
		// Do not do this on the fixed percussion channel.
		if(MIDIConstant.GM1_PERCUSSION_CHANNEL-1 != channel)
			{
			final ShortMessage pc = new ShortMessage();
			pc.setMessage(ShortMessage.PROGRAM_CHANGE, channel, instrument, 0);
			trackMelody.add(new MidiEvent(pc, 0));
			}
		// Volume setting (CC 7); do not assume a consistent synthesiser default.
		// Also set expression to something sensible.
		final ShortMessage vol = new ShortMessage();
		vol.setMessage(ShortMessage.CONTROL_CHANGE, channel, 7, ts.volume());
		trackMelody.add(new MidiEvent(vol, 0));
		final ShortMessage exp = new ShortMessage();
		exp.setMessage(ShortMessage.CONTROL_CHANGE, channel, 11, MIDIConstant.DEFAULT_EXPRESSION);
		trackMelody.add(new MidiEvent(exp, 0));
		// Pan (if not default).
		if(MIDIConstant.DEFAULT_PAN != ts.pan())
			{
			final ShortMessage pan = new ShortMessage();
			pan.setMessage(ShortMessage.CONTROL_CHANGE, channel, 10, ts.pan());
			trackMelody.add(new MidiEvent(pan, 0));
			}
	    }

    /**Generate a MIDI Sequence from a MIDITune; never null.
     * @param tune  abstract tune with zero or more tracks and an optional plan; never null
     * @param params  generation params; may be null
     * @param db  data bounds; may be null
     * @return internal MIDI representation of the tune
     * @throws InvalidMidiDataException
     */
    public static Sequence genFromTuneSequence(
    		final MIDITune tune,
    		final GenerationParameters params,
    		final DataBounds db)
		throws InvalidMidiDataException
	    {
    	// Validate, including that argument is non-null.
    	validateMIDITune(tune);

		final int barClocks = DEFAULT_CLKSPQTR * DEFAULT_BEATS_PER_BAR;
		final Sequence sequence = new Sequence(Sequence.PPQ, DEFAULT_CLKSPQTR);

		// Tempo (first) track
		final Track tempoTrack = sequence.createTrack();
		// TODO: set tempo!
		// Set a copyright disclaimer.
		final byte[] copyright = "autogenerated output released as CC0 / public domain.".getBytes(StandardCharsets.US_ASCII);
        final MetaMessage copyrightMM = new MetaMessage(MIDIConstant.METAMESSAGE_COPYRIGHT, copyright, copyright.length);
		// Set the track name, if available.
		if((null != params) && (null != params.name()) && !params.name().isBlank())
			{
			final byte[] title = params.name().getBytes(StandardCharsets.US_ASCII);
            final MetaMessage titleMM = new MetaMessage(MIDIConstant.METAMESSAGE_TITLE, title, title.length);
            tempoTrack.add(new MidiEvent(titleMM, 0));
			}
        tempoTrack.add(new MidiEvent(copyrightMM, 0));
        // Note the date and time of generation.
        final byte[] datetime = ("generated: " + (new Date()).toString()).getBytes(StandardCharsets.US_ASCII);
        final MetaMessage textDT = new MetaMessage(MIDIConstant.METAMESSAGE_TEXT, datetime, datetime.length);
        tempoTrack.add(new MidiEvent(textDT, 0));
		if(null != params)
			{
			final byte[] text = ("params: " + params).getBytes(StandardCharsets.US_ASCII);
	        final MetaMessage textMM = new MetaMessage(MIDIConstant.METAMESSAGE_TEXT, text, text.length);
	        tempoTrack.add(new MidiEvent(textMM, 0));
			}
		if(null != db)
			{
			final byte[] text = ("date range: " + db.firstDate() +"/"+ db.lastDate()).getBytes(StandardCharsets.US_ASCII);
	        final MetaMessage textMM = new MetaMessage(MIDIConstant.METAMESSAGE_TEXT, text, text.length);
	        tempoTrack.add(new MidiEvent(textMM, 0));
			}
		// Indicate this program version, if available.
    	final String version = Main.getManifestVersion();
        if(null != version)
	        {
			final byte[] text = ("statsHouse version: " + version).getBytes(StandardCharsets.US_ASCII);
	        final MetaMessage textMM = new MetaMessage(MIDIConstant.METAMESSAGE_TEXT, text, text.length);
	        tempoTrack.add(new MidiEvent(textMM, 0));
	        }
		// TODO: markers
		// TODO: other tempo track!

		// Generate from support tracks, eg including percussion.
    	for(final MIDISupportTrack t : tune.supportTracks())
	    	{
			final Track track = sequence.createTrack();
			final MIDITrackSetup ts = t.setup();
			final byte channel = ts.channel();
			_setupMIDITrack(track, ts);

			// All data melody tracks start at the default expression level.
			byte expression = MIDIConstant.DEFAULT_EXPRESSION;

			// Generate each bar for this track.
			// Note that the javax implementation inserts events in correct order,
			// which means that this code can insert the on and off events easily,
			// even when notes of differing lengths overlap or nest, etc.
			// TODO: disallow notes extending beyond bar end, usually?
			int clock = 0;
			for(final MIDIPlayableBar b : t.bars())
				{
				final int startOfBarClock = clock;

				byte targetExpression = b.expressionStart();
				// Change in expression per clock tick.
				final boolean flatExpression = (b.expressionEnd() == b.expressionStart());
				final float expressionDeltaPerClock = flatExpression ? 0f :
						((b.expressionEnd() - b.expressionStart()) / (float) barClocks);

				// Play each note in this bar.
				for(final MIDIPlayableBar.StartNoteVelocityDuration n : b.notes())
					{
					final int start = startOfBarClock + n.start();
					final int end = start + Math.max(0, n.duration() - 1);

					// Adjust expression level just before each played note as needed.
					if(!flatExpression)
						{
						targetExpression = (byte) Math.max(0, Math.min(127, Math.round(
							b.expressionStart() + (n.start() * expressionDeltaPerClock))));
						}
					if(expression != targetExpression)
						{
						expression = targetExpression;
						final ShortMessage exp = new ShortMessage();
						exp.setMessage(ShortMessage.CONTROL_CHANGE, channel, 11, expression);
						track.add(new MidiEvent(exp, start));
						}

					// Add a note-on event to the track.
				    final ShortMessage noteOn = new ShortMessage();
				    noteOn.setMessage(ShortMessage.NOTE_ON, channel, n.note().note(), n.note().velocity());
				    final MidiEvent noteOnEvent = new MidiEvent(noteOn, start);
				    track.add(noteOnEvent);
				    // Add a note-off event to the track.
				    final ShortMessage noteOff = new ShortMessage();
				    noteOff.setMessage(ShortMessage.NOTE_OFF, channel, n.note().note(), 0);
				    final MidiEvent noteOffEvent = new MidiEvent(noteOff, end);
				    track.add(noteOffEvent);
					}
				clock += barClocks; // Ensure correct clocks per bar.
				}
	    	}

		// Generate from data melody tracks.
		for(final MIDIDataMelodyTrack mt : tune.dataMelody())
			{
			final Track trackMelody = sequence.createTrack();
			final MIDITrackSetup ts = mt.setup();
			final byte channel = ts.channel();
			_setupMIDITrack(trackMelody, ts);

			// All data melody tracks start at the default expression level.
			byte expression = MIDIConstant.DEFAULT_EXPRESSION;

			int clock = 0;
			for(final MIDIPlayableMonophonicDataBar b : mt.bars())
				{
				final int noteCount = b.notes().size();
				final int clocksPerNote = barClocks / noteCount;
				int subClock = clock;

				byte targetExpression = b.expressionStart();
				// Change in expression per note.
				final int expressionDelta = (b.expressionEnd() - b.expressionStart()) / noteCount;
				// FIXME: better handle case where noteCount > change in expression.

				for(final NoteAndVelocity nv : b.notes())
				    {
					// Rest for null/missing/silent note.
					if((null != nv) && (0 != nv.velocity()))
						{
						// Adjust expression level just before each played note as needed.
						if(expression != targetExpression)
							{
							expression = targetExpression;
							final ShortMessage exp = new ShortMessage();
							exp.setMessage(ShortMessage.CONTROL_CHANGE, channel, 11, expression);
							trackMelody.add(new MidiEvent(exp, subClock));
							}

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

					targetExpression += expressionDelta;
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
		final byte range = 12 * DEFAULT_RANGE_OCTAVES;
		final byte offset = DEFAULT_ROOT_NOTE;
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
