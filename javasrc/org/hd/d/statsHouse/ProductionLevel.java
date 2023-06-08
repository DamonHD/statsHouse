package org.hd.d.statsHouse;

/**How 'produced' the output is as (danceable) music.
 * Low ordinal values such as for None retain maximum fidelity to the source,
 * and should be most useful for science purposes.
 * <p>
 * High ordinal values such as for Danceable have most transformations applied,
 * and should be most useful for entertainment.
 */
public enum ProductionLevel {
    None,
    Gentle,
    Danceable;
}
