#!/bin/bash

set -o nounset
. ~/bin/watson_common.sh

#------------------------------------------------------------------------------
# Changeable bits up here:

ADDITIONAL_PACKAGE_DIRS="watson ClientCommands"
ADDITIONAL_RESOURCE_FILES="*.yml"

#------------------------------------------------------------------------------
# Hopefully no serviceable parts below here:

MINECRAFT_JAR=$JAR_DIR/minecraft-$MC_VER.jar
OUTPUT_JAR=$JAR_DIR/minecraft-$MC_VER-roundtrip.jar
OBFUSCATED_JAR=../temp/client_reobf.jar
STAGING_DIR="$MCP_DIR/packaging"

#------------------------------------------------------------------------------
# Compile and reobfuscate.

cd "$MCP_DIR" || fn_error "MCP directory doesn't exist."

./recompile.sh
./reobfuscate.sh

#------------------------------------------------------------------------------
# Prepare the staging area with the stock Minecraft JAR contents.

rm    -rf "$STAGING_DIR" && \
mkdir -p  "$STAGING_DIR" && \
cd        "$STAGING_DIR" && \
jar xf "$MINECRAFT_JAR"  || fn_error "can't re-create staging area"

#------------------------------------------------------------------------------
# Extract the updated classes including only those directories listed in
# "$ADDITIONAL_PACKAGE_DIRS".

jar xf "$OBFUSCATED_JAR" $(jar tf "$OBFUSCATED_JAR" | grep -v /) || fn_error "Can't unpack obfuscated classes"
for DIR in $ADDITIONAL_PACKAGE_DIRS; do
  jar xvf "$OBFUSCATED_JAR" "$DIR" || fn_error "Can't unpack obfuscated classes for $DIR"
done
rm META-INF/*

#------------------------------------------------------------------------------
# Add in any resources in $ADDITIONAL_PACKAGE_DIRS as specified by globs in
# "$ADDITIONAL_RESOURCE_FILES".

for DIR in $ADDITIONAL_PACKAGE_DIRS; do
  rsync -qa "$MCP_DIR/src/minecraft/$DIR" --exclude='*~' --exclude='*.java' --include="$ADDITIONAL_RESOURCE_FILES" .
done

#------------------------------------------------------------------------------
# Blend in whatever is in $MCP_DIR/lib/.

for f in `ls $MCP_DIR/lib/*.jar 2>/dev/null`; do
  jar xf "$f"
done

#------------------------------------------------------------------------------
# Package the result.

jar cf "$OUTPUT_JAR" . || fn_error "Can't create output JAR."

