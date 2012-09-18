package watson.cli;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// ----------------------------------------------------------------------------
/**
 * Keeps track of all a set of {@link Command}s and can parse them, print help
 * messages and dispatch each command to a {@link ICommandAction}.
 */
public class CommandManager
{
  // --------------------------------------------------------------------------
  /**
   * Description column start for help text.
   */
  public static final int DESCRIPTION_COLUMN = 24;

  // --------------------------------------------------------------------------
  /**
   * Constructor.
   */
  public CommandManager()
  {
    // Empty.
  }

  // --------------------------------------------------------------------------
  /**
   * Set the {@link IMessageSink} through which messages are displayed to the
   * client.
   * 
   * @param messageSink the sink.
   */
  public void setMessageSink(IMessageSink messageSink)
  {
    _messageSink = messageSink;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the {@link IMessageSink} through which messages are displayed to the
   * client.
   * 
   * @return the {@link IMessageSink} through which messages are displayed to
   *         the client.
   */
  public IMessageSink getMessageSink()
  {
    return _messageSink;
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the specified command line contains a recognised command
   * (whether free of errors or not).
   * 
   * @param line the command line to parse.
   * @return true if the specified command line contains a recognised command
   *         (whether free of errors or not).
   */
  public boolean parse(String line)
  {
    List<String> words = split(line);
    if (words.size() > 0)
    {
      Command command = getCommand(words.get(0));
      if (command != null)
      {
        try
        {
          command.parse(words.subList(1, words.size()));
        }
        catch (Exception ex)
        {
          error(ex.getMessage());
        }
        return true;
      }
    }
    return false;
  } // parse

  // --------------------------------------------------------------------------
  /**
   * Show a help message describing all known commands, listed in the order that
   * they were registered.
   */
  public void showHelp()
  {
    for (Command command : _commands.values())
    {
      command.showHelp();
    }
  } // showHelp

  // --------------------------------------------------------------------------
  /**
   * Register a Command.
   * 
   * @param command the command to add.
   */
  public void addCommand(Command command)
  {
    if (isCommand(command.getName()))
    {
      throw new IllegalArgumentException(command.getName()
                                         + " is already registered");
    }
    _commands.put(command.getName(), command);
    command.setCommandManager(this);
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if word is the name of a Command.
   */
  public boolean isCommand(String word)
  {
    return _commands.containsKey(word);
  }

  // --------------------------------------------------------------------------
  /**
   * Return the command beginning with word.
   */
  public Command getCommand(String word)
  {
    return _commands.get(word);
  }

  // --------------------------------------------------------------------------
  /**
   * Split a command line into argument words, keeping double quoted strings
   * together.
   * 
   * @param command the command line.
   * @return the list of arguments.
   */
  protected List<String> split(String command)
  {
    // Split up the command into args, either by splitting at spaces or grouping
    // double-quote delimited strings.
    Pattern p = Pattern.compile("(\"[^\"]*\"|(?:[^\"\\s])+)");
    Matcher m = p.matcher(command);
    int end = command.length();

    ArrayList<String> args = new ArrayList<String>();
    while (m.find())
    {
      args.add(m.group());
      end = m.end();
    }

    // The one small price to pay with the above regexp: you may get a left
    // over bit of unmatched text at the end of the line. If it contains a
    // double quote then you it is mismatched.
    if (end < command.length())
    {
      String remainder = command.substring(end);
      if (remainder.contains("\""))
      {
        error("mismatched double quote");
        args.clear();
      }
    }
    return args;
  } // split

  // --------------------------------------------------------------------------
  /**
   * Display a message to the client.
   * 
   * @param message the message.
   */
  public void clientMessage(String message)
  {
    getMessageSink().process(message);
    // Controller.instance.localChat(message);
  }

  // --------------------------------------------------------------------------
  /**
   * Show an error message on the client.
   * 
   * @param message the message.
   */
  public void error(String message)
  {
    clientMessage("§4Error: " + message);
  }

  // --------------------------------------------------------------------------
  /**
   * Defines the processing of messages to be displayed and decouples this class
   * from the rest of the Watson and Minecraft code base when testing.
   */
  private IMessageSink         _messageSink = new StdoutMessageSink();

  /**
   * A map from command name to Command instance for all known commands.
   */
  private Map<String, Command> _commands    = new LinkedHashMap<String, Command>();
} // class CommandManager