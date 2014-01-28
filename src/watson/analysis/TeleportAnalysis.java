package watson.analysis;

import static watson.analysis.LogBlockPatterns.LB_TP;

import java.util.regex.Matcher;

import net.minecraft.util.IChatComponent;
import watson.Controller;
import watson.chat.IMatchedChatHandler;
import watson.db.BlockEdit;

// --------------------------------------------------------------------------
/**
 * Responds to a LogBlock teleport by finding the relevant edit in the
 * BlockEditSet and setting variables from it, so that /w pre will work nicely
 * even when we teleport way above a diamond ore deposit and can't coalblock the
 * edits directly.
 */
public class TeleportAnalysis extends Analysis
{
  // ----------------------------------------------------------------------------
  /**
   * Constructor.
   */
  public TeleportAnalysis()
  {
    addMatchedChatHandler(LB_TP, new IMatchedChatHandler()
    {
      @Override
      public boolean onMatchedChat(IChatComponent chat, Matcher m)
      {
        lbTp(chat, m);
        return true;
      }
    });
  } // constructor

  // --------------------------------------------------------------------------
  /**
   * Parse the message shown when the player uses /lb tp and select the
   * corresponding edit so that /w pre works.
   */
  @SuppressWarnings("unused")
  void lbTp(IChatComponent chat, Matcher m)
  {
    try
    {
      int x = Integer.parseInt(m.group(1));
      int y = Integer.parseInt(m.group(2));
      int z = Integer.parseInt(m.group(3));

      // Multiple players could conceivably edit the block (rarely but it
      // happens). Pass in the most recently queried player, if known.
      // findEdit() currently does an (inefficient) search from oldest to
      // newest edit, meaning that the typical pattern of mining down to an
      // ore, then pillaring up with cobble through the ore will find the ore
      // block destruction (earlier) and not the cobble creation (later).
      String player = (String) Controller.instance.getVariables().get("player");
      BlockEdit edit = Controller.instance.getBlockEditSet().findEdit(x, y, z, player);
      Controller.instance.selectBlockEdit(edit);
    }
    catch (Exception ex)
    {
      // System.out.println(ex);
    }
  } // lbTp
} // class TeleportAnalysis