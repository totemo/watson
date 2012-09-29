#!/bin/bash

MCP_VER=72
MC_VER=1.3.2
OUTPUT_DIR=~/.minecraft/older_versions
MCP_DIR=~/bin/mcp$MCP_VER
TMP_DIR=/tmp/watson_packaging/

#------------------------------------------------------------------------------

DATE=$(date +%Y-%m-%d)
OUTPUT_FILE=$OUTPUT_DIR/watson-$MC_VER-$DATE.zip

#------------------------------------------------------------------------------

fn_error()
{
  echo >&2 "ERROR: ""$@"
  exit 1
}


#------------------------------------------------------------------------------
# Compile and reobfuscate.

cd "$MCP_DIR" || fn_error "MCP directory doesn't exist."

./recompile.sh
./reobfuscate.sh

#------------------------------------------------------------------------------

mkdir -p "$OUTPUT_DIR" || fn_error "could not create output directory."
rm -f "$OUTPUT_FILE" || fn_error "could not delete old file."

rm -rf "$TMP_DIR" && mkdir -p "$TMP_DIR" || fn_error "could not create staging area."

#------------------------------------------------------------------------------

cp "$MCP_DIR"/src/minecraft/watson/*.yml "$MCP_DIR"/reobf/minecraft/watson/ || fn_error "could not copy YAML config files."
cd "$TMP_DIR"
cp -r "$MCP_DIR"/reobf/minecraft/* . || fn_error "could not copy reobfuscated classes."
jar xf "$MCP_DIR"/lib/snakeyaml-1.10.jar org/ || fn_error "could not extract SnakeYAML classes."
zip -r "$OUTPUT_FILE" *
