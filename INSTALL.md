Installation
------------
Minecraft Version 1.7.2
=======================
As of 1.7.2, Watson is a LiteLoader mod.  There is no version of Macro/Keybind mod for 1.7.2, so support for that is not in this version of Watson.  Currently, no modding tools support Minecraft version 1.7.4.

Installation procedure:
 1. If you require Minecraft Forge/FML support, then download the recommended Installer from [http://files.minecraftforge.net](http://files.minecraftforge.net) and run that first.
 1. Download the LiteLoader 1.7.2_02 (beta) installer from [the Minecraft Forums thread](http://www.minecraftforum.net/topic/1868280-172api-liteloader-for-minecraft-164-172-beta-available/page__st__1120#entry28781181) and run that installer.  If you have already installed Forge, then select the option to "Chain to Minecraft Forge".
 1. Make a .minecraft/mods/1.7.2/ folder, where .minecraft/ is your Minecraft installation folder.
 1. Download Watson using the link in the table below and put the .litemod file in .minecraft/mods/1.7.2/.
 
Regardless of whether you have installed Minecraft Forge or not, run the LiteLoader 1.7.2 profile in the Minecraft launcher.

Minecraft Version 1.6.4
=======================
For Minecraft version 1.6.4, install Watson according to the instructions [here](https://github.com/totemo/watson/blob/master/INSTALL_1.6.4.md).


Downloads
---------

GitHub has dropped support for uploading files, so downloads will be hosted on Google Drive from now on.

<table>
<tr>
  <th>Minecraft 1.7.2</th> 
</tr>
<tr>
<table>
  <tr>
    <th>Watson version</th> <td>0.5.0.83-mc1.7.2_02</td> 
  </tr>
  <tr>
    <th>File Name</th> <td>watson-1.6.4-2013-10-08.zip</td>
  </tr>
  <tr>
    <th>Download</th> <td>https://docs.google.com/uc?export=download&id=0Bzf2TVOCqgpXZ3JHczJDMWo1T0k</td>
  </tr>
  <tr>
    <th>sha256sum -b</th> <td>4ea4f85e0a5ccc308151a67b3a631dceff77dcb5f3fa74c7e7cfeb594e15b171</td>
  </tr>
  <tr>
    <th>Changes</th> <td><a href="https://github.com/totemo/watson/blob/master/Changes.md#164-2013-10-08">description</a></td>
  </tr>
  <tr>
    <th>Compatibility</th> <td><ul><li>Minecraft Forge 1.7.2-10.12.0.1024</li><li>LiteLoader 1.7.2_02 (beta)</li></ul></td>
  </tr>
</table>
</tr>

<tr>
  <th>Minecraft 1.6.4</th> 
</tr>
<tr>
<table>
  <tr>
    <th>Watson version</th> <td>1.6.4 (2013-10-08)</td> 
  </tr>
  <tr>
    <th>File Name</th> <td>watson-1.6.4-2013-10-08.zip</td>
  </tr>
  <tr>
    <th>Download</th> <td>https://docs.google.com/uc?export=download&id=0Bzf2TVOCqgpXZ3JHczJDMWo1T0k</td>
  </tr>
  <tr>
    <th>sha256sum -b</th> <td>4ea4f85e0a5ccc308151a67b3a631dceff77dcb5f3fa74c7e7cfeb594e15b171</td>
  </tr>
  <tr>
    <th>Changes</th> <td><a href="https://github.com/totemo/watson/blob/master/Changes.md#164-2013-10-08">description</a></td>
  </tr>
  <tr>
    <th>Compatibility</th> <td><ul><li>Minecraft Forge 1.6.4-9.11.1.916</li><li>LiteLoader for 1.6.4</li><li>Macro/Keybind Mod 0.9.11 for Minecraft 1.6.4</li></ul></td>
  </tr>
</table>
</tr>

<tr>
  <th>Minecraft 1.6.2</th> 
</tr>
<tr>
<table>
  <tr>
    <th>Watson version</th> <td>1.6.2 (2013-08-06)</td> 
  </tr>
  <tr>
    <th>File Name</th> <td>watson-1.6.2-2013-08-06.zip</td>
  </tr>
  <tr>
    <th>Download</th> <td>https://docs.google.com/uc?export=download&id=0Bzf2TVOCqgpXeTZWaTAxZm1oNDQ</td>
  </tr>
  <tr>
    <th>sha256sum -b</th> <td>392b1b2e0ba8c316f3f3c6c3c6fc36e68fa8a61313dffcf7ddead4d6dbcd54b2</td>
  </tr>
  <tr>
    <th>Changes</th> <td><a href="https://github.com/totemo/watson/blob/master/Changes.md#162-2013-08-06">description</a></td>
  </tr>
  <tr>
    <th>Compatibility</th> <td><ul><li>Minecraft Forge 1.6.2-9.10.0.804</li><li>LiteLoader for 1.6.2</li><li>Macro/Keybind Mod 0.9.10 for Minecraft 1.6.2</li></ul></td>
  </tr>
</table>
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

