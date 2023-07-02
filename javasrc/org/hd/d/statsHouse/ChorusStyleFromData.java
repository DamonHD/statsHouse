package org.hd.d.statsHouse;

/**This enumerates some possible algorithmic styles of data melody chorus generation. */
public enum ChorusStyleFromData
	{
	FirstDataBar,
    FirstFullDataBar,

    /**Can capture means and max/min or 90/10 percentile points. */
    SyntheticRepresentativeDataBar,
    SyntheticRepresentativeDataBarPlusCounterpoint,

    /**Downsampling in at least one bar, then synthetic style. */
    MeansPlusSyntheticRepresentativeDataBar,
    MeansPlusSyntheticRepresentativeDataBarPlusCounterpoint;
	}
