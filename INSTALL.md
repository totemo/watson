Installation
------------

Prior to the 1.5.2 (2013-07-03) version, Watson was a ModLoader-compatible mod.  From that version onwards, it requires Minecraft Forge to work.

With either the older versions of Watson or the latest Forge versions, you will need to install the modding API (ModLoader or Forge) and then copy the Watson classes into your Minecraft JAR.

As of Minecraft version 1.6, the Minecraft launcher enables mods to inject modifications into Minecraft without changing the contents of the Minecraft JAR file.  I'm excited by the prospect, but the documentation on doing what Forge refers to as "CoreMod"s is scant and I haven't had the time to reverse-engineer it.  So for now, you must patch your JAR file the old fashioned way.

1. Download a version of Watson that matches your current Minecraft version <i>exactly</i>.
1. Download the recommended <i>installer</i> for Minecraft Forge from <a href"http://files.minecraftforge.net/">the Forge downloads site</a> and run that.  It will create a profile in a directory like %APPDATA%/.minecraft/versions/1.6.2-Forge9.10.0.804/ containing files called 1.6.2-Forge9.10.0.804.json and 1.6.2-Forge9.10.0.804.jar.  The names will vary according to what version of Forge you install.
1. Run the Forge profile from the Minecraft launcher at least once.
1. If you want to use the Macro/Keybind integration in Watson, you'll want to install LiteLoader as the start of the tweak chain, according to the <a href="http://www.minecraftforum.net/topic/1868280-162api-liteloader-for-minecraft-162/"><i>Manual Installation [advanced]</i> procedure in the LiteLoader forum post.</a>.  This requires editing the JSON file that describes the profile: %APPDATA%/.minecraft/versions/1.6.2-Forge9.10.0.804/1.6.2-Forge9.10.0.804.json.  After this, you will have both LiteLoader and Forge installed.  The start of the 1.6.2-Forge9.10.0.804.json file will look something like this (without the line wrapping of the minecraftArguments value):
<pre>
{
	"minecraftArguments": "--username ${auth_player_name} --session ${auth_session} --version ${version_name} 
	--gameDir ${game_directory} --assetsDir ${game_assets} 
	--tweakClass com.mumfrey.liteloader.launch.LiteLoaderTweaker 
	--cascadedTweaks cpw.mods.fml.common.launcher.FMLTweaker",
	"libraries": [
		{
			"name": "com.mumfrey:liteloader:1.6.2",
			"url": "http://dl.liteloader.com/versions/"
		},
		{
			"name": "net.minecraft:launchwrapper:1.3"
		},
		{
			"url": "http://files.minecraftforge.net/maven/",
			"name": "net.minecraftforge:minecraftforge:9.10.0.804"
		},
</pre>
1. Now you can patch the corresponding JAR file with the Watson classes.  It's at this point that you would add in other mods that also (at the time of writing) patch the JAR file, such as Rei's minimap or Optifine.
1. Open %APPDATA%/.minecraft/versions/1.6.2-Forge9.10.0.804/1.6.2-Forge9.10.0.804.jar with your chosen ZIP file editing program.
1. Paste in the full contents of the Watson ZIP file.
1. Delete the contents of the META-INF/ folder of your modified JAR file.
1. Save the modified JAR file.
1. Start the Minecraft launcher, select Forge in the list of Profiles and click the Edit Profile button.
1. Check the box marked JVM Arguments and add the following text: -Xmx1G -Dfml.ignoreInvalidMinecraftCertificates=true -Dfml.ignorePatchDiscrepancies=true
1. Click Save Profile.


Downloads
---------

GitHub has dropped support for uploading files, so downloads will be hosted on Google Drive from now on.

<table>
  <tr>
    <th>Version</th> <th colspan="2">Details</th>
  </tr>
  <tr>
    <td rowspan="5">1.6.2<br>(2013-07-03)</td> <td>sha256sum -b</td> <td>392b1b2e0ba8c316f3f3c6c3c6fc36e68fa8a61313dffcf7ddead4d6dbcd54b2</td>
  </tr>
  <tr>
    <td>File Name</td> <td>watson-1.6.2-2013-08-06.zip</td>
  </tr>
  <tr>
    <td>Download</td> <td>https://docs.google.com/uc?export=download&id=0Bzf2TVOCqgpXeTZWaTAxZm1oNDQ</td>
  </tr>
  <tr>
    <td>Changes</td> <td><a href="https://github.com/totemo/watson/blob/master/Changes.md#152-2013-08-06">description</a></td>
  </tr>
  <tr>
    <td>Compatibility</td> <td><ul><li>Minecraft Forge 1.6.2-9.10.0.804</li><li>LiteLoader for 1.6.2</li><li>Macro/Keybind Mod 0.9.10 for Minecraft 1.6.2</li></ul></td>
  </tr>

  <tr>
    <th>Version</th> <th colspan="2">Details</th>
  </tr>
  <tr>
    <td rowspan="5">1.5.2<br>(2013-07-03)</td> <td>sha256sum -b</td> <td>cefb5bc28a9ec0fb113b54617c5a134bd0d72a71b69c79caf594e491ae2157bc</td>
  </tr>
  <tr>
    <td>File Name</td> <td>watson-1.5.2-2013-07-03.zip</td>
  </tr>
  <tr>
    <td>Download</td> <td>https://docs.google.com/uc?export=download&id=0Bzf2TVOCqgpXblZVRTlucC1PNnc</td>
  </tr>
  <tr>
    <td>Changes</td> <td><a href="https://github.com/totemo/watson/blob/master/Changes.md#152-2013-07-03">description</a></td>
  </tr>
  <tr>
    <td>Compatibility</td> <td><ul><li>Minecraft Forge 1.5.2-7.8.1.737</li><li>Macro/Keybind Mod 0.9.9 for Minecraft 1.5.2</li></ul></td>
  </tr>

  <tr>
    <th>Version</th> <th colspan="2">Details</th>
  </tr>
  <tr>
    <td rowspan="5">1.5.2<br>(2013-06-01)</td> <td>sha256sum -b</td> <td>1641005e002831d23b9f709c030fedfb1f509b6a47ecb691675d368f5af599ef</td>
  </tr>
  <tr>
    <td>File Name</td> <td>watson-1.5.2-2013-06-01.zip</td>
  </tr>
  <tr>
    <td>Download</td> <td>https://docs.google.com/uc?export=download&id=0Bzf2TVOCqgpXeGJkYlFMSEZ4cVU</td>
  </tr>
  <tr>
    <td>Changes</td> <td><a href="https://github.com/totemo/watson/blob/master/Changes.md#152-2013-06-01">description</a></td>
  </tr>
  <tr>
    <td>Compatibility</td> <td>Macro/Keybind Mod 0.9.9 for Minecraft 1.5.2</td>
  </tr>

  <tr>
    <th>Version</th> <th colspan="2">Details</th>
  </tr>
  <tr>
    <td rowspan="5">1.5.2<br>(2013-05-15)</td> <td>sha256sum -b</td> <td>ecd0a05332cd699db9c8860a6b0fe4897e90fd62736f4bb46d8c8af756f60f2c</td>
  </tr>
  <tr>
    <td>File Name</td> <td>watson-1.5.2-2013-05-15.zip</td>
  </tr>
  <tr>
    <td>Download</td> <td>https://docs.google.com/uc?export=download&id=0Bzf2TVOCqgpXak1nREl3TFY3bzg</td>
  </tr>
  <tr>
    <td>Changes</td> <td><a href="https://github.com/totemo/watson/blob/master/Changes.md#152-2013-05-15">description</a></td>
  </tr>
  <tr>
    <td>Compatibility</td> <td>Macro/Keybind Mod 0.9.9 for Minecraft 1.5.2</td>
  </tr>

  <tr>
    <th>Version</th> <th colspan="2">Details</th>
  </tr>
  <tr>
    <td rowspan="5">1.5.2<br>(2013-05-05)</td> <td>sha256sum -b</td> <td>d1602cb32d8822231e8a22fb99c57ed8f12dad3b3f3aab777ffee9f9363da2ea</td>
  </tr>
  <tr>
    <td>File Name</td> <td>watson-1.5.2-2013-05-05.zip</td>
  </tr>
  <tr>
    <td>Download</td> <td>https://docs.google.com/uc?export=download&id=0Bzf2TVOCqgpXR2x0Sk5CRmZPR0E</td>
  </tr>
  <tr>
    <td>Changes</td> <td><a href="https://github.com/totemo/watson/blob/master/Changes.md#152-2013-05-05">description</a></td>
  </tr>
  <tr>
    <td>Compatibility</td> <td>Macro/Keybind Mod 0.9.9 for Minecraft 1.5.2</td>
  </tr>

  <tr>
    <th>Version</th> <th colspan="2">Details</th>
  </tr>
  <tr>
    <td rowspan="5">1.5.1<br>(2013-04-28)</td> <td>sha256sum -b</td> <td>bf7637920de44e8c78a088064a3a5b5126deb353461aaf8e7ace15be345a7055</td>
  </tr>
  <tr>
    <td>File Name</td> <td>watson-1.5.1-2013-04-28.zip</td>
  </tr>
  <tr>
    <td>Download</td> <td>https://docs.google.com/uc?export=download&id=0Bzf2TVOCqgpXTjNicDZ5ZVEzNTA</td>
  </tr>
  <tr>
    <td>Changes</td> <td><a href="https://github.com/totemo/watson/blob/master/Changes.md#151-2013-04-28">description</a></td>
  </tr>
  <tr>
    <td>Compatibility</td> <td>Macro/Keybind Mod 0.9.8.2 for Minecraft 1.5.1</td>
  </tr>

  <tr>
    <th>Version</th> <th colspan="2">Details</th>
  </tr>
  <tr>
    <td rowspan="4">1.5.1<br>(2013-04-10)</td> <td>sha256sum -b</td> <td>70709b6fb7e24e3e7ace49325783fc145536489b96bfb308890e7f9b8c797c01</td>
  </tr>
  <tr>
    <td>File Name</td> <td>watson-1.5.1-2013-04-10.zip</td>
  </tr>
  <tr>
    <td>Download</td> <td>https://docs.google.com/uc?export=download&id=0Bzf2TVOCqgpXS0k4bDlTaGxwZEk</td>
  </tr>
  <tr>
    <td>Changes</td> <td><a href="https://github.com/totemo/watson/blob/master/Changes.md#151-2013-04-10">description</a></td>
  </tr>

  <tr>
    <th>Version</th> <th colspan="2">Details</th>
  </tr>
  <tr>
    <td rowspan="4">1.5.1<br>(2013-04-05)</td> <td>sha256sum -b</td> <td>2221be2210b68a65d7eee83b19f197ca3cc31d8acbdf2ab29156bf8e147ccb04</td>
  </tr>
  <tr>
    <td>File Name</td> <td>watson-1.5.1-2013-04-05.zip</td>
  </tr>
  <tr>
    <td>Download</td> <td>https://docs.google.com/uc?export=download&id=0Bzf2TVOCqgpXRE5uWHdWRWNSS0E</td>
  </tr>
  <tr>
    <td>Changes</td> <td><a href="https://github.com/totemo/watson/blob/master/Changes.md#151-2013-04-05">description</a></td>
  </tr>

  <tr>
    <th>Version</th> <th colspan="2">Details</th>
  </tr>
  <tr>
    <td rowspan="4">1.5.1<br>(2013-04-02)</td> <td>sha256sum -b</td> <td>1d8571a9e139e4b6ee09285263a12711b5f6a523542e7ab5376800d1bd166fbd</td>
  </tr>
  <tr>
    <td>File Name</td> <td>watson-1.5.1-2013-04-02.zip</td>
  </tr>
  <tr>
    <td>Download</td> <td>https://docs.google.com/uc?export=download&id=0Bzf2TVOCqgpXa05hRVVlaHVSZ0k</td>
  </tr>
  <tr>
    <td>Changes</td> <td><a href="https://github.com/totemo/watson/blob/master/Changes.md#151-2013-04-02">description</a></td>
  </tr>

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


Compatibility
-------------

Watson has been tested for compatibility with:

* Minecraft with ModLoader
* Minecraft with Minecraft Forge
* WorldEditCUI
* Rei's Minimap
* Optifine
* LiteLoader for Minecraft
* Macro/Keybind Mod
* MagicLauncher


Troubleshooting
---------------

<table>
  <tr>
    <th>Problem</th> <th>Resolution</th>
  </tr>
  <tr>
    <td>Minecraft crashes on startup.</td> <td>Make sure you've followed the installation instructions exactly.  In particular, make sure you add Watson to the Minecraft JAR <i>last</i> and that you delete the contents of the META-INF/ folder of the JAR.</td>
  </tr>
  <tr>
    <td>I don't see anything.</td> <td>Make sure you put "coords" in your /lb query.  Watson needs coordinates to know where to draw things.</td>
  </tr>
  <tr>
    <td>I still don't see anything.</td> <td>Make sure you turn on the Watson display: /w display on</td>
  </tr>
  <tr>
    <td>I can't teleport with "/w tp".</td> <td>By default, Watson expects a /tppos command that accepts decimal numbers for coordinates, e.g. "/tppos -120.5 7 345.5".  Many teleport commands don't, however, or they have a different name.  If your teleport command requires integer coordinates, try "/w config teleport_command /tppos %d %d %d".  If you're using the the CraftBukkit /tp command, then you can use: "/w config teleport_command /tp %d %d %d".</td>
  </tr>
  <tr>
    <td>I see the edits, but ore deposits don't get numbered. /w ore doesn't work.</td> <td>An older version of Watson attempted to detect creative-mode servers and disable labelling of ore deposits (xray is pointless in creative mode).  However, what it actually detected was the user's game mode.  If you are using Watson in creative mode, and /w ore doesn't work, then you may need to turn on the group_ores_in_creative setting (recent Watson versions have this on by default).  The command is: /w config group_ores_in_creative on</td>
  </tr>
  <tr>
    <td>When I query a particular block it shows up as a smallish bright pink cube.</td>
    <td>Watson scrapes LogBlock/Prism query results out of chat.  If it doesn't recognise the name of a block it just draws the pink cube as a reminder for me to add that name.  Let me know about it and I'll fix it.</td>
  </tr>
</table>


Contact Details
---------------

If Watson's not working for you or you want to suggest improvements, I'm happy to help.  You can contact me directly in the following ways:

* On http://reddit.com and the http://nerd.nu forums, I have the same user name.
* On gmail.com, append the word "research" to my name to get my address.

You can also raise bug reports or feature requests via GitHub, [here](https://github.com/totemo/watson/issues).

