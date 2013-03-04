Change History
==============
1.4.7 (2013-03-04)
------------------
* Recompiled with JDK 7.
* Changed all string formatting to use an explicit US locale so that user's language settings don't break file I/O and server teleport commands.
* Added a chat_timeout setting to configure the rate at which all programmatically generated commands are sent to the server via chat.

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

