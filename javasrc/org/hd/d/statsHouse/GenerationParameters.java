package org.hd.d.statsHouse;

import java.util.Objects;

/**Parameters for music generation from data.
 * May be extracted from a command-line or elsewhere.
 * <p>
 * Immutable as all its constituents should be.
 * <p>
 * Independent of particular input or output source,
 * though the expectation is that the input is a consolidated data CSV
 * and the output is MIDI in some form.
 * <p>
 * For the seed, before in configuration before creation of this record, values are:
 * -ve for unique each run, 0 for no randomness, 1 for based on data, other +ve use seed as-is.
 * For the -1 and 1 cases the appropriate new seed will be generated
 * before being passed as an argument to this record instance.
 *
 * @param seed  randomisation seed; 0 for no randomness (use 'best') choices
 * @param style  the style of music to generate; never null
 * @param introBars  intro/outro length in bars, and section length if +ve; non-negative
 * @param hetero  true if heterogeneous data rather than different sources of the same data
 */
public record GenerationParameters(int seed, Style style, int introBars, boolean hetro)
	{
    public GenerationParameters
	    {
	    Objects.requireNonNull(style);
	    if(introBars < 0) { throw new IllegalArgumentException(); }
	    }

    /**Default sensible defaults for homogeneous data. */
    public GenerationParameters() { this(0, Style.house, 0, false); }

    /**True if no randomness should be applied, using only 'best' choices. */
    public boolean noRandomness() { return(0 == seed); }
	}
