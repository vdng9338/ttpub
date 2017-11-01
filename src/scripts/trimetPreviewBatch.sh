#!/bin/sh

#
# RUNS ON pan
# run by hand to generate a preview of a new service (usually a month before)
#

export CONFIG_DIR=${CONFIG_DIR:="/home/ttpub/time_table_configuration/configure"}
export ZIP_FILE=${ZIP_FILE:="PREVIEW-SCH.ZIP"}
export TABLE_TYPE=${TABLE_TYPE:="TRANS"}
. ./envars.sh

#
# Execute Command Line TTPUB
#
echo
echo PREVIEW
echo "ttpub: start - `date`"
java -Xmx1025m -cp ${CP} -Djava.util.logging.config.file=logger.quiet.properties ${TOOL} -maps -vert -noRenameTimepoint -redirects -date ${EFFECTIVE_DATE} -report -preview -cdir ${CONFIG_DIR} -tdir ${TEMPLATES} -htmlTemplate ${HTML} -vertTemplate ${VERT} -mapTemplate ${MAP} -allTemplate ${ALL} -zip ${ZIP_FILE} -type ${TABLE_TYPE} $*
echo "ttpub: end - `date`"
echo


#
# COPY THE ZIP FILE TO 
#
if [ -w zips/$ZIP_FILE ]; then
  ZDIR="~/public_html/schedules/new"

  echo "copying zips/$ZIP_FILE over to server (eg: web3) $ZDIR"
  echo scp zips/${ZIP_FILE} ${WEB_SERVER}:${PROD_ZDIR}
  scp zips/${ZIP_FILE} ${WEB_SERVER}:${ZDIR}
  mv zips/${ZIP_FILE} zips/old-${ZIP_FILE} 
fi
