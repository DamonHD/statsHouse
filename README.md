# statsHouse

[![Java CI](https://github.com/DamonHD/statsHouse/actions/workflows/ant.yml/badge.svg)](https://github.com/DamonHD/statsHouse/actions/workflows/ant.yml)

Turning home (energy) stats into house music mechanically: sonification.

Converting homogeneous and heterogeneous (CSV) data sets into MIDI.

One driver is entirely automated (but useful, interesting) output
generated in (slowish) real time as data series are updated.

The aim is to have at least three levels of 'production':
   * none: the data is sonified plainly, for sciencing
   * gentle: some gentle percussion and other musical effects added
   * house: aiming to sound like a grown-up house track

Adding samples and SuperCollider treatment is in scope.

Creating audio samples [direct from data values](https://www.earth.org.uk/statscast-202005.html) is also in scope.

See [Earth Notes Sonification](https://www.earth.org.uk/sonification.html).

Contributors may have different implementation language preferences: (g)awk is present!

[MIDICSV](https://www.fourmilab.ch/webtools/midicsv/) allows a useful intermediate lossless ASCII CSV representation.

This Java implementation version numbering starts at 5.0.0 so as to follow the awk/MIDICSV V4.x implementations at:
    [https://www.earth.org.uk/script/mkaudio/house/textToMIDIv4*](https://www.earth.org.uk/script/mkaudio/house/)


An example reference house mix: https://soundcloud.com/user-472988235-244983752/mix-01-house
Also: https://www.nts.live/infinite-mixtapes/4-to-the-floor
