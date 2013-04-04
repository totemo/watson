#!/bin/bash

set -o nounset
. ~/bin/watson_common.sh

#------------------------------------------------------------------------------
# Show a usage message and exit with error.

fn_usage()
{
  echo Usage: "$THIS_SCRIPT [basic|chaos|mod|moderator|watson]"
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

THIS_SCRIPT=$(basename "$0")
MCPSETUP_DIR=~/bin/mcpsetup/$MC_VER
MC_JAR=$MCPSETUP_DIR/minecraft.jar
MODLOADER_ZIP=$MCPSETUP_DIR/ModLoader.zip
REIS_ZIP=$(ls $MCPSETUP_DIR/mods/\[*\]ReiMinimap_*.zip | head -1)
WECUI_ZIP=$(ls $MCPSETUP_DIR/mods/CUI-*.zip 2>/dev/null | head -1)
WATSON_ZIP=$(ls $MCPSETUP_DIR/mods/watson-$MC_VER-????-??-??.zip 2>/dev/null | head -1)
OPTIFINE_ZIP=$(ls $MCPSETUP_DIR/mods/OptiFine_*.zip | head -1)
TMP_DIR=$MCPSETUP_DIR/mods/tmp

#------------------------------------------------------------------------------
# Clear out the temp directory.

if [ -d "$TMP_DIR" ]; then
  rm -rf "$TMP_DIR" || fn_error "could not clean temporary directory"
fi
mkdir -p "$TMP_DIR" || fn_error "could not make temporary directory"

#------------------------------------------------------------------------------
# Parse command line.

DO_MODLOADER=false
DO_REIS=false
DO_WECUI=false
DO_WATSON=false
DO_OPTIFINE=false
if [ $# -eq 0 ]; then
  JAR_TYPE="basic"
  DO_REIS=true
  DO_OPTIFINE=true
elif [ $# -eq 1 ]; then
  if [ "$1" = "basic" ]; then
    JAR_TYPE="basic"
    DO_REIS=true
    DO_OPTIFINE=true
  elif [ "$1" = "mod" -o "$1" = "moderator" ]; then
    JAR_TYPE="moderator"
    DO_MODLOADER=true
    DO_REIS=true
    DO_WECUI=true
    DO_WATSON=true
    DO_OPTIFINE=true
  elif [ "$1" = "chaos" ]; then
    JAR_TYPE="chaos"
    DO_MODLOADER=true
    DO_WATSON=true
    DO_REIS=true
    DO_OPTIFINE=true
  elif [ "$1" = "watson" ]; then
    JAR_TYPE="watson"
    DO_MODLOADER=true
    DO_WATSON=true
  else
    echo "$1: unsupported argument"JAR_TYPE
    fn_usage
  fi
else
  fn_usage
fi

#------------------------------------------------------------------------------
# Order of mods:
#   Basic or moderator:
#     ModLoader - always first
#   All:
#     Rei's Minimap
#   Moderator:
#     Watson
#     WorldEdit CUI
#   All:
#     OptiFine - always last
#------------------------------------------------------------------------------
# Check pre-requisites exist.

PREREQUISITES=true
if $DO_MODLOADER; then
  fn_check   "ModLoader"       "$MODLOADER_ZIP" || PREREQUISITES=false
fi
if $DO_REIS; then
  fn_check   "Rei's Minimap"   "$REIS_ZIP"      || PREREQUISITES=false
fi
if $DO_WECUI; then
 fn_check    "WorldEdit CUI"   "$WECUI_ZIP"     || PREREQUISITES=false
fi
if $DO_OPTIFINE; then
  fn_check   "Optifine"        "$OPTIFINE_ZIP"  || PREREQUISITES=false
fi
if $DO_WATSON; then
  fn_check   "Watson"          "$WATSON_ZIP"    || PREREQUISITES=false
fi

if ! $PREREQUISITES; then
  fn_error "missing mod files."
fi

#------------------------------------------------------------------------------
# Extract into staging area.

echo -n "Extracting Minecraft JAR... "
cd "$TMP_DIR" && jar xf "$MC_JAR" >&/dev/null || \
  { echo "FAILED." && fn_error "could not extract Minecraft JAR."; }
rm "$TMP_DIR/META-INF/"*          >&/dev/null || \
  { echo "FAILED." && fn_error "could not expunge metadata."; }
echo "DONE."

if $DO_MODLOADER; then
  fn_unzip           "ModLoader"      "$MODLOADER_ZIP"
fi
if $DO_REIS; then
  fn_unzip           "Rei's Minimap"  "$REIS_ZIP"
fi
if $DO_WATSON; then
  fn_unzip           "Watson"         "$WATSON_ZIP"
fi
if $DO_WECUI; then
  fn_unzip_subdir    "WorldEdit CUI"  "$WECUI_ZIP"  $(basename "$WECUI_ZIP" .zip)
fi
if $DO_OPTIFINE; then
  fn_unzip           "Optifine"       "$OPTIFINE_ZIP"
fi

#------------------------------------------------------------------------------
# Construct the JAR.

JAR_FILE="$JAR_DIR/minecraft-$MC_VER-$JAR_TYPE.jar"
echo -n "Building $JAR_TYPE JAR..."
jar -cf "$JAR_FILE" -C "$TMP_DIR" . >&/dev/null || \
  { echo "FAILED." && fn_error "failed to build output JAR."; }
echo "DONE."


