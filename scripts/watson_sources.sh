#!/bin/bash

set -o nounset
. ~/bin/watson_common.sh

PACKAGE_DIR=net/minecraft/src

#------------------------------------------------------------------------------
# NOTE: getchangedsrc.sh only works correctly if you have run recompile.sh.

cd "$GIT_DIR"/scripts/ && \
  for FILE in *.sh; do
    cp ~/bin/$FILE . || fn_error "could not update $FILE"
  done

cd "$MCP_DIR/modsrc" && rm -rf ./minecraft/ || fn_error "could not purge client modsrc directory"
cd "$MCP_DIR" && ./getchangedsrc.sh || fn_error "could not get changed sources"

#------------------------------------------------------------------------------
# Get a list of changed Mojang sources to generate patches for.

cd $MCP_DIR
SOURCES=$(ls modsrc/minecraft/$PACKAGE_DIR/ | grep -v mod_)

#------------------------------------------------------------------------------

for SOURCE in $SOURCES; do
  echo "Generating patch for $PACKAGE_DIR/$SOURCE"
  diff -Nu vanillasrc/minecraft/$PACKAGE_DIR/$SOURCE modsrc/minecraft/$PACKAGE_DIR/$SOURCE > modsrc/minecraft/$PACKAGE_DIR/$SOURCE.patch
done

#------------------------------------------------------------------------------

cd "$MCP_DIR/modsrc/minecraft/" && cp -r * "$GIT_DIR"/src || fn_error "could not copy sources"
cd "$MCP_DIR" && cp src/minecraft/watson/*.yml modsrc/minecraft/watson || fn_error "could not copy YAML files"

#------------------------------------------------------------------------------
# Clear out any Mojang sources. Any changes should be covered by .patch files.
# There are probably more elegant ways to do this.

cd "$GIT_DIR/src/$PACKAGE_DIR"
for FILE in *.java; do 
  if ! expr match "$FILE" 'mod_.*java' >&/dev/null; then 
    rm "$FILE" 
  fi
done

