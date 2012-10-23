package watson.yaml;

// ----------------------------------------------------------------------------
/**
 * Classes wishing to receive (error) messages from the SnakeValidator must
 * implement this interface.
 */
public interface ValidatorMessageSink
{
  /**
   * Report a message from the validator.
   * 
   * @param text the message text.
   */
  public void message(String text);
} // class ValidatorMessageSink