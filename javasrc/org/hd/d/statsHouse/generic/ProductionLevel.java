package org.hd.d.statsHouse.generic;

/**How 'produced' the output is as (danceable) music.
 * Low ordinal values such as for None retain more fidelity to the source,
 * and should be most useful for science purposes.
 * <p>
 * High ordinal values such as for Danceable have more stylistic transformations applied,
 * and should be most useful for entertainment.
 */
public enum ProductionLevel
    {
    None,
    Gentle,
    Danceable;
    }