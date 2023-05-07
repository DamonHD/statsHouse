# statsHouse

Turning home (energy) stats into house music mechanically: sonification.

Converting homogeneous and heterogeneous (CSV) data sets into MIDI.

One driver is entirely automated (but useful, interesting) output
generated in (slowish) real time as data series are updated.

The aim is to have at least three levels of 'production':
   * none: the data is sonified plainly, for sciencing
   * gentle: some gentle percussion and other musical effects added
   * house: aiming to sound like a grown-up house track

Adding samples and supercollider treatment is in scope.

Creating audio samples [direct from data values](https://www.earth.org.uk/statscast-202005.html) is also in scope.

See [Earth Notes Sonification](https://www.earth.org.uk/sonification.html).

Contributors may have different implementation language preferences: awk is present!

[MIDICSV](https://www.fourmilab.ch/webtools/midicsv/) allows a useful intermediate lossless ASCII CSV representation.