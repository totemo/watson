#!/bin/bash

MCP_VER=72
MC_VER=1.3.2
DESTINATION_DIR=~/projects/watson
MCP_DIR=~/bin/mcp$MCP_VER

#------------------------------------------------------------------------------

fn_error()
{
  echo >&2 "ERROR: ""$@"
  exit 1
}


#------------------------------------------------------------------------------

cd "$DESTINATION_DIR"/scripts/ && \
  for FILE in *.sh; do
    cp ~/bin/$FILE . || fn_error "could not update $FILE"
  done

cd "$MCP_DIR/modsrc" && rm -rf ./minecraft/ || fn_error "could not purge client modsrc directory"
cd "$MCP_DIR" && ./getchangedsrc.sh || fn_error "could not get changed sources"
cd "$MCP_DIR/modsrc/minecraft/" && cp -r * "$DESTINATION_DIR" || fn_error "could not copy sources"
cd "$MCP_DIR" && cp src/minecraft/watson/*.yml modsrc/minecraft/watson || fn_error "could not copy YAML files"



