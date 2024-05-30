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
import java.util.List;
import java.util.Objects;

import org.hd.d.statsHouse.feedHits.data.FeedStatusBlocks;
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

	/**Summary type 1; of by-hour data. */
	public static MIDITune summary1(final List<String> dirnames) throws IOException
		{
		final FeedStatusBlocks fsbs = FeedStatusBlocks.loadStatusByHourFromDirs(dirnames);

        // Total number of distinct hours to sonify; 24 summary hours for each block.
		final int nTotalHours = fsbs.blocks().size() * 24;
		// Hours' data for each bar (must be a factor of 24).
		final int nHoursPerBar = 4;
		// Total number of data bars to generate.
		final int nDataBars = nTotalHours / nHoursPerBar;


        throw new RuntimeException("NOT IMPLEMENTED");
		}
    }
