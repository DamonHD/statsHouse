package org.hd.d.statsHouse.generic;

import java.util.List;
import java.util.Objects;

/**Plan of song sections.
 * Is immutable if sections is.
 *
 * @param sections  in-order list of sections; non-null and non-empty
 *
 */
public record TuneSectionPlan(List <TuneSectionMetadata> sections)
	{
	public TuneSectionPlan
		{
		Objects.requireNonNull(sections);
		if(sections.isEmpty()) { throw new IllegalArgumentException(); }
		}
	}
