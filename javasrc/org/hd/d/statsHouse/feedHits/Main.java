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

package org.hd.d.statsHouse.feedHits;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**Main (command-line) entry-point for the data handler.
 */
public final class Main
    {
    /**Print a summary of command options to stderr. */
    private static void printOptions()
        {
    	final String version = getManifestVersion();
    	System.err.println("feedHits: statsHouse" + ((null == version) ? "" : (" V" + version)));
    	System.err.println();
        System.err.println("Commands/options:");
        System.err.println("  -help");
        System.err.println("    This summary/help.");
        System.err.println("  -@(<cmdfilename>|-)");
        System.err.println("    Read independent command lines from specified file or stdin if '-'");
        System.err.println("    Do not process further command-line arguments.");
        System.err.println("  -summary <typeN> {dir}*");
        System.err.println("    Summary/quick play, 0 (use internal) or more directories.");
        System.err.println("    Each directory should contain a standard set of data extract files.");
    	System.err.println();
        }

	/**Charset for command list (ASCII 7-bit). */
	public static final Charset CMD_STREAM_CHARSET = StandardCharsets.US_ASCII;

	/**Immutable regex pattern used to split command line into arguments; never null.
	 * This is one or more spaces or tabs.
	 */
	public static final Pattern delimCmdStream = Pattern.compile("[ \t]+");

	/**Default output MIDI filename. */
	public static final File DEFAULT_OUT_MIDI = new File("out.midi");


    /**Accepts command-line invocation. */
    public static void main(final String[] args)
        {
    	final long start = System.currentTimeMillis();

        // List of command lines split at spaces.
        List<List<String>> cmdlines = null;

        // Generate help if asked or if no parameters.
        if((args.length < 1) || "-help".equals(args[0]))
            {
            printOptions();
            return; // Not an error.
            }

        try
            {
            // If "-@" is specified then select a command stream...
            if((null != args[0]) && args[0].startsWith("-@"))
    	        {
            	cmdlines = new ArrayList<>();
    	        final String cmdfilename = args[0].substring(2);
    	        try(Reader cmdStreamReader = switch(cmdfilename) {
	    	        case "-" -> new InputStreamReader(System.in);
	    	        case "" -> throw new IllegalArgumentException("missing command file name");
	    	        default -> new FileReader(cmdfilename);
	    	        })
	    	        {
	    	        try(final BufferedReader br = new BufferedReader(cmdStreamReader, 8192))
		    	        {
	    	        	String line;
	    		        while(null != (line = br.readLine()))
	    		            {
	    		        	// Skip empty lines.
	    		        	if("".equals(line)) { continue; }
	    		            final String fields[] = delimCmdStream.split(line);
                            cmdlines.add(Collections.unmodifiableList(Arrays.asList(fields)));
	    		            }
		    	        }
	    	        }
    	        }

            // If no command stream then wrap up args[] as a single command.
            if(null == cmdlines)
	            { cmdlines = Collections.singletonList(Arrays.asList(args)); }

            runCommands(cmdlines, false);

        	final long end = System.currentTimeMillis();
            System.out.println(String.format("INFO: runtime %.3fs", (end - start) / 1000f));

            // Done, no errors.
            System.exit(0);
            }
        catch(final Throwable e)
            {
            System.err.println("ERROR: FAILED command");
            e.printStackTrace();
            System.exit(1);
            }

        // Unrecognised/unhandled command.
        System.err.println("ERROR: unrecognised or unhandled command.");
        printOptions();
        System.exit(1);
        }

    /**Run zero or more command lines, aborting with an exception in case of error.
     * Any caught exception is rethrown as a RuntimeException, wrapped in some extra context.
     *
     * @param cmdlines  zero or more command lines each consisting of arguments pre-parsed into separate Strings; never null
     * @param quiet  if true, minimise output such as progress indication
     */
	public static void runCommands(final List<List<String>> cmdlines, final boolean quiet)
		{
		// Execute command line(s) sequentially, aborting at any exception.
		int cmdCount = 0;
		for(final List<String> cmdline : cmdlines)
			{
			final int argCount = cmdline.size();
			if(argCount < 1) { continue; } // Skip empty commands.

		    final File outputFileName = DEFAULT_OUT_MIDI;

		    System.out.println("INFO: command: " +
			        (++cmdCount) + "/" + (cmdlines.size()) + ": " +
			        Arrays.toString(cmdline.toArray()));

		    try {
		    	switch(cmdline.get(0))
			    	{
			    	case "-summary":
			    		{
						if(argCount < 3) { throw new IllegalArgumentException("too few arguments to -summary"); }
						GenerateSummary.summary(Integer.parseInt(cmdline.get(1), 10), cmdline.subList(2, cmdline.size()));
						break;
			    		}

		    		default:
		    			throw new IllegalArgumentException("Command/switch not recognised: " + cmdline.get(0));
			    	}
				}
		    catch(final Exception e)
		        {
				e.printStackTrace();
				}
			}
		}

	/**Get version number as a.b.c String, else null if not available. */
	public static final String getManifestVersion()
		{ return(Main.class.getPackage().getImplementationVersion()); }
    }
