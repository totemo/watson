Building
--------

### Notes

* The build scripts use variables set in scripts/watson_common.sh to customise the paths to inputs and outputs.
* scripts/watson_binaries.sh outputs a ZIP file of the mod classes and resources in ~/.minecraft/versions/.  The ZIP can be loaded with MagicLauncher or applied to minecraft.jar as a patch.
* A copy of src/watson/*.yml is placed in the ZIP under watson/.  These serve as defaults for configuration files.
* A simple text file, watson/version is stored as a resource in the ZIP and displayed as the current Watson version at startup.
* The SnakeYAML classes are also built into the ZIP.

### Procedure

1. Ensure that scripts/watson_common.sh is correct for your environment.  In particular, check that the MCP_DIR variable matches the location of your MCP installation.
2. Patch minecraft.jar with ModLoader.
3. Decompile with MCP.
4. Copy in the Watson sources.
5. Patch the Mojang sources with src/net/minecraft/src/*.java.patch.
6. Put snakeyaml-1.10.jar in the mcp<version>/lib/ directory.
7. Run scripts/watson_binaries.sh.

