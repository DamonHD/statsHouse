package org.hd.d.statsHouse;

/**How produced the output is as (danceable) music.
 * Low values such as for None (0) retain maximum fidelity to the source,
 * and should be most useful for science purposes.
 * <p>
 * High values such as for Danceable (2) have most transformations applied,
 * and should be most useful for entertainment.
 * <p>
 * The 'level' field provides an int representation
 * that may be used for sorting, etc.
 * 
 * @author dhd
 */
public enum ProductionLevel {
    None(0),
    Gentle(1),
    Danceable(2);
	
	/**Higher means more produced; lower means better fidelity to the data. */
	public final int level;

    private ProductionLevel(int level) {
        this.level = level;
    }
	
}
