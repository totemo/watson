package watson.transformer;

import com.mumfrey.liteloader.transformers.Callback;
import com.mumfrey.liteloader.transformers.Callback.CallbackType;
import com.mumfrey.liteloader.transformers.CallbackInjectionTransformer;

// ----------------------------------------------------------------------------
/**
 * This class transformer puts callback hooks on:
 * <ul>
 * <li>EntityClientPlayerMP.sendChatMessage(String) - to intercept outgoing
 * chats to the server so that Watson commands can be filtered out.</li>
 * </ul>
 * 
 * Notes on names of classes, fields and methods:
 * <ul>
 * <li>Classes, fields and methods can have 3 names: the unobfuscated MCP name,
 * the name assigned by Searge (srg name), and the original obfuscated name.</li>
 * <li>Searge names for fields and methods take the form field_<number>_<obf>
 * and func_<number>_<obf>, respectively. <obj> is the original obfuscated name
 * when Searge first encountered the method.</li>
 * <li>Searge names do not change between versions of Minecraft.</li>
 * <li>For classes, the MCP name will always match the Searge name.</li>
 * <li>Forge apparently deobfuscates names to the Searge names at runtime. So
 * these names must be registered for compatibility with Forge.</li>
 * </ul>
 */
public class WatsonCallbackInjectionTransformer extends CallbackInjectionTransformer
{
  // TODO: Update obfuscation after 1.7.2

  private static final String EntityClientPlayerMP    = "net.minecraft.client.entity.EntityClientPlayerMP";
  private static final String EntityClientPlayerMPObf = "bje";

  private static final String sendChatMessage         = "sendChatMessage";
  private static final String sendChatMessageSrg      = "func_71165_d";
  private static final String sendChatMessageObf      = "a";
  private static final String sendChatMessageDesc     = "(Ljava/lang/String;)V";

  // private static final String GuiChat = "net.minecraft.client.gui.GuiChat";
  // private static final String GuiChatObf = "bbb";
  //
  // private static final String newChatEntered = "newChatEntered";
  // private static final String newChatEnteredMcp = "func_146403_a";
  // private static final String newChatEnteredSrg = "func_146403_a";
  // private static final String newChatEnteredObf = "a";
  // private static final String newChatEnteredDesc = "(Ljava/lang/String;)V";

  // --------------------------------------------------------------------------
  /**
   * @see com.mumfrey.liteloader.transformers.CallbackInjectionTransformer#addCallbacks()
   */
  @Override
  protected void addCallbacks()
  {
    this.addCallback(EntityClientPlayerMP, sendChatMessage, sendChatMessageDesc,
      new Callback(CallbackType.REDIRECT, sendChatMessage, "watson.LiteModWatson"));
    this.addCallback(EntityClientPlayerMP, sendChatMessageSrg, sendChatMessageDesc,
      new Callback(CallbackType.REDIRECT, sendChatMessage, "watson.LiteModWatson"));
    this.addCallback(EntityClientPlayerMPObf, sendChatMessageObf, sendChatMessageDesc,
      new Callback(CallbackType.REDIRECT, sendChatMessage, "watson.LiteModWatson"));

    // this.addCallback(GuiChat, newChatEnteredMcp, newChatEnteredDesc,
    // new Callback(CallbackType.REDIRECT, newChatEntered,
    // "watson.LiteModWatson"));
    // this.addCallback(GuiChat, newChatEnteredSrg, newChatEnteredDesc,
    // new Callback(CallbackType.REDIRECT, newChatEntered,
    // "watson.LiteModWatson"));
    // this.addCallback(GuiChatObf, newChatEnteredObf, newChatEnteredDesc,
    // new Callback(CallbackType.REDIRECT, newChatEntered,
    // "watson.LiteModWatson"));
  }
} // class WatsonCallbackInjectionTransformer
