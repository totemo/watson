package watson.cli;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// ----------------------------------------------------------------------------
/**
 * A {@link Command} implementation that defers parsing of parameters to named
 * {@link SubCommands}.
 * 
 * Compound commands take the form:
 * 
 * <pre>
 * /parentcommand subcommand param1 param2 ...
 * </pre>
 * 
 * The /parentcommand identifies a CompoundCommand instance. The subcommand
 * argument is the name of a child {@link SubCommand}. The paramter values,
 * param1 etc, are handed to the {@link SubComand} to parse in its own way.
 */
public class CompoundCommand extends Command
{
  /**
   * Constructor.
   * 
   * @param name the name of the top level (parent) compound command.
   * @param description the help description of the parent command.
   */
  public CompoundCommand(String name, String description)
  {
    super(name, description);
  }

  // --------------------------------------------------------------------------
  /**
   * Set a default {@link SubCommand} to parse if a valid {@link SubCommand}
   * name is not specified as the first argument to the parent command.
   * 
   * @param defaultSubCommand the default {@link SubCommand}.
   */
  public void setDefaultSubCommand(SubCommand defaultSubCommand)
  {
    _defaultSubCommand = defaultSubCommand;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the default {@link SubCommand} to parse if a valid
   * {@link SubCommand} name is not specified as the first argument to the
   * parent command.
   * 
   * @return the default {@link SubCommand} to parse if a valid
   *         {@link SubCommand} name is not specified as the first argument to
   *         the parent command.
   */
  public SubCommand getDefaultSubCommand()
  {
    return _defaultSubCommand;
  }

  // --------------------------------------------------------------------------
  /**
   * Add the specified child command.
   * 
   * @param sub the {@link SubCommand}.
   */
  public void addSubCommand(SubCommand sub)
  {
    _subCommands.put(sub.getName(), sub);
    sub.setParentCommand(this);
    sub.setCommandManager(getCommandManager());
  }

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public void showHelp()
  {
    String help = String.format(
      "%-" + Integer.toString(CommandManager.DESCRIPTION_COLUMN) + "s - %s",
      getName(), getDescription());
    clientMessage(help);

    for (SubCommand subCommand : _subCommands.values())
    {
      subCommand.showHelp();
    }
  } // showHelp

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   * 
   * @throws CLIParseException
   */
  @Override
  public void parse(List<String> args)
    throws CLIParseException
  {
    if (args.size() > 0)
    {
      SubCommand subcommand = _subCommands.get(args.get(0));
      if (subcommand == null)
      {
        // Can't find a subcommand the matches the first arg. Could be that the
        // first arg is a parameter to the default SubCommand.
        if (getDefaultSubCommand() != null)
        {
          getDefaultSubCommand().parse(args);
        }
        else
        {
          error(args.get(0) + " is not a sub-command");
        }
      }
      else
      {
        // Got a SubCommand. Pass the remainder of the args to it.
        subcommand.parse(args.subList(1, args.size()));
      }
    }
    else
    {
      // No arguments. Call the default SubCommand.
      if (getDefaultSubCommand() != null)
      {
        getDefaultSubCommand().parse(args);
      }
      else
      {
        error(getName() + " requires arguments");
      }
    }
  } // parse

  // --------------------------------------------------------------------------
  /**
   * The default {@link SubCommand}; can be null.
   */
  private SubCommand              _defaultSubCommand;

  /**
   * A map from command name to Command instance for all known commands.
   */
  private Map<String, SubCommand> _subCommands = new LinkedHashMap<String, SubCommand>();

} // class CompoundCommand