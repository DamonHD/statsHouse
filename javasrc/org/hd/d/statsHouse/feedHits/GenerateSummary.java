/*
Copyright (c) 2024, Damon Hart-Davis

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

package org.hd.d.statsHouse.feedHits;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hd.d.statsHouse.data.DataVizBeatPoint;
import org.hd.d.statsHouse.feedHits.data.FeedStatus;
import org.hd.d.statsHouse.feedHits.data.FeedStatusBlock;
import org.hd.d.statsHouse.feedHits.data.FeedStatusBlocks;
import org.hd.d.statsHouse.generic.NoteAndVelocity;
import org.hd.d.statsHouse.generic.TuneSectionPlan;
import org.hd.d.statsHouse.midi.MIDIConstant;
import org.hd.d.statsHouse.midi.MIDIDataMelodyTrack;
import org.hd.d.statsHouse.midi.MIDIGen;
import org.hd.d.statsHouse.midi.MIDIPercusssionInstrument;
import org.hd.d.statsHouse.midi.MIDIPlayableBar;
import org.hd.d.statsHouse.midi.MIDISupportTrack;
import org.hd.d.statsHouse.midi.MIDITrackSetup;
import org.hd.d.statsHouse.midi.MIDITune;

/**Generate sonification from summary information across 1 or more data blocks.
 */
public final class GenerateSummary
    {
	/**Generate sonification from summary information across 1 or more data blocks.
	 * @param summaryType  type of summary to generate (small +ve int)
	 * @param dirnames  in-order names of directories to extract data from
	 */
	public static MIDITune summary(final int summaryType, final List<String> dirnames)
	    throws IOException
		{
		Objects.requireNonNull(dirnames);
		if(dirnames.isEmpty()) { throw new IllegalArgumentException(); }

        return switch (summaryType) {
		case 1 -> (summary1(dirnames));
		case 2 -> (summary2(dirnames));
		default -> throw new IllegalArgumentException("unknown summary type " + summaryType);
		};
		}

	/**Generate the percussion track for summary type 1 (and 2); never null.
	 * Also generates the beat labels, which must be empty.
	 *
	 * @param fsbs  in-order blocks of feed status data; never null
	 * @param dataRendered  rendered data to append to; never null
	 * @param beatLabels  beat labels to append to; never null
	 * @return
	 */
	private static MIDISupportTrack generatePercussionType1(final FeedStatusBlocks fsbs,
			final List<List<Float>> dataRendered,
			final List<String> dataLabels,
			final List<String> beatLabels)
		{
		Objects.requireNonNull(fsbs);
		Objects.requireNonNull(dataRendered);
		Objects.requireNonNull(dataLabels);
		Objects.requireNonNull(beatLabels);

		// For now assume that the render info is EMPTY.
		// May have to append sideways to it in future.
		if(!dataRendered.isEmpty()) { throw new RuntimeException("unexpected state"); }
		if(!dataLabels.isEmpty()) { throw new RuntimeException("unexpected state"); }
		if(!beatLabels.isEmpty()) { throw new RuntimeException("unexpected state"); }

		// Add in the columns that this routine will insert data for.
		dataLabels.addAll(List.of("bytes/h", "hits/h"));

        // Total number of distinct hours to sonify; 24 summary hours for each block.
		final int nTotalHours = fsbs.blocks().size() * 24;
		// Hours' data for each bar (must be a factor of 24).
		final int nHoursPerBar = 4;
		// Total number of data bars to generate.
		final int nDataBars = nTotalHours / nHoursPerBar;

		// Setup for the hits and bytes per-hour track.
		final MIDITrackSetup trSetupBytes = new MIDITrackSetup(
			MIDIConstant.GM1_PERCUSSION_CHANNEL0,
			(byte) 0, // MIDIPercusssionInstrument.ACOUSTIC_BASE_DRUM.instrument0,
			MIDIConstant.DEFAULT_VOLUME,
			(MIDIConstant.DEFAULT_PAN),
			"bytes&hits by hour");
		// Bytes and hits per-hour percussion track.
		final List<MIDIPlayableBar> pbBytes = new ArrayList<>(nDataBars);
        final MIDISupportTrack result = new MIDISupportTrack(trSetupBytes, pbBytes);


		// Compute hits and bytes per hour, normalising by the days in each block.
		// Capture maximum normalised value of each also.
		final float[] normalisedHitsPerHour = new float[nTotalHours];
		final float[] normalisedBytesPerHour = new float[nTotalHours];
		float normalisedHitsPerHourMax = 0;
		float normalisedBytesPerHourMax = 0;
		// Capture skipHours.
		final boolean[] isSkipHour = new boolean[nTotalHours];

		int hourIndex = 0;
		for(final FeedStatusBlock fsb : fsbs.blocks())
			{
			final int nDays = fsb.nDays();
			final float nDaysF = nDays;
			// For the 24h in each block.
			for(int h = 0; h < 24; ++h)
				{
				final FeedStatus feedStatus = fsb.records().get(h);
				final float nh = feedStatus.hits() / nDaysF;
				final float nb = feedStatus.bytes() / nDaysF;
				normalisedHitsPerHour[hourIndex] = nh;
				normalisedBytesPerHour[hourIndex] = nb;
				if(nh > normalisedHitsPerHourMax) { normalisedHitsPerHourMax = nh; }
				if(nb > normalisedBytesPerHourMax) { normalisedBytesPerHourMax = nb; }

				// Is a skipHour if non-zero "SH" hits
				// OR "SH" key absent and time >=22:00 and < 08:00.
                final Integer hitsHS = feedStatus.getColsMap().get("SH");
                if(null != hitsHS) { if(hitsHS > 0) { isSkipHour[hourIndex] = true; } }
                else
                	{
                	final int hourOfDay = hourIndex % 24;
                	if((hourOfDay < 8) || (hourOfDay >= 22)) { isSkipHour[hourIndex] = true; }
                	}

				++hourIndex;
				}
			}

		// Create bars from the normalised data.
		// Further normalise strength to maximum encountered.
		for(int h = 0; h < nTotalHours; h += nHoursPerBar)
			{
			final SortedSet<MIDIPlayableBar.StartNoteVelocityDuration> notes = new TreeSet<>();

			final byte DRUMB = MIDIPercusssionInstrument.HI_MID_TOM.instrument0;
			final byte vDRUM = MIDIGen.DEFAULT_MAX_MELODY_VELOCITY;

			for(int b = 0; b < nHoursPerBar; ++b)
				{
				final int hour = h+b;

				// Push hits tone even lower in skip hours.
				final byte DRUMH = (isSkipHour[h+b] ?
						MIDIPercusssionInstrument.LOW_TOM :
						MIDIPercusssionInstrument.LOW_MID_TOM)
						.instrument0;

				final int beatStart = b * MIDIGen.DEFAULT_CLKSPQTR;
				// On beat: bytes
				final float intB = normalisedBytesPerHour[hour] / normalisedBytesPerHourMax;
//System.out.println(intB);
				notes.add(new MIDIPlayableBar.StartNoteVelocityDuration(
						beatStart,
						new NoteAndVelocity(DRUMB, (byte) Math.round(vDRUM * intB)),
						MIDIGen.DEFAULT_CLKSPQTR/2-1));
				// Off beat: hits
				final float intH = normalisedHitsPerHour[hour] / normalisedHitsPerHourMax;
//System.out.println(intH);
				notes.add(new MIDIPlayableBar.StartNoteVelocityDuration(
						beatStart + MIDIGen.DEFAULT_CLKSPQTR/2,
						new NoteAndVelocity(DRUMH, (byte) Math.round(vDRUM * intH)),
						MIDIGen.DEFAULT_CLKSPQTR/2-1));

				// Capture for visualisation.
				final List<Float> d = List.of(
					normalisedBytesPerHour[hour],
					normalisedHitsPerHour[hour]
					);
				dataRendered.add(d);

				// Add hour-of-day label.
				String hh = Integer.toString(hour % 24);
				if(hh.length() < 2) { hh = "0" + hh; }
				beatLabels.add(hh);
				}

			final MIDIPlayableBar bar = new MIDIPlayableBar(Collections.unmodifiableSortedSet(notes));
			pbBytes.add(bar);
			}

        return(result);
		}

	/**Summary type 1; by-hour data blocks percussion.
	 * @param dirnames  in-order names of directories to extract data from; never null
	 * @return  a complete MIDI 'tune'; never null
	 * @throws IOException
	 */
	public static MIDITune summary1(final List<String> dirnames) throws IOException
		{
		final FeedStatusBlocks fsbs = FeedStatusBlocks.loadStatusByHourFromDirs(dirnames);

        // Total number of distinct hours to sonify; 24 summary hours for each block.
		final int nTotalHours = fsbs.blocks().size() * 24;
//		// Hours' data for each bar (must be a factor of 24).
//		final int nHoursPerBar = 4;
//		// Total number of data bars to generate.
//		final int nDataBars = nTotalHours / nHoursPerBar;

		// Data for the data visualisation.
        final List<List<Float>> dataRendered = new ArrayList<>(nTotalHours);
        final List<String> dataLabels = new ArrayList<>();
        final List<String> beatLabels = new ArrayList<>(nTotalHours);

        final MIDISupportTrack percussion =
    		generatePercussionType1(fsbs, dataRendered, dataLabels, beatLabels);

		// Set up the data visualisation.
        final DataVizBeatPoint dv = new DataVizBeatPoint(nTotalHours, dataLabels.size(), dataLabels, dataRendered, beatLabels);

		final List<MIDIDataMelodyTrack> dataMelody = Collections.emptyList();
		final TuneSectionPlan tsp = null;
		return(new MIDITune(dataMelody, List.of(percussion), tsp, dv));
		}


	/**Summary type 2; by-hour data blocks percussion and some trend melody.
	 * Uses the same (drums) percussion as summary type 1.
	 * <p>
	 * Use one melody track for 'good' responses, and another for errors,
	 * playing a separate (chord) note for each response type.
	 * Maybe pan good and bad to different sides?
	 * Velocity of each note proportional to hits per hour?
	 * May play quiet drone note (triplet?) panned hard off to the side
	 * to indicate current level relative to mean level across entire piece to give
	 * insight on now relative to the mean at all times.
	 * <p>
	 * 200s can be the chord root, 304 above it.
	 * <p>
	 * Worse things can be lower, eg from bad to worse:
	 * SH, 406, 429, 503?
	 *
	 * @param dirnames  in-order names of directories to extract data from; never null
	 * @return  a complete MIDI 'tune'; never null
	 * @throws IOException
	 */
	public static MIDITune summary2(final List<String> dirnames) throws IOException
		{
		final FeedStatusBlocks fsbs = FeedStatusBlocks.loadStatusByHourFromDirs(dirnames);

        // Total number of distinct hours to sonify; 24 summary hours for each block.
		final int nTotalHours = fsbs.blocks().size() * 24;
//		// Hours' data for each bar (must be a factor of 24).
//		final int nHoursPerBar = 4;
//		// Total number of data bars to generate.
//		final int nDataBars = nTotalHours / nHoursPerBar;

		// Data for the data visualisation.
        final List<List<Float>> dataRendered = new ArrayList<>(nTotalHours);
        final List<String> dataLabels = new ArrayList<>();
        final List<String> beatLabels = new ArrayList<>(nTotalHours);

        final MIDISupportTrack percussion =
    		generatePercussionType1(fsbs, dataRendered, dataLabels, beatLabels);

		// Set up the data visualisation.
        final DataVizBeatPoint dv = new DataVizBeatPoint(nTotalHours, dataLabels.size(), dataLabels, dataRendered, beatLabels);

		final List<MIDIDataMelodyTrack> dataMelody = Collections.emptyList();
		final TuneSectionPlan tsp = null;
		return(new MIDITune(dataMelody, List.of(percussion), tsp, dv));
		}
    }
