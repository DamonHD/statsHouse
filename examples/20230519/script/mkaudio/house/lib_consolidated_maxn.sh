#!/bin/sh

# Extract from consolidated-format CSV $1 the set/stream with the most points.
# 1 for the first/leftmost data stream, 2 for the next, etc.
# May return blank or zero if no data.

# Sample input:
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

exec awk -F, < $1 '
    /^2/{for(i=4;i<=NF;i+=3){if($i!=""){++c[(i-1)/3]}}}
    END {
        maxcol=0;
        maxcolcount=0;
        for(key in c) {
            count = c[key];
#print key, count
            if(count > maxcolcount) {
                maxcolcount = count;
                maxcol = key;
                }
            }
        print maxcol;
    }
    '
