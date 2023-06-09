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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/**MIDICSV utilities.
 * Generation of CSV output that the MIDICSV utility can convert to MIDI binary.
 */
public final class MIDICSVUtils
    {
    /**Prevent creation of an instance. */
    private MIDICSVUtils() { }

    
    /**Charset for MIDICSV CSV format (ASCII 7-bit). */
    public static final Charset MIDICSVCSV_CHARSET = StandardCharsets.US_ASCII;

    }
