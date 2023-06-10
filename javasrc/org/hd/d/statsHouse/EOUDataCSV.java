package org.hd.d.statsHouse;

import java.util.List;
import java.util.Objects;

/**Wraps input CSV data to make it clear what it is; data cannot be null.  */
public record EOUDataCSV(List<List<String>> data)
    { public EOUDataCSV { Objects.requireNonNull(data); } }
