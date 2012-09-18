package watson.analysis;

import watson.chat.TagDispatchChatHandler;

// ----------------------------------------------------------------------------
/**
 * Abstract base of classes that respond to chat lines by gathering information
 * and/or performing actions on behalf of {@link Sherlock}.
 * 
 * The intent of this class is to componentise {@link Sherlock}. Rather than
 * giving {@link Sherlock} dozens of attributes for storing the state multiple
 * disjoint behaviours, each behaviour is a separate Analysis instance that
 * stores its own state.
 * 
 * TODO: Parsing of chat lines should be automatic, based on descriptions of
 * matcher groups (to be added to chatcategories.yml).
 */
public abstract class Analysis
{
  // --------------------------------------------------------------------------
  /**
   * Register any {@link ChatHandler}s needed to extract information from chat.
   */
  public abstract void registerAnalysis(TagDispatchChatHandler tagDispatchChatHandler);
} // class Analysis

