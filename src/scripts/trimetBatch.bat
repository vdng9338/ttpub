@ECHO OFF
REM - THE BELOW 
echo Hello TimeTable Publisher

set TOOL=org.timetablepublisher.view.cmdline.TimesTableBatchGenerator
set EFFECTIVE_DATE="1-14-2007"

set CONFIG_DIR=\\pan\time_table_configuration\webconfig\
set TTPUB_HOME=.
set TEMPLATES=%TTPUB_HOME%\templates

set AGENCY_DIR=trimet
set HTML=%AGENCY_DIR%\basic.web
set MAP=%AGENCY_DIR%\map.web
set VERT=%AGENCY_DIR%\vert.web
set ALL=%AGENCY_DIR%\allstops.web

set HIBERNATE_CP=.;%TTPUB_HOME%\lib\hibernate-3.1\antlr-2.7.6rc1.jar;%TTPUB_HOME%\lib\hibernate-3.1\asm-attrs.jar;%TTPUB_HOME%\lib\hibernate-3.1\asm.jar;%TTPUB_HOME%\lib\hibernate-3.1\c3p0-0.9.1-pre5a.jar;%TTPUB_HOME%\lib\hibernate-3.1\cglib-2.1.3.jar;%TTPUB_HOME%\lib\hibernate-3.1\dom4j-1.6.1.jar;%TTPUB_HOME%\lib\hibernate-3.1\ehcache-1.1.jar;%TTPUB_HOME%\lib\hibernate-3.1\hibernate3.jar;%TTPUB_HOME%\lib\hibernate-3.1\jta.jar;%TTPUB_HOME%\lib\hibernate-3.1\oscache-2.1.jar;%TTPUB_HOME%\lib\hibernate-build-tools\hibernate-tools.jar;%TTPUB_HOME%\lib\hibernate-build-tools\jtidy-r8-21122004.jar;%TTPUB_HOME%\lib\commons-collections-3.1.jar;%TTPUB_HOME%\;%TTPUB_HOME%\lib\jdom.jar;%TTPUB_HOME%\;%TTPUB_HOME%\;%TTPUB_HOME%\;%TTPUB_HOME%\;%TTPUB_HOME%\;%TTPUB_HOME%\;%TTPUB_HOME%\;%TTPUB_HOME%\;%TTPUB_HOME%\;%TTPUB_HOME%\lib\ojdbc14.jar;%TTPUB_HOME%\lib\commons-logging-1.0.4.jar;%TTPUB_HOME%\;%TTPUB_HOME%\;%TTPUB_HOME%\;%TTPUB_HOME%\lib\log4j-1.2.12.jar;%TTPUB_HOME%\;%TTPUB_HOME%\lib\ttpub.jar
set CP=%HIBERNATE_CP%;%JAVA_HOME%\lib\tools.jar;.;%TTPUB_HOME%;%TTPUB_HOME%\lib\ttpub.jar;%TTPUB_HOME%\lib\args4j\args4j-tools-2.0.7.jar;%TTPUB_HOME%\lib\iText;%TTPUB_HOME%\lib\iText\itext-1.4.1.jar;%TTPUB_HOME%;%TTPUB_HOME%\lib\freemarker;%TTPUB_HOME%\lib\freemarker\freemarker.jar;%TTPUB_HOME%\lib\opencsv;%TTPUB_HOME%\lib\opencsv\opencsv-1.4.jar;c:\java\DEV;%TTPUB_HOME%_20060824-1631.zip;%TTPUB_HOME%\build\classes

echo %TEMPLATES%

rem Get remaining unshifted command line arguments and save them in the
set CMD_DATE=-date %EFFECTIVE_DATE%
set CMD_LINE_ARGS=
:setArgs
set THIS=%1
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
if "%THIS%"=="-date" goto unsetDate
goto setArgs
:unsetDate
set CMD_DATE=
goto setArgs
:doneSetArgs

rem Special handling of effective date -- if it's not on cmd line, then append the default here
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %CMD_DATE%

rem Execute Command Line TTPUB
java -Xmx1025m -cp %CP% -Djava.util.logging.config.file=logger.quiet.properties %TOOL% -cdir %CONFIG_DIR% -tdir %TEMPLATES% -htmlTemplate %HTML% -mapTemplate %MAP% -allTemplate %ALL% -zip NEW-SCH.ZIP -maps -redirects %CMD_LINE_ARGS%