#!/bin/bash

set -o nounset
. ~/bin/watson_common.sh

#------------------------------------------------------------------------------

MC_JAR=$JAR_DIR/minecraft-$MC_VER.jar
TEMP_DIR=/tmp/moddiffs/
MAP_FILE=$TEMP_DIR/map 

#------------------------------------------------------------------------------
# Set up an associative array mapping obfuscated names to deobfuscated ones.

declare -A DEOBF_CLASSES
mkdir -p $TEMP_DIR && \
  grep CL: "$MCP_DIR"/temp/client_ro.srg | awk '{print $2, $3}' > $MAP_FILE
while read OBF DEOBF; do
  DEOBF_CLASSES[$OBF]=$DEOBF
done < $MAP_FILE

#------------------------------------------------------------------------------

if [ $# -ne 1 ]; then
  fn_error "you must specify the name of the mod ZIP to compare against the stock Minecraft JAR."
fi

rm -rf $TEMP_DIR
mkdir -p $TEMP_DIR/stock || fn_error "could not create stock JAR staging area."
mkdir -p $TEMP_DIR/mod   || fn_error "could not create mod ZIP staging area."

unzip -qq "$1" -d "$TEMP_DIR/mod"        || fn_error "could not unpack stock mod ZIP."
cd "$TEMP_DIR/stock" && jar xf "$MC_JAR" || fn_error "could not unpack stock JAR."

#------------------------------------------------------------------------------
# Generate a list of obfuscated class names and map back to deobfuscated ones.

OBF_CLASSES=$(cd $TEMP_DIR && diff -rq stock mod | grep -v "Only in" | awk '{print $2; }' | xargs -I {} basename {} .class)
for OBF in $OBF_CLASSES; do
  echo -e "$OBF\t\t$(basename ${DEOBF_CLASSES[$OBF]})"
done

