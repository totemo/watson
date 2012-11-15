#!/bin/bash

set -o nounset
. ~/bin/watson_common.sh

TMP_DIR=/tmp/watson_packaging/

#------------------------------------------------------------------------------

OUTPUT_FILE=$JAR_DIR/watson-$MC_VER-$DATE.zip

#------------------------------------------------------------------------------
# Compile and reobfuscate.

cd "$MCP_DIR" || fn_error "MCP directory doesn't exist."
./recompile.sh && ./reobfuscate.sh || fn_error "could not rebuild the mod."

#------------------------------------------------------------------------------

mkdir -p "$JAR_DIR" || fn_error "could not create output directory."
rm -f "$OUTPUT_FILE" || fn_error "could not delete old file."
rm -rf "$TMP_DIR" && mkdir -p "$TMP_DIR" || fn_error "could not create staging area."

#------------------------------------------------------------------------------
# Update the version information in the source tree and build area.

mkdir -p "$TMP_DIR/watson"                                       && \
echo "$MC_VER ($DATE)" > "$MCP_DIR"/src/minecraft/watson/version && \
cp "$MCP_DIR"/src/minecraft/watson/version "$TMP_DIR/watson"     || \
fn_error "could not update version resource"
  
#------------------------------------------------------------------------------

cp "$MCP_DIR"/src/minecraft/watson/*.yml "$MCP_DIR"/reobf/minecraft/watson/ || fn_error "could not copy YAML config files."
cd "$TMP_DIR"
cp -r "$MCP_DIR"/reobf/minecraft/* . || fn_error "could not copy reobfuscated classes."
jar xf "$MCP_DIR"/lib/snakeyaml-1.10.jar org/ >& /dev/null || fn_error "could not extract SnakeYAML classes."
zip -rq "$OUTPUT_FILE" *
