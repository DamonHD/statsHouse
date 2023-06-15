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

package org.hd.d.statsHouse.live;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

import org.hd.d.statsHouse.EOUDataCSV;
import org.hd.d.statsHouse.MIDIGen;

/**Main (command-line) entry-point for live access.
 */
public final class Main
    {
    /**Print a summary of command options to stderr. */
    private static void printOptions()
        {
        System.err.println("Commands/options");
        System.err.println("  -help");
        System.err.println("    This summary/help.");
        System.err.println("  -testLivePlay-testLivePlay");
        System.err.println("    Test live MIDI play of some built-in data.");
        }

    /**Accepts command-line arguments.
     *
     * Accepts the following commands:
     * <ul>
     * </ul>
     */
    public static void main(final String[] args)
        {
        if((args.length < 1) || "-help".equals(args[0]))
            {
            printOptions();
            return; // Not an error.
            }

        // Command is first argument.
        final String command = args[0];

        try
            {
        	switch(command)
	        	{
        		case "-testLivePlay": { sanityTestMIDIPlay(); System.exit(0); }




	        	}


            }
        catch(final Throwable e)
            {
            System.err.println("FAILED command: " + command);
            e.printStackTrace();
            System.exit(1);
            }

        // Unrecognised/unhandled command.
        System.err.println("Unrecognised or unhandled command: " + command);
        printOptions();
        System.exit(1);
        }


    /**Sanity test conversion of some data to MIDI then played live.
     * @throws MidiUnavailableException
     * @throws InvalidMidiDataException
     * @throws IOException
     * @throws InterruptedException
     */
    private static void sanityTestMIDIPlay()
		throws MidiUnavailableException, IOException, InvalidMidiDataException, InterruptedException
	    {
    	/**Full yearly-cadence PV generation data CSV to partial 2023, including comment rows.
    	 * Sample from:
    	 * <pre>
% cat data/consolidated/energy/std/gen/Y/gen-Y.csv
         * </pre>
    	 */
    	final String sample_gen_Y = """
#YYYY,device,coverage,gen,device,coverage,gen,device,coverage,gen
#input,"data/consolidated/energy/std/gen/Y/Enphase/gen-Y-Enphase.csv"
#input,"data/consolidated/energy/std/gen/Y/meter/gen-Y-meter.csv"
#input,"data/consolidated/energy/std/gen/Y/SunnyBeam/gen-Y-SunnyBeam.csv"
2008,,,,meter,0.916667,915,SunnyBeam,0.845238,889.93
2009,,,,meter,1,2956.1,SunnyBeam,1,2907.15
2010,,,,meter,1,3546.9,SunnyBeam,1,3482.76
2011,,,,meter,1,3988.1,SunnyBeam,1,3922.27
2012,,,,meter,1,3777.8,SunnyBeam,1,3712.68
2013,,,,meter,1,3749.7,SunnyBeam,1,3687.79
2014,,,,meter,1,3944,SunnyBeam,1,3881.99
2015,,,,meter,1,3828.6,SunnyBeam,1,3766.9
2016,,,,meter,1,3703.2,SunnyBeam,1,3676.54
2017,,,,meter,1,3794.4,SunnyBeam,1,3736.89
2018,Enphase,0.410714,1069.29,meter,1,3927.8,SunnyBeam,1,3931.44
2019,Enphase,0.999888,3870.89,meter,1,3855.5,SunnyBeam,1,3800.95
2020,Enphase,0.999888,4084.42,meter,1,4069.9,SunnyBeam,1,4020.86
2021,Enphase,0.999888,3514.19,meter,1,3500.8,SunnyBeam,1,3448.6
2022,Enphase,0.999888,3943.38,meter,1,3925.1,SunnyBeam,1,3865.5
2023,Enphase,0.416555,1415.92,meter,0.416667,1411,SunnyBeam,0.440476,1554.63
    			""";

    	System.out.println("Testing live play of MIDI generated from internal data.");
    	// Get default sequencer.
    	try(final Sequencer sequencer = MidiSystem.getSequencer())
	    	{
	    	if(null == sequencer)
	    	    {
	    		System.err.println("MIDI sequencer not available.");
	    		System.exit(1);
	    	    }

    	    // Acquire resources and make operational.
    	    sequencer.open();

    	    // Load data sample and convert to MIDI.
            final EOUDataCSV csv1 = EOUDataCSV.parseEOUDataCSV(new StringReader(sample_gen_Y));
            final Sequence s = MIDIGen.genMinimalMelodyMIDISCV(Writer.nullWriter(), csv1);
            sequencer.setSequence(s);
            final long usLength = sequencer.getMicrosecondLength();
        	System.out.println(String.format("Duration %.1fs...", usLength / 1_000_000f));
            sequencer.start();
            while(sequencer.isRunning()) { Thread.sleep(1000); }
            Thread.sleep(1000); // Allow for some graceful decay of the sound!
            sequencer.stop();
	    	}
    	}
    }
