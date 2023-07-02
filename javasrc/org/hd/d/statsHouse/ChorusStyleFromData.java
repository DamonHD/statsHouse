package org.hd.d.statsHouse;

/**This enumerates some possible algorithmic styles of data melody chorus generation. */
public enum ChorusStyleFromData
	{
	FirstDataBar,
    FirstFullDataBar,
    SyntheticRepresentativeDataBar, // Can capture means and max/min or 90/10 percentile points.
    SyntheticRepresentativeDataBarPlusCounterpoint,
    MeansPlusSyntheticRepresentativeDataBar,
    MeansPlusSyntheticRepresentativeDataBarPlusCounterpoint;
	}
