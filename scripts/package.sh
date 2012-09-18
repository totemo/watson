#!/bin/bash
#------------------------------------------------------------------------------
# Changeable bits up here:

MINECRAFT_VERSION=1.3.2
MCP_DIR=~/bin/mcp72
ADDITIONAL_PACKAGE_DIRS="watson"
ADDITIONAL_RESOURCE_FILES="*.yml"

#------------------------------------------------------------------------------
# Hopefully no serviceable parts below here:

MINECRAFT_JAR=~/.minecraft/older_versions/minecraft-$MINECRAFT_VERSION.jar
OUTPUT_JAR=~/.minecraft/older_versions/minecraft-$MINECRAFT_VERSION-roundtrip.jar
OBFUSCATED_JAR=../temp/client_reobf.jar
STAGING_DIR="$MCP_DIR/packaging"
ECLIPSE_BIN_DIR="$MCP_DIR/eclipse/Client/bin"

#------------------------------------------------------------------------------

fn_error()
{
  echo >&2 "ERROR: ""$@"
  exit 1
}

#------------------------------------------------------------------------------
# Compile and reobfuscate.

cd "$MCP_DIR" || fn_error "MPC directory doesn't exist."

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
  rsync -a "$ECLIPSE_BIN_DIR/$DIR" --exclude='*.class' --include="$ADDITIONAL_RESOURCE_FILES" .
done

#------------------------------------------------------------------------------
# Blend in whatever is in $MCP_DIR/lib/.

for f in `ls $MCP_DIR/lib/*.jar 2>/dev/null`; do
  jar xf "$f"
done

#------------------------------------------------------------------------------
# Package the result.

jar cf "$OUTPUT_JAR" . || fn_error "Can't create output JAR."

