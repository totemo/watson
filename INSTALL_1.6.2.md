Watson Installation Instructions for Minecraft 1.6.2
----------------------------------------------------

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
1. Now you can patch the corresponding JAR file with the Watson classes.  It's at this point that you would add in other mods that also (at the time of writing) patch the JAR file, such as Rei's Minimap or Optifine.
1. Open %APPDATA%/.minecraft/versions/1.6.2-Forge9.10.0.804/1.6.2-Forge9.10.0.804.jar with your chosen ZIP file editing program.
1. Paste in the full contents of the Watson ZIP file.
1. Delete the contents of the META-INF/ folder of your modified JAR file.
1. Save the modified JAR file over the original 1.6.2-Forge9.10.0.804.jar.
1. Start the Minecraft launcher, select Forge in the list of Profiles and click the Edit Profile button.
1. Check the box marked JVM Arguments and add the following text:<br>
  -Xmx1G -Dfml.ignoreInvalidMinecraftCertificates=true -Dfml.ignorePatchDiscrepancies=true
1. Click Save Profile.

