#!/bin/sh
export CLASSPATH=.
for i in lib/*.jar;
do CLASSPATH=./$i:"$CLASSPATH";
cat  $CLASSPATH;
done

CLASSPATH=\
.:\
./conf:\
$CLASSPATH:\
eayun-schedule-res-1.0.0-RELEASE.jar

export CLASSPATH
java -server -Xms256m -Xmx1024m -XX:MaxPermSize=256M com.eayun.schedule.ScheduleResourceStartup
