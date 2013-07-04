Change History
==============
1.5.2 (2013-07-03)
------------------
* Modified to work with the Minecraft Forge mod API (7.8.1.737) instead of ModLoader.
* Removed one dependency on core Minecraft classes (NetClientHandler), but it's still necessary to patch your Minecraft JAR.
* Requests made to the Forge developers to add Forge APIs for screenshots and client-side commands.

1.5.2 (2013-06-01)
------------------
* Remove the ability to rejoin lines split by the server. It's not necessary and can go wrong whenever Watson doesn't recognise a particular message.

1.5.2 (2013-05-15)
------------------
* Compatibility fix for newer Prism versions (including 1.6.1) that number each query result in square brackets.
* Added aliases for various skull types (for Prism).
* Bug fix for Prism when parsing query results that are not place, break or pour.
* Remove the WorldGuard 'Can you build?' message from the default set of chat exclusions.

1.5.2 (2013-05-05)
------------------
* Updated for Minecraft 1.5.2 and Macro/Keybind Mod 0.9.9 for Minecraft 1.5.2.
* Added an information overlay GUI to the sample Macro/Keybind Mod configuration (Ctrl-O to toggle).

1.5.1 (2013-04-28)
------------------
* Added an option to highlight just selected portions of a chat line.
* Fixed a crash in the ChatClassifier.
* Added basic integration with the Macro/Keybind Mod, in the form of a WATSON(<string>) script action, %WATSON_*% variables, and events "onWatsonDisplay" and "onWatsonSelection".

1.5.1 (2013-04-10)
------------------
* Automatically toggle the Watson display when enabling/disabling Duties Mode (Duties plugin).  If the Duties messages for these events have been customised, you will need to modify .minecraft/mods/watson/chatcategories.yml (or the file in watson-1.5.1-2013-04-10.zip) accordingly.
* Added basic support for CoreProtect inspector and lookup results.
* Fixed a bug in removing formatting codes from chat in the chat classifier.
* Fixed a bug setting screenshot names for replacement edits.
* Allow the default Duties plugin /dutymode messages to enable/disable the Watson display.

1.5.1 (2013-04-05)
------------------
* Fixed a bug in mod_ClientCommands allowing commands to omit the leading '/'.
* Fixed '/w ratio' not calculating for very recent mining.
* Fixed disabling of Rei's Minimap entities radar.  Added support for other similar mechanisms.
* Fixed a bug where player name for screenshots would be set even for edits excluded by the filter.
* Change '/w edits (hide|show|remove)' and '/w filter (add|remove)' to work with lists of player names.

1.5.1 (2013-04-02)
------------------
* Basic support for the Prism plugin.
* Drop logging of chat to console; the vanilla client now does that.
* Fixed rendering of upward pointing vectors.
* Render vectors for each player separately.
* Added new models for plants, stairs, hoppers and anvils.
* Made patterns for save and restart chats more generic.
* Allow control over filtering of parsed edits on a per-player basis.
* Allow control of visibility of edits and removal of edits on a per-player basis.
* Label quartz ore deposits.

1.5.1 (2013-03-27)
------------------
* Updated for 1.5.1.
* Adjusted the colour of nether quartz ore for better contrast with netherrack.
* Added basic support for displaying results of Prism's inspector and -extended queries.

1.5 (2013-03-16)
----------------
* Updated for 1.5.

1.4.7 (2013-03-07)
------------------
* Recompiled with JDK 7.
* Changed all string formatting to use an explicit US locale so that user's language settings don't break file I/O and server teleport commands.
* Added a chat_timeout setting to configure the rate at which all programmatically generated commands are sent to the server via chat.
* Recognise "piston moving piece" as a type of block.
* Added optional count parameter to "/w pre" to specify number of edits to fetch.
* Added "/w post [&lt;count&gt;]" to query up to count edits after the selected edit.
* Added a configuration option for the maximum number of pages to query automatically.
* Added configuration options for the default count values for "/w pre" and "/w post".
* Added paging of "/w file list" output.
* Added paging of "/w ore" output.
* Added a configuration option for the Watson command prefix.
* Added a "/w file delete &lt;prefix&gt;" command to delete save files.
* Added a "/w file expire &lt;YYYY-MM-DD&gt;" to delete files last modified before a specified date.
* Added configuration settings to customise naming and output directories of screenshots.

1.4.7 (2013-01-23)
------------------
* Recompiled for 1.4.7.

1.4.6 (2013-01-07)
------------------
* Added a configurable format specifier for /tppos-style commands.
* Default to grouping ores in creative, since detecting the server's gamemode doesn't work.
* Fixed initial suppression of 'No results found.' message.

1.4.6 (2012-12-29)
------------------
* Added nether brick slabs to blocks.yml.
* Recompiled against non-buggy ModLoader.
* Uploaded binaries.

1.4.6 (2012-12-22)
------------------
* Recompiled 1.4.5 (2012-12-18) for 1.4.6.

1.4.5 (2012-12-18)
------------------
* Added spatial database, ore listing (/w ore) and ore teleport (/w tp) features.
* Added labels to each ore deposit; visibility controlled with /w label.
* Added automatic stone:diamond ratio calculations (/w ratio).
* Added "/w servertime" to get the time at the server.
* Fixed code to guess the year in LogBlock timestamps.
* Fixed re-echoing of sign contents in /lb coords queries.
* The visibility of the vector display is now saved between client sessions.
* Rendering of edits is now unaffected by REI's minimap rendering settings.
* Added some chat categories in an attempt to fix an intermittent bug echoing /search results.
* Suppress the version in the startup message if it is "unknown".
* Automatic paging through LogBlock coords results of 3 pages or less is now the default for new installations.
  * If you have run previous versions, enable this with: /w config auto_page on
* Adjusted the default highlight patterns to highlight some common errors and reduce false positives.

1.4.5 (2012-11-28)
------------------
* Added support for text styles in the chat highlighter.

1.4.5 (2012-11-22)
------------------
* Fixed client crash when entering / on its own (Dumbo52 patch).
* Allow world names in chatcategories.yml to be essentially arbitrary.

1.4.4 (2012-11-20)
------------------
* Recompiled 1.4.2 (2012-11-20) for 1.4.4

1.4.2 (2012-11-20)
------------------
* Configuration setting for rate of automatic /region info command with wood sword.
* Experimental support for automatic paging through /w pre results (configured off by default).
* Throttling of commands sent to server.
* Regexp fixes in chatcategories.yml.
* Allow highlight patterns to contain spaces, rather than requiring \s.

1.4.2 (2012-11-15)
------------------
* Added configuration file and commands that can enable/disable the mod as a whole.
* Load mod version from resource in JAR.
* Show version number and help command in startup banner.
* /w help mentions github doco and /tag, /anno and /hl commands.
* Removed limit on count of re-echoed coordinates.
* Removed leading space in /calc output.

