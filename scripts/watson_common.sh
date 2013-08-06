#------------------------------------------------------------------------------
# These lines need to change with the Minecraft and MCP versions.

MCP_VER=804
MC_VER=1.6.2
FML_VER=7

#------------------------------------------------------------------------------
# These lines only change if your directory setup is different to mine.

MCP_DIR=~/bin/forge/mcp
GIT_DIR=~/projects/watson
JAR_DIR=~/.minecraft/versions

#------------------------------------------------------------------------------

DATE=$(date +%Y-%m-%d)

#------------------------------------------------------------------------------
# Echo the message in $* to stderr and exit with error status 1.

fn_error()
{
  echo >&2 "ERROR: ""$@"
  exit 1
}

#------------------------------------------------------------------------------


