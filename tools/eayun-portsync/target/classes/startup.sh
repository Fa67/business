#!/bin/sh
dcId=1601281411350
export CLASSPATH=.
for i in lib/*.jar;
do CLASSPATH=./$i:"$CLASSPATH";
cat  $CLASSPATH;
done

CLASSPATH=\
.:\
./conf:\
$CLASSPATH:\
eayun-portsync-1.0.0-RELEASE.jar

export CLASSPATH
java -server com.eayun.sync.LabelRuleSyncStartup $dcId
