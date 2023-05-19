#!/bin/sh

# Run chosen script with fixed parameters for dev/testing.

##########
# May be used / adapted / etc without any promise of fitness for purpose
# under the terms of the Apache License Version 2.0, January 2004
#     http://www.apache.org/licenses/LICENSE-2.0
##########

DATETIME="`date -u +%Y%m%dT%H%MZ`"
echo Run at: $DATETIME

sh script/mkaudio/house/textToMIDIv4p2-consolidated.sh -intro 0 -perc none data/consolidated/energy/std/imp/M/imp-M.csv > $HOME/tmp/run.$DATETIME.mid
#sh script/mkaudio/house/textToMIDIv4p2-consolidated.sh -intro 0 -perc gentle data/consolidated/energy/std/imp/M/imp-M.csv > $HOME/tmp/run.$DATETIME.mid
#sh script/mkaudio/house/textToMIDIv4p2-consolidated.sh -intro 4 -perc house data/consolidated/energy/std/imp/M/imp-M.csv > $HOME/tmp/run.$DATETIME.mid

ls -al $HOME/tmp/run.$DATETIME.mid
rm -f $HOME/tmp/latest.mid
ln -s $HOME/tmp/run.$DATETIME.mid $HOME/tmp/latest.mid
ls -al $HOME/tmp/latest.mid
