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

/**Parent interface of MIDI music bars covering expression (CC 11) handling. */
public sealed interface MIDIBarExpression permits MIDIPlayableBar, MIDIPlayableMonophonicDataBar
	{
	/**Get expression level (CC 11) at the start of the bar [0,127]. */
    byte expressionStart();
	/**Get expression level (CC 11) at the end of the bar [0,127]. */
    byte expressionEnd();

    /**Make an immutable copy/clone with specified expression levels.
     * @param newExpressionStart  expression level (CC 11) at the start of the bar [0,127]
     * @param newExpressionEnd  expression level (CC 11) at the end of the bar [0,127]
	 * @return immutable clone of original with the specified change
	 */
//    <T extends MIDIBarExpression> T cloneAndSetExpression(
    MIDIBarExpression cloneAndSetExpression(
    		final byte newExpressionStart, final byte newExpressionEnd);
	}
