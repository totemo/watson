package watson.cli;

// ----------------------------------------------------------------------------
/**
 * A {@link Command} implementation for commands that are sub-commands of a
 * parent command. The parent command will have a syntax like:
 * 
 * <pre>
 * /parentcommand subcommand1 parameter1 parameter2 ... 
 * /parentcommand subcommand2   
 * /parentcommand subcommand3 parameter1 ...
 * </pre>
 */
public class SubCommand extends Command
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param name the name of this command (and the word that must be entered to
   *          begin it).
   * @paran description the description presented in the help text.
   */
  public SubCommand(String name, String description)
  {
    super(name, description);
  }

  // --------------------------------------------------------------------------
  /**
   * Set the parent of this SubCommand.
   * 
   * @param parentCommand
   */
  public void setParentCommand(Command parentCommand)
  {
    _parentCommand = parentCommand;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the parent of this SubCommand.
   * 
   * @return the parent of this SubCommand.
   */
  public Command getParentCommand()
  {
    return _parentCommand;
  }

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public void showHelp()
  {
    StringBuilder usage = new StringBuilder();
    usage.append(getParentCommand().getName());
    usage.append(' ');
    usage.append(getName());
    if (getParameterSyntax() != null)
    {
      usage.append(getParameterSyntax().getSyntaxHelp());
    }

    String help = String.format(
      "%-" + Integer.toString(CommandManager.DESCRIPTION_COLUMN) + "s - %s",
      usage, getDescription());
    clientMessage(help);
  } // showHelp
  // --------------------------------------------------------------------------
  /**
   * The parent of this SubCommand.
   */
  protected Command _parentCommand;
} // class SubCommand