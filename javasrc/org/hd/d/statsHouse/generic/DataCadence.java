package org.hd.d.statsHouse.generic;

/**Cadence of input data: daily, monthly, yearly. */
public enum DataCadence
    {
    D(32, 32, 12),
    M(12, 12, 0),
    Y(4, 0, 0);

	/**Default data points per bar; strictly positive. */
	public final int defaultPerBar;
	/**Default +ve cycle in the data (possibly after padding/alignment), or zero if none. */
	public final int defaultCycle;
	/**Default +ve higher cycle in bars, or zero if none. */
	public final int defaultBarsCycle;

	/**True if can align (which requires there to be a default cycle. */
	public boolean canAlign() { return(0 != defaultCycle); }

	private DataCadence(final int defaultPerBar, final int defaultCycle, final int defaultBarsCycle)
		{
		this.defaultPerBar = defaultPerBar;
		this.defaultCycle = defaultCycle;
		this.defaultBarsCycle = defaultBarsCycle;
		}
    }
