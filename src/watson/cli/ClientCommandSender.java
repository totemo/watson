package watson.cli;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import watson.chat.Chat;

// ----------------------------------------------------------------------------
/**
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
    _sender = Minecraft.getMinecraft().thePlayer;
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.minecraft.src.ICommandSender#getCommandSenderName()
   */
  @Override
  public String getName()
  {
    return _sender.getName();
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.minecraft.src.ICommandSender#canCommandSenderUseCommand(java.lang.String)
   */
  @Override
  public boolean canUseCommand(int unknown, String command)
  {
    return true;
  }

  // --------------------------------------------------------------------------
  /**
   * Vanilla class ChatComponentTranslation extracts the ChatStyle out of this
   * IChatComponent and uses it to set the style of translated text.
   * 
   * @see net.minecraft.command.ICommandSender#func_145748_c_()
   */
  @Override
  public IChatComponent getDisplayName()
  {
    // ChatComponentStyle.getChatStyle() creates a default ChatStyle instance on
    // demand, so a default ChatComponentText instance suffices.
    // TODO: correct, or should this string be player name?
    return new ChatComponentText("");
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.minecraft.command.ICommandSender#addChatMessage(net.minecraft.util.IChatComponent)
   */
  @Override
  public void addChatMessage(IChatComponent chat)
  {
    Chat.localChat(chat);
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.minecraft.command.ICommandSender#getEntityWorld()
   */
  @Override
  public World getEntityWorld()
  {
    return Minecraft.getMinecraft().thePlayer.worldObj;
  }

    /**
     * Returns the entity associated with the command sender. MAY BE NULL!
     */
    @Override
    public Entity getCommandSenderEntity() {
        return Minecraft.getMinecraft().thePlayer;
    }

    /**
     * Returns true if the command sender should be sent feedback about executed commands
     */
    @Override
    public boolean sendCommandFeedback() {
        return false;
    }

    @Override
    public void setCommandStat(CommandResultStats.Type type, int amount) {

    }

    // --------------------------------------------------------------------------
  /**
   * @see net.minecraft.src.ICommandSender#getPlayerCoordinates()
   */
  @Override
  public BlockPos getPosition()
  {
    return Minecraft.getMinecraft().thePlayer.getPosition();
  }

  // --------------------------------------------------------------------------

    @Override
    public Vec3 getPositionVector() {
        return Minecraft.getMinecraft().thePlayer.getPositionVector();
    }

  /**
   * The default sender - Minecraft.thePlayer.
   */
  protected ICommandSender       _sender;

  /**
   * A reference to the ClientCommandManager instance.
   */
  protected ClientCommandManager _ccm;

} // class ClientCommandSender