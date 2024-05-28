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

package org.hd.d.statsHouse.feedHits.data;

import java.util.List;
import java.util.Objects;

/**Ordered List of FeedStatusBlocks, immutable.
 * This makes a defensive immutable copy of the list to ensure record immutability.
 */
public record FeedStatusBlocks(List<FeedStatusBlock> blocks)
    {
	public FeedStatusBlocks
	    {
		Objects.nonNull(blocks);
		blocks = List.copyOf(blocks); // Defensive copy to enforce immutability.
	    }
	}
