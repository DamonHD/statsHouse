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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

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

    /**Accepts command-line invocation. */
    public static void main(final String[] args)
        {
    	// Name of file (or "-" for stdin) for list of command, one per line.
        InputStream cmdstream = null;

        // Generate help if asked or if no parameters.
        if((args.length < 1) || "-help".equals(args[0]))
            {
            printOptions();
            return; // Not an error.
            }

        try
            {
            // If "-@" is specified, select a command stream...
            if((null != args[0]) && args[0].startsWith("-@"))
    	        {
    	        final String cmdfilename = args[1].substring(2);
    	        if("-".equals(cmdfilename))
    	            { cmdstream = new BufferedInputStream(System.in); }
    	        else
    	            { cmdstream = new BufferedInputStream(new FileInputStream(cmdfilename)); }
    	        }






            }
        catch(final Throwable e)
            {
            System.err.println("FAILED command");
            e.printStackTrace();
            System.exit(1);
            }

        // Unrecognised/unhandled command.
        System.err.println("Unrecognised or unhandled command");
        printOptions();
        System.exit(1);
        }
    }
