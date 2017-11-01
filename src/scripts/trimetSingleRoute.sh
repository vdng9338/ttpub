#!/bin/sh

ZIP_FILE=${ZIP_FILE:="SINGLE_ROUTE.ZIP"}
. ./envars.sh
#
# Execute Command Line TTPUB
#
echo
echo PREVIEW
echo "ttpub: start - `date`"
CMD_LINE="java -Xmx1025m -cp ${CP} -Djava.util.logging.config.file=logger.quiet.properties ${TOOL} -cdir ${CONFIG_DIR} -tdir ${TEMPLATES} -htmlTemplate ${HTML} -vertTemplate ${VERT} -mapTemplate ${MAP} -allTemplate ${ALL} -zip ${ZIP_FILE} -maps -vert -redirects -noRenameTimepoint -once -report -route ${ROUTE} -type ${TABLE_TYPE} -date ${EFFECTIVE_DATE} $*"
echo $CMD_LINE
$CMD_LINE
echo "ttpub: end - `date`"
echo
