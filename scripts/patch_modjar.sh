#!/bin/bash

set -o nounset
. ~/bin/watson_common.sh

TMP_DIR=/tmp/watson_modjar/

#------------------------------------------------------------------------------

TREVOR_JAR=$JAR_DIR/minecraft-$MC_VER-trevor.jar
WATSON_ZIP=$JAR_DIR/watson-$MC_VER-$DATE.zip
OUTPUT_JAR=$JAR_DIR/minecraft-$MC_VER-trevor-watson-$DATE.jar

#------------------------------------------------------------------------------

rm -rf "$TMP_DIR" && mkdir "$TMP_DIR" || fn_error "could not remake staging area"
cd "$TMP_DIR" || fn_error "could not change to staging area"
jar xf "$TREVOR_JAR" >& /dev/null || fn_error "could not unpack Trevor's modjar"
unzip -o "$WATSON_ZIP" >& /dev/null || fn_error "could not unpack today's Watson build"
rm -f "$OUTPUT_JAR" && jar cf "$OUTPUT_JAR" . >& /dev/null || fn_error "could not create output jar"
