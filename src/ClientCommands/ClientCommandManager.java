package ClientCommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.src.CommandException;
import net.minecraft.src.CommandNotFoundException;
import net.minecraft.src.ICommand;
import net.minecraft.src.ICommandManager;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.WrongUsageException;

// ----------------------------------------------------------------------------
/**
 * An ICommandManager implementation that executes commands locally in the
 * client, rather than sending them to the server.
 * 
 * Even in single player mode, Minecraft processes commands at the built in
 * server by default. However, I want commands to be processed at the client,
 * particularly in the case of multiplayer mode.
 */
public class ClientCommandManager implements ICommandManager
{
  // --------------------------------------------------------------------------
  /**
   * Parse and execute the command line if it is a command that has been
   * registered with this ICommandManager.
   * 
   * @param commandLine the command line.
   * @return true if the command was handled locally; false if it must be passed
   *         on to the server (via chat packets).
   */
  public boolean handleClientCommand(String commandLine)
  {
    if (!commandLine.startsWith("/"))
    {
      return false;
    }

    String[] tokens = getTokens(commandLine);
    if (tokens.length == 0)
    {
      return false;
    }
    ICommand command = getCommand(tokens[0]);
    if (command != null)
    {
      executeCommand(getCommandSender(), commandLine);
    }
    return command != null;
  } // handleClientCommand

  // --------------------------------------------------------------------------
  /**
   * Register the specified ICommand.
   * 
   * @param command the command.
   */
  public void registerCommand(ICommand command)
  {
    _commands.put(command.getCommandName(), command);
    _canonicalCommands.add(command);

    // Add all aliases of the command.
    List<String> aliases = command.getCommandAliases();
    if (aliases != null)
    {
      for (String alias : aliases)
      {
        _commands.put(alias, command);
      }
    }
  } // registerCommand

  // --------------------------------------------------------------------------
  /**
   * De-register the specified ICommand.
   * 
   * @param command the command.
   */
  public void unregisterCommand(ICommand command)
  {
    _commands.remove(command.getCommandName());
    _canonicalCommands.remove(command);

    // remove all aliases of the command.
    List<String> aliases = command.getCommandAliases();
    if (aliases != null)
    {
      for (String alias : aliases)
      {
        _commands.remove(alias);
      }
    }
  } // unregisterCommand

  // --------------------------------------------------------------------------
  /**
   * Return the command with the specified name, or null if there is no such
   * command.
   * 
   * @return the command with the specified name, or null if there is no such
   *         command.
   */
  public ICommand getCommand(String name)
  {
    return _commands.get(name);
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.minecraft.src.ICommandManager#executeCommand(net.minecraft.src.ICommandSender,
   *      java.lang.String)
   * 
   *      The JavaDocs for the interface don't currently describe the exact
   *      meaning of the return value. Looking at the code for
   *      {@link net.minecraft.src.CommandHandler} it contains a loop that
   *      applies a command for all players who match a particular name pattern.
   *      The returned value is the number of times that the command was
   *      successfully executed by that loop. Therefore in the case of this
   *      class, it returns 1 on success and 0 on error.
   */
  @Override
  public int executeCommand(ICommandSender sender, String commandLine)
  {
    try
    {
      String[] tokens = getTokens(commandLine);
      String verb = tokens[0];
      ICommand command = getCommand(verb);
      if (command == null)
      {
        throw new CommandNotFoundException();
      }
      tokens = Arrays.copyOfRange(tokens, 1, tokens.length);
      if (command.canCommandSenderUseCommand(sender))
      {
        command.processCommand(sender, tokens);
        return 1;
      }
      else
      {
        sender.sendChatToPlayer("\u00a7cYou do not have permission to use this command.");
      }
    }
    catch (WrongUsageException ex)
    {
      sender.sendChatToPlayer("\u00a7c"
                              + sender.translateString(
                                "commands.generic.usage",
                                new Object[]{sender.translateString(
                                  ex.getMessage(), ex.getErrorOjbects())}));
    }
    catch (CommandException ex)
    {
      sender.sendChatToPlayer("\u00a7c"
                              + sender.translateString(ex.getMessage(),
                                ex.getErrorOjbects()));
    }
    catch (Throwable ex)
    {
      sender.sendChatToPlayer("\u00a7c"
                              + sender.translateString(
                                "commands.generic.exception", new Object[0]));
      ex.printStackTrace();
    }
    return 0;
  } // executeCommand

  // --------------------------------------------------------------------------
  /**
   * @see net.minecraft.src.ICommandManager#getPossibleCommands(net.minecraft.src.ICommandSender,
   *      java.lang.String)
   */
  @Override
  public List<String> getPossibleCommands(ICommandSender var1, String prefix)
  {
    List<String> commands = new ArrayList<String>();
    for (String command : _commands.keySet())
    {
      if (command.startsWith(prefix))
      {
        commands.add(command);
      }
    }
    return commands;
  }

  // --------------------------------------------------------------------------
  /**
   * The local client is assumed to be able to use any commands that have been
   * registered with mod_CLI.
   * 
   * @see net.minecraft.src.ICommandManager#getPossibleCommands(net.minecraft.src.ICommandSender)
   */
  @Override
  public List<ICommand> getPossibleCommands(ICommandSender var1)
  {
    return new ArrayList<ICommand>(_commands.values());
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.minecraft.src.ICommandManager#getCommands()
   */
  @Override
  public Map<String, ICommand> getCommands()
  {
    return Collections.unmodifiableMap(_commands);
  }

  // --------------------------------------------------------------------------
  /**
   * Split the specified command into tokens.
   * 
   * Double-quoted strings are kept together as a single token. The quotes are
   * retained.
   * 
   * @param commandLine the command to tokenise.
   * @return an array of strings that are the individual words/tokens of the
   *         command.
   */
  protected static String[] getTokens(String commandLine)
  {
    // Throw away any trailing spaces on the command.
    commandLine = commandLine.trim();
    if (commandLine.startsWith("/"))
    {
      commandLine = commandLine.substring(1);
    }

    // Split up the command into args, either by splitting at spaces or grouping
    // double-quote delimited strings.
    Pattern p = Pattern.compile("(\"[^\"]*\"|(?:[^\"\\s])+)");
    Matcher m = p.matcher(commandLine);
    int end = commandLine.length();

    ArrayList<String> args = new ArrayList<String>();
    while (m.find())
    {
      args.add(m.group());
      end = m.end();
    }

    // There may be a left over bit of unmatched text at the end of the line.
    // Since we have removed the trailing spaces from the command it will be
    // a mismatched quote. So we can just return that as an extra token.
    if (end < commandLine.length())
    {
      String remainder = commandLine.substring(end);
      if (remainder.contains("\""))
      {
        args.add(remainder);
      }
    }
    return args.toArray(new String[args.size()]);
  } // split

  // --------------------------------------------------------------------------
  /**
   * Contruct and cache a ClientCommandSender instance in demand.
   * 
   * The Minecraft.thePlayer instance may not be initialised at the time that
   * this ClientCommandManager is constructed, so defer initialisation to here.
   * 
   * @return the {@link ClientCommandSender}.
   */
  public ICommandSender getCommandSender()
  {
    if (_sender == null)
    {
      _sender = new ClientCommandSender(this);
    }
    return _sender;
  }

  // --------------------------------------------------------------------------
  /**
   * An ICommandSender that forwards most commands to Minecraft.thePlayer, but
   * says that the player can use them if they are registered with the
   * ClientCommandManager.
   */
  ClientCommandSender             _sender;

  /**
   * A map from ICommand name to corresponding ICommand instance.
   */
  protected Map<String, ICommand> _commands          = new LinkedHashMap<String, ICommand>();

  /**
   * All unique ICommand implementations, registered according to their
   * canonical command name, ICommand.getCommandName().
   */
  protected Set<ICommand>         _canonicalCommands = new HashSet<ICommand>();
} // class ClientCommandManager