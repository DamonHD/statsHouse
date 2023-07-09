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
import java.util.List;
import java.util.Objects;

/**A single data stream melody track that is playable as MIDI.
 * Is immutable if 'bars' is.
 * <p>
 * Melody bars are assumed to be a constant duration (eg ticks)
 * throughout a track and across melody (and other) tracks
 * in one piece.
 *
 * @param setup  setup for the whole track; never null
 * @param bars   zero or more bars of melody; never null
 */
public record MIDIDataMelodyTrack(MIDITrackSetup setup, List<MIDIPlayableMonophonicDataBar> bars)
	{
	public MIDIDataMelodyTrack
	    {
	    Objects.requireNonNull(setup);
	    Objects.requireNonNull(bars);
	    }
	}
