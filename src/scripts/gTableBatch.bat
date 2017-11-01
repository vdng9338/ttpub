@ECHO OFF
REM - THE BELOW 
echo Hello TimeTable Publisher

set TTPUB_HOME=.
set TEMPLATES=%TTPUB_HOME%\templates

set SIMPLE_HTML="webPageTemplates/simpleTable.web"
set SIMPLE_MAP="webPageTemplates/simpleGMap.web"
set METRO_HTML="webPageTemplates/metroTable.web"
set METRO_MAP="webPageTemplates/metroGMap.web"

echo %TEMPLATES%

set TOOL=org.timetablepublisher.view.cmdline.GTimesTableBatchGenerator

set CP="%JAVA_HOME%\lib\tools.jar;.;%TTPUB_HOME%;%TTPUB_HOME%\lib\ttpub.jar;%TTPUB_HOME%\lib\args4j\args4j-tools-2.0.7.jar;%TTPUB_HOME%\lib\iText;%TTPUB_HOME%\lib\iText\itext-1.4.1.jar;%TTPUB_HOME%;%TTPUB_HOME%\lib\freemarker;%TTPUB_HOME%\lib\freemarker\freemarker.jar;%TTPUB_HOME%\lib\opencsv;%TTPUB_HOME%\lib\opencsv\opencsv-1.4.jar;c:\java\DEV;%TTPUB_HOME%_20060824-1631.zip;%TTPUB_HOME%\build\classes"

rem Get remaining unshifted command line arguments and save them in the
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

rem Execute Command Line TTPUB
java -Xmx1025m -cp %CP% -Djava.util.logging.config.file=logger.quiet.properties %TOOL% -tdir %TEMPLATES% -htmlTemplate %METRO_HTML% -mapTemplate %METRO_MAP% -zip allGTables.zip %CMD_LINE_ARGS%