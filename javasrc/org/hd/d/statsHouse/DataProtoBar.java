package org.hd.d.statsHouse;

import java.util.Objects;

import org.hd.d.statsHouse.DataUtils.EOUDataCSV;

/**A prototype music bar made from (a subset of) raw EOUDataCSV records.
 *
 * Entries in dataRows may be null (padding at either end)
 * or contiguous in-date-order EOUDataCSV rows.
 * <p>
 * This can be used as an intermediate data representation
 * between the raw data coming in and the music bars.
 * 
 * @param dataNotesPerBar number of data/note slots; strictly positive
 * @param dataRows must be exactly dataNotesPerBar long; never null
 */
public record DataProtoBar(int dataNotesPerBar, EOUDataCSV dataRows)
    {
	public DataProtoBar
		{
		if(dataNotesPerBar < 1) { throw new IllegalArgumentException(); }
		Objects.requireNonNull(dataRows);
		Objects.requireNonNull(dataRows.data());
		if(dataNotesPerBar != dataRows.data().size()) { throw new IllegalArgumentException(); }
		}
    }
