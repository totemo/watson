package watson.chat;

import watson.Controller;

// ----------------------------------------------------------------------------
/**
 * An IChatHandler that passes chat lines back into the Minecraft code for its
 * normal vanilla handling.
 */
public class MinecraftChatHandler implements IChatHandler
{

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public void classify(ChatLine line)
  {
    Controller.instance.localChat(line.getFormatted());
  }

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public void revise(ChatLine oldLine, ChatLine newLine)
  {
    // Work out what text was appended to the oldLine.
    String addedText = newLine.getUnformatted().substring(
      oldLine.getUnformatted().length());
    // Prefix the colour of the tail end of the previous line.
    StringBuilder builder = new StringBuilder();
    builder.append(ChatClassifier.COLOUR_CHAR);
    builder.append(oldLine.getLastColour());
    builder.append(addedText);
    Controller.instance.localChat(builder.toString());
  } // revise
} // class MinecraftChatHandler

