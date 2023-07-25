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

import java.util.Random;

import org.hd.d.statsHouse.generic.PickOne;

import junit.framework.TestCase;

/**Test possibly-weighted selection of choice from a nominal non-empty array.
 */
public final class TestPickOne extends TestCase
    {
    /**Test ALWAYS_ZERO does always return zero. */
    public static void testZERO()
	    {
	    assertEquals(0, PickOne.ZERO.pickOne(null, 1));
	    assertEquals(0, PickOne.ZERO.pickOne(null, 2));
	    assertEquals(0, PickOne.ZERO.pickOne(null, 999));
	    assertEquals(0, PickOne.ZERO.pickOne(new Random(), (new Random()).nextInt(999)));
	    }

    /**Typical max choice array size. */
    private static final int TYPICAL_MAX_CHOICE_SIZE = 6;

    /**Test UNIFORM. */
    public static void testUNIFORM()
	    {
    	final Random r1 = new Random();
    	final Random r2 = new Random();
	    assertEquals(0, PickOne.UNIFORM.pickOne(r1, 1));
	    boolean over0 = false;
	    boolean atMax = false;
	    for(int i = 100 * TYPICAL_MAX_CHOICE_SIZE; --i >= 0; )
		    {
	        final int bound = 1 + r2.nextInt(TYPICAL_MAX_CHOICE_SIZE);
	        final int choice = PickOne.UNIFORM.pickOne(r1, bound);
	        assertTrue(choice >= 0);
	        assertTrue(choice < bound);
	        if(choice > 0) { over0 = true; }
	        if(bound-1 == choice) { atMax = true; }
	        // TODO: test statistical distribution!
		    }
	    assertTrue("expect at least one choice > 0", over0);
	    assertTrue("expect at least one choice at max", atMax);
	    }

    /**Test SQUARE. */
    public static void testSQUARE()
	    {
    	final Random r1 = new Random();
    	final Random r2 = new Random();
	    assertEquals(0, PickOne.SQUARE.pickOne(r1, 1));
	    boolean over0 = false;
	    boolean atMax = false;
	    for(int i = 100 * TYPICAL_MAX_CHOICE_SIZE; --i >= 0; )
		    {
	        final int bound = 1 + r2.nextInt(TYPICAL_MAX_CHOICE_SIZE);
	        final int choice = PickOne.SQUARE.pickOne(r1, bound);
	        assertTrue(choice >= 0);
	        assertTrue(choice < bound);
	        if(choice > 0) { over0 = true; }
	        if(bound-1 == choice) { atMax = true; }
	        // TODO: test statistical distribution!
		    }
	    assertTrue("expect at least one choice > 0", over0);
	    assertTrue("expect at least one choice at max", atMax);
	    }
    }
