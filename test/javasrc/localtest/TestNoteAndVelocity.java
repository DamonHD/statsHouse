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

package localtest;

import org.hd.d.statsHouse.data.Datum;
import org.hd.d.statsHouse.generic.NoteAndVelocity;
import org.hd.d.statsHouse.generic.Scale;
import org.hd.d.statsHouse.midi.MIDIGen;

import junit.framework.TestCase;

/**Test MIDIGen.
 */
public final class TestNoteAndVelocity extends TestCase
    {
    /**Test generation of notes on Scale. */
    public static void testDatumToNoteAndVelocity()
	    {
    	final NoteAndVelocity result0 = MIDIGen.datumToNoteAndVelocity(
    			new Datum(null, 1f, -1f),
    			true, // isNotSecondaryDataStream,
    			Scale.CHROMATIC,
    			1,
    			0f);
    	assertNull("should not (yet) generate a note with negative value", result0);

    	final NoteAndVelocity result1 = MIDIGen.datumToNoteAndVelocity(
    			new Datum(null, 1f, 0f),
    			true, // isNotSecondaryDataStream,
    			Scale.CHROMATIC,
    			1,
    			0f);
    	assertNotNull("should generate a note", result1);
    	assertEquals("should generate a root note", MIDIGen.DEFAULT_ROOT_NOTE, result1.note());
    	assertTrue("should generate a non-slient note", result1.velocity() > 0);

    	final NoteAndVelocity result2 = MIDIGen.datumToNoteAndVelocity(
    			new Datum(null, 1f, 1f),
    			true, // isNotSecondaryDataStream,
    			Scale.CHROMATIC,
    			1,
    			1f);
    	assertNotNull("should generate a note", result2);
    	assertEquals("should generate a root+12 note", MIDIGen.DEFAULT_ROOT_NOTE+12, result2.note());
    	assertTrue("should generate a non-slient note", result2.velocity() > 0);

    	final NoteAndVelocity result3 = MIDIGen.datumToNoteAndVelocity(
    			new Datum(null, 1f, 1f),
    			true, // isNotSecondaryDataStream,
    			Scale.CHROMATIC,
    			2,
    			1f);
    	assertNotNull("should generate a note", result3);
    	assertEquals("should generate a root+24 note", MIDIGen.DEFAULT_ROOT_NOTE+24, result3.note());
    	assertTrue("should generate a non-slient note", result3.velocity() > 0);

    	final NoteAndVelocity result4 = MIDIGen.datumToNoteAndVelocity(
    			new Datum(null, 1f, 1f),
    			true, // isNotSecondaryDataStream,
    			Scale.MAJOR,
    			2,
    			1f);
    	assertNotNull("should generate a note", result4);
    	assertEquals("should generate a root+24 note", MIDIGen.DEFAULT_ROOT_NOTE+24, result4.note());
    	assertTrue("should generate a non-slient note", result4.velocity() > 0);

    	final NoteAndVelocity result5 = MIDIGen.datumToNoteAndVelocity(
    			new Datum(null, 1f, 1f),
    			true, // isNotSecondaryDataStream,
    			Scale.CHROMATIC,
    			2,
    			4f);
    	assertNotNull("should generate a note", result5);
    	assertEquals("should generate a root+6 note", MIDIGen.DEFAULT_ROOT_NOTE+6, result5.note());
    	assertTrue("should generate a non-slient note", result5.velocity() > 0);

    	final NoteAndVelocity result6 = MIDIGen.datumToNoteAndVelocity(
    			new Datum(null, 1f, 1f),
    			true, // isNotSecondaryDataStream,
    			Scale.MAJOR,
    			2,
    			4f);
    	assertNotNull("should generate a note", result6);
    	assertEquals("should generate a root+7 note", MIDIGen.DEFAULT_ROOT_NOTE+7, result6.note());
    	assertTrue("should generate a non-slient note", result6.velocity() > 0);
	    }
    }
