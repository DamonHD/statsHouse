package org.hd.d.statsHouse;

/**Music/percussion style.
 * Implies a level of production/'art' also.
 */
public enum Style
    {
    plain(ProductionLevel.None),
    gentle(ProductionLevel.Gentle),
    house(ProductionLevel.Danceable);
	// TODO: trance, drumAndBase, ...

	/**Production level for this style. */
	public final ProductionLevel level;

	private Style(final ProductionLevel level)
		{ this.level = level; }
    }