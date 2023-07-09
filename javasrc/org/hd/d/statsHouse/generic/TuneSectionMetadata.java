package org.hd.d.statsHouse.generic;

import java.util.Objects;

/**Details of metadata for one section of a tune.
 * TODO: extend to tempo, volume, etc, changes, and transitions.
 *
 * @param sectionType  type of section; default is verse, eg for 'plain' style
 * @param bars  count of bars in this section; strictly positive, usually even
 */
public record TuneSectionMetadata(int bars, TuneSection sectionType)
	{
    public TuneSectionMetadata
	    {
	    if(bars <= 0) { throw new IllegalArgumentException(); }
	    Objects.requireNonNull(sectionType);
	    }

    /**Default (verse) section.
     * @param bars  count of bars in this section; strictly positive, usually even
     */
    public TuneSectionMetadata(final int bars) { this(bars, TuneSection.verse); }
	}
