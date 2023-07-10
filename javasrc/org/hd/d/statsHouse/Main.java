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
import java.io.IOException;
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

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.hd.d.statsHouse.data.DataBounds;
import org.hd.d.statsHouse.data.EOUDataCSV;
import org.hd.d.statsHouse.data.FileUtils;
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
    	final String version = getManifestVersion();
    	System.err.println("statsHouse" + ((null == version) ? "" : (" V" + version)));
    	System.err.println();
        System.err.println("Commands/options:");
        System.err.println("  -help");
        System.err.println("    This summary/help.");
        System.err.println("  -@(<cmdfilename>|-)");
        System.err.println("    Read independent command lines from specified file or stdin if '-'");
        System.err.println("    Do not process further command-line arguments.");
        System.err.println("  infilename.csv (-play|<outfilename>.(csv|mid|wav)))");
        System.err.println("      [-seed n] [-het] [-intro (auto|<bars>)]");
        System.err.println("      [-style (plain|gentle|house)]");
		System.err.println("      [-highWorse] [OFFSET [INSTRUMENT]]");
        System.err.println("    This format may be used, one per line, in the command file.");
        }

	/**Charset for command list (ASCII 7-bit). */
	public static final Charset CMD_STREAM_CHARSET = StandardCharsets.US_ASCII;

	/**Immutable regex pattern used to split command line into arguments; never null.
	 * This is one or more spaces or tabs.
	 */
	public static final Pattern delimCmdStream = Pattern.compile("[ \t]+");

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
     *
     * @param cmdlines  zero or more command lines each consisting of arguments pre-parsed into separate Strings; never null
     * @param quiet  if true, minimise output such as progress indication
     *
     * @throws IOException
     * @throws InvalidMidiDataException
     * @throws UnsupportedAudioFileException
     * @throws MidiUnavailableException
     * @throws InterruptedException
     */
	public static void runCommands(final List<List<String>> cmdlines, final boolean quiet)
		throws IOException,
			InvalidMidiDataException,
			UnsupportedAudioFileException,
			MidiUnavailableException,
			InterruptedException
		{
		// Execute command line(s) sequentially, aborting at any exception.
		int cmdCount = 0;
		for(final List<String> cmdline : cmdlines)
			{
			final int argCount = cmdline.size();
			if(!quiet)
			    { System.out.println("INFO: sonifying: " + (++cmdCount) + "/" + (cmdlines.size()) + ": " + Arrays.toString(cmdline.toArray())); }
			if(argCount < 2)
			    { throw new IllegalArgumentException("too few arguments: at least input.csv and -play or output.csv or output.mid required"); }

		    final String inputFileName = cmdline.get(0);
		    final String outputFileName = cmdline.get(1);

		    // Remaining optional args determine GenerationParameters.
		    // Use the final component of the input file name as the tune name.
		    final GenerationParameters params =
				GenerationParameters.parseOptionalCommandArguments(cmdline.subList(2, cmdline.size()),
						(new File(inputFileName)).getName());

			// Generate the abstract MIDI form.
		    final EOUDataCSV data = EOUDataCSV.loadEOUDataCSV(new File (inputFileName));
		    final DataBounds db = new DataBounds(data);
		    final MIDITune mt = MIDIGen.genMelody(params, data);

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
		    	final Sequence s = MIDIGen.genFromTuneSequence(mt, params, db);
		    	final boolean isMid = outputFileName.endsWith(".mid");
		    	if(isMid || outputFileName.endsWith(".wav"))
		        	{
		            // Generate MIDI binary file.
		        	try (ByteArrayOutputStream baos = new ByteArrayOutputStream(256))
		            	{
		            	MidiSystem.write(s, MIDIConstant.PREFERRED_MIDI_FILETYPE, baos);
		            	if(isMid)
		                    {
		            		// Publish binary MIDI file.
		            		FileUtils.replacePublishedFile(outputFileName, baos.toByteArray(), true);
		            		}
		            	else
		                	{
		            		// Generate and publish WAV.
		            	    final AudioInputStream stream = AudioSystem.getAudioInputStream(
		        	    		new ByteArrayInputStream(baos.toByteArray()));
		            	    try (ByteArrayOutputStream baosWAV = new ByteArrayOutputStream(16384))
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
		}

	/**Get version number as a.b.c String, else null if not available. */
	public static final String getManifestVersion()
		{ return(Main.class.getPackage().getImplementationVersion()); }
    }
