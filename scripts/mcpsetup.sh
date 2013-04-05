#!/bin/bash
set -o nounset

#------------------------------------------------------------------------------

THIS_SCRIPT=$(basename "$0")
VERSIONS_DIR=~/.minecraft/versions
MODLOADER_TMP_DIR=/tmp/mcpsetup_modloader

#------------------------------------------------------------------------------
# Echo the message in $* to stderr and exit with error status 1.

fn_usage()
{
  echo >&2 "Usage: $THIS_SCRIPT <minecraft-version> <mcp-version>"
  exit 1
}

#------------------------------------------------------------------------------
# Echo the message in $* to stderr and exit with error status 1.

fn_error()
{
  echo >&2 "ERROR: $@"
  exit 1
}

#------------------------------------------------------------------------------

if [ $# -ne 2 ]; then
  fn_usage
fi

#------------------------------------------------------------------------------

MC_VER="$1"
MCP_VER="$2"

#------------------------------------------------------------------------------

MCP_DIR=~/bin/"mcp$MCP_VER"
if [ -d "$MCP_DIR" ]; then
  fn_error "MCP $MCP_VER has already been installed under $MCP_DIR"
fi

#------------------------------------------------------------------------------
# Check requirements for installation.

MCPSETUP_DIR=~/bin/mcpsetup/"$MC_VER"

fn_requirement()
{
  echo -n "File $1 ... "
  if [ -f "$1" ]; then
    echo "FOUND."
    true
  else
    echo "MISSING."
    false
  fi
}

echo "Checking requirements..."
REQUIREMENTS=true
fn_requirement "$MCPSETUP_DIR/mcp$MCP_VER.zip"      || REQUIREMENTS=false
fn_requirement "$MCPSETUP_DIR/minecraft.jar"        || REQUIREMENTS=false
fn_requirement "$MCPSETUP_DIR/minecraft_server.jar" || REQUIREMENTS=false
fn_requirement "$MCPSETUP_DIR/ModLoader.zip"        || REQUIREMENTS=false
fn_requirement "$MCPSETUP_DIR/snakeyaml-1.10.jar"   || REQUIREMENTS=false

#------------------------------------------------------------------------------

if ! $REQUIREMENTS; then
  fn_error "there are missing requirements for installation."
fi

#------------------------------------------------------------------------------
# Unpack MCP and copy in required JARs and ModLoader.

mkdir -p "$MCP_DIR" && cd "$MCP_DIR" && unzip -qq "$MCPSETUP_DIR/mcp$MCP_VER.zip" || \
  fn_error "failed to unpack mvp$MC_VER.zip"

cp "$MCPSETUP_DIR/minecraft.jar" "$VERSIONS_DIR/minecraft-$MC_VER.jar" || \
  fn_error "failed to copy minecraft-$MC_VER.jar to $VERSIONS_DIR"

rm -rf "$MODLOADER_TMP_DIR" && mkdir -p "$MODLOADER_TMP_DIR" && cd "$MODLOADER_TMP_DIR" && \
jar xf "$MCPSETUP_DIR/minecraft.jar" && rm META-INF/* && unzip -o -qq "$MCPSETUP_DIR/ModLoader.zip" && \
jar cf "$VERSIONS_DIR/minecraft-$MC_VER-ModLoader.jar" . || \
  fn_error "failed to create $VERSIONS_DIR/minecraft-$MC_VER-ModLoader.jar"
  
cp "$MCPSETUP_DIR/minecraft_server.jar" "$MCP_DIR"/jars/ && \
cp -r ~/.minecraft/resources/           "$MCP_DIR"/jars/ && \
cp -r ~/.minecraft/bin                  "$MCP_DIR"/jars/ || \
  fn_error "could not copy stock Minecraft binaries"

rm -f "$MCP_DIR"/jars/bin/minecraft.jar && \
cp "$VERSIONS_DIR/minecraft-$MC_VER-ModLoader.jar" "$MCP_DIR"/jars/bin/minecraft.jar || \
  fn_error "could not copy minecraft-$MC_VER-ModLoader.jar into MCP installation"

mkdir -p "$MCP_DIR/lib" && cp "$MCPSETUP_DIR/snakeyaml-1.10.jar" "$MCP_DIR/lib" || \
  fn_error "failed to add $MCPSETUP_DIR/snakeyaml-1.10.jar to lib/ subdirectory"

#------------------------------------------------------------------------------
# Update MCP.  In 721, this caused decompilation problems.

cd "$MCP_DIR" && echo Yes | ./updatemcp.sh || fn_error "could not update MCP"

#------------------------------------------------------------------------------
# Keep on decompiling until it actually succeeds.

cd "$MCP_DIR"
while [ ! -d "$MCP_DIR"/src/minecraft/net/minecraft/src/ ]; do
  ./decompile.sh || fn_error "could not do initial decompilation"
done

# Save a copy of the Minecraft + ModLoader sources for later patch generation.
mkdir -p "$MCP_DIR"/vanillasrc && cp -r "$MCP_DIR"/src/minecraft "$MCP_DIR"/vanillasrc || \
  fn_error "could not save vanilla Minecraft + ModLoader sources"

cd "$MCP_DIR" && ./recompile.sh || fn_error "could not do initial recompilation"

cd ~/bin/"mcp$MCP_VER"/eclipse/.metadata/.plugins/org.eclipse.core.runtime/.settings && \
cp ~/projects/.metadata/.plugins/org.eclipse.core.runtime/.settings/*.prefs . || \
  fn_error "could not copy Eclipse configuration"

