package watson.cli;

import java.util.LinkedHashMap;
import java.util.List;

// ----------------------------------------------------------------------------
/**
 * Represents one possible command (verb) supported by a command-line interface.
 */
public abstract class Command
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param name the name of this command (and the word that must be entered to
   *          begin it).
   * @paran description the description presented in the help text.
   */
  public Command(String name, String description)
  {
    _name = name;
    _description = description;
  }

  // --------------------------------------------------------------------------
  /**
   * Return he name of the command as it appears on the command line.
   * 
   * @return he name of the command as it appears on the command line.
   */
  public String getName()
  {
    return _name;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the help message for the command.
   * 
   * @return the help message for the command.
   */
  public String getDescription()
  {
    return _description;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the action to invoke when the command is successfully parsed.
   * 
   * @param commandAction the action.
   */
  public void setCommandAction(ICommandAction commandAction)
  {
    _commandAction = commandAction;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the action to invoke when the command is successfully parsed.
   * 
   * @return the action to invoke when the command is successfully parsed.
   */
  public ICommandAction getCommandAction()
  {
    return _commandAction;
  }

  // --------------------------------------------------------------------------
  /**
   * Set a reference to the {@link CommandManager} that owns this Command.
   * 
   * @param commandManager the {@link CommandManager}.
   */
  void setCommandManager(CommandManager commandManager)
  {
    _commandManager = commandManager;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the {@link CommandManager} that owns this Command.
   * 
   * @return the {@link CommandManager} that owns this Command.
   */
  public CommandManager getCommandManager()
  {
    return _commandManager;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the {@link ParameterSyntax} that defines how {@link Parameters} are
   * parsed.
   * 
   * @param parameterSyntax the {@link ParameterSyntax}.
   */
  public void setParameterSyntax(ParameterSyntax parameterSyntax)
  {
    _parameterSyntax = parameterSyntax;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the {@link ParameterSyntax} that defines how {@link Parameters} are
   * parsed.
   * 
   * @return the {@link ParameterSyntax} that defines how {@link Parameters} are
   *         parsed.
   */
  public ParameterSyntax getParameterSyntax()
  {
    return _parameterSyntax;
  }

  // --------------------------------------------------------------------------
  /**
   * Show a help message for this command.
   */
  public abstract void showHelp();

  // --------------------------------------------------------------------------

  public void parse(List<String> args)
    throws CLIParseException
  {
    if (getParameterSyntax() != null)
    {
      LinkedHashMap<String, Object> values = getParameterSyntax().parse(args);
      if (getCommandAction() != null)
      {
        getCommandAction().perform(this, values);
      }
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Show a client-side message.
   * 
   * @param message the message.
   */
  public void clientMessage(String message)
  {
    getCommandManager().clientMessage(message);
  }

  // --------------------------------------------------------------------------
  /**
   * Show a client-side error message.
   * 
   * @param message the message.
   */
  public void error(String message)
  {
    getCommandManager().error(message);
  }

  // --------------------------------------------------------------------------
  /**
   * The name of this command, which is whatever word the user must enter to
   * trigger it.
   */
  protected String          _name;

  /**
   * User-friendly description text.
   */
  protected String          _description     = "";

  /**
   * The {@link CommandManager} that owns this Command.
   */
  protected CommandManager  _commandManager;

  /**
   * The action to invoke when the command is successfully parsed.
   */
  protected ICommandAction  _commandAction;

  /**
   * Describes the syntax of the {@link Parameter}s.
   */
  protected ParameterSyntax _parameterSyntax = new OrderedParameterSyntax();
} // class Command