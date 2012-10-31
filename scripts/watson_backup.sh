#!/bin/bash

set -o nounset
. ~/bin/watson_common.sh

#------------------------------------------------------------------------------
# Shouldn't need to change anything below here.

DATE=$(date +%Y-%m-%d_%H%M%S)
FILE=~/backups/watson-$DATE.tar.gz
tar cvzf "$FILE" -C ~/bin/mcp$MCP_VER/src/minecraft/ \
  net/minecraft/src/mod_Watson.java \
  watson/ \
  scripts/ \
  ClientCommands/ || echo "Backup failed." && exit 1
