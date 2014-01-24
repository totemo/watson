package watson.transformer;

import com.mumfrey.liteloader.core.transformers.Callback;
import com.mumfrey.liteloader.core.transformers.Callback.CallBackType;
import com.mumfrey.liteloader.core.transformers.CallbackInjectionTransformer;

// ----------------------------------------------------------------------------
/**
 * This class transformer puts callback hooks on:
 * <ul>
 * <li>EntityClientPlayerMP.sendChatMessage(String) - to intercept outgoing
 * chats to the server so that Watson commands can be filtered out.</li>
 * </ul>
 */
public class WatsonCallbackInjectionTransformer extends CallbackInjectionTransformer
{
  // TODO: Update obfuscation after 1.7.2

  private static final String EntityClientPlayerMP    = "net.minecraft.client.entity.EntityClientPlayerMP";
  private static final String EntityClientPlayerMPObf = "bje";

  private static final String sendChatMessage         = "sendChatMessage";
  private static final String sendChatMessageObf      = "a";
  private static final String sendChatMessageDesc     = "(Ljava/lang/String;)V";

  // --------------------------------------------------------------------------
  /**
   * @see com.mumfrey.liteloader.core.transformers.CallbackInjectionTransformer#addMappings()
   */
  @Override
  protected void addMappings()
  {
    this.addCallbackMapping(EntityClientPlayerMP, sendChatMessage, sendChatMessageDesc, CallBackType.REDIRECT,
      new Callback(sendChatMessage, "watson.LiteModWatson", true));
    this.addCallbackMapping(EntityClientPlayerMPObf, sendChatMessageObf, sendChatMessageDesc, CallBackType.REDIRECT,
      new Callback(sendChatMessage, "watson.LiteModWatson", true));
  }
} // class WatsonCallbackInjectionTransformer
