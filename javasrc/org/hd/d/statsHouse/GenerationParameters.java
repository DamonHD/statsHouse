package org.hd.d.statsHouse;

import java.util.Objects;

/**Parameters for music generation from data.
 * May be extracted from a command-line or elsewhere.
 * <p>
 * Independent of input or output source,
 * though the expectation is that the input is a consolidated data CSV
 * and the output is MIDI in some form.
 *
 * @param style  the style of music to generate
 * @param introLength  intro/outro length in bars, and section length if +ve; non-negative
 */
public record GenerationParameters(Style style, int introLength)
	{
    public GenerationParameters
	    {
	    Objects.nonNull(style);
	    if(introLength < 0) { throw new IllegalArgumentException(); }
	    }
	}
