#!/bin/sh

# Converts data in 'consolidated' format, eg kWh, to MIDI.
# The input data is assumed to be dense, with no missing times.
# Output is MIDI binary format 1 c/o MIDICSV utility.
# Each data source in the input will use its own channel in one melody track.
# The maximum number data channels used is 4.
# Mapping choices are auto-ranged based on the file name and data content.
# May optionally have (gentle or house-style) percussion track alongside.
#
# TODO:
#    * Start allowing (data-derived) 'seed' to change eg instruments/scale.
#    * Fix lead-out behaviour for tracks that finish well before others.
#    * Move preferred data channel to (near) centre for homogeneous data.
#    * When aligning D data, force to calendar-month-per-bar.
#    * Deal with missing points in general alignment mechanism.
#    * Introduce breakdown and mini-breakdown.
#    * Chnage bass instrument on phase boundaries or in breakdown.
#
# NEW IN V4.5:
#    * Injecting script name into MIDI output for traceability.
#    * Tweaking voice levels for "house" (lead synth quieter).
#
# NEW IN V4.4:
#    * Tweaking instruments and volumes for "house".
#
# NEW IN V4.3:
#    * Phase length is INTRO bars, or 1 bar if not set.
#    * When aligning, pad each data track to whole phrase start and end.
#    * Avoid overflow of highest value; keep *within* RANGE octaves.
#
# NEW IN V4.2:
#    * Daily data is played at 32 notes (~1 month) per bar.
#    * Option to run in heterogeneous mode, with eg all streams equal weight.
#    * Velocity is adjusted alongside pitch for data.
#    * Panning is wider for data voices.
#    * Use major scale rather than using all 12/octave when not plainest mode.
#    * Added simple base line (octave below root) on beat (can do off-beat).
#
# NEW IN V4.1:
#    * No percussion also brings a plainer data melody.
#
# NEW IN V4.0 (released 2023-04-24):
#    * uses library awk functions for some behaviour
#    * labels the (first) track with the input file basename
#    * annotates (marks) key times in output
#    * pans voices across L-R range
#    * reduces volume/velocity of low coverage values
#    * prefers the data stream with the most points
#    * reduces volume of non-preferred data streams
#    * copyright notice stating CC0 / public domain
#    * track 1 is now metadata/tempo only
#    * uses a track (2 onwards) for each voice, and MIDI channels from 0 onwards
#
# For M (monthly) data, 12 samples, will be fit in a (4/4) bar.
# If percussion is selected then a bar may be forced to start with January.
# For Y (yearly) data, 1 sample per beat, so four in a (4/4) bar.
#
# If percussion is added this may round data/melody to at least whole bars.
#
# Default behaviour is to map input value to note and volume.
#
# Writes MIDI (format 1) binary via MIDICSV to stdout.
#     https://www.fourmilab.ch/webtools/midicsv/
#     http://www.music.mcgill.ca/~ich/classes/mumt306/StandardMIDIfileformat.html
# Also see:
#     http://www.nortonmusic.com/midi_cc.html
#     https://www.guitarland.com/Music10/FGA/LectureMIDIscales.html

# Usage:
#     $0 in.csv [-seed n] [-het] [-intro bars] [-perc [none|gentle|house]] [OFFSET [INSTRUMENT]]
#
# -seed n
#     use a fixed positive (integer) seed, else (or for 0) the system may choose
#     -1 means pick a new random seed each time, eg from time of day
# -het
#     treats data streams as heterogenous, ie different variables;
#     may be auto-detectable from the file content in some cases
# -intro bars
#     adds bars intro (with percussion if supplied) and outro (if percussion)
#     0 means no intro/outro
# -perc level
#     can choose a percussion level from none through gentle to full/house
#     If not "none" may imply alignment of periods such as whole years to bars.
#     If not "none" may produce fancier presentation of the data elements.
# OFFSET
#     added to a zero input value to make the MIDI note.
# INSTRUMENT 
#     selects the primary MIDI data/melody instrument number, eg 47 for harp.


##########
# May be used / adapted / etc without any promise of fitness for purpose
# under the terms of the Apache License Version 2.0, January 2004
#     http://www.apache.org/licenses/LICENSE-2.0
##########


# TODO: insert hiss for missing values (across all sources)?


# Sample CSV input:
##YYYY-MM,device,coverage,gen,device,coverage,gen,device,coverage,gen
##input,"data/consolidated/energy/std/gen/M/Enphase/gen-M-Enphase.csv"
##input,"data/consolidated/energy/std/gen/M/meter/gen-M-meter.csv"
##input,"data/consolidated/energy/std/gen/M/SunnyBeam/gen-M-SunnyBeam.csv"
#2008-02,,,,meter,1,4,SunnyBeam,0.142857,3.54
#2008-03,,,,meter,1,70,SunnyBeam,1,68.55
#2008-04,,,,meter,1,108,SunnyBeam,1,106.13
#2008-05,,,,meter,1,135,SunnyBeam,1,134.03
#2008-06,,,,meter,1,160,SunnyBeam,1,158.1
#2008-07,,,,meter,1,161,SunnyBeam,1,146.12
#...


# Model format 1 MIDI file...
#0, 0, Header, 1, 2, 480
#1, 0, Start_track
#1, 0, Title_t, "Close Encounters"
#1, 0, Text_t, "Sample for MIDIcsv Distribution"
#1, 0, Copyright_t, "This file is in the public domain"
#1, 0, Time_signature, 4, 2, 24, 8
#1, 0, Tempo, 500000
#1, 0, End_track
#2, 0, Start_track
#2, 0, Instrument_name_t, "Church Organ"
#2, 0, Program_c, 1, 19
#2, 0, Note_on_c, 1, 79, 81
#2, 960, Note_off_c, 1, 79, 0
#2, 960, Note_on_c, 1, 81, 81
#2, 1920, Note_off_c, 1, 81, 0
#2, 1920, Note_on_c, 1, 77, 81
#2, 2880, Note_off_c, 1, 77, 0
#2, 2880, Note_on_c, 1, 65, 81
#2, 3840, Note_off_c, 1, 65, 0
#2, 3840, Note_on_c, 1, 72, 81
#2, 4800, Note_off_c, 1, 72, 0
#2, 4800, End_track
#0, 0, End_of_file

# Start of model format 1 MIDI file...
#0, 0, Header, 1, 14, 192
#1, 0, Start_track
#1, 0, Tempo, 769230
#1, 0, Time_signature, 2, 2, 24, 8
#1, 56832, Tempo, 389610
#1, 78720, Tempo, 769230
#1, 262143, End_track
#2, 0, Start_track
#2, 0, Text_t, "Lead Vox"
#2, 170, Control_c, 5, 121, 0
#2, 242, Control_c, 5, 0, 0
#2, 244, Control_c, 5, 32, 0
#2, 246, Program_c, 5, 60


# Set a seed n>0, -1 means dynamic/new each time, 0 means data-derived.
TSEED=0
if [ $# -ge 2 -a "-seed" = "$1" ]; then
    TSEED=$2;
    shift;
    shift;
fi

# Heterogeneous or homogeneous variables / datastreams.
HET=false
if [ "-het" = "$1" ]; then
    HET="true"
    shift
fi

# Intro bars (default none).
INTRO="0"
if [ $# -ge 2 -a "-intro" = "$1" ]; then
    INTRO=$2;
    shift;
    shift;
fi

# Percussion level (default none).
PERC="none"
if [ $# -ge 2 -a "-perc" = "$1" ]; then
    PERC=$2;
    shift;
    shift;
fi
#echo "PERC: $PERC" 1>&2

SRCCSV=$1
if [ "" = "$SRCCSV" -o ! -s "$SRCCSV" ]; then
    echo "ERROR: input file must be specified." 1>&2
    exit 1
fi

OFFSET=60
if [ $# -gt 1 ]; then
    OFFSET="$2";
    if [ "$OFFSET" -lt 1 -o "$OFFSET" -gt 120 ]; then
        echo "ERROR: OFFSET ($OFFSET) invalid." 1>&2
        exit 1
    fi
    shift
fi

# Chose primary instrument.
# TODO: this voice plays the main data source, at highest volume.
INSTRUMENT=67 # Tenor sax(GB)
#INSTRUMENT=80 # Flute solo (GB)
#INSTRUMENT=55 # Dream voice
#INSTRUMENT=63 # Bright synth brass (GB)
#INSTRUMENT=82 # Soft saw lead (GB)
#INSTRUMENT=54
#INSTRUMENT=16
#INSTRUMENT=108
if [ $# -gt 1 ]; then
    INSTRUMENT="$2";
    if [ "$INSTRUMENT" -lt 1 -o "$INSTRUMENT" -gt 128 ]; then
        echo "ERROR: INSTRUMENT ($INSTRUMENT) invalid." 1>&2
        exit 1
    fi
    shift
fi


# Data cadence in source file.
CADENCE="UNKNOWN"
# Extract cadence from this file.
case $SRCCSV in
    *-D.csv) CADENCE=D;;
    *-M.csv) CADENCE=M;;
    *-Y.csv) CADENCE=Y;;
esac
#echo CADENCE: $CADENCE 1>&2


# Extract number of sources in this CSV.
NSOURCES="`awk -F, < $SRCCSV '/^2/{print int((NF-1)/3);exit}'`"
#echo NSOURCES: $NSOURCES 1>&2
# Extract the highest data value.
#MAXVAL="`awk -F, < $SRCCSV '/^2/{for(i=4;i<=NF;i+=3){if($i>m){m=$i}}}END{print 0+m}'`"
MAXVAL="`sh script/mkaudio/house/lib_consolidated_max.sh $SRCCSV`"
#echo MAXVAL: $MAXVAL 1>&2
# Extract the minimum (most negative) data value.
MINVAL="`awk -F, < $SRCCSV '/^2/{for(i=4;i<=NF;i+=3){if($i<m){m=$i}}}END{print 0+m}'`"
#echo MINVAL: $MINVAL 1>&2
# Extract the data set with the maximum number of data points (or 0/"").
MAXNVAL="`sh script/mkaudio/house/lib_consolidated_maxn.sh $SRCCSV`"
#echo MAXNVAL: $MAXNVAL 1>&2


# Force output to C locale (avoid UTF-8 for example).
LANG=C
export LANG

# This may need to make multiple passes over the input file.
gawk -F, </dev/null \
    -v SRCCSV=$SRCCSV \
    -v BASENAME="`basename $SRCCSV .csv`" \
    -v SCRIPTBASENAME="`basename $0 .sh`" \
    -v CADENCE=$CADENCE \
    -v NSOURCES=$NSOURCES -v MAXVAL=$MAXVAL -v MINVAL=$MINVAL \
    -v MAXNVAL=$MAXNVAL \
    -v TSEED=$TSEED \
    -v HET=$HET \
    -v INTRO=$INTRO \
    -v PERC=$PERC \
    -v OFFSET=$OFFSET \
    -v INSTRUMENT=$INSTRUMENT \
    -f script/mkaudio/house/lib_perc_general.awk \
    -f script/mkaudio/house/lib_bass_simple.awk \
    -e 'BEGIN {
         MAXSOURCES=4; # Maximum number of sources/channels that we can use.

         SOURCES=NSOURCES;
         if(SOURCES > MAXSOURCES) { SOURCES=MAXSOURCES; }

         # If dynamic seed (<0) is requested, make it now for this run.
         tseed = TSEED; # tseed is internal value: TSEED is left as-is.
         if(TSEED < 0) { seed(); tseed = rand(); }

         # True if input is heterogeneous data set to contrast, eg imp and exp.
         isHet = 0;
         if("true" == HET) { isHet = 1; }

         # Start with one tempo / conductor track.
         TRACKS=1;

         # One track per data stream.
         FIRSTDATATRACK=2;
         TRACKS += SOURCES;

         # Has a percussion track if PERC != none.
         doingPerc=0;
         if("none" != PERC) { doingPerc=1; }
         if(doingPerc) { ++TRACKS; }
         PERCTRACK=TRACKS;

         # Adjust a few parameters for full-on house.
         isHouse=0;
         if("house" == PERC) { isHouse=1; }

         # If doing perc then doing base.
         doingBass = 0;
         if(doingPerc) { doingBass = 1; }
         if(doingBass) { ++TRACKS; }
         BASSTRACK=TRACKS;

         # If doing percussion, there will nominally be some alignment.
         doingAlign = 0;
         if(doingPerc) { doingAlign = 1; }

         # If doing any percussion then the melody can be made a bit fancier.
         fancyMelody = 0;
         if(doingPerc) { fancyMelody = 1; }

         TEMPO=500000; # 120bpm (500uS per quarter note / beat).

         # Intervals per
         #    https://www.guitarland.com/Music10/FGA/LectureMIDIscales.html
         # Sums to 12, so last is essentially a redundant check digit!
         SCALE_MAJOR="2,2,1,2,2,2,1";
         # Defaults to no scale (all MIDI notes available).
         scale=""
         if(fancyMelody) { scale = SCALE_MAJOR; }

         OCTAVES = 2;
         n = parseScale(scale, offsetArray, OCTAVES);
         RANGE = n * OCTAVES; # Note range index on +ve scale.
         # Basic scaling of input value to note, to just *within* RANGE octaves.
         multScaling=1;
         if(MAXVAL > 0) { multScaling=(RANGE-1)/MAXVAL; }

         #ALTVOLUME=63; # Fallback/secondary note volume.
         #NOTEVOLUME=127; # Main note (max) volume.
         ALTVOLUME=48; # Fallback/secondary note volume.
         NOTEVOLUME=63; # Main note (max) volume.

         CLKSPQTR=480; # clock pulses per quarter note
         BeatsPerBar = 4;
         BAR=BeatsPerBar*CLKSPQTR; # bar duration
         INTROclks=BAR*INTRO
         # A phrase is 1 bar, or intro/outro length if set.
         PhraseClks=(INTRO < 1) ? BAR : INTROclks;
         # 4 data points/notes per bar by default.
         NotesPerBar = BeatsPerBar;
         if("M" == CADENCE) {
             # 12 notes (1 year) per bar for monthly data.
             NotesPerBar = 12;
         } else if("D" == CADENCE) {
             # 32 notes (~1 month) per bar for daily data.
             NotesPerBar = 32;
             }
         NoteDeltaTime=CLKSPQTR / (NotesPerBar/BeatsPerBar);

         doAVE = 0;
         # For running averages, take 1 bar by default.
         AVEcount = NotesPerBar;
         #if("M" == CADENCE) {
         #    # 12 (1 year) for monthly data.
         #    AVEcount = 12;
         #} else if("D" == CADENCE) {
         #    # 7 (~1 week) per bar for daily data.
         #    AVEcount = 7;
         #    }
         # Assume 0 is the mean/return/default state.
         AVEdata=0;
         # Initial coverage is taken to be 100%.
         AVEcoverage=1;

# Just the title and tempo etc metadata in track 1.
print "0, 0, Header, 1, "TRACKS",", CLKSPQTR; # MIDI format 1, tracks, clks/qtr.
print "1, 0, Start_track";
print "1, 0, Title_t, \""BASENAME"\"";
print "1, 0, Text_t, \"Params: intro="INTRO", perc="PERC"\"";
print "1, 0, Text_t, \"Script: "SCRIPTBASENAME"\"";
if(0 != tseed) { print "1, 0, Text_t, \"tseed: "tseed"\""; }
print "1, 0, Copyright_t, \"Autogenerated output released as CC0 / public domain.\"";
print "1, 0, Tempo, "TEMPO;
print "1, 0, Time_signature, 4, 2, 24, 8";

# Compute any extra delay of first data for alignment.
# Insert markers based on (first) data track.
            alignedFirst=0;
            datastartclocks=INTROclks; # Clock pulses since start for data.
            clocks = datastartclocks;
            while((getline < SRCCSV) > 0) {
                if($1 ~ /^#/) { continue; } # Skip comment rows.

    # Do any initial data alignment, eg to bar start.
    if(doingAlign && !alignedFirst) {

        if(("M" == CADENCE) && (0 == match($1, /-01$/ ))) {
            # First data is not month 01.

            # Insert enough pause so that the next 01 will start a bar.
            # If this were month 1, we would nominally insert 0 notes pause.
            # If this is month 12, we nominally insert 11 notes pause.
            # If this is month 11, we nominally insert 10 notes pause.
            # ...
            # If this is month n, we nominally insert n-1 notes pause.
            split($1, a, "-");
            monthNow = 0+a[2];
            pauseNotes = monthNow - 1;
            clocks += (pauseNotes * NoteDeltaTime);
            datastartclocks += (pauseNotes * NoteDeltaTime);

            # Insert a marker for the actual start month.
print "1, "clocks", Marker_t, \""$1"\"";
            }

        alignedFirst=1;
        }

    # Insert a marker when ls unit is "01", or always for yearly.
    if(("Y" == CADENCE) || (0 != match($1, /-01$/ ))) {
print "1, "clocks", Marker_t, \""$1"\"";
        }

                clocks += NoteDeltaTime;
                }
            close(SRCCSV);

            # Note the data length/end.
            dataendclocks = clocks;

print "1, " clocks ", End_track";

# Separate channels (and tracks) for each data voice.
# Lowest channel used is 1.
          # Must be at least as many instruments as NSOURCES / MAXSOURCES.
          # Secondary instrument, eg for all homo secondary data streams.
          INSTRUMENT2 = 63; # Bright synth brass (GB)
          INSTRUMENT3 = 55; # Dream voice
          INSTRUMENT4 = 82; # Soft saw lead (GB)
          instrument[1] = isHouse ? INSTRUMENT4 : INSTRUMENT;
          instrument[2] = INSTRUMENT2;
          instrument[3] = isHet ? INSTRUMENT3 : INSTRUMENT2;
          instrument[4] = isHet ? INSTRUMENT4 : INSTRUMENT2;

          if(MAXNVAL > 1) {
              # Swap primary voice to primary channel.
              tmp = instrument[1];
              instrument[1] = instrument[MAXNVAL];
              instrument[MAXNVAL] = tmp;
              }

        # Old 'average' note, if any.
        AVENOTE = -1

        # GENERATE A TRACK FOR EACH DATA SOURCE VOICE.
        # Channels number from 0, tracks from FIRSTDATATRACK.
        for(i = 1; i <= SOURCES; ++i) {
            ti = (FIRSTDATATRACK +i-1); # Track index.
            ci = i - 1; # Channel index.
print ti ", 0, Start_track";
            # Set the instrument.
            # All data voices are the same without fancyMelody.
print ti ", 0, Program_c, "ci", " (fancyMelody ? (instrument[i]-1) : (INSTRUMENT-1));
            # Set the volume.
            # Have the channel with the preferred data set be loudest.
print ti ", 0, Control_c, "ci", 7, " (""+((isHouse?0.5:1)*(((i==MAXNVAL) || isHet)?NOTEVOLUME:ALTVOLUME)));
            # Pan the voices around the available space.
            # Use just off-centre centre when one track,
            # or a preferred homogenous data track.
            if((SOURCES < 2) || (!isHet && (i==MAXNVAL))) {
                pan = 70;
            } else {
                if(isHet) {
                    # If het, pan evenly across entire space.
                    pan = int(127 * ((i-1) / (SOURCES-1)));
                } else {
                    # If homo, pan spread out on other side from preferred.
                    pan = int(60 * ((i-1) / (SOURCES-1)));
                    }
            }
print ti ", 0, Control_c, "ci", 10, "pan;

            # Start the clock for data.
            clocks=datastartclocks;

            # Since pre-incremented at start of loop, wind clock back here.
            clocks -= NoteDeltaTime;
            # Convert data stream to MIDI voice.
            DEVNAME="";
            while((getline < SRCCSV) > 0) {
                # Skip comment rows.
                if($1 ~ /^#/) { continue; }

                # Advance the clock for anything other than a comment row.
                clocks += NoteDeltaTime;

                if("" == DEVNAME) {
                    # Extract first non-empty device name from data stream
                    # as track title.
                    # Must be no data points before this.
                    DEVNAME=$(-1 + 3*i);
if("" != DEVNAME) { print ti ", 0, Title_t, \"source: "DEVNAME"\""; } else { continue; }

                    # About to play the first note from this stream.
                    # Wind back to the start of the phrase
                    # and insert a holding note.
                    # Use octave below 'base' note.
                    # TODO: do some foreshadowing?
                    phraseNum = int(clocks/PhraseClks);
                    startOfPhrase = phraseNum * PhraseClks;
                    if(doingAlign && (startOfPhrase < clocks)) {
print ti ", "startOfPhrase", Note_on_c, "ci", "(OFFSET-12)", 1";
print ti ", "clocks", Note_off_c, "ci", "(OFFSET-12)", 0";
                        }
                    }

                # Insert 'mean' voice for each beat in the bar.
                # Insert the 'mean' voice for data before this point.
                # (Also lets the mean voice lose gently if same as main voice.)
                if(doAVE && (0 == (clocks % CLKSPQTR))) {
                    # Stop any previous mean note.
if(AVENOTE > 0) { print ti ", "clocks", Note_off_c, "ci", "AVENOTE", 0"; }
                    # Down an octave from main voice.
                    scaled = int(multScaling * AVEdata);
                    AVENOTE = scaled + OFFSET - 12;
                    vol = (((i==MAXNVAL) || isHet)?ALTVOLUME:(ALTVOLUME/2));
                    # Adjust volume alongside pitch [50%,100].
                    vol *= (0.5 + (0.5 * (AVEdata / MAXVAL)));
                    # Reduce volume further for identically-zero points.
                    if(0 == AVEdata) { vol *= 0.25; }
                    # Reduce volume for low/unknown coverage points.
                    if(AVEcoverage < 1) { AVEvol *= AVEcoverage; }
print ti ", "clocks", Note_on_c, "ci", "AVENOTE", "int(vol);
                    }

                # Note on where data is present.
                dataRaw = $(1 + 3*i);
                coverageRaw = $(0 + 3*i);
                # Cope with non-numeric content!
                data = 0 + dataRaw;
                coverage = 0 + coverageRaw;
                AVEdata = (data + (AVEcount-1)*AVEdata) /AVEcount;
                AVEcoverage = (coverage + (AVEcount-1)*AVEcoverage) /AVEcount;
                if("" == data) { continue; } # Missing data point.
                scaled = int(multScaling * data);
                if("" == scale) { NOTE = scaled + OFFSET; } else {
                    if(scaled < 0) { continue; }
                    NOTE = OFFSET + offsetArray[1 + scaled];
                    }
                if((NOTE < 1) || (NOTE > 126)) { continue; }
                vol = (((i==MAXNVAL) || isHet)?NOTEVOLUME:ALTVOLUME);
                # Crank down melody volume a bit in "house" mode.
                if(isHouse) { vol *= 0.9; }
                # Adjust volume alongside pitch [50%,100] while non-negative.
                if(data >= 0) { vol *= (0.5 + (0.5 * (data / MAXVAL))); }
                # Reduce volume further for identically-zero points.
                if(0 == data) { vol *= 0.25; }
                # Reduce volume for low/unknown coverage points.
                if(coverage <= 0) { continue; }
                if(coverage < 1) { vol *= coverage; }
print ti ", "clocks", Note_on_c, "ci", "NOTE", "int(vol);
print ti ", "(clocks+NoteDeltaTime-1)", Note_off_c, "ci", "NOTE", 0";
                }
            close(SRCCSV);

            clocks += NoteDeltaTime;

            # Before ending track,
            # play out holding note quietly to end of phrase if needed.
            # Use octave below 'base' note.
            # TODO: Use 'average' note, or min/mean/max?
            phraseNum = int((clocks+NoteDeltaTime-1)/PhraseClks);
            endOfPhrase = (phraseNum * PhraseClks) + (PhraseClks-1);
            if(doingAlign && (endOfPhrase > clocks)) {
print ti ", "clocks", Note_on_c, "ci", "(OFFSET-12)", 1";
print ti ", "endOfPhrase", Note_off_c, "ci", "(OFFSET-12)", 0";
                clocks = endOfPhrase+1;
                }

print ti ", " clocks ", End_track";
            }

        }

    END {
    # Insert percussion if any, as separate track.
    # Generate enough full bars until longer than the data track + outro.
    stopAt = dataendclocks + INTROclks;
    if(doingPerc && (INTROclks > 0)) {
        # Push out to leave space for data melody of multiple of intro/outro.
        stopAt = int((stopAt + INTROclks - 1) / INTROclks) * INTROclks;
        }
    if(doingPerc) { perc_general(tseed, NOTEVOLUME, PERCTRACK, PERC, "", stopAt, TEMPO, CLKSPQTR); }

    # Percussion type also determines the bess.
    BASSCHANNEL = 10; # Right after the percussion channel.
    if(doingBass) { bass_simple(tseed, NOTEVOLUME, BASSTRACK, BASSCHANNEL, OFFSET, PERC, "", stopAt, TEMPO, CLKSPQTR); }

    # End MIDI file.
print "0, 0, End_of_file";
    }

    # Support functions.
    # Some may be farmed out into libraries in due course.

    # Parse intervals string for one octave in form "2,2,1,2,2,2,1"
    # and fill array for nOctaves (default/min 1) as semitone offsets from root.
    # (This does not clear any existing entries in offsetArray.)
    # This returns the number of intervals/notes per octave in this scale.
    # The input values should sum to 12.
    # With an empty input string this generates as if 12 "1"s.
    # The offsetArray is filled in from indexes starting at 1, per awk.
    function parseScale(intervalsStr, offsetArray, nOctaves,
        # Locals
        i, j, a, n, intervalTotal, oAIndex)
        {
        if("" == intervalsStr) { intervalsStr = "1,1,1,1,1,1,1,1,1,1,1,1"; }
        if(!(nOctaves >= 1)) { nOctaves = 1; }
        n = split(intervalsStr, a, ",");
        intervalTotal = 0;
        for(i = 1; i <= n; ++i) {
            intervalTotal += a[i];
            }
        if(12 != intervalTotal) {
            # Error
            nOctaves[1] = 12;
            return(1);
            }

        oAIndex = 0;
        offset = 0;
#print n, intervalsStr >> "tmp.txt"
        for(i = 1; i <= nOctaves; ++i) {
            for(j = 1; j <= n; ++j) {
                offsetArray[++oAIndex] = offset;
#print oAIndex, offset >> "tmp.txt"
                offset += a[j];
                }
#print "EOO" >> "tmp.txt"
            }

        return(n);
        }

    ' | \
csvmidi

exit 0
