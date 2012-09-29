#!/bin/bash

MC_VER=1.3.2
OUTPUT_DIR=~/.minecraft/older_versions
TMP_DIR=/tmp/watson_modjar/

#------------------------------------------------------------------------------

TREVOR_JAR=$OUTPUT_DIR/minecraft-$MC_VER-trevor.jar
DATE=$(date +%Y-%m-%d)
WATSON_ZIP=$OUTPUT_DIR/watson-$MC_VER-$DATE.zip
OUTPUT_JAR=$OUTPUT_DIR/minecraft-$MC_VER-trevor-watson-$DATE.jar

#------------------------------------------------------------------------------

fn_error()
{
  echo >&2 "ERROR: ""$@"
  exit 1
}

#------------------------------------------------------------------------------

rm -rf "$TMP_DIR" && mkdir "$TMP_DIR" || fn_error "could not remake staging area"
cd "$TMP_DIR" || fn_error "could not change to staging area"
jar xf "$TREVOR_JAR" || fn_error "could not unpack Trevor's modjar"
unzip -o "$WATSON_ZIP" || fn_error "could not unpack today's Watson build"
rm -f "$OUTPUT_JAR" && jar cf "$OUTPUT_JAR" . || fn_error "could not create output jar"
