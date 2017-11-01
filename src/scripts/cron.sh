#
# RUNS ON pan from HOME directory
# executed by cron (see crontab.txt)
#

cd /home/ttpub
export HOME=/home/ttpub
export JAVA_HOME=$HOME/jdk
export PATH=".:$JAVA_HOME/bin:$PATH"

cd time_table_configuration/ttpublisher
trimetBatch.sh >> /home/ttpub/LOG 1>> /home/ttpub/LOG
