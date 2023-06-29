package org.hd.d.statsHouse;

/**Cadence of input data: daily, monthly, yearly. */
public enum DataCadence
    {
    D(32, 32),
    M(12, 12),
    Y(4, 0);

	/**Default data points per bar; strictly positive. */
	public final int defaultPerBar;
	/**Default cycle in the data (possibly after padding/alignment), or zero if none. */
	public final int defaultCycle;

	/**True if can align (which requires there to be a default cycle. */
	public boolean canAlign() { return(0 != defaultCycle); }

	private DataCadence(final int defaultPerBar, final int defaultCycle)
		{
		this.defaultPerBar = defaultPerBar;
		this.defaultCycle = defaultCycle;
		}
    }
