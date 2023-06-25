package org.hd.d.statsHouse;

import java.util.Objects;

import org.hd.d.statsHouse.midi.MIDIGen;

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
 * @param introBars  intro/outro length in bars, also section length if +ve; non-negative
 * @param hetero  true if heterogeneous data rather than different sources of the same data
 * @param name  short ASCII name of track or source, eg "gen-M"; can be null
 */
public record GenerationParameters(int seed, Style style, int introBars, boolean hetro, String name)
	{
    public GenerationParameters
	    {
	    Objects.requireNonNull(style);
	    if(introBars < 0) { throw new IllegalArgumentException(); }
	    }

    public static final int DEFAULT_SEED = 0;
    public static final Style DEFAULT_SYLE = Style.plain;
    public static final int DEFAULT_INTRO_BARS = 0;
    public static final boolean DEFAULT_HETERO = false;
    public static final String DEFAULT_NAME = null;

    /**Default sensible (sciency) defaults for homogeneous data. */
    public GenerationParameters() { this(DEFAULT_SEED, DEFAULT_SYLE, DEFAULT_INTRO_BARS, DEFAULT_HETERO, DEFAULT_NAME); }

    /**Parse optional arguments from command-line starting at the given index (usually 2).
     * <pre>
infilename.csv (-play|outfilename.(csv|mid|wav)))
  [-seed n] [-het] [-intro bars]
  [-style (plain|gentle|house)]
  [-highWorse] [OFFSET [INSTRUMENT]]
     * </pre>
     * <p>
     * TODO: unit tests
     */
    public static GenerationParameters parseOptionalCommandArguments(final String args[], final int firstIndex)
	    {
    	final int seed = DEFAULT_SEED;
    	boolean hetero = DEFAULT_HETERO;
    	Style style = DEFAULT_SYLE;
    	int introBars = DEFAULT_INTRO_BARS;
    	final String name = DEFAULT_NAME;

    	for(int i = firstIndex; i < args.length; )
	    	{

	    	// FIXME: parse remaining args

	    	if("-het".equals(args[i]))
		    	{
				hetero = true;
				++i;
				continue;
		    	}

	    	if((i+1 < args.length) && "-intro".equals(args[i]))
				{
	    		introBars = Integer.parseInt(args[i+1]);
	            i += 2;
	            continue;
				}

	    	if((i+1 < args.length) && "-style".equals(args[i]))
		    	{
	            final String styleType = args[i+1];
	            style = (switch(styleType) {
		            // Accept "none" as synonym for "plain" for backward compatibility with V4.x.
		            case "none", "plain" -> Style.plain;
		            case "gentle" -> Style.gentle;
		            case "house" -> Style.house;
		            default -> throw new IllegalArgumentException("unknown style '"+ styleType + "'");
		            });
	            i += 2;
	            continue;
		    	}

    		throw new IllegalArgumentException("unknown argument '"+ args[i] + "'");
	    	}

    	return(new GenerationParameters(seed, style, introBars, hetero, name));
	    }

    /**True if no randomness should be applied to the music generation: use only 'best' choices. */
    public boolean noRandomness() { return(0 == seed); }

    /**Get section length in bars - uses intro length if non-zero; strictly positive */
    public int sectionBars() { return((0 != introBars) ? introBars : MIDIGen.DEFAULT_SECTION_BARS); }
	}
