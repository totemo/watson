package clientcommands;

import net.minecraft.command.ICommandSender;
import net.minecraft.src.ModLoader;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

/*
 * ----------------------------------------------------------------------------
 * An ICommandSender implementation for client-side command handling.
 * 
 * Most methods simply defer to Minecraft.thePlayer's handling, but for
 * client-side commands we need to allow the sender to use any command that is
 * registered with the {@link ClientCommandManager}.
 */
public class ClientCommandSender implements ICommandSender
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param ccm the {@link ClientCommandManager} with which client-side commands
   *          are registered.
   */
  public ClientCommandSender(ClientCommandManager ccm)
  {
    _ccm = ccm;
    _sender = ModLoader.getMinecraftInstance().thePlayer;
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.minecraft.src.ICommandSender#getCommandSenderName()
   */
  @Override
  public String getCommandSenderName()
  {
    return _sender.getCommandSenderName();
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.minecraft.command.ICommandSender#sendChatToPlayer(net.minecraft.util.ChatMessageComponent)
   */
  @Override
  public void sendChatToPlayer(ChatMessageComponent chatmessagecomponent)
  {
    _sender.sendChatToPlayer(chatmessagecomponent);
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.minecraft.src.ICommandSender#canCommandSenderUseCommand(java.lang.String)
   */
  @Override
  public boolean canCommandSenderUseCommand(int unknown, String command)
  {
    return true;
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.minecraft.src.ICommandSender#getPlayerCoordinates()
   */
  @Override
  public ChunkCoordinates getPlayerCoordinates()
  {
    return ModLoader.getMinecraftInstance().thePlayer.getPlayerCoordinates();
  }

  // --------------------------------------------------------------------------
  /**
   * Not documented in the deobfuscated sources yet.
   * 
   * Presumed to be the natural compliment to getPlayerCoordinates().
   */
  @Override
  public World func_130014_f_()
  {
    return ModLoader.getMinecraftInstance().thePlayer.worldObj;
  }

  // --------------------------------------------------------------------------
  /**
   * The default sender - Minecraft.thePlayer.
   */
  protected ICommandSender       _sender;

  /**
   * A reference to the ClientCommandManager instance.
   */
  protected ClientCommandManager _ccm;

} // class ClientCommandSender