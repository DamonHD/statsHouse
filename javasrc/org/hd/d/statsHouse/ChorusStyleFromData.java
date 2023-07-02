package org.hd.d.statsHouse;

/**This enumerates some possible algorithmic styles of data melody chorus generation. */
public enum ChorusStyleFromData
	{
	/**Use the first data stream bar as-is; may be partly or entirely empty. */
	FirstDataBar,
	/**More robust for aligned data and/or on non-primary stream; such a full bar may not exist so will need fallback. */
    FirstFullDataBar,

    /**Can capture means and max/min or 90/10 percentile points; hopes for periodicity on ls date component. */
    SyntheticRepresentativeDataBar,
    SyntheticRepresentativeDataBarPlusCounterpoint,

    /**Downsampling in at least one bar, then synthetic style; hopes for periodicity on ls date component. */
    MeansPlusSyntheticRepresentativeDataBar,
    MeansPlusSyntheticRepresentativeDataBarPlusCounterpoint;
	}
