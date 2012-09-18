#!/bin/bash

DO_WECUI=true
DO_REI=true
DO_OPTI=true
DO_LITE=true

STAGE_DIR=/tmp/stage
MODS_DIR=/tmp/mods

ROUNDTRIP_JAR=~/.minecraft/older_versions/minecraft-1.3.2-roundtrip.jar
MOD_JAR=~/.minecraft/older_versions/minecraft-1.3.2-mod.jar

WECUI_ZIP=~/bin/mcp72/compat/WorldEditCUI-1.3.2a.zip
WECUI_SUBDIR=WorldEditCUI-1.3.2a/classes/

REI_ZIP=~/bin/mcp72/compat/'[1.3.2]ReiMinimap_v3.2_05.zip'
REI_SUBDIR=./

OPTI_ZIP=~/bin/mcp72/compat/OptiFine_1.3.2_HD_U_B3.zip
OPTI_SUBDIR=./

LITE_ZIP=~/bin/mcp72/compat/liteloader_1.3.2.zip
LITE_SUBDIR=./

#------------------------------------------------------------------------------

fn_error()
{
  echo >&2 "ERROR: ""$@"
  exit 1
}

#------------------------------------------------------------------------------

rm -rf $MODS_DIR && mkdir -p $MODS_DIR && cd $MODS_DIR || fn_error "could not set up mod unpacking area"
if $DO_WECUI; then
  unzip -qq $WECUI_ZIP || fn_error "could not unpack $WECUI_ZIP"
fi
if $DO_REI; then
  unzip -qq $REI_ZIP || fn_error "could not unpack $REI_ZIP"
fi
if $DO_OPTI; then
  unzip -qq $OPTI_ZIP || fn_error "could not unpack $OPTI_ZIP"
fi
if $DO_LITE; then
  unzip -qq $LITE_ZIP || fn_error "could not unpack $LITE_ZIP"
fi

#------------------------------------------------------------------------------

rm -rf $STAGE_DIR && mkdir -p $STAGE_DIR || fn_error "could not create staging area"
cd $STAGE_DIR && jar xf $ROUNDTRIP_JAR || fn_error "could not unpack roundtrip JAR"

if $DO_WECUI; then
  cd $MODS_DIR/$WECUI_SUBDIR || fn_error "could not change to WECUI directory"
  rsync -a ./ $STAGE_DIR || fn_error "could not copy WECUI to staging area"
fi
if $DO_REI; then
cd $MODS_DIR/$REI_SUBDIR || fn_error "could not change to Rei's directory"
rsync -a ./ $STAGE_DIR || fn_error "could not copy Rei's to staging area"
fi
if $DO_OPTI; then
  cd $MODS_DIR/$OPTI_SUBDIR || fn_error "could not change to Optifine directory"
  rsync -a ./ $STAGE_DIR || fn_error "could not copy Optifine to staging area"
fi
if $DO_LITE; then
  cd $MODS_DIR/$LITE_SUBDIR || fn_error "could not change to LiteLoader directory"
  rsync -a ./ $STAGE_DIR || fn_error "could not copy LiteLoader to staging area"
fi

#------------------------------------------------------------------------------

cd $STAGE_DIR && \
rm -f $MOD_JAR && \
jar cf $MOD_JAR . || fn_error "could not re-create mod JAR"

