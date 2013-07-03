#!/bin/bash

set -o nounset
. ~/bin/watson_common.sh

PACKAGE_DIR=net/minecraft

#------------------------------------------------------------------------------

cd "$GIT_DIR"/scripts/ && \
  for FILE in *.sh; do
    cp ~/bin/$FILE . || fn_error "could not update $FILE"
  done

# NOTE: getchangedsrc.sh only works correctly if you have run recompile.sh.
if [ ! -d "$MCP_DIR/modsrc" ]; then
  cd "$MCP_DIR" && ./recompile.sh && ./getchangedsrc.sh || fn_error "could not set up modsrc subdirectory"
fi

cd "$MCP_DIR/modsrc" && rm -rf ./minecraft/ || fn_error "could not purge client modsrc directory"
cd "$MCP_DIR" && ./getchangedsrc.sh || fn_error "could not get changed sources"

#------------------------------------------------------------------------------
# Get a list of changed Mojang sources to generate patches for.

if [ -d "$MCP_DIR/modsrc/minecraft/$PACKAGE_DIR" ]; then
  cd "$MCP_DIR/modsrc/minecraft/$PACKAGE_DIR"
  SOURCES=$(find "." -name '*.java' | egrep -v 'mod_' 2>/dev/null)
  cd $MCP_DIR
  for SOURCE in $SOURCES; do
    echo "Generating patch for $PACKAGE_DIR/$SOURCE"
    diff -Nu vanillasrc/minecraft/$PACKAGE_DIR/$SOURCE modsrc/minecraft/$PACKAGE_DIR/$SOURCE > modsrc/minecraft/$PACKAGE_DIR/$SOURCE.patch
  
    # For pre-existing patch files, ignore the changes to the timestamp line.
    if [ -f "$GIT_DIR/src/$PACKAGE_DIR/$SOURCE.patch" ]; then
      grep -v "$SOURCE" "$GIT_DIR/src/$PACKAGE_DIR/$SOURCE.patch"     > /tmp/watson_patch.old && \
      grep -v "$SOURCE" "modsrc/minecraft/$PACKAGE_DIR/$SOURCE.patch" > /tmp/watson_patch.new || \
        fn_error "could not prepare patches for comparison."
  
      if ! diff /tmp/watson_patch.old /tmp/watson_patch.new; then
        echo "Patch updated."
      else
        rm "modsrc/minecraft/$PACKAGE_DIR/$SOURCE.patch"
        echo "Patch has not changed."
      fi
    fi
  done
fi

#------------------------------------------------------------------------------

cd "$MCP_DIR" && cp src/minecraft/watson/*.yml   modsrc/minecraft/watson || fn_error "could not copy YAML files"
cd "$MCP_DIR" && cp src/minecraft/watson/version modsrc/minecraft/watson || fn_error "could not copy version resource"
cd "$MCP_DIR/modsrc/minecraft/" && cp -r * "$GIT_DIR"/src || fn_error "could not copy sources"

#------------------------------------------------------------------------------
# Clear out any Mojang sources. Any changes should be covered by .patch files.
# There are probably more elegant ways to do this.

if [ -d "$GIT_DIR/src/$PACKAGE_DIR" ]; then
  SOURCES=$(find "$GIT_DIR/src/$PACKAGE_DIR" -name '*.java' | egrep -v 'mod_' 2>/dev/null)
  for FILE in $SOURCES; do 
    if ! expr match "$FILE" 'mod_.*java' >&/dev/null; then 
      rm "$FILE" 
    fi
  done
fi

