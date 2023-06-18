package org.hd.d.statsHouse;

import java.util.Objects;

/**Details of metadata for one section of a tune.
 * TODO: extend to tempo, volume, etc, changes, and transitions.
 *
 * @param sectionType  type of section; default is verse, eg for 'plain' style
 * @param bars  count of bars in this section; strictly positive, usually even
 */
public record TuneSectionDetails(int bars, TuneSection sectionType)
	{
    public TuneSectionDetails
	    {
	    if(bars <= 0) { throw new IllegalArgumentException(); }
	    Objects.requireNonNull(sectionType);
	    }

    /**Default (verse) section.
     * @param bars  count of bars in this section; strictly positive, usually even
     */
    public TuneSectionDetails(final int bars) { this(bars, TuneSection.verse); }
	}
