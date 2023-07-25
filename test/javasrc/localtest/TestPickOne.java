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

/**Test possibly-weighted selection of entries from a nominal non-empty array.
 */
public final class TestPickOne extends TestCase
    {
    /**Test expected cadence alignment. */
    public static void testALWAYS_ZERO()
	    {
	    assertEquals(0, PickOne.ALWAYS_ZERO.pickOne(null, 1));
	    assertEquals(0, PickOne.ALWAYS_ZERO.pickOne(null, 2));
	    assertEquals(0, PickOne.ALWAYS_ZERO.pickOne(null, 999));
	    assertEquals(0, PickOne.ALWAYS_ZERO.pickOne(new Random(), (new Random()).nextInt(999)));
	    }
    }
