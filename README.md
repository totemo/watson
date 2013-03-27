Watson Overview
===============

Watson is a Minecraft mod that displays LogBlock (and to a limited extent Prism) logs in 3-D.  It also has some features to make moderation tasks, such as observing chat and managing screenshots, a little easier.  The current features of the mod are:

*  It reassembles chat lines that were split by Bukkit, so that they can be categorised and parsed with regular expressions. Watson can exclude chat lines from being displayed in the client, based on their category.
* It displays individual edits as wireframe 3-D boxes.
* It groups edits of ore blocks into ore deposits, numbers each deposit, shows the numbers in 3-D space and provides commands to teleport to deposits and compute a stone:diamond ratio.
* It draws vectors between edits indicating the time sequence of edits.
* It draws text annotations in 3-D space. These can act as teleport targets.
* Edits and annotations can be saved to files and loaded at a later date.
* There's a simple built-in calculator for working out stone:diamond ratios.
* It uses colour to highlight parts of chat that match regular expressions. This can be used to draw attention to banned words. It can also be used to highlight the names of people, acting as a rudimentary friends list.
* It adds player names to screenshots automatically.
* It does a `/region info regionname` for you when you right click on a region with the wooden sword (rate limited to once every 10 seconds - the wooden sword will simply list the region name the other times).
* In order to shorten coordinate displays and make them easier to read, Watson also hides the LogBlock coords lines from chat and re-echoes them in a custom, brief format, where block IDs are numeric rather than words.  Re-echoed coordinates are assigned colours based on their physical proximity.  This makes separate ore deposits easy to distinguish in the coordinate listing.


Downloads
---------
GitHub has dropped support for uploading files, so downloads will be hosted on Google Drive from now on.

<table>
  <tr>
    <th>Version</th> <th colspan="2">Details</th>
  </tr>
  <tr>
    <td rowspan="4">1.5.1<br>(2013-03-27)</td> <td>sha256sum -b</td> <td>dcb728358e9ef71eb9ff1a778c6cf6fb0d70985e6d936b924fb918e76df28744</td>
  </tr>
  <tr>
    <td>File Name</td> <td>watson-1.5.1-2013-03-27.zip</td>
  </tr>
  <tr>
    <td>Download</td> <td>https://docs.google.com/uc?export=download&id=0Bzf2TVOCqgpXUV9LNVNReGVoN2c</td>
  </tr>
  <tr>
    <td>Changes</td> <td><a href="https://github.com/totemo/watson/blob/master/Changes.md#151-2013-03-27">description</a></td>
  </tr>

  <tr>
    <th>Version</th> <th colspan="2">Details</th>
  </tr>
  <tr>
    <td rowspan="4">1.5<br>(2013-03-16)</td> <td>sha256sum -b</td> <td>661a15a42fc363c25dea53c471221e791315ff883d0e0fcfbd4a76f76c0761cd</td>
  </tr>
  <tr>
    <td>File Name</td> <td>watson-1.5-2013-03-16.zip</td>
  </tr>
  <tr>
    <td>Download</td> <td>https://docs.google.com/uc?export=download&id=0Bzf2TVOCqgpXSnk5YkdyYlFJOUk</td>
  </tr>
  <tr>
    <td>Changes</td> <td><a href="https://github.com/totemo/watson/blob/master/Changes.md#15-2013-03-16">description</a></td>
  </tr>

  <tr>
    <th>Version</th> <th colspan="2">Details</th>
  </tr>
  <tr>
    <td rowspan="4">1.4.7<br>(2013-03-07)</td> <td>sha256sum -b</td> <td>2b17a38597d8854ed16cea6c49f92aedee8db9e0bb0e3de9a342b434ee53e8c2</td>
  </tr>
  <tr>
    <td>File Name</td> <td>watson-1.4.7-2013-03-07.zip</td>
  </tr>
  <tr>
    <td>Download</td> <td>https://docs.google.com/uc?export=download&id=0Bzf2TVOCqgpXUXJKZ2M5X2Z6M0k</td>
  </tr>
  <tr>
    <td>Changes</td> <td><a href="https://github.com/totemo/watson/blob/master/Changes.md#147-2013-03-07">description</a></td>
  </tr>
</table>


Installation
------------
Watson is ModLoader-compatible mod.  It can be installed in either of two ways:

* You can install MagicLauncher and configure it to use ModLoader and the Watson ZIP file appropriate to the version of Minecraft that you are using.
* Alternatively, manually patch the Minecraft JAR file with ModLoader and Watson.

The basic procedure for manually patching Minecraft's JAR file is:

1. Download a version of Watson that matches your current Minecraft version <i>exactly</i>.
1. Download a version of <a href="http://www.minecraftforum.net/topic/75440-v151-risugamis-mods-updated/">ModLoader</a> that also matches your current Minecraft version <i>exactly</i>.
1. Locate the Minecraft JAR file.  On Windows, it will be "%APPDATA%\.minecraft\bin\minecraft.jar".  Typical Windows configurations will not show you the ".jar" on the end of that filename.  On UNIX-like systems (Macs and Linux), it will be ~/.minecraft/bin/minecraft.jar
1. Save a backup copy of your minecraft.jar file (just in case).
1. Open minecraft.jar with your chosen ZIP file editing program.
1. Open ModLoader.zip and copy its full contents into minecraft.jar.
1. If you plan on installing other mods such as Rei's or Optifine, copy and paste the contents of those ZIP files into your modified JAR here.
1. Open the Watson ZIP file and copy its full contents into minecraft.jar.  Due to a current incompatibility with Rei's Minimap, Watson needs to be the last mod installed.  I will try to fix this in a later version.  Apologies.
1. Delete the contents of the META-INF/ folder of your modified Minecraft JAR file.
1. Save the modified Minecraft JAR file.


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
    /lb page 4

![Edits with default vector length.](https://raw.github.com/totemo/watson/master/wiki/images/screenshot1.png)

When listing edits in chat, Watson groups them together based on spatial proximity and echoes the co-ordinates using a different colour for each group.  This allows separate ore deposits to be readily distinguished, for the purpose of selecting which edit to teleport to with /lb tp.  (However, note that there is a separate /w tp feature for teleporting to ore deposits, discussed later.)

To teleport to an edit of interest:

    /lb tp 25

Perhaps, look at what happened immediately before that edit.  The Watson "pre" command displays the edits immediately before the most recently "selected" block. Just teleporting to an edit selects it for this purpose. Alternatively, when you check a block using the LogBlock toolblock (coal ore), that also selects it.

    /w pre
    
By default, "/w pre" queries 45 edits from LogBlock.  You can explicitly override that number:

    /w pre 75
    
There's also a "/w post" command that queries LogBlock for the edits that happened immediately after the selected block:

    /w post
    /w post 60
    
The default numbers of edits for the "/w pre" and "/w post" queries are adjustable using the pre_count and post_count configuration setting, respectively.  If you increase those, you will also want to adjust the max_auto_pages setting to page through all of those results automatically.

Perhaps, look at the immediate vicinity of an edit:

    /lb area 3 player playername time 12h coords

Check individual blocks using a coal ore block. Watson will draw this query result in 3-D, the same as with a "coords" query.

Possibly take some screenshots. The screenshot filename will include the name of the player whose last edit was selected.  Depending on the Watson settings, the screenshot may also be placed in a subdirectory of the Minecraft .minecraft/screenshots directory.  See the section on Screenshot Management for more information.

When you're done investigating, clear the currently stored edits. This also clears the player name (in screenshot filenames) and information about the coordinates, time and block type of the most recently selected edit.

    /w clear

If you forget any of the above commands:

    /w help


### Viewing Ore Deposits

Watson groups adjacent destructions of ore blocks into ore deposits.  Here, "adjacent" includes blocks up to 1 block away along all three cardinal axes simultaneously.  Ore deposits are assigned numeric labels starting at 1 and increasing in time.  All diamonds are numbered first, then emeralds, then iron, gold, lapis, redstone and finally coal.  Thus, if the coordinates of 5 diamond deposits and 10 iron deposits have been retrieved from the LogBlock database, the diamond deposits will be numbered from 1 to 5, with 1 being the oldest diamond, and the iron deposits will be numbered from 6 to 15, with 6 being the oldest iron deposit.

![Vector length 1.](https://raw.github.com/totemo/watson/master/wiki/images/screenshot2.png)

Ore deposits are colour-coded according to ore type, with diamonds listed in light blue, emerald in light green, iron orange, gold yellow, lapis blue, redstone red and coal listed in dark grey.

To list all of the deposits:

    /w ore

Or if there are multiple pages (50 deposits to a page) you may need to specify a page number:

    /w ore 2

The "/w tp" command can teleport to the next deposit in the sequence (starting at one), the previous one, or the deposit with a specific number:

    /w tp
    /w tp next
    /w tp prev
    /w tp 17

The "/w tp" command is just a synonym for "/w tp next".  Teleporting to an ore deposit with "/w tp" selects that deposit, so that "/w pre" will show the edits leading up to it.

To automatically compute stone:diamond ratios for the current set of diamond deposits:

    /w ratio
    
Watson will compute one stone:diamond ratio for the time period that includes all diamond deposits listed by /w ore.  If there are segments of time where diamonds were mined particularly quickly, Watson will compute additional stone:diamond ratios for those smaller time segments too.

It is also possible to see what the current time is at the server, which can be useful information when looking at LogBlock time stamps:

    /w servertime

The numbers of deposits are drawn in 3-D and can be hidden, shown or toggled with the "/w label" command:

    /w label off
    /w label on
    /w label

Given the above commands for working with ore deposits, a basic x-ray checking procedure would be as follows:

1. List top miners: `/lb time 12h block 56 sum p`
1. List diamond edits for one particular miner: `/lb time 12h player fred block 56 coords`
1. Page through all coordinate results: `/lb next`
1. List ore deposits.  This will show their timestamps: `/w ore`
1. Check the stone:diamond ratio: `/w ratio`
1. Teleport to specific deposits for more detailed examination: `/w tp`
1. See what happened before the deposit in question was uncovered: `/w pre`


### Manipulating the Vector and Outline Displays

Watson draws vectors (arrows) from each edit to the next edit which is more recent, provided that the distance in space between the edits is greater than the minimum vector length.  The default minimum length is 4.  To draw vectors between all edits:

    /w vector length 1

To hide, show or toggle the vector display:

    /w vector off
    /w vector on
    /w vector
    
Watson remembers whether the vector display was on or off the last time Minecraft was run and uses that as the default state at startup.

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
    
Or alternatively:

    /w file list *
    
If there are multiple pages of files (50 to a page) then you may need to specify a page number:

    /w file list * 3

To list all files for players whose names begin with the specified text (case insensitive); the example below would list Notch's edit files, possibly among others:

    /w file list notc

To load the most recently saved file for a given player name (case insensitive):

    /w file load notch

Files can be deleted by specifying a pattern for the beginning of the file name:

    /w file delete not
    
You can delete all files with the asterisk:

    /w file delete *

You can also delete all files older than a specified date, in the form YYYY-MM-DD.  For example, to delete all files that were last modified before 2013:

    /w file expire 2013-01-01


### Built-In Calculator

Watson contains a simple calculator that understands +, -, *, / and parentheses ().  Currently, the calculator considers '-' to bind to any digits that immediately follow (making a negative number), so when subtracting, use spaces.  Example:

    /calc 800/(57 - 32)


### Highlighting Chat Content

Watson can highlight text that matches a specified [Java regular expression](http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html) using colour and formatting.  Let's say we'd like to make the "Unknown command." error message stand out a bit more by making it bright red:

    /hl add red ^Unknown\scommand.*

All currently defined highlights are applied to a line of chat in the order that they were defined.

Valid colour names are: black, darkblue/navy, darkgreen/green, cyan, darkred/red, purple, orange/gold/brown, lightgrey/lightgray, darkgrey/darkgray/grey/gray, blue, lightgreen, lightblue, lightred/brightred/rose, pink/lightpurple/magenta, yellow, white

The /hl add command allows a style instead of a colour, or preceding the colour.

<table>
  <tr>
    <th>Style</th> <th>Code</th> <th>Example</th>  <th>Meaning</th>
  </tr>
  <tr>
    <td>Bold</td> <td>+</td> <td><pre>/hl add + hello</pre></td>  <td>Highlight hello in bold.</td>
  </tr>
  <tr>
    <td>Italic</td> <td>/</td> <td><pre>/hl add /orange ^&lt;\w+&gt;</pre></td>  <td>Highlight the player name in global chat messages in orange italics.</td>
  </tr>
  <tr>
    <td>Underline</td> <td>_</td> <td><pre>/hl add _ the</pre></td>  <td>Underline "the".</td>
  </tr>
  <tr>
    <td>Strikethrough</td> <td>-</td> <td><pre>/hl add - redacted</pre></td>  <td>Strike through the word "redacted".</td>
  </tr>
  <tr>
    <td>Random</td> <td>?</td> <td><pre>/hl add ? magic</pre></td>  <td>Replace "magic" with random glyphs.</td>
  </tr>
</table>

To list the existing patterns if you need to remove any:

    /hl list

To remove a specific pattern by number:

    /hl remove 3

And if you forget any commands, try:

    /hl help


### Hiding Chat Lines by Category

Watson classifies chat text into categories according to patterns specified in chatcategories.yml in the Minecraft JAR file (part of the Watson ZIP file).  That file can be overridden by extracting it to .minecraft/mods/watson/chatcategories.yml.  Individual categories can be hidden from chat using the /tag command.

To turn off chat lines, e.g. the deathroll:

    /tag hide server.obituary
    /tag hide server.pvp

To turn chat lines back on:

    /tag show server.pvp

To list the lines that are currently excluded from chat:

    /tag list

And for help:

    /tag help


### Screenshot Management

If you ban players for grief or xray, inevitably you will end up with a large number of screenshots that must be retained until the ban is appealed.  Watson includes features to make it easier to manage many Minecraft screenshots and to find the ones that pertain to a particular player.

When the name of the player is known (because Watson saw an "/lb coords" result for that player since the last "/w clear") the screenshot will be placed in the directory .minecraft/screenshots/&lt;playername&gt;/, and &lt;playername&gt; will be appended to the filename, e.g. ".minecraft/screenshots/Notch/2013-02-21_12.47.50-Notch.png".  Both of these behaviours can be turned on or off using the ss_player_directory and ss_player_suffix configuration settings, respectively.

When the player name is not known, then by default the screenshot just ends up in .minecraft/screenshots/.  But Watson can be configured to place the screenshot in a subdirectory based on the current date and time.  For example, "/w config ss_date_directory yyyy-MM-dd" would put the screenshot in a subdirectory based on the full numeric year, month and day, e.g. 2013-03-15.  Whereas "/w config ss_date_directory MMMM yyyy" would use the long name of the month, e.g. "March 2013".  There are many options and they are described in greater detail in the section on the Configuration File.


### Prism Support

Although Watson was originally developed for use with LogBlock, some basic support for Prism has recently been added.  Watson can now display "break", "place" and "pour" actions for any queries that return extended results (i.e. contain coordinates).  In order for Watson to show Prism inspector results, Prism must be configured to <i>always</i> return extended results, as follows:

    messenger:
      always-show-extended: true

If that configuration setting is not made, it is still possible to get extended results from <i>lookups</i> using the -extended parameter:

    /prism l r:20 p:totemo -extended
    
By default, Prism groups together what it considers to be related edits.  For example, if a player placed red wool 5 minutes ago and just now places green wool, Prism may report that as placing multiple red wool 5 minutes ago.  In order for watson to show individual edits, it is necessary to configure Prism to report each edit separately by setting lookup-auto-group to false in the configuration of the plugin.

At the time of writing, Watson does not support automatically calculating stone:diamond ratios, or automatically paging through results when used with Prism.


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
    <td>watson_prefix</td> <td>string</td> <td>w</td> <td>Set the prefix with which all Watson commands begin.</td> <td>/w config watson_prefix watson<br>/watson help<br>/watson config watson_prefix w</td>
  </tr>
  <tr>
    <td>debug</td> <td>on / off</td> <td>off</td> <td>Enable/disable all debug messages in the log file.</td> <td>/w config debug</td>
  </tr>
  <tr>
    <td>auto_page</td> <td>on / off</td> <td>off</td> <td>Enable/disable automatic paging through "/lb cooords" results (up to max_auto_pages pages).</td> <td>/w config auto_page on</td>
  </tr>
  <tr>
    <td>max_auto_pages</td> <td>integer</td> <td>3</td> <td>The number of pages of "/lb coords" results to step through automatically.</td> <td>/w config max_auto_pages 4</td>
  </tr>
  <tr>
    <td>pre_count</td> <td>integer</td> <td>45</td> <td>The number of "/lb coords" results that will be returned by "/w pre", by default.</td> <td>/w config pre_count 60</td>
  </tr>
  <tr>
    <td>post_count</td> <td>integer</td> <td>45</td> <td>The number of "/lb coords" results that will be returned by "/w post", by default.</td> <td>/w config post_count 60</td>
  </tr>
  <tr>
    <td>region_info_timeout</td> <td>decimal number of seconds >= 1.0</td> <td>5.0</td> <td>Minimum elapsed time between automatic "/region info" commands when right clicking with the wooden sword.</td> <td>/w config region_info_timeout 3</td>
  </tr>
  <tr>
    <td>chat_timeout</td> <td>decimal number of seconds >= 0.0</td> <td>1.1</td> <td>Minimum elapsed time between automatically issued commands commands (e.g. /lb next) being sent to the server in the form of chat messages.</td> <td>/w config chat_timeout 1.0</td>
  </tr>
  <tr>
    <td>billboard_background</td> <td>ARGB colour as 8 hexadecinal digits</td> <td>A8000000</td> <td>The colour of the background of annotation and ore label billboards.</td> <td>/w config billboard_background 7f000000</td>
  </tr>
  <tr>
    <td>billboard_foreground</td> <td>ARGB colour as 8 hexadecinal digits</td> <td>7FFFFFFF</td> <td>The colour of the foreground of annotation and ore label billboards.</td> <td>/w config billboard_foreground 7fa0a0a0</td>
  </tr>
  <tr>
    <td>group_ores_in_creative</td> <td>on / off</td> <td>on</td> <td>If "on", edits are grouped into ore deposits even in creative mode.  If "off", that processing only happens in survival mode.  Currently defaulted to on until a reliable way to distinguish the server's gamemode from that of the player is determined.</td> <td>/w config group_ores_in_creative on</td>
  </tr>
  <tr>
    <td>teleport_command</td> <td>format string</td> <td>/tppos %g %d %g</td> <td>Specifies the formatting of the command used to teleport to specific coordinates in the implementation of "/w tp" and "/anno tp" commands.  Only %d (for integers) and %g (for decimal numbers) are supported as formatting specifiers.</td> <td>/w config teleport_command /tppos %d %d %d</td>
  </tr>
  <tr>
    <td>ss_player_directory</td> <td>on / off</td> <td>on</td> <td>When on, each screenshot is placed in a subdirectory: .minecraft/screenshots/&lt;player&gt;/, where &lt;player&gt; is the player who performed the most recently selected edit.  If there is no currently selected player, then the value of the ss_date_directory setting determines the name of the directory where the screenshot will be stored.</td> <td>/w config ss_player_directory off</td>
  </tr>
  <tr>
    <td>ss_player_suffix</td> <td>on / off</td> <td>on</td> <td>When on, each the name of the player who performed the most recently selected edit is appended as a suffix of each screenshot file name.</td> <td>/w config ss_player_suffix off</td>
  </tr>
  <tr>
    <td>ss_date_directory</td> <td>date format string</td> <td></td> <td>This setting determines the directory to store screenshots when ss_player_directory is off, or when no player is currently selected.  The setting is a format specifier for the Java <a href="http://docs.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html">SimpleDateFormat</a> class, interpreted in the user's locale (language settings), allowing considerable flexibility in the name of the output directory.  If set to the empty string (the default setting), then screenshots without a selected player will end up in .minecraft/screenshots/.  If a format is specified, then a subdirectory of .minecraft/screenshots/ is created to place each screenshot in, based on the time and date when the image was taken.  Recommended settings include "yyyy-MM-dd" (numeric year, month and day, e.g. 2013-03-15), "yyyy-MM" (year and month, e.g. 2013-03) and "MMMM yyyy" (month in long form and year, e.g. March 2013).  The format can be set to the empty string using the command "/w config ss_date_directory".</td> <td>/w config ss_date_directory yyyy-MM-dd</td>
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

* Minecraft with ModLoader
* WorldEditCUI
* Rei's Minimap
  * However, there is a slight compatibility issue that prevents Rei's player/entity radar from working, and Watson must be installed after Rei's in order to function correctly.
* Optifine
* LiteLoader for Minecraft
* Macro/Keybind Mod - but note that commands originating here bypass the Watson command interpreter and go direct to the server.
* MagicLauncher


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


Planned Features
----------------

* A simple keybinding facility, since the Macro/Keybind mod bypasses the Watson CLI.
* Parsing of fields out of LogBlock results is currently hard-coded. This can and should be driven by a description of the fields in chatcategories.yml.
* A 3-D cursor that can highlight edits and step through them in the sequence that they occurred.
* The ability to filter edits and coalblock results by player, distance, time etc.
* Some automatic queries to hone in on probable grief and xray patterns.
* The ability to customise the re-echoing of coordinate lines.


Bugs
----

* If you see a block drawn as bright magenta and somewhat smaller than 1 cubic meter, it means that the name for that block in blocks.yml doesn't match what LogBlock calls it. Let me know.
* Re-echoed coordinate lines are currently hard coded to not echo stone at all. This should be customisable.
* The calculator should probably use a custom lexer (rather than JDK class) so that extra spaces in mathematical expressions can be removed.
* Command line help is ugly because of the variable width font.

