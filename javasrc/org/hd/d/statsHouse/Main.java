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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.hd.d.statsHouse.midi.MIDIConstant;
import org.hd.d.statsHouse.midi.MIDIGen;
import org.hd.d.statsHouse.midi.MIDITune;

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
        System.err.println("  infilename.csv (-play|outfilename.(csv|mid|wav)))");
        System.err.println("      [-seed n] [-het] [-intro bars]");
        System.err.println("      [-style (plain|gentle|house)]");
		System.err.println("      [-highWorse] [OFFSET [INSTRUMENT]]");
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

            // If no command stream then wrap up args[] as a single command.
            if(null == cmdlines)
	            { cmdlines = Collections.singletonList(Arrays.asList(args)); }

            // Execute command line(s) sequentially, aborting at any exception.
            int cmdCount = 0;
            for(final List<String> cmdline : cmdlines)
            	{
            	final int argCount = cmdline.size();
            	System.out.println("INFO: sonifying: " + (++cmdCount) + ": " + Arrays.toString(args));
            	if(argCount < 2)
            	    { throw new IllegalArgumentException("too few arguments: at least input.csv and -play or output.csv or output.mid required"); }

                final String inputFileName = cmdline.get(0);
                final String outputFileName = cmdline.get(1);

                // TODO: remaining optional args determine GenerationParameters.
                final GenerationParameters param = new GenerationParameters();

                // Generate the abstract MIDI form.
                final MIDITune mt = MIDIGen.genMelody(param, EOUDataCSV.loadEOUDataCSV(new File (inputFileName)));

                // Choose output type based on suffix, or -play.
                if(outputFileName.endsWith(".csv"))
	                {
	                // Generate and publish MIDICSV file.
                	try (
            			ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
                		Writer w = new OutputStreamWriter(baos)
                		)
	                	{
	                	MIDIGen.genFromTuneMIDICSV(w, mt);
                        FileUtils.replacePublishedFile(outputFileName, baos.toByteArray(), true);
	                	}
	                }
                else
	                {
                	// MIDI output to play immediately or to save.
                	final Sequence s = MIDIGen.genFromTuneSequence(mt);
                	final boolean isMid = outputFileName.endsWith(".mid");
                	if(isMid || outputFileName.endsWith(".wav"))
	                	{
    	                // Generate and publish MIDI binary file.
                    	try (ByteArrayOutputStream baos = new ByteArrayOutputStream(256))
    	                	{
    	                	MidiSystem.write(s, MIDIConstant.PREFERRED_MIDI_FILETYPE, baos);
    	                	if(isMid)
                                { FileUtils.replacePublishedFile(outputFileName, baos.toByteArray(), true); }
    	                	else
	    	                	{
    	                		// Write WAV instead.
	                    	    final AudioInputStream stream = AudioSystem.getAudioInputStream(
                    	    		new ByteArrayInputStream(baos.toByteArray()));
	                    	    try (ByteArrayOutputStream baosWAV = new ByteArrayOutputStream(256))
		                    	    {
		                            AudioSystem.write(stream, AudioFileFormat.Type.WAVE, baosWAV);
		                            FileUtils.replacePublishedFile(outputFileName, baosWAV.toByteArray(), true);
		                    	    }
	    	                	}
    	                	}
	                	}
                	else if("-play".equals(outputFileName))
	                	{
                    	// Get default sequencer.
                    	try(final Sequencer sequencer = MidiSystem.getSequencer())
                	    	{
                	    	if(null == sequencer)
                	    	    {
                	    		throw new UnsupportedOperationException("MIDI sequencer not available.");
                	    	    }

                    	    // Acquire resources and make operational.
                    	    sequencer.open();

                            sequencer.setSequence(s);
                            final long usLength = sequencer.getMicrosecondLength();
                        	System.out.println(String.format("INFO: duration %.1fs...", usLength / 1_000_000f));
                            sequencer.start();
                            while(sequencer.isRunning()) { Thread.sleep(1000); }
                            Thread.sleep(1000); // Allow for some graceful decay of the sound!
                	    	}
	                	}
                	else
	                	{
	                	throw new IllegalArgumentException("unrecognised output type/suffix: " + outputFileName);
	                	}
	                }
            	}
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
    }
