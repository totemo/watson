package watson.analysis;

import java.util.ArrayList;
import java.util.logging.Level;

import watson.chat.ChatCategory;
import watson.chat.ChatClassifier;
import watson.chat.IChatHandler;
import watson.chat.TagDispatchChatHandler;
import watson.debug.Log;

// ----------------------------------------------------------------------------
/**
 * Makes inferences based on LogBlock query results.
 * 
 * And generally tells Watson what is up. :)
 * 
 * TODO: Allow dynamic loading of analyses?
 */
public class Sherlock
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param chatClassifier classifies incoming chat messages.
   */
  public Sherlock(ChatClassifier chatClassifier)
  {
    // Add the TagChatHandler to receive chat callbacks.
    chatClassifier.addChatHandler(_chatHandler);
    _analyses.add(new LbCoordsAnalysis());
    _analyses.add(new CoalBlockAnalysis());
    _analyses.add(new ModModeAnalysis());
    _analyses.add(new RegionInfoAnalysis());
    _analyses.add(new TeleportAnalysis());
    _analyses.add(new RatioAnalysis());
    _analyses.add(ServerTime.instance);

    for (Analysis analysis : _analyses)
    {
      try
      {
        analysis.registerAnalysis(_chatHandler);
      }
      catch (Exception ex)
      {
        // The above has a tendency to NullPointerException when it doesn't find
        // a particular ChatCategory by ID.
        Log.exception(Level.SEVERE, "exception setting up Sherlock: ", ex);
      }
    } // for
  } // Sherlock constructor

  // --------------------------------------------------------------------------
  /**
   * Dispatches chat lines to specific {@link IChatHandler} implementations
   * based on the {@link ChatCategory} tags of those lines.
   */
  protected TagDispatchChatHandler _chatHandler = new TagDispatchChatHandler();

  /**
   * Analyses applied to chat lines.
   */
  protected ArrayList<Analysis>    _analyses    = new ArrayList<Analysis>();

} // class Sherlock