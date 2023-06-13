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
 * For the seed, before in config before creation of this record, values are:
 * -ve for unique each run, 0 for no randomness, 1 for based on data, other +ve use seed as-is.
 * For the -1 and 1 cases the appropriate new seed will be generated
 * before being passed as an argument to this record instance.
 *
 * @param seed  randomisation seed; 0 for no randomness (use 'best') choices
 * @param style  the style of music to generate; never null
 * @param introLength  intro/outro length in bars, and section length if +ve; non-negative
 */
public record GenerationParameters(int seed, Style style, int introLength)
	{
    public GenerationParameters
	    {
	    Objects.nonNull(style);
	    if(introLength < 0) { throw new IllegalArgumentException(); }
	    }

    public boolean noRandomness() { return(0 == seed); }
	}
