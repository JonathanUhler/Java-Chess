# c - create a jar archive
# v - generate verbose output
# m - specify manifest file to manifest.mf
# f - set the file name of the jar to PeriodCountdown.jar
# C - cd to the given directory and reference the classes in that directory
jar cvmf manifest.mf Chess.jar -C src/jv/ .
mv Chess.jar src/