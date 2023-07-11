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

/**Simple MIDI tack setup, in particular including the default channel and instrument.
 * For data melody the 1-based channel number will be the 1-based data stream number.
 * (This the actual raw channel number in messages will be one less.)
 *
 * @param name  track/source name; may be null
 * @param comment  additional comment if any; may be null
 */
public record MIDITrackSetup(byte channel, byte instrument, byte volume, byte pan, String name, String comment)
	{
    public MIDITrackSetup
	    {
	    if(channel < 0) { throw new IllegalArgumentException(); }
	    if(channel > 15) { throw new IllegalArgumentException(); }
	    if(instrument < 0) { throw new IllegalArgumentException(); }
	    if(volume < 0) { throw new IllegalArgumentException(); }
	    if(pan < 0) { throw new IllegalArgumentException(); }
	    }

    /**The comment is null. */
    public MIDITrackSetup(final byte channel, final byte instrument, final byte volume, final byte pan, final String name)
    	{ this(channel, instrument, volume, pan, name, null); }
    /**The name and comment are null. */
    public MIDITrackSetup(final byte channel, final byte instrument, final byte volume, final byte pan)
    	{ this(channel, instrument, volume, pan, null, null); }
    /**The pan is set to the default centre position. */
    public MIDITrackSetup(final byte channel, final byte instrument, final byte volume)
    	{ this(channel, instrument, volume, MIDIConstant.DEFAULT_PAN); }
    /**The volume is set to its default and pan is set to the default centre position. */
    public MIDITrackSetup(final byte channel, final byte instrument)
		{ this(channel, instrument, MIDIConstant.DEFAULT_VOLUME); }
	}
