package watson.cli;

// ----------------------------------------------------------------------------
/**
 * A simple {@link Command} implementation with {@link Parameter}s but no
 * subcommands.
 * 
 * See also: {@link CompoundCommand}.
 */
public class SimpleCommand extends Command
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param name the name of the command, including the leading /.
   * @param description the help text of the command.
   */
  public SimpleCommand(String name, String description)
  {
    super(name, description);
  }

  // --------------------------------------------------------------------------
  /**
   * Show the help message.
   */
  @Override
  public void showHelp()
  {
    StringBuilder usage = new StringBuilder();
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
} // class SimpleCommand