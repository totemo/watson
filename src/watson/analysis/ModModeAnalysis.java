package watson.analysis;

import watson.Controller;
import watson.chat.ChatClassifier;
import watson.chat.MethodChatHandler;
import watson.chat.TagDispatchChatHandler;

// ----------------------------------------------------------------------------
/**
 * Use (abuse) the {@link Analysis} mechanism to turn Watson displays on and off
 * when entering and leaving ModMode or duties mode for the ModMode or Duties
 * plugins, respectively.
 */
public class ModModeAnalysis extends Analysis
{
  // --------------------------------------------------------------------------
  /**
   * Watch for mod.entermodmode and mod.leavemodmode and switch on and off
   * Watson displays.
   */
  @Override
  public void registerAnalysis(TagDispatchChatHandler tagDispatchChatHandler)
  {
    tagDispatchChatHandler.addChatHandler("mod.entermodmode",
      new MethodChatHandler(this, "changeModMode"));
    tagDispatchChatHandler.addChatHandler("mod.leavemodmode",
      new MethodChatHandler(this, "changeModMode"));
    tagDispatchChatHandler.addChatHandler("mod.enabledutymode",
      new MethodChatHandler(this, "changeDutyMode"));
    tagDispatchChatHandler.addChatHandler("mod.disabledutymode",
      new MethodChatHandler(this, "changeDutyMode"));
  } // registerAnalysis

  // --------------------------------------------------------------------------
  /**
   * This method is called by the {@link ChatClassifier} when a chat line is
   * assigned the "mod.entermodmode" or "mod.leavemodmode" category.
   */
  @SuppressWarnings("unused")
  private void changeModMode(watson.chat.ChatLine line)
  {
    Controller.instance.getDisplaySettings().setDisplayed(
      line.getCategory().getTag().equals("mod.entermodmode"));
  }

  // --------------------------------------------------------------------------
  /**
   * This method is called by the {@link ChatClassifier} when a chat line is
   * assigned the "mod.enabledutymode" or "mod.disabledutymode" category.
   */
  @SuppressWarnings("unused")
  private void changeDutyMode(watson.chat.ChatLine line)
  {
    Controller.instance.getDisplaySettings().setDisplayed(
      line.getCategory().getTag().equals("mod.enabledutymode"));
  }
} // class ModModeAnalysis