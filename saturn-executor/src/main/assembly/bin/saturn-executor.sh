#!/bin/bash

ulimit -s 20280
ulimit -c unlimited
ulimit -n 20480

export PATH=$PATH:/usr/sbin

WORKING_DIR=`pwd`
COMMAND="$0 $*"
PRG_DIRECT="$0"

#PRG="$0"
PRG=$(readlink -f $0)
PRG_DIR=`dirname "$PRG"`
BASE_DIR=`cd "$PRG_DIR/.." >/dev/null; pwd`
PARENT_DIR=`cd "$BASE_DIR/.." >/dev/null; pwd`

LOGDIR=""
OUTFILE=""
NAMESPACE=""
EXECUTORNAME=`hostname`
LOCALIP=`ip addr| grep 'inet '| grep -v '127.0.0.1'`
LOCALIP=`echo $LOCALIP | cut -d/ -f1|awk '{print $2}'`
JMX_PORT="24501"
START_TIME=20
SATURN_LIB_DIR=$BASE_DIR/lib
APP_LIB_DIR=$PARENT_DIR/app

STATUS_FILE=${PRG_DIR}/status
PID_FILE=${PRG_DIR}/PID
LAST_START_COMMAND_FILE=${PRG_DIR}/last_start_command.sh

RUN_MODE="background"

STARTUP_DELAY_SECONDS=20

USAGE()
{
	echo -e "Usage: ${PRG_FILE} start | stop | restart"
	echo -e "    -------------------------------"
	echo -e "    start [-n|--namespace namespace] [-e|--executorName executorName] [-jmx|--jmx-port port] [jvmArgs, its position should be the last.]"
	echo -e "        '-n|--namespace': required."
	echo -e "        '-e|--executorName': optional,default value is ${EXECUTORNAME}."
	echo -e "        '-d|--libdir': optional, default value is ${PARENT_DIR}/app."
	echo -e "        '-r|--runmode': optional, default value is ${RUN_MODE}, you can set it foreground"
	echo -e "        '-jmx|--jmx-port': optional, default value is ${JMX_PORT}."
	echo -e "        '-env|--environment': optional."
	echo -e "        '-sld|--saturnLogDir': optional."
	echo -e "        jvmArgs: optional."
	echo -e "    -------------------------------"
	echo -e "    dump, no parameters."
	echo -e "    -------------------------------"
	echo -e "    stop, no parameters."
	echo -e "    -------------------------------"
	echo -e "    restart, no parameters."
	echo -e "    -------------------------------"
}

if [ $# -lt 1 ]; then
	USAGE
	exit -1
fi

LOG_FMT()
{
    local MSG=$*
    echo -e "[$(date +'%Y-%m-%d %H:%M:%S')] [saturn-executor] ${MSG}"
}

CMD="$1"
shift

while true; do
	case "$1" in
		-n|--namespace) NAMESPACE="$2"; shift 2;;
		-e|--executorName) EXECUTORNAME="$2"; shift 2;;
		-d| --libdir) APP_LIB_DIR="$2"; shift 2;;
		-r| --runmode) RUN_MODE="$2"; shift 2;;
		-jmx|--jmx-port) JMX_PORT="$2" ; shift 2 ;;
		-env|--environment) RUN_ENVIRONMENT="$2" ; shift 2 ;;
		-sld|--saturnLogDir) LOGDIR="$2" ; shift 2 ;;
		*) break;;
	esac
done

ADDITIONAL_OPTS=$*;

PERM_SIZE="256m"
MAX_PERM_SIZE="512m"

if [[ "$RUN_ENVIRONMENT" = "dev" ]]; then
  ENVIRONMENT_MEM="-Xms512m -Xmx512m -Xss256K"
  PERM_SIZE="128m"
  MAX_PERM_SIZE="256m"
elif [[ "$RUN_ENVIRONMENT" = "docker" ]]; then
  ENVIRONMENT_MEM="-Xms512m -Xmx512m -Xss256K"
  PERM_SIZE="128m"
  MAX_PERM_SIZE="256m"
else
  ENVIRONMENT_MEM="-Xms2048m -Xmx2048m"
fi

if [[ "$LOGDIR" = "" ]]; then
  LOGDIR=/apps/logs/saturn/${NAMESPACE}/${EXECUTORNAME}-${LOCALIP}
fi

OUTFILE=$LOGDIR/saturn-executor.log

JAVA_OPTS="-XX:+PrintCommandLineFlags -XX:-OmitStackTraceInFastThrow -XX:-UseBiasedLocking -XX:AutoBoxCacheMax=20000"
MEM_OPTS="-server ${ENVIRONMENT_MEM} -XX:NewRatio=1 -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=75 -XX:+UseCMSInitiatingOccupancyOnly -XX:+ParallelRefProcEnabled -XX:+AlwaysPreTouch -XX:MaxTenuringThreshold=6 -XX:+ExplicitGCInvokesConcurrent"
GCLOG_OPTS="-Xloggc:${LOGDIR}/gc.log  -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCApplicationConcurrentTime -XX:+PrintGCDateStamps -XX:+PrintGCDetails"
CRASH_OPTS="-XX:ErrorFile=${LOGDIR}/hs_err_%p.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOGDIR}/"
JMX_OPTS="-Dcom.sun.management.jmxremote.port=${JMX_PORT} -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dsun.rmi.transport.tcp.threadKeepAliveTime=75000 -Djava.rmi.server.hostname=${LOCALIP}"
SETTING_CONF="-DVIP_SATURN_ENABLE_EXEC_SCRIPT=true -DVIP_SATURN_PRG=${PRG} -DVIP_SATURN_LOG_DIR=${LOGDIR} -DVIP_SATURN_LOG_OUTFILE=${OUTFILE} -Dstart.check.outfile=${STATUS_FILE}"

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')

if [[ "$JAVA_VERSION" < "1.8" ]]; then
  MEM_OPTS="$MEM_OPTS -XX:PermSize=${PERM_SIZE} -XX:MaxPermSize=${MAX_PERM_SIZE} -Djava.security.egd=file:/dev/./urandom"
else
  MEM_OPTS="$MEM_OPTS -XX:MetaspaceSize=${PERM_SIZE} -XX:MaxMetaspaceSize=${MAX_PERM_SIZE} "
fi

CHECK_JMX()
{
	if [ x"$JMX_PORT" == x ]; then
		JMX_OPTS=""
	else
		TMP=$(echo `lsof -P -i :${JMX_PORT} | grep LISTEN | awk '{print $2}'`)
		if [ x"$TMP" != x ]; then
			LOG_FMT "The jmx port is used, please use other port."
			exit -1
		fi
		LOG_FMT "The jmx port is ${JMX_PORT}."
	fi
}

GET_PID()
{
    if [ -f $PID_FILE ] ; then
        cat $PID_FILE
    else
        echo `ps -ef | grep java | grep "\-jar" | grep "saturn-executor.jar" | grep -v grep | awk '{print $2}' `
    fi
}

GET_LOGDIR()
{
    echo `ps -ef | grep java | grep "\-jar" | grep "saturn-executor.jar" | grep $1 | sed -r 's/.*HeapDumpPath=(.*)/\1/' | awk '{print $1}'`
}

CHECK_PARAMETERS()
{
	if [ x$NAMESPACE == x ]; then
		LOG_FMT "The parameter -n|--namespace is required."
		USAGE;
		exit -1
	fi
}

STARTUP_DELAY()
{
    if [[ "$RUN_ENVIRONMENT" = "production" ]]; then
        i=1
        while(($i<=$STARTUP_DELAY_SECONDS)); do
        	echo -e ".\c"
        	sleep 1
        	i=$(($i+1))
        done
    fi
}

START()
{
    LOG_FMT "Begin to start executor."
    LOG_FMT "The java version is ${JAVA_VERSION}"
	LOG_FMT "Log redirects to ${LOGDIR}"
	CHECK_JMX

	CHECK_PARAMETERS

	if [ ! -d $LOGDIR ]; then
		LOG_FMT "Warning, the log directory of $LOGDIR is not existed, try to create it."
		mkdir -p $LOGDIR
		if [ -d $LOGDIR ]; then
			LOG_FMT "Create log directory successfully."
		else
			LOG_FMT "Create log directory failed."
			exit -1
		fi
    fi

	if [ -f $PID_FILE ] ; then
		PID=`cat $PID_FILE`
	fi

	if [ "$PID" != "" ]; then
		if [ -d /proc/$PID ];then
		 LOG_FMT "Saturn executor is running, please stop it first!!"
		 exit -1
		fi
	fi

	STARTUP_DELAY

	echo "" > ${STATUS_FILE}
	RUN_PARAMS="-namespace ${NAMESPACE} -executorName ${EXECUTORNAME} -saturnLibDir ${SATURN_LIB_DIR} -appLibDir ${APP_LIB_DIR}"
    nohup java  $JAVA_OPTS $MEM_OPTS $JMX_OPTS $GCLOG_OPTS $CRASH_OPTS $SETTING_CONF $ADDITIONAL_OPTS -jar ${BASE_DIR}/saturn-executor.jar ${RUN_PARAMS}  >> $OUTFILE 2>&1 &
	PID=$!
	echo $PID > $PID_FILE

	#record the start command
	echo -e "#!/bin/bash\ncd ${WORKING_DIR}\nchmod +x ${PRG_DIRECT}\n${COMMAND}" > ${LAST_START_COMMAND_FILE}
	chmod +x ${LAST_START_COMMAND_FILE}

    LOG_FMT "Starting...\c"
	sleep 3

	CHECK_STATUS=`cat ${STATUS_FILE}`
	starttime=0
	while  [ x"$CHECK_STATUS" == x ]; do
        if [[ "$starttime" -lt ${START_TIME} ]]; then
          sleep 1
          ((starttime++))
          echo -e ".\c"
          CHECK_STATUS=`cat ${STATUS_FILE}`
        else
          echo -e ""
          LOG_FMT "Saturn executor start may fails, checking not finished until reach the starting timeout! See ${OUTFILE} for more information."
          exit -1
        fi
	done

    echo -e ""

	if [ $CHECK_STATUS = "SUCCESS" ]; then
		LOG_FMT "Saturn executor start successfully, running as process:$PID."
		echo ${RUN_PARAMS} > ${STATUS_FILE}
	fi

	if [ $CHECK_STATUS = "ERROR" ]; then
		kill -9 $PID
		LOG_FMT "Saturn executor start failed ! See ${OUTFILE} for more information."
		exit -1
	fi

	if [[ "$RUN_MODE" = "foreground" ]]; then
		trap STOP SIGTERM
		wait $PID
	fi
}

DUMP()
{
    LOG_FMT "Begin to dump executor."
    local PID=$(GET_PID)
    while true; do
        case "$1" in
            -pid) PID="$2"; shift 2;;
            *) break;;
        esac
    done
    if [ "$PID" != "" ]; then
		if [ -d /proc/${PID} ];then
		    LOGDIR=$(GET_LOGDIR ${PID})
            # do the thread dump
            LOG_FILE_POSTFIX="${PID}_`date '+%Y-%m-%d-%H%M%S'`"
            jstack -l ${PID} > ${LOGDIR}/dump_${LOG_FILE_POSTFIX}.log
            LOG_FMT "Thread dump done: dump_${LOG_FILE_POSTFIX}.log"
            # backup gc log
            cp ${LOGDIR}/gc.log ${LOGDIR}/gc_${LOG_FILE_POSTFIX}.log
            LOG_FMT "Backup gc log done: gc_${LOG_FILE_POSTFIX}.log"
            LOG_FMT "Dump executor successfully."
        else
			LOG_FMT "Executor(pid:${PID}) is not running."
		fi
	else
		LOG_FMT "Executor is not running."
    fi
}

STOP()
{
    LOG_FMT "Begin to stop executor."
    PID=$(GET_PID)
	stoptime=0
    if [ "$PID" != "" ]; then
		if [ -d /proc/$PID ];then
		    DUMP -pid ${PID}
			RUN_PARAMS=`cat ${STATUS_FILE}`
			LOG_FMT "Saturn executor pid is ${PID}, params are : ${RUN_PARAMS}."
			LOG_FMT "Stopping...\c"
			while [ -d /proc/$PID ]; do
				if	[[ "$stoptime" -lt 300 ]];	then
					kill $PID
					sleep 1
					((stoptime++))
					echo -e ".\c"
				else
				    echo -e ""
					LOG_FMT "Stop failed after 300 seconds. now kill -9 ${PID}"
					kill -9 $PID
				fi
			done
			echo -e ""
			LOG_FMT "Kill the process successfully."
		else
			LOG_FMT "Saturn executor is not running."
		fi
	else
		LOG_FMT "Saturn executor is not running."
	fi
}

RESTART()
{
    STOP
    if [ -f ${LAST_START_COMMAND_FILE} ]; then
        chmod +x ${LAST_START_COMMAND_FILE}
        ${LAST_START_COMMAND_FILE}
    else
        LOG_FMT "The ${LAST_START_COMMAND_FILE} is not existing."
    fi
}

STATUS()
{
    if [ -f $PID_FILE ] ; then
        PID=`cat $PID_FILE`
    fi
    if [ "$PID" != "" ] ; then
        if [ -d /proc/$PID ] ; then
            RUN_PARAMS=`cat ${STATUS_FILE}`
            LOG_FMT "Saturn executor is running, params are : ${RUN_PARAMS}."
            exit 0
        fi
    fi
    LOG_FMT "Saturn executor is not running."
}


case "$CMD" in
  start) START;;
  dump) DUMP;;
  stop) STOP;;
  restart) RESTART;;
  status) STATUS;;
  help) USAGE;;
  *) USAGE;;
esac
