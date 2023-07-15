/*
Copyright (c) 2023, Damon Hart-Davis

Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package org.hd.d.statsHouse;

import java.util.List;
import java.util.Objects;

import org.hd.d.statsHouse.generic.Style;

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
 * -ve for unique each run, 0 for no randomness, 1 for based on name, other +ve use seed as-is.
 * For the -1 and 1 cases the appropriate new seed will be generated
 * before being passed as an argument to this record instance.
 *
 * @param seed  randomisation seed; 0 for no randomness (use 'best') choices
 * @param style  the style of music to generate; never null
 * @param introBars  intro/outro length in bars, also section length if +ve, -1 if auto, else non-negative
 * @param hetero  true if heterogeneous data rather than different sources of the same data
 * @param name  short ASCII name of track or source, eg "gen-M"; can be null
 * @param derivedSeed should be 0 (RANDOMNESS_NONE) for no randomness, else strictly positive;
 *     can automatically be derived from seed and name
 */
public record GenerationParameters(int seed, Style style, int introBars, boolean hetero, String name,
		int derivedSeed)
	{
    public GenerationParameters
	    {
	    Objects.requireNonNull(style);
	    if(introBars < AUTO_INTRO_BARS) { throw new IllegalArgumentException(); }
	    if(derivedSeed < 0) { throw new IllegalArgumentException(); }
	    }

    /**Generate the derived seed automatically. */
    public GenerationParameters(final int seed, final Style style, final int introBars, final boolean hetero, final String name)
	    {
    	this(seed, style, introBars, hetero, name,
    			makeDerivedSeed(seed, name));
	    }

    /**Randomness: unique on each run derived from time of day. */
    public static final int RANDOMNESS_UNIQUE = -1;
    /**Randomness: none (though progression may still happen. */
    public static final int RANDOMNESS_NONE = 0;
    /**Randomness: from name (or data). */
    public static final int RANDOMNESS_NAME = 1;

    /**Used to request an intro/outro of length automatically selected to suit the data. */
    public static final int AUTO_INTRO_BARS = -1;

    /**Default seed is for no randomness. */
    public static final int DEFAULT_SEED = RANDOMNESS_NONE;
    /**Default style is 'plain', ie the least-produced and highest fidelity to the source data. */
    public static final Style DEFAULT_STYLE = Style.plain;
    /**Default intro length is 0, ie no intro/outro. */
    public static final int DEFAULT_INTRO_BARS = 0;
    /**Default heterogeneity is false, ie the data is homogeneous and for a single variable. */
    public static final boolean DEFAULT_HETERO = false;
    /**Default name is absent (though the input filename can be used), ie null. */
    public static final String DEFAULT_NAME = null;

    /**Default sensible (sciency) defaults for homogeneous data. */
    public GenerationParameters()
        {
    	this(DEFAULT_SEED, DEFAULT_STYLE, DEFAULT_INTRO_BARS, DEFAULT_HETERO, DEFAULT_NAME,
    			makeDerivedSeed(DEFAULT_SEED, DEFAULT_NAME));
    	}

    /**Parse optional arguments from command-line after fixed parameters.
     * <pre>
  [-seed n] [-het] [-intro (auto|<bars>)]
  [-style (plain|gentle|house)]
  [-highWorse] [OFFSET [INSTRUMENT]]
     * </pre>
     * <p>
     * TODO: unit tests
     *
     * @param args  optional arguments; never null
     * @param inputFileName  input name or null
     */
    public static GenerationParameters parseOptionalCommandArguments(
    		final List<String> args,
    		final String inputFileName)
	    {
    	int seed = DEFAULT_SEED;
    	boolean hetero = DEFAULT_HETERO;
    	Style style = DEFAULT_STYLE;
    	int introBars = DEFAULT_INTRO_BARS;
    	final String name = inputFileName;

    	for(int i = 0; i < args.size(); )
	    	{
    		// Current first argument.
	    	final String arg = args.get(i);

	    	// FIXME: parse remaining flag and argument types

			if((i+1 < args.size()) && "-seed".equals(arg))
				{
	    		seed = Integer.parseInt(args.get(i+1));
	            i += 2;
	            continue;
				}

	    	if("-het".equals(arg))
		    	{
				hetero = true;
				++i;
				continue;
		    	}

	    	if((i+1 < args.size()) && "-intro".equals(arg))
				{
	    		if("auto".equals(args.get(i+1))) { introBars = AUTO_INTRO_BARS; }
	    		else { introBars = Integer.parseInt(args.get(i+1)); }
	            i += 2;
	            continue;
				}

	    	if((i+1 < args.size()) && "-style".equals(arg))
		    	{
	            final String styleType = args.get(i+1);
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

    		throw new IllegalArgumentException("unknown argument '"+ arg + "'");
	    	}

    	return(new GenerationParameters(seed, style, introBars, hetero, name));
	    }

    /**True if some sort of intro/outro is requested. */
    public boolean introRequested() { return(0 != introBars); }

    /**True if fixed-length intro/outro is requested. */
    public boolean introRequestedFixedLength() { return(introBars > 0); }

    /**True if automatic-length intro/outro is requested. */
    public boolean introRequestedAutoLength() { return(AUTO_INTRO_BARS == introBars); }

    /**True if unique-each-time randomness should be applied to the music generation. */
    public boolean randomnessUnique() { return(RANDOMNESS_UNIQUE == seed); }
    /**True if no randomness should be applied to the music generation: use only 'best' choices. */
    public boolean randomnessNone() { return(RANDOMNESS_NONE == seed); }
    /**True if name-based (or eg data-based) randomness should be applied to the music generation. */
    public boolean randomnessName() { return(RANDOMNESS_NAME == seed); }

    /**Make derived seed (ie for when not supplied/overridden); non-negative.
     * <ul>
     * <li>If seed is RANDOMNESS_NONE then is returned as-is.</li>
     * <li>If seed is RANDOMNESS_NAME then strictly-positive value based on the name if non-null, else RANDOMNESS_NONE.</li>
     * <li>If seed is RANDOMNESS_UNIQUE then strictly-positive value based on time of day.</li>
     * <li>All other values are coerced into something strictly positive.</li>
     * </ul>
     *
     * @return non-negative new seed value, zero if input is zero, else strictly positive,
     *     ie a positive result requests an output varied from standard '0' form
     */
    public static int makeDerivedSeed(final int seed, final String name)
	    {
	    return(switch(seed) {
	    case RANDOMNESS_NONE -> RANDOMNESS_NONE;
	    case RANDOMNESS_NAME -> (null == name) ? RANDOMNESS_NONE : Math.min(1, name.hashCode() >>> 1);
	    case RANDOMNESS_UNIQUE -> Math.max(1, ((int) System.currentTimeMillis()) >>> 1);
	    default -> Math.max(1, seed & 0x7fffffff);
	    });
	    }
	}
