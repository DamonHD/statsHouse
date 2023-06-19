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

import java.io.BufferedReader;
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
        System.err.println("Commands/options");
        System.err.println("  -help");
        System.err.println("    This summary/help.");
        System.err.println("  -@(cmdfilename|-)");
        System.err.println("    Read independent command lines from specified file or stdin if '-'");
        System.err.println("    Do not process further command-line arguments.");
        System.err.println("  infilename.csv (-play|outfilename.(csv|mid)) [-seed n] [-het] [-intro bars] [-style (plain|gentle|house)] [OFFSET [INSTRUMENT]]");
        System.err.println("    This format may be used, one per line, in the command file.");
        }


	/**Charset for command list (ASCII 7-bit). */
	public static final Charset CMD_STREAM_CHARSET = StandardCharsets.US_ASCII;

	/**Immutable regex pattern used to split command line into arguments; never null.
	 * This is basically just a simple " "
	 * which with split() should preserve empty fields.
	 */
	public static final Pattern delimCmdStream = Pattern.compile(" ");

    /**Accepts command-line invocation. */
    public static void main(final String[] args)
        {
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
    	        final String cmdfilename = args[1].substring(2);
    	        try(Reader cmdStreamReader = switch(cmdfilename) {
	    	        case "" -> new InputStreamReader(System.in);
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

            // If no command stream then wrap up args[].
            if(null == cmdlines)
	            { cmdlines = Collections.singletonList(Arrays.asList(args)); }

            // Execute command line(s) sequentially, aborting at any exception.
            int cmdCount = 0;
            for(final List<String> cmdline : cmdlines)
            	{
            	final int argCount = cmdline.size();
            	System.out.println((++cmdCount) + ": " + Arrays.toString(args));
            	if(argCount < 2)
            	    { throw new IllegalArgumentException("Too few arguments: at least input.csv and -play or output.csv or output.mid required."); }






            	// TODO






            	}
            // Done, no errors.
            System.exit(0);
            }
        catch(final Throwable e)
            {
            System.err.println("FAILED command");
            e.printStackTrace();
            System.exit(1);
            }

        // Unrecognised/unhandled command.
        System.err.println("Unrecognised or unhandled command.");
        printOptions();
        System.exit(1);
        }
    }
