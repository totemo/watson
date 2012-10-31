package watson.analysis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import watson.BlockEdit;
import watson.Controller;
import watson.chat.ChatClassifier;
import watson.chat.ChatProcessor;
import watson.chat.MethodChatHandler;
import watson.chat.TagDispatchChatHandler;

// --------------------------------------------------------------------------
/**
 * Responds to a LogBlock teleport by finding the relevant edit in the
 * BlockEditSet and setting variables from it, so that /w pre will work nicely
 * even when we teleport way above a diamond ore deposit and can't coalblock the
 * edits directly.
 */
public class TeleportAnalysis extends Analysis
{
  // --------------------------------------------------------------------------
  /**
   * Set up to scrape lb.tp lines.
   */
  @Override
  public void registerAnalysis(TagDispatchChatHandler tagDispatchChatHandler)
  {
    tagDispatchChatHandler.setChatHandler("lb.tp", new MethodChatHandler(this,
      "lbTp"));
    _lbTp = ChatProcessor.getInstance().getChatClassifier().getChatCategoryById(
      "lb.tp").getFullPattern();
  }

  // --------------------------------------------------------------------------
  /**
   * This method is called by the {@link ChatClassifier} when a chat line is
   * assigned the "lb.tp" category.
   */
  @SuppressWarnings("unused")
  private void lbTp(watson.chat.ChatLine line)
  {
    try
    {
      Matcher m = _lbTp.matcher(line.getUnformatted());
      if (m.matches())
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
        BlockEdit edit = Controller.instance.getBlockEditSet().findEdit(x, y,
          z, (String) Controller.instance.getVariables().get("player"));
        if (edit != null)
        {
          Controller.instance.getVariables().put("time", edit.time);
          Controller.instance.getVariables().put("player", edit.player);
          Controller.instance.getVariables().put("block", edit.type.getId());
          Controller.instance.getVariables().put("x", x);
          Controller.instance.getVariables().put("y", y);
          Controller.instance.getVariables().put("z", z);
        }
      }
    }
    catch (Exception ex)
    {
      // System.out.println(ex);
    }
  } // lbPage

  // --------------------------------------------------------------------------
  /**
   * The Pattern of full lines with the ID (not tag) lb.tp.
   */
  protected Pattern _lbTp;
} // class TeleportAnalysis