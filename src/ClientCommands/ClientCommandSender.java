package ClientCommands;

import net.minecraft.src.ChunkCoordinates;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.ModLoader;

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
   * @see net.minecraft.src.ICommandSender#sendChatToPlayer(java.lang.String)
   */
  @Override
  public void sendChatToPlayer(String chat)
  {
    _sender.sendChatToPlayer(chat);
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
   * @see net.minecraft.src.ICommandSender#translateString(java.lang.String,
   *      java.lang.Object[])
   */
  @Override
  public String translateString(String key, Object... values)
  {
    return _sender.translateString(key, values);
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

  // --------------------------------------------------------------------------
  /**
   * @see net.minecraft.src.ICommandSender#getPlayerCoordinates()
   */
  @Override
  public ChunkCoordinates getPlayerCoordinates()
  {
    return ModLoader.getMinecraftInstance().thePlayer.getPlayerCoordinates();
  }
} // class ClientCommandSender