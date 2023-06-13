package org.hd.d.statsHouse;

import java.util.Objects;

/**Parameters for music generation from data.
 * May be extracted from a command-line or elsewhere.
 * <p>
 * Independent of input or output source,
 * though the expectation is that the input is a consolidate data CSV
 * and the output is MIDI in some form.
 */
public record GenerationParameters(Style style)
	{
    public GenerationParameters
	    {
	    Objects.nonNull(style);
	    }
	}
