package watson.chat;

// --------------------------------------------------------------------------
/**
 * Contains callback methods invoked by {@link ChatClassifier} when a line of
 * chat has been classified to be of a particular {@link ChatCategory}.
 * 
 * The ChatCategory of a {@link ChatLine} is accessed through the
 * {@link ChatLine#getCategory()} method.
 */
public interface IChatHandler
{
  /**
   * This method is called the first time the specified line is assigned a
   * {@link ChatCategory}.
   * 
   * @param line the ChatLine that was categorised.
   */
  public void classify(ChatLine line);

  /**
   * This method is called when it is found that a line was previously split,
   * and is rejoined to form a longer line.
   * 
   * @param oldLine the older, shorter version of the line, previously passed to
   *          classify() or revise().
   * @param newLine the revised version of the line with extra characters
   *          appended.
   */
  public void revise(ChatLine oldLine, ChatLine newLine);

} // class IChatHandler