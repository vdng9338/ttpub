NOTE: the junit.jar file is here more for a clean compile rather than running the tests...

to run tests, type ant unit-test from command line (or some other mode).

if you get an "Ant could not find the task or a class this task relies upon." error, you need to get
junit.jar onto your CLASSPATH prior to running Ant (having it in this directory won't fit that unless
you put this dir in your commandline CLASSPATH).   

the best thing to do (IMHO) is to copy junit.jar into your ANT_HOME/lib directory.