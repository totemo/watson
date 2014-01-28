package watson.analysis;

import static watson.analysis.MiscPatterns.DUTYMODE_DISABLE;
import static watson.analysis.MiscPatterns.DUTYMODE_ENABLE;
import static watson.analysis.MiscPatterns.MODMODE_DISABLE;
import static watson.analysis.MiscPatterns.MODMODE_ENABLE;

import java.util.regex.Matcher;

import net.minecraft.util.IChatComponent;
import watson.Controller;
import watson.chat.IMatchedChatHandler;

// ----------------------------------------------------------------------------
/**
 * Use (abuse) the {@link Analysis} mechanism to turn Watson displays on and off
 * when entering and leaving ModMode or duties mode for the ModMode or Duties
 * plugins, respectively.
 */
public class ModModeAnalysis extends Analysis
{
  // ----------------------------------------------------------------------------
  /**
   * Constructor.
   */
  public ModModeAnalysis()
  {
    IMatchedChatHandler modmodeHandler = new IMatchedChatHandler()
    {
      @Override
      public boolean onMatchedChat(IChatComponent chat, Matcher m)
      {
        changeModMode(chat, m);
        return true;
      }
    };

    addMatchedChatHandler(MODMODE_ENABLE, modmodeHandler);
    addMatchedChatHandler(MODMODE_DISABLE, modmodeHandler);

    IMatchedChatHandler dutiesHandler = new IMatchedChatHandler()
    {
      @Override
      public boolean onMatchedChat(IChatComponent chat, Matcher m)
      {
        changeDutyMode(chat, m);
        return true;
      }
    };

    addMatchedChatHandler(DUTYMODE_ENABLE, dutiesHandler);
    addMatchedChatHandler(DUTYMODE_DISABLE, dutiesHandler);
  }

  // --------------------------------------------------------------------------
  /**
   * This method is called by the {@link ChatClassifier} when a chat line is
   * assigned the "mod.entermodmode" or "mod.leavemodmode" category.
   */
  @SuppressWarnings("unused")
  void changeModMode(IChatComponent chat, Matcher m)
  {
    Controller.instance.getDisplaySettings().setDisplayed(m.pattern() == MODMODE_ENABLE);
  }

  // --------------------------------------------------------------------------
  /**
   * This method is called by the {@link ChatClassifier} when a chat line is
   * assigned the "mod.enabledutymode" or "mod.disabledutymode" category.
   */
  @SuppressWarnings("unused")
  void changeDutyMode(IChatComponent chat, Matcher m)
  {
    Controller.instance.getDisplaySettings().setDisplayed(m.pattern() == DUTYMODE_ENABLE);
  }
} // class ModModeAnalysis