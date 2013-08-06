#!/bin/bash

set -o nounset
. ~/bin/watson_common.sh

#------------------------------------------------------------------------------
# Show a usage message and exit with error.

fn_usage()
{
  echo Usage: "$THIS_SCRIPT [watson|full]"
  exit 1
}

#------------------------------------------------------------------------------
# Usage: fn_check <name> <zipfile>
#
# Check that the mod whose name is $1 exists as a file $2.  Return false if not
# found.

fn_check()
{
  NAME="$1"
  ZIP="$2"
  BASEZIP=$(basename "$ZIP")
  echo -n "Checking for $NAME ($BASEZIP)... "
  if [ -f "$ZIP" ]; then
    echo "FOUND."
    true
  else
    echo "MISSING."
    false
  fi
}

#------------------------------------------------------------------------------
# Usage: fn_unzip <name> <zipfile>
#
# Unzip the mod whose name is $1 from file $2 into the staging area.
#
# Exit with error if there is a problem.

fn_unzip()
{
  NAME="$1"
  ZIP="$2"
  BASEZIP=$(basename "$ZIP")
  echo -n "Extracting $NAME ($BASEZIP)... "
  if unzip -qq -o -d "$TMP_DIR" "$ZIP" >&/dev/null; then
    echo "DONE."
  else
    echo "FAILED."
    fn_error "could not extract $NAME archive."
  fi
}

#------------------------------------------------------------------------------
# Usage: fn_unzip_subdir <name> <zipfile> <subdir>
#
# For the mod whose name is $1, extract just the subdirectory (within the zip),
# $3, from ZIP file $2 into the staging area.  Then copy all of those files up 
# to the top of the staging area and delete them from the filesystem where they 
# were originally extracted.
#
# NOTE: may leave behind some empty directories if the subdirectory path 
# consists of more than one level of subdirectories.
#
# Exit with error if there is a problem.

fn_unzip_subdir()
{
  NAME="$1"
  ZIP="$2"
  SUBDIR="$3"
  BASEZIP=$(basename "$ZIP")
  echo -n "Extracting $NAME ($BASEZIP)... "
  unzip -qq -o -d "$TMP_DIR" "$ZIP" "$SUBDIR/*" >&/dev/null || \
    { echo "FAILED." && fn_error "could not extract $NAME archive."; }
  rsync -a "$TMP_DIR/$SUBDIR/" "$TMP_DIR/" >&/dev/null || \
    { echo "FAILED." && fn_error "could not relocate ZIP contents."; }
  rm -rf "$TMP_DIR/$SUBDIR/" || \
    { echo "FAILED." && fn_error "could not remove ZIP subdirectory."; }
  echo "DONE."
}

#------------------------------------------------------------------------------
# Let the Forge installer set up the directory structure under 
# .minecraft/versions/$FORGE_VERSION/ and then match that JAR.

THIS_SCRIPT=$(basename "$0")
MCPSETUP_DIR=~/bin/mcpsetup/$MC_VER
FORGE_VERSION=1.6.2-Forge9.10.0.804
INPUT_JAR=~/.minecraft/versions/$FORGE_VERSION/$FORGE_VERSION.jar.orig
OUTPUT_JAR=~/.minecraft/versions/$FORGE_VERSION/$FORGE_VERSION.jar
REIS_ZIP=$(ls $MCPSETUP_DIR/mods/\[*\]ReiMinimap_*.zip 2>/dev/null | head -1)
WATSON_ZIP=$(ls $MCPSETUP_DIR/mods/watson-$MC_VER-????-??-??.zip 2>/dev/null | head -1)
OPTIFINE_ZIP=$(ls $MCPSETUP_DIR/mods/OptiFine_*.zip 2>/dev/null | head -1)
TMP_DIR=$MCPSETUP_DIR/mods/tmp

#------------------------------------------------------------------------------
# Clear out the temp directory.

if [ -d "$TMP_DIR" ]; then
  rm -rf "$TMP_DIR" || fn_error "could not clean temporary directory"
fi
mkdir -p "$TMP_DIR" || fn_error "could not make temporary directory"

#------------------------------------------------------------------------------
# Parse command line.

DO_REIS=false
DO_WATSON=false
DO_OPTIFINE=false
if [ $# -eq 0 ]; then
  JAR_TYPE="watson"
elif [ $# -eq 1 ]; then
  case "$1" in
    watson|full) 
      JAR_TYPE="$1" 
      ;;
    *)
      echo "$1: unsupported argument"JAR_TYPE
      fn_usage
      ;;
  esac
else
  fn_usage
fi

#------------------------------------------------------------------------------

case "$JAR_TYPE" in
  watson)
    DO_WATSON=true
    ;;
  full)
    DO_WATSON=true
    DO_REIS=true
    DO_OPTIFINE=true
    ;;    
esac

#------------------------------------------------------------------------------
# As of 1.6, LiteLoader installs itself using the tweaks mechanism and I 
# configure it to chain to FML with cascaded tweaks.  I simply allow the Forge
# installer to set up the basic structure under ~/.minecraft/versions and then
# modify the JSON configuration per the instructions for LiteLoader.
#
# Order of mods:
#   All:
#     Rei's Minimap
#   Moderator:
#     Watson
#   All:
#     OptiFine - always last
#------------------------------------------------------------------------------
# Check pre-requisites exist.

PREREQUISITES=true
if $DO_REIS; then
  fn_check   "Rei's Minimap"   "$REIS_ZIP"       || PREREQUISITES=false
fi
if $DO_OPTIFINE; then
  fn_check   "Optifine"        "$OPTIFINE_ZIP"   || PREREQUISITES=false
fi
if $DO_WATSON; then
  fn_check   "Watson"          "$WATSON_ZIP"     || PREREQUISITES=false
fi

if ! $PREREQUISITES; then
  fn_error "missing mod files."
fi

#------------------------------------------------------------------------------
# Extract into staging area.

echo -n "Extracting Minecraft JAR... "
cd "$TMP_DIR" && jar xf "$INPUT_JAR" >&/dev/null || \
  { echo "FAILED." && fn_error "could not extract Minecraft JAR."; }
echo "DONE."

if $DO_REIS; then
  fn_unzip           "Rei's Minimap"  "$REIS_ZIP"
fi
if $DO_WATSON; then
  fn_unzip           "Watson"         "$WATSON_ZIP"
fi
if $DO_OPTIFINE; then
  fn_unzip           "Optifine"       "$OPTIFINE_ZIP"
fi

# Metadata needs to be removed after Forge is unpacked.
rm "$TMP_DIR/META-INF/"*          >&/dev/null || \
  { echo "FAILED." && fn_error "could not expunge metadata."; }


#------------------------------------------------------------------------------
# Construct the JAR.

echo -n "Building $JAR_TYPE JAR..."
jar -cf "$OUTPUT_JAR" -C "$TMP_DIR" . >&/dev/null || \
  { echo "FAILED." && fn_error "failed to build output JAR."; }
echo "DONE."


