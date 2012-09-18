package watson.chat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

// ----------------------------------------------------------------------------
/**
 * Implements the {@link IChatHandler} interface by appending all chats lines to
 * a text log file or OutputStream.
 */
public class LogChatHandler implements IChatHandler
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param file the file to append to.
   */
  public LogChatHandler(OutputStream out)
    throws IOException
  {
    _writer = new PrintWriter(out);
  }

  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param file the file to append to.
   */
  public LogChatHandler(File file)
    throws IOException
  {
    this(new FileOutputStream(file, true));
  }

  // --------------------------------------------------------------------------
  /**
   * Finalizer.
   */
  public void finalize()
  {
    if (_writer != null)
    {
      _writer.close();
      _writer = null;
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public void classify(ChatLine line)
  {
    _writer.print(line.getCategory().getTag());
    _writer.print(": ");
    _writer.println(line.getFormatted());
    _writer.flush();
  }

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public void revise(ChatLine oldLine, ChatLine newLine)
  {
    _writer.print(newLine.getCategory().getTag());
    _writer.print(" REVISED: ");
    _writer.println(newLine.getFormatted());
    _writer.flush();
  }
  // --------------------------------------------------------------------------
  /**
   * The Writer through which text is written.
   */
  private PrintWriter _writer;
} // class LogChatHandler