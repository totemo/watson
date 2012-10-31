package watson.analysis;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import watson.BlockEdit;
import watson.BlockType;
import watson.BlockTypeRegistry;
import watson.Controller;
import watson.chat.ChatClassifier;
import watson.chat.ChatProcessor;
import watson.chat.MethodChatHandler;
import watson.chat.TagDispatchChatHandler;

// --------------------------------------------------------------------------
/**
 * An {@link Analysis} implementation that adds new {@link BlockEdit}s in
 * response to results returned hitting specific blocks with a coal block (the
 * lb toolblock).
 */
public class CoalBlockAnalysis extends Analysis
{
  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public void registerAnalysis(TagDispatchChatHandler tagDispatchChatHandler)
  {
    tagDispatchChatHandler.setChatHandler("lb.position", new MethodChatHandler(
      this, "lbPosition"));
    tagDispatchChatHandler.setChatHandler("lb.edit", new MethodChatHandler(
      this, "lbEdit"));
    tagDispatchChatHandler.setChatHandler("lb.editreplaced",
      new MethodChatHandler(this, "lbEditReplaced"));
    _lbPosition = ChatProcessor.getInstance().getChatClassifier().getChatCategoryById(
      "lb.position").getFullPattern();
    _lbEdit = ChatProcessor.getInstance().getChatClassifier().getChatCategoryById(
      "lb.edit").getFullPattern();
    _lbEditReplaced = ChatProcessor.getInstance().getChatClassifier().getChatCategoryById(
      "lb.editreplaced").getFullPattern();
  } // registerAnalysis

  // --------------------------------------------------------------------------
  /**
   * This method is called by the {@link ChatClassifier} when a chat line is
   * assigned the "lb.position" category. That category corresponds to the
   * result header when checking the logs for a single block using coal ore.
   */
  @SuppressWarnings("unused")
  private void lbPosition(watson.chat.ChatLine line)
  {
    Matcher m = _lbPosition.matcher(line.getUnformatted());
    if (m.matches())
    {
      Controller.instance.getVariables().put("x", Integer.parseInt(m.group(1)));
      Controller.instance.getVariables().put("y", Integer.parseInt(m.group(2)));
      Controller.instance.getVariables().put("z", Integer.parseInt(m.group(3)));
      _lbPositionTime = System.currentTimeMillis();
      _expectingFirstEdit = true;
    }
  }

  // --------------------------------------------------------------------------
  /**
   * This method is called by the {@link ChatClassifier} when a chat line is
   * assigned the "lb.edit" category. That category corresponds to a "created"
   * or "destroyed" result in the logs for a single block using coal ore.
   */
  @SuppressWarnings("unused")
  private void lbEdit(watson.chat.ChatLine line)
  {
    Matcher m = _lbEdit.matcher(line.getUnformatted());
    if (m.matches()
        && (System.currentTimeMillis() - _lbPositionTime) < POSITION_TIMEOUT_MILLIS)
    {
      int month = Integer.parseInt(m.group(1));
      int day = Integer.parseInt(m.group(2));
      int hour = Integer.parseInt(m.group(3));
      int minute = Integer.parseInt(m.group(4));
      int second = Integer.parseInt(m.group(5));
      _time.set(_now.get(Calendar.YEAR), month - 1, day, hour, minute, second);

      String player = m.group(6);
      String action = m.group(7);
      String block = m.group(8);

      BlockType type = BlockTypeRegistry.instance.getBlockTypeByName(block);

      // Update player, etc, variables on the most recent edit result only.
      if (_expectingFirstEdit)
      {
        Controller.instance.getVariables().put("time", _time.getTimeInMillis());
        Controller.instance.getVariables().put("player", player);
        Controller.instance.getVariables().put("block", type.getId());
        _expectingFirstEdit = false;
      }

      int x = (Integer) Controller.instance.getVariables().get("x");
      int y = (Integer) Controller.instance.getVariables().get("y");
      int z = (Integer) Controller.instance.getVariables().get("z");

      boolean created = action.equals("created");
      Controller.instance.getBlockEditSet().addBlockEdit(
        new BlockEdit(_time.getTimeInMillis(), player, created, x, y, z, type));
    }
  } // lbEdit

  // --------------------------------------------------------------------------
  /**
   * This method is called by the {@link ChatClassifier} when a chat line is
   * assigned the "lb.edit" category. That category corresponds to a "created"
   * or "destroyed" result in the logs for a single block using coal ore.
   */
  @SuppressWarnings("unused")
  private void lbEditReplaced(watson.chat.ChatLine line)
  {
    Matcher m = _lbEditReplaced.matcher(line.getUnformatted());
    if (m.matches()
        && (System.currentTimeMillis() - _lbPositionTime) < POSITION_TIMEOUT_MILLIS)
    {
      int month = Integer.parseInt(m.group(1));
      int day = Integer.parseInt(m.group(2));
      int hour = Integer.parseInt(m.group(3));
      int minute = Integer.parseInt(m.group(4));
      int second = Integer.parseInt(m.group(5));
      _time.set(_now.get(Calendar.YEAR), month - 1, day, hour, minute, second);
      Controller.instance.getVariables().put("time", _time.getTimeInMillis());

      String player = m.group(6);
      String oldBlock = m.group(7);

      BlockType type = BlockTypeRegistry.instance.getBlockTypeByName(oldBlock);
      Controller.instance.getVariables().put("player", player);
      Controller.instance.getVariables().put("block", type.getId());

      int x = (Integer) Controller.instance.getVariables().get("x");
      int y = (Integer) Controller.instance.getVariables().get("y");
      int z = (Integer) Controller.instance.getVariables().get("z");

      // Just add the destruction.
      Controller.instance.getBlockEditSet().addBlockEdit(
        new BlockEdit(_time.getTimeInMillis(), player, false, x, y, z, type));
    }
  } // lbEditReplaced

  // --------------------------------------------------------------------------
  /**
   * A reusable Calendar instance used to interpret any time stamps found in
   * LogBlock results.
   */
  protected Calendar        _time                   = Calendar.getInstance();

  /**
   * Used to infer the implicit (absent) year in LogBlock timestamps.
   */
  protected Calendar        _now                    = Calendar.getInstance();

  /**
   * The pattern of full lb.position lines.
   */
  protected Pattern         _lbPosition;

  /**
   * The pattern of full lb.edit lines.
   */
  protected Pattern         _lbEdit;

  /**
   * The pattern of full lb.editreplaced lines.
   */
  protected Pattern         _lbEditReplaced;

  /**
   * Local time at which lb.position line was parsed. {@see
   * #POSITION_TIMEOUT_MILLIS}.
   */
  protected long            _lbPositionTime         = 0;

  /**
   * This flag is set to true when the coordinate header for LogBlock toolblock
   * (coal ore) queries has just been parsed, and we are expecting to see the
   * first edit result. It is cleared to false again after the first result.
   * 
   * The purpose of this is to allow us to set variables (particularly "player")
   * from the most recent edit, which is the first listed. Older edits of the
   * same block should have no effect on variables.
   */
  protected boolean         _expectingFirstEdit     = false;

  /**
   * The maximum time separation between an lb.position and a subsequent lb.edit
   * chat message for which Sherlock will consider the two messages to be
   * related. lb.edit messages can also be matched by a query, such as: "/lb
   * player playername time 1d block 56", and that would result in a diamond
   * edit being erroneously marked at the last coal block position. The timeout
   * makes that much less likely.
   */
  private static final long POSITION_TIMEOUT_MILLIS = 250;

} // class CoalBlockAnalysis