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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.hd.d.statsHouse.data.DataVizBeatPoint;
import org.hd.d.statsHouse.generic.TuneSectionPlan;

/**A representation of a full MIDI 'tune' created from data.
 * Is immutable if the bar List items are.
 * <p>
 * The section plan may in part be folded into markers etc in a MIDI tempo track.
 *
 * @param dataMelody  the data melody parts of the final tune;
 *     non-null, and no null entries, but may be empty
 * @param supportTracks  the non-data tracks of the final tune;
 *     non-null, and no null entries but may be empty
 * @param plan  the section plan which should cover the whole melody at least if present;
 *     may be null
 * @param dataLabels  column labels for dataRendered (not containing nulls or commas);
 *     may be null
 * @param dataRendered  the set of key data as rendered in the tune;
 *     may be null
 */
public record MIDITune(List<MIDIDataMelodyTrack> dataMelody,
		      List<MIDISupportTrack> supportTracks,
		      TuneSectionPlan plan,
		      DataVizBeatPoint dataRendered)
    {
    public MIDITune
	    {
	    Objects.requireNonNull(dataMelody);
	    if(dataMelody.stream().anyMatch(t -> t == null)) { throw new IllegalArgumentException(); }
	    dataMelody = List.copyOf(dataMelody); // Defensive copy.
	    Objects.requireNonNull(supportTracks);
	    if(supportTracks.stream().anyMatch(t -> t == null)) { throw new IllegalArgumentException(); }
	    supportTracks = List.copyOf(supportTracks); // Defensive copy.
	    }

    /**Data melody and support track and plan, no rendered data. */
    public MIDITune(
		final List<MIDIDataMelodyTrack> dataMelody,
		final List<MIDISupportTrack> supportTracks,
		final TuneSectionPlan plan)
        { this(dataMelody, supportTracks, plan, null); }

    /**Data melody and support track, no plan nor rendered data. */
    public MIDITune(
		final List<MIDIDataMelodyTrack> dataMelody,
		final List<MIDISupportTrack> supportTracks)
        { this(dataMelody, supportTracks, null); }

    /**Bare data melody, no support nor plan nor rendered data. */
    public MIDITune(
		final List<MIDIDataMelodyTrack> dataMelody)
        { this(dataMelody, Collections.emptyList()); }

    /**Empty tune. */
    public MIDITune() { this(Collections.emptyList()); }
    }
