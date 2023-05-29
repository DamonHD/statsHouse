#!/bin/sh

# Converts value-per-line temperature (C) file to simple MIDI output as notes.
# Input value must be blank for silence, or 0-127 for note.
# Output is MIDI binary format 1 c/o MIDICSV utility.
# May optionally have an extra (house-style) percussion track alongside.

# Usage:
#     $0 in.csv [-intro bars] [-perc [none|gentle|house]] [OFFSET [INSTRUMENT]]
#
# -intro bars
#     adds an intro (with percussion if supplied) and outro (if percussion)
# -perc level
#     can choose a percussion level from none through gentle to house (full)
# OFFSET
#     is the value added to a zero value to make the MIDI note.
# INSTRUMENT 
#     is the MIDI instrument number, eg 47 for harp.


##########
# May be used / adapted / etc without any promise of fitness for purpose
# under the terms of the Apache License Version 2.0, January 2004
#     http://www.apache.org/licenses/LICENSE-2.0
##########


# Sample input:
#
#23
#24
# 
#25


# MIDICSV input sample from start:
#0, 0, Header, 1, 18, 384
#1, 0, Start_track
#1, 0, Tempo, 472440
#1, 0, Time_signature, 4, 2, 24, 8
#1, 15360, Tempo, 461530
#1, 124416, Tempo, 521744
#1, 127488, Tempo, 461530
#1, 172416, Tempo, 483862
#1, 175104, Tempo, 500000
#1, 175872, Tempo, 612240
#1, 176064, Tempo, 500000
#1, 176640, Tempo, 491800
#1, 210432, End_track
#2, 0, Start_track
#2, 0, Title_t, "piano"
#2, 384, Control_c, 0, 0, 0
#2, 388, Control_c, 0, 32, 3
#2, 392, Program_c, 0, 1
#2, 396, Control_c, 0, 7, 115
#...

# Alternate MIDICSV input sample from start:
#0, 0, Header, 0, 1, 96
#1, 0, Start_track
#1, 0, Time_signature, 4, 2, 24, 8
#1, 0, Tempo, 500000
#1, 0, Program_c, 0, 79
#1, 0, Note_on_c, 0, 49, 32
#1, 48, Note_off_c, 0, 49, 0
#1, 48, Note_on_c, 0, 49, 32
#1, 96, Note_off_c, 0, 49, 0
#1, 96, Note_on_c, 0, 49, 32
#...

# Alternate MIDICSV input sample from start:
#0, 0, Header, 1, 1, 480
#1, 0, Start_track
#1, 0, Title_t, "min0v0"
#1, 0, Tempo, 500000
#1, 0, Note_on_c, 9, 36, 127
#1, 240, Note_on_c, 9, 46, 64
#1, 480, Note_off_c, 9, 46, 0
#1, 480, Note_off_c, 9, 36, 0
#1, 480, Note_on_c, 9, 36, 127
#...


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

IN=$1
if [ "" = "$IN" -o ! -s "$IN" ]; then
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

INSTRUMENT=80
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

LENGTH=`wc -l < $IN | awk '{print $1}'`


# Write MIDI (format 1) binary directly to stdout.
# For format see eg:
#     http://www.music.mcgill.ca/~ich/classes/mumt306/StandardMIDIfileformat.html
#     https://www.csie.ntu.edu.tw/~r92092/ref/midi/

# Force output to C locale (avoid UTF-8 for example).
LANG=C
export LANG

gawk <$IN \
    -v BASENAME="`basename $IN .csv`" \
    -v SCRIPTBASENAME="`basename $0 .sh`" \
    -v INTRO=$INTRO \
    -v PERC=$PERC \
    -v LENGTH=$LENGTH \
    -v OFFSET=$OFFSET \
    -v INSTRUMENT=$INSTRUMENT \
    -f script/mkaudio/house/lib_perc_general.1p0.awk \
    -e 'BEGIN {
         # Minimum is tempo track plus melody track.
         TRACKS=2;

         # Has a percussion track if PERC != none.
         doingPerc=0;
         if("none" != PERC) { doingPerc=1; }
         if(doingPerc) { ++TRACKS; }
         PERCTRACK=TRACKS;

         # Adjust a few parameters for full-on house.
         isHouse=0;
         if("house" == PERC) { isHouse=1; }

         TEMPO=500000; # 120bpm (500uS per quarter note / beat).

         ALTNOTE=49; # Fallback when data missing.
         #NOTEVOLUME=64; # Main note volume.
         #ALTVOLUME=32; # Fallback note volume.
         NOTEVOLUME=127; # Main note volume.
         ALTVOLUME=64; # Fallback note volume.

         CLKSPQTR=480; # clock pulses per quarter note
         BAR=4*CLKSPQTR; # bar duration
         # 12 notes (1 year) per bar.
         NoteDeltaTime=CLKSPQTR / 3;
         INTROclks=BAR*INTRO

         clocks=INTROclks; # Clock pulses since of main sonified data.

# Just the title and tempo etc metadata in track 1.
print "0, 0, Header, 1, "TRACKS",", CLKSPQTR; # MIDI format 1, tracks, clks/qtr.
print "1, 0, Start_track";
print "1, 0, Title_t, \""BASENAME"\"";
print "1, 0, Text_t, \"Script: "SCRIPTBASENAME"\"";
print "1, 0, Copyright_t, \"Autogenerated output released as CC0 / public domain.\"";
print "1, 0, Tempo, "TEMPO;
print "1, 0, Time_signature, 4, 2, 24, 8";
print "1, 1, End_track";
 
print "2, 0, Start_track";
print "2, 0, Program_c, 0,", (INSTRUMENT-1); # Channel 0 select instrument.
# Turn down the melody volume wrt percussion for house.
print "2, 0, Control_c, 2, 7, " (""+int(127*(isHouse?0.5:1)));
# Pan a little off to one side.
print "2, 0, Control_c, 2, 10, "50;
        }
    {
    NOTE=$1+OFFSET

    if(("" == $1) || (NOTE > 127)) {
        # Missing data point: use alternate note and volume.
print "2, "clocks", Note_on_c, 2, "ALTNOTE", "ALTVOLUME;
print "2, "(clocks+NoteDeltaTime-1)", Note_off_c, 2, "ALTNOTE", 0";
        } else {
print "2, "clocks", Note_on_c, 2, "NOTE", "NOTEVOLUME;
print "2, "(clocks+NoteDeltaTime-1)", Note_off_c, 2, "NOTE", 0";
        }

    clocks += NoteDeltaTime;
    }

    END {
print "2,", clocks ", End_track";

    # Insert percussion if any, as separate track.
    # Generate enough full bars until longer than the data track + outro.
    stopAt = clocks + INTROclks;
    hints["intro"] = INTRO;
    if(doingPerc && (INTROclks > 0)) {
        # Push out to leave space for data melody of multiple of intro/outro.
        stopAt = int((stopAt + INTROclks - 1) / INTROclks) * INTROclks;
        }
    if(doingPerc) { perc_general(0, NOTEVOLUME, PERCTRACK, PERC, hints, stopAt, TEMPO, CLKSPQTR); }

print "0, 0, End_of_file";
    }' | \
csvmidi

exit 0



# Partial changelog:

# V2.1:
#   * Backporting various fixes/improvements from V4.x.
#   * Use script/mkaudio/house/lib_perc_general.1p0.awk for percussion.
#   * Separate tempo track from melody to make output 'legal' format 1.
#   * Include script name and rights disclaimer in metadata.

# V2.0 original.
# 2023-05-028 copied to textToMIDIv2p0-house.sh.