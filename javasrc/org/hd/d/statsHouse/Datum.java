package org.hd.d.statsHouse;

import java.util.List;

/**A single data point from a single data stream, not including the date.
 * Sample data record:
 * <p>
<pre>
2008-03,,,,meter,1,70,SunnyBeam,1,68.55
</pre>
 * <p>
 *
 * @param source  name of the source device;
 *     null if absent (never empty)
 * @param coverage  non-negative finite coverage value usually [0.0f,1.0f];
 *     null if absent or unparseable
 * @param value  finite data value usually non-negative;
 *     null if absent or unparseable
 */
public record Datum(String source, Float coverage, Float value)
	{
	public Datum
		{
        if("".equals(source)) { throw new IllegalArgumentException(); }
        if((null != coverage) && !Float.isFinite(coverage)) { throw new IllegalArgumentException(); }
        if((null != coverage) && (coverage < 0)) { throw new IllegalArgumentException(); }
        if((null != value) && !Float.isFinite(value)) { throw new IllegalArgumentException(); }
		}

	/**Extract a Datum from the specified 1-indexed stream in the supplied parsed record.
	 * @param stream  stream number to extract the datum from
	 *     with 1 being the first (left-most) stream
	 * @param row  one row of EOUDataCSV data
	 * @return  extract Datum, or empty datum for null row, invalid stream, all values missing
	 */
	public static Datum extractDatum(final int stream, final List<String> row)
		{
		if(null == row) { return(EMPTY); }
		if(stream < 1) { return(EMPTY); }

		// If final index needed is absent then the requested stream is not present.
		final int lastIndex = stream * 3;
		if(lastIndex >= row.size()) { return(EMPTY); }

		// Store missing source name as null not "".
		String source = row.get(lastIndex - 2);
		if("".equals(source)) { source = null; }

		Float coverage = null;
		try {
			coverage = Float.parseFloat(row.get(lastIndex - 1));
	        if(!Float.isFinite(coverage)) { coverage = null; }
	        else if(coverage < 0) { coverage = null; }
	        }
		catch(final NumberFormatException e) { }

		Float value = null;
		try {
			value = Float.parseFloat(row.get(lastIndex));
	        if(!Float.isFinite(value)) { value = null; }
			}
		catch(final NumberFormatException e) { }

        final Datum result = new Datum(source, coverage, value);
        if(result.isEmpty()) { return(EMPTY); }
        return(result);
		}

	/**Create an empty record, eg when requesting a stream that does not exist. */
	public Datum() { this(null, null, null); }

	/**Single empty instance to reduce GC load. */
	private static final Datum EMPTY = new Datum();

	/**Returns true if this represents and empty or missing datum. */
	public boolean isEmpty()
		{ return((null == source) && (null == coverage) && (null == value)); }
	}
