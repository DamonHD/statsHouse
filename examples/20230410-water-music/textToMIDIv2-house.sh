#!/bin/sh

# Converts value-per-line temperature (C) file to simple MIDI output as notes.
# Input value must be blank for silence, or 0-127 for note.
# Output is MIDI binary format 1 c/o MIDICSV utility.
# May optionally have (house-style) percussion track alongside.

# Usage:
#     $0 in.csv [-intro bars] [-perc [none|gentle|house]] [OFFSET [INSTRUMENT]]
#
# -intro bars
#     adds an intro (with percussion if supplied) and outro (if percussion)
# -perc level
#     can choose a percussion level from none through gentle to full/house
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

awk <$IN \
    -v INTRO=$INTRO \
    -v PERC=$PERC \
    -v LENGTH=$LENGTH \
    -v OFFSET=$OFFSET \
    -v INSTRUMENT=$INSTRUMENT \
        'BEGIN {
         # Has a percussion track if PERC != none.
         TRACKS=1;
         if("none" != PERC) { TRACKS=2; }

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
 
print "0, 0, Header, 1, "TRACKS",", CLKSPQTR; # MIDI format 1, tracks, clks/qtr.
print "1, 0, Start_track";
print "1, 0, Tempo, "TEMPO;
print "1, 0, Time_signature, 4, 2, 24, 8";
print "1, 0, Program_c, 0,", (INSTRUMENT-1); # Channel 0 select instrument.
        }
    {
    NOTE=$1+OFFSET

#1, 0, Note_on_c, 0, 49, 32
#1, 48, Note_off_c, 0, 49, 0
#1, 48, Note_on_c, 0, 49, 32
#1, 96, Note_off_c, 0, 49, 0
#1, 96, Note_on_c, 0, 49, 32

    if(("" == $1) || (NOTE > 127)) {
        # Missing data point: use alternate note and volume.
print "1, "clocks", Note_on_c, 0, "ALTNOTE", "ALTVOLUME;
print "1, "(clocks+NoteDeltaTime-1)", Note_off_c, 0, "ALTNOTE", 0";
        } else {
print "1, "clocks", Note_on_c, 0, "NOTE", "NOTEVOLUME;
print "1, "(clocks+NoteDeltaTime-1)", Note_off_c, 0, "NOTE", 0";
        }

    clocks += NoteDeltaTime;
    }
    END {
print "1,", clocks ", End_track";

    if(TRACKS > 1) {
        # Append separate percussion track if requested.
# Channel 1 percussion.
PERCHAN=9
# Percussion 1, defaults to kick drum.
PERC1=36
PERC1VOL=NOTEVOLUME
if("gentle" == PERC) { PERC1VOL=PERC1VOL/2; }
PERC1VOLO=int(PERC1VOL * 0.8)
# Percussion 2, defaults to clap.
PERC2=39
PERC2VOL=100
if("gentle" == PERC) { PERC2VOL=1; }
# Percussion 3, defaults to (open) hi hat.
PERC3=46
PERC3VOL=64
if("gentle" == PERC) { PERC3VOL=1; }

print "2, 0, Start_track";
print "2, 0, Tempo, "TEMPO;
print "2, 0, Time_signature, 4, 2, 24, 8";

        # Generate enough full bars until longer than the data track + outro.
        stopAt = clocks + INTROclks;
        for(t = 0; t < stopAt; t += (4 * CLKSPQTR)) {
print "2, "(t           )", Note_on_c, "PERCHAN", "PERC1", "PERC1VOL;
print "2, "(t+0.5*CLKSPQTR)", Note_on_c, "PERCHAN", "PERC3", "PERC3VOL;
print "2, "(t+1*CLKSPQTR)", Note_off_c, "PERCHAN", "PERC3", 0";
print "2, "(t+1*CLKSPQTR)", Note_off_c, "PERCHAN", "PERC1", 0";

print "2, "(t+1*CLKSPQTR)", Note_on_c, "PERCHAN", "PERC1", "PERC1VOLO;
print "2, "(t+1*CLKSPQTR)", Note_on_c, "PERCHAN", "PERC2", "PERC2VOL;
print "2, "(t+1.5*CLKSPQTR)", Note_on_c, "PERCHAN", "PERC3", "PERC3VOL;
print "2, "(t+2*CLKSPQTR)", Note_off_c, "PERCHAN", "PERC3", 0";
print "2, "(t+2*CLKSPQTR)", Note_off_c, "PERCHAN", "PERC2", 0";
print "2, "(t+2*CLKSPQTR)", Note_off_c, "PERCHAN", "PERC1", 0";

print "2, "(t+2*CLKSPQTR)", Note_on_c, "PERCHAN", "PERC1", "PERC1VOLO;
print "2, "(t+2.5*CLKSPQTR)", Note_on_c, "PERCHAN", "PERC3", "PERC3VOL;
print "2, "(t+3*CLKSPQTR)", Note_off_c, "PERCHAN", "PERC3", 0";
print "2, "(t+3*CLKSPQTR)", Note_off_c, "PERCHAN", "PERC1", 0";

print "2, "(t+3*CLKSPQTR)", Note_on_c, "PERCHAN", "PERC1", "PERC1VOLO;
print "2, "(t+3*CLKSPQTR)", Note_on_c, "PERCHAN", "PERC2", "PERC2VOL;
print "2, "(t+3.5*CLKSPQTR)", Note_on_c, "PERCHAN", "PERC3", "PERC3VOL;
print "2, "(t+4*CLKSPQTR)", Note_off_c, "PERCHAN", "PERC3", 0";
print "2, "(t+4*CLKSPQTR)", Note_off_c, "PERCHAN", "PERC2", 0";
print "2, "(t+4*CLKSPQTR)", Note_off_c, "PERCHAN", "PERC1", 0";
            }

print "2, "t", End_track";
        }

print "0, 0, End_of_file";
    }' | \
csvmidi
