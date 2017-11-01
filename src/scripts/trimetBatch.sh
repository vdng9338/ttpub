#!/bin/sh

#
# RUNS ON pan
# weekly generating of the production batch timetables
# see crontab.txt and devcron.sh & cron.sh
#
# 1st run generates a preview available on wwwdev for Friday mornings
# 2nd run generates production timetable for www3 on Saturday mornings
#

#source the environment
. ./envars.sh

#
# Execute Command Line TTPUB
#
echo
echo "ttpub: start - `date`"
java -Xmx1025m -cp ${CP} -Djava.util.logging.config.file=logger.quiet.properties ${TOOL} -report -cdir ${CONFIG_DIR} -tdir ${TEMPLATES} -htmlTemplate ${HTML} -mapTemplate ${MAP} -vertTemplate ${VERT} -allTemplate ${ALL} -zip ${ZIP_FILE} -maps -vert -redirects -noRenameTimepoint -type ${TABLE_TYPE} -nextSunday ${EFFECTIVE_DATE} $*
echo "ttpub: end - `date`"
echo


#
# COPY THE ZIP FILE TO 
#
if [ -w zips/$ZIP_FILE ]; then
  ZDIR=$PROD_ZDIR
    
  echo "copying zips/$ZIP_FILE over to server (eg: web3) $ZDIR"
  echo scp zips/${ZIP_FILE} ${WEB_SERVER}:${PROD_ZDIR}
  scp zips/${ZIP_FILE} ${WEB_SERVER}:${ZDIR}
  mv zips/${ZIP_FILE} zips/old-${ZIP_FILE} 
fi

