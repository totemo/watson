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

} // class IChatHandler