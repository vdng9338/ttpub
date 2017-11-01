#
# RUNS ON pan
# common (default) environment for running the batch timetable jobs
# sourced by trimetBatch.sh (and like minded scripts)
#

export TTPUB_HOME="."
export TEMPLATES="${TTPUB_HOME}/templates"
export AGENCY_DIR=${AGENCY_DIR:="trimet"}
export HTML="${AGENCY_DIR}/basic.web"
export MAP="${AGENCY_DIR}/map.web"
export VERT="${AGENCY_DIR}/vert.web"
export ALL="${AGENCY_DIR}/allstops.web"


export TOOL="org.timetablepublisher.view.cmdline.TimesTableBatchGenerator"
echo Batch Gen Tool: $TOOL

export ROUTE=${ROUTE:="4"}
echo ROUTE: $ROUTE

export CONFIG_DIR=${CONFIG_DIR:="/home/ttpub/time_table_configuration/webconfig"}
echo CONFIG DIR: $CONFIG_DIR

export EFFECTIVE_DATE=${EFFECTIVE_DATE:=`date +%m-%d-%Y`}
echo EFFECTIVE DATE - calculated to the Sunday after this date $EFFECTIVE_DATE

export WEB_SERVER=${WEB_SERVER:="trimet@web3"}
export PROD_ZDIR=${PROD_ZDIR:="~/public_html/schedules/zip"}
export TEST_ZDIR=${TEST_ZDIR:="~/public_html/schedules/new"}


#
# CLASSPATH CRUD
#
export CP="${JAVA_HOME}"
for x in `find . -name *.jar`; do CP="$x:$CP"; done
export CP=$CP
echo CLASSPATH:  $CP

export ZIP_FILE=${ZIP_FILE="NEW-SCH.ZIP"}
echo ZIP FILE: $ZIP_FILE

export TABLE_TYPE=${TABLE_TYPE:="TRANS"}
echo TABLE TYPE: $TABLE_TYPE
