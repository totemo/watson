Watson Overview
===============

Watson is a Minecraft mod designed to make the task of moderating on the reddit Minecraft servers a little easier.  The current features of the mod are:

*  It reassembles chat lines that were split by Bukkit, so that they can be categorised and parsed with regular expressions. Watson can exclude chat lines from being displayed in the client, based on their category.
* It displays individual edits as wireframe 3-D boxes.
* It draws vectors between edits indicating the time sequence of edits.
* It draws text annotations in 3-D space. These can act as teleport targets.
* Edits and annotations can be saved to files and loaded at a later date.
* There's a simple built-in calculator for working out stone:diamond ratios.
* It uses colour to highlight parts of chat that match regular expressions. This can be used to draw attention to banned words. It can also be used to highlight the names of people, acting as a rudimentary friends list.
* It adds player names to screenshots automatically.
* It does a `/region info regionname` for you when you right click on a region with the wooden sword (rate limited to once every 10 seconds - the wooden sword will simply list the region name the other times).
* In order to shorten coordinate displays and make them easier to read, Watson also hides the LogBlock coords lines from chat and re-echoes them in a custom, brief format, where block IDs are numeric rather than words.  Re-echoed coordinates are assigned colours based on their physical proximity.  This makes separate ore deposits easy to distinguish in the coordinate listing.

Using Watson
------------
### Viewing Edits

Turn on the Watson display. This display is turned on and off automatically when switching in and out of modmode on S and P:

    /w display on

The Watson display can be toggled by omitting the on|off parameter:

    /w display

Use LogBlock to get the coordinates.  As Watson sees coordinates listed in chat, it makes a record of the edit and draws a wireframe outline of the block where the edit occurred.

    /lb time 12h player playername block 56 coords
    /lb page 2
    /lb page 3

![Edits with default vector length.](https://raw.github.com/totemo/watson/master/wiki/images/2012-10-24_02.20.01-flipflopfla.png)

Watson groups edits together based on spatial proximity and echoes the co-ordinates in chat using a different colour for each group.  This allows separate ore deposits to be readily distinguished.

To teleport to an edit of interest:

    /lb tp 25

Perhaps, look at what happened immediately before that edit.  The Watson "pre" command displays the edits immediately before the most recently "selected" block. Just teleporting to an edit selects it for this purpose. Alternatively, when you check a block using the LogBlock toolblock (coal ore), that also selects it.

    /w pre

Perhaps, look at the immediate vicinity of an edit:

    /lb area 3 player playername time 12h coords

Check individual blocks using a coal ore block. Watson will draw this query result in 3-D, the same as with a "coords" query.

Possibly take some screenshots. The screenshot filename will include the name of the player whose last edit was selected.

When you're done investigating, clear the currently stored edits. This also clears the player name (in screenshot filenames) and information about the coordinates, time and block type of the most recently selected edit.

    /w clear

If you forget any of the above commands:

    /w help


### Manipulating the Vector and Outline Displays

Watson draws vectors (arrows) from each edit to the next edit which is more recent, provided that the distance in space between the edits is greater than the minimum vector length.  The default minimum length is 4.  To draw vectors between all edits:

    /w vector length 1

![Vector length 1.](https://raw.github.com/totemo/watson/master/wiki/images/2012-10-24_02.20.31-flipflopfla.png)

To hide, show or toggle the vector display:

    /w vector off
    /w vector on
    /w vector

To hide, show or toggle the outlines of blocks:

    /w outline off
    /w outline on
    /w outline


### Manipulating Annotations

Annotations are text associated with a particular location and displayed in 3-D space.  They are similar to waypoints in the Rei's Minimap mod.

To create an annotation, first get a location, either with /lb coords query, or by simply using the LogBlock toolblock (coal ore) to mark a position.  When using the LogBlock toolblock, it is not necessary for the LogBlock database to contain any edits for that location.  The coordinates will be noted by Watson, regardless.

To add an annotation:

    /anno add This is the spot

To list all annotations:

    /anno list

To remove a single annotation, by number:

    /anno remove 1

Teleport to an annotation, by number:

    /anno tp 3

To hide, show or toggle the visibility of all annotations:

    /w anno off
    /w anno on
    /w anno

To remove all annotations:

    /anno clear


### Saving and Loading Edits from Files

Watson can save the current set of edits and annotations to a file in .minecraft/mods/watson/saves/.  Watson save files are in a self-explanatory text format that can be processed by UNIX text processing tools like `grep`.  If you don't specify a file name, Watson derives one from the current local time and the name of the player who performed the most recently selected edit.

To save a file (for Notch's edits the file might be Notch-2012-10-23-17.21.34):

    /w file save

To list all files:

    /w file list

To list all files for players whose names begin with the specified text (case insensitive); the example below would list Notch's edit files, possibly among others:

    /w file list notc

To load the most recently saved file for a given player name (case insensitive):

    /w file load notch


### Built-In Calculator

Watson contains a simple calculator that understands +, -, *, / and parentheses ().  Currently, the calculator considers '-' to bind to any digits that immediately follow (making a negative number), so when subtracting, use spaces.  Example:

    /calc 800/(57 - 32)

### Highlighting Chat Content

Let's say we'd like to make the "Unknown command." error message stand out a bit more by making it bright red:

    /hl add red ^Unknown\scommand.*

List the existing patterns if you need to remove any:

    /hl list

Remove a specific pattern by number:

    /hl remove 3

And if you forget any commands, try:

    /hl help

### Hiding Chat Lines by Category

To turn off chat lines, e.g. the deathroll:

    /tag hide server.obituary
    /tag hide server.pvp

To turn chat lines back on:

    /tag show server.pvp

To list the lines that are currently excluded from chat:

    /tag list

And for help:

    /tag help

The list of tag names is a file chatcategories.yml in the Minecraft JAR file.  It can be overridden by extracting it to .minecraft/mods/watson/chatcategories.yml.


### Configuration File

Watson's main configuration settings are stored in ".minecraft/mods/watson/configuration.yml".  They can be changed using the "/w config" command.  If a setting can be either "on" or "off", omitting a value for it in "/w config" will reverse the current value.

<table>
  <tr>
    <th>Setting</th> <th>Values</th> <th>Default</th>  <th>Purpose</th> <th>Example</th>
  </tr>
  <tr>
    <td>watson</td> <td>on / off</td> <td>on</td> <td>Enable/disable all Watson functions.</td> <td>/w config watson off</td>
  </tr>
  <tr>
    <td>debug</td> <td>on / off</td> <td>off</td> <td>Enable/disable all debug messages in the log file.</td> <td>/w config debug</td>
  </tr>
  <tr>
    <td>auto_page</td> <td>on / off</td> <td>off</td> <td>(Experimental) Enable/disable automatic paging through "/w pre" results (up to 3 pages).</td> <td>/w config auto_page on</td>
  </tr>
  <tr>
    <td>region_info_timeout</td> <td>decimal number of seconds >= 1.0</td> <td>5.0</td> <td>Minimum elapsed time between automatic "/region info" commands when right clicking with the wooden sword.</td> <td>/w config region_info_timeout 3</td>
  </tr>
  
</table>


Files
-----

* **.minecraft/mods/watson/log.txt** - The debugging log. Also includes a log of chat messages.
* **.minecraft/mods/watson/configuration.yml** - The main configuration file.  Stores a variety of settings that persist between Minecraft sessions.
* **.minecraft/mods/watson/chatexclusions.yml** - The list of excluded chat category tags in YAML format.
* **.minecraft/mods/watson/chathighights.yml** - The list of colours and regular expressions for highlighting chat content. The default contents of this file are saved in the modified minecraft.jar file and saved as a separate file the first time /hl add or /hl remove is run.
* **.minecraft/mods/watson/chatcategories.yml** - If this file exists, it overrides the default version of it stored in minecraft.jar. It defines the categories of chat lines and the regular expressions used to recognise them.
* **.minecraft/mods/watson/blocks.yml** - If this file exists, it overrides the default version of it stored in minecraft.jar. It defines the canonical names of block types, as they appear in LogBlock query results, as well as aliases, and defines the shape, colour and line thickness used to draw the block in 3-D.
* **.minecraft/mods/watson/saves/** - Directory of save files containing records of edited blocks and annotations.


Compatibility
-------------

Watson has been tested for compatibility with:

* Minecraft 1.4.2 with ModLoader 1.4.2
* WorldEditCUI for 1.4.2 (when that is available)
* Rei's Minimap for 1.4.2, version 3.2_05
* Optifine 1.4.2_HD_U_A7
* LiteLoader for Minecraft 1.4.2
* Macro/Keybind Mod 0.9.5 - but note that commands originating here bypass the Watson command interpreter and go direct to the server.
* MagicLauncher


Building
--------

### Notes

* The build scripts use variables set in scripts/watson_common.sh to customise the paths to inputs and outputs.
* scripts/watson_binaries.sh outputs a ZIP file of the mod classes and resources in ~/.minecraft/versions/.  The ZIP can be loaded with MagicLauncher or applied to minecraft.jar as a patch.
* A copy of src/watson/*.yml is placed in the ZIP under watson/.  These serve as defaults for configuration files.
* The SnakeYAML classes are also built into the ZIP.

### Procedure

1. Ensure that scripts/watson_common.sh is correct for your environment.  In particular, check that the MCP_DIR variable matches the location of your MCP installation.
2. Patch minecraft.jar with ModLoader.
3. Decompile with MCP.
4. Copy in the Watson sources.
5. Patch the Mojang sources with src/net/minecraft/src/*.java.patch.
6. Put snakeyaml-1.10.jar in the mcp<version>/lib/ directory.
7. Run scripts/watson_binaries.sh.


Planned Features
----------------

* A client-side spatial database for grouping adjacent edits (e.g. ores) together to facilitate better reporting of related edits (e.g. grouping related diamond edits) that can then act as a target for a smart teleport feature.
* A simple keybinding facility, since the Macro/Keybind mod bypasses the Watson CLI.
* Parsing of fields out of LogBlock results is currently hard-coded. This can and should be driven by a description of the fields in chatcategories.yml.
* A 3-D cursor that can highlight edits and step through them in the sequence that they occurred.
* The ability to filter edits and coalblock results by player, distance, time etc.
* Rei's minimap style billboards identifying the timestamps of key edits (e.g. ores).
* Some automatic queries to hone in on probable grief an xray patterns.
* The ability to customise the re-echoing of coordinate lines.


Bugs
----

* If you see a block drawn as bright magenta and somewhat smaller than 1 cubic meter, it means that the name for that block in blocks.yml doesn't match what LogBlock calls it. Let me know.
* Re-echoed coordinate lines are currently hard coded to not echo stone at all. This should be customisable.
* Currently any code that deals with timestamps assumes the current year. This will break around New Year's.
* The calculator should probably use a custom lexer (rather than JDK class) so that extra spaces in mathematical expressions can be removed.
* Command line help is ugly because of the variable width font.

