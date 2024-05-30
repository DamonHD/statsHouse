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

import org.hd.d.statsHouse.feedHits.data.FeedStatusBlock;
import org.hd.d.statsHouse.feedHits.data.FeedStatusBlocks;
import org.hd.d.statsHouse.generic.TuneSectionPlan;
import org.hd.d.statsHouse.midi.MIDIConstant;
import org.hd.d.statsHouse.midi.MIDIDataMelodyTrack;
import org.hd.d.statsHouse.midi.MIDISupportTrack;
import org.hd.d.statsHouse.midi.MIDITrackSetup;
import org.hd.d.statsHouse.midi.MIDITune;

/**Generate sonification from summary information across 1 or more data blocks.
 */
public final class GenerateSummary
    {
	/**Generate sonification from summary information across 1 or more data blocks.
	 * @param summaryType  type of summary to generate (small +ve int)
	 * @param dirnames  names of directories to extract data from
	 */
	public static MIDITune summary(final int summaryType, final List<String> dirnames)
	    throws IOException
		{
		Objects.requireNonNull(dirnames);
		if(dirnames.isEmpty()) { throw new IllegalArgumentException(); }

        return switch (summaryType) {
		case 1 -> (summary1(dirnames));
		default -> throw new IllegalArgumentException("unknown summary type " + summaryType);
		};
		}

	/**Summary type 1; by-hour data blocks. */
	public static MIDITune summary1(final List<String> dirnames) throws IOException
		{
		final FeedStatusBlocks fsbs = FeedStatusBlocks.loadStatusByHourFromDirs(dirnames);

        // Total number of distinct hours to sonify; 24 summary hours for each block.
		final int nTotalHours = fsbs.blocks().size() * 24;
		// Hours' data for each bar (must be a factor of 24).
		final int nHoursPerBar = 4;
		// Total number of data bars to generate.
		final int nDataBars = nTotalHours / nHoursPerBar;

		// Compute hits and bytes per hour, normalising by the days in each block.
		// Capture maximum normalised value of each also.
		final float[] normalisedHitsPerHour = new float[nTotalHours];
		final float[] normalisedBytesPerHour = new float[nTotalHours];
		float normalisedHitsPerHourMax = 0;
		float normalisedBytesPerHourMax = 0;
		int hourIndex = 0;
		for(final FeedStatusBlock fsb : fsbs.blocks())
			{
			final int nDays = fsb.nDays();
			final float nDaysF = nDays;
			// For the 24h in each block.
			for(int h = 0; h < 24; ++h)
				{
				final float nh = fsb.records().get(h).hits() / nDaysF;
				final float nb = fsb.records().get(h).bytes() / nDaysF;
				normalisedHitsPerHour[hourIndex] = nh;
				normalisedBytesPerHour[hourIndex] = nb;
				if(nh > normalisedHitsPerHourMax) { normalisedHitsPerHourMax = nh; }
				if(nb > normalisedBytesPerHourMax) { normalisedBytesPerHourMax = nb; }
				++hourIndex;
				}
			}


		// Setup for the bytes-per-hour track.
		final MIDITrackSetup trSetupBytes = new MIDITrackSetup(
			MIDIConstant.GM1_PERCUSSION_CHANNEL0,
			(byte) 0, // MIDIPercusssionInstrument.ACOUSTIC_BASE_DRUM.instrument0,
			MIDIConstant.DEFAULT_VOLUME,
			(byte)(MIDIConstant.DEFAULT_PAN - 10),
			"bytes/h");

		// Setup for the hits-per-hour track.
		final MIDITrackSetup trSetupHits = new MIDITrackSetup(
			MIDIConstant.GM1_PERCUSSION_CHANNEL0,
			(byte) 0, // MIDIPercusssionInstrument.ACOUSTIC_BASE_DRUM.instrument0,
			MIDIConstant.DEFAULT_VOLUME,
			(byte)(MIDIConstant.DEFAULT_PAN + 10),
			"hits/h");

		final List<MIDIDataMelodyTrack> dataMelody = new ArrayList<>();


		// TODO


		// No support track or section plan yet.
		final List<MIDISupportTrack> supportTracks = Collections.emptyList();
		final TuneSectionPlan tsp = null;
		return(new MIDITune(dataMelody, supportTracks, tsp));
		}
    }
