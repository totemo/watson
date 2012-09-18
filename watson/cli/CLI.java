package watson.cli;

import java.util.LinkedHashMap;

import watson.Controller;
import watson.chat.ChatProcessor;

// -----------------------------------------------------------------------------
/**
 * The Watson Command Line Interface (CLI).
 * 
 * Intercepts Watson commands entered via chat and either executes them,
 * possibly issuing LogBlock queries as chat commands to the server.
 * 
 * There is no "good" place to intercept outgoing chat messages in the client
 * that is absolutely <i>guaranteed</i> not to clash with any other mods. Watson
 * modifies GuiChat.keyTyped() where the Enter key is handled and
 * "this.mc.thePlayer.sendChatMessage(var3)" is called. It might equally modify
 * Minecraft.handleClientCommand(), but the Minecraft class is probably more
 * likely to be modified by other mods.
 */
public class CLI
{
  // --------------------------------------------------------------------------
  /**
   * Main program for ad-hoc interactive testing.
   */
  public static void main(String[] args)
  {
    try
    {
      CLI.instance.showHelp();

      StringBuilder commandLine = new StringBuilder();
      for (int i = 0; i < args.length; ++i)
      {
        if (i > 0)
        {
          commandLine.append(' ');
        }
        commandLine.append(args[i]);
      }
      System.out.println("Is a command: "
                         + CLI.instance.interceptedCommand(commandLine.toString()));
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  } // main

  // --------------------------------------------------------------------------
  /**
   * Single instance, public so that it can be easily accessed from
   * GuiChat.keyTyped().
   */
  public static final CLI instance = new CLI();

  // --------------------------------------------------------------------------
  /**
   * Show help on all commands.
   * 
   * TODO: improve this.
   */
  public void showHelp()
  {
    _cm.showHelp();
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the specified chat message was intercepted as a Watson
   * command and should therefore not be sent to the server.
   * 
   * @return true if the specified chat message was intercepted as a Watson
   *         command and should therefore not be sent to the server.
   */
  public boolean interceptedCommand(String chat)
  {
    return _cm.parse(chat);
  } // interceptedCommand

  // --------------------------------------------------------------------------
  /**
   * Default constructor.
   */
  private CLI()
  {
    configureWatsonCommands();
    configureTagCommands();
    configureHLCommands();

    // Drop this line when testing interactively (and the setCommandAction()s).
    _cm.setMessageSink(new LocalChatMessageSink());
  }

  // --------------------------------------------------------------------------
  /**
   * Configure the Watson /w command set.
   */
  private void configureWatsonCommands()
  {
    CompoundCommand watson = new CompoundCommand("/w", "Watson command set.");
    _cm.addCommand(watson);

    SubCommand help = new SubCommand("help", "Show this help message.");
    SubCommand lb = new SubCommand("lb",
      "Run a LogBlock query, using current defaults.");
    SubCommand def = new SubCommand("default",
      "Set defaults for LogBlock queries.");
    SubCommand clear = new SubCommand("clear",
      "Clear all stored LogBlock results for the current world dimension.");
    SubCommand pre = new SubCommand("pre",
      "Show what happened immediately before the most recently selected block.");
    SubCommand tp = new SubCommand("tp",
      "Teleport to a numbered edit or \"down\".");
    SubCommand analysis = new SubCommand("analysis",
      "Enable or disable all Watson analysis.");
    SubCommand display = new SubCommand("display",
      "Enable or disable all Watson displays.");
    SubCommand hud = new SubCommand("hud",
      "Enable or disable the Watson Heads-Up Display (HUD).");
    SubCommand outline = new SubCommand("outline",
      "Enable or disable the block outline display.");
    SubCommand motion = new SubCommand("motion",
      "Enable or disable the direction-of-motion display.");

    // Currently unsupported commands will be commented out here.
    watson.addSubCommand(help);
    // watson.addSubCommand(lb);
    // watson.addSubCommand(def);
    watson.addSubCommand(clear);
    watson.addSubCommand(pre);
    // watson.addSubCommand(tp);
    // watson.addSubCommand(analysis);
    watson.addSubCommand(display);
    // watson.addSubCommand(hud);
    watson.addSubCommand(outline);
    // watson.addSubCommand(motion);

    clear.setParameterSyntax(new OrderedParameterSyntax());
    addSingleOnOffParameter(analysis, "state");
    addSingleOnOffParameter(display, "state");
    addSingleOnOffParameter(hud, "state");
    addSingleOnOffParameter(outline, "state");
    addSingleOnOffParameter(motion, "state");

    help.setCommandAction(new MethodCommandAction(watson, "showHelp",
      new Class<?>[0]));
    clear.setCommandAction(new MethodCommandAction(Controller.instance,
      "clearBlockEditSet", new Class<?>[0]));
    pre.setCommandAction(new MethodCommandAction(Controller.instance,
      "queryPreviousEdits", new Class<?>[0]));
    display.setCommandAction(new MethodCommandAction(Controller.instance,
      "setDisplayed", Boolean.TYPE));
    outline.setCommandAction(new MethodCommandAction(Controller.instance,
      "setOutlineShown", Boolean.TYPE));
  }

  // --------------------------------------------------------------------------
  /**
   * Configure the Watson /tag command set.
   */
  private void configureTagCommands()
  {
    CompoundCommand tag = new CompoundCommand("/tag",
      "Show or hide chat messages according to their category tag.");
    _cm.addCommand(tag);

    SubCommand tagHelp = new SubCommand("help", "Show this help message.");
    SubCommand tagHide = new SubCommand("hide",
      "Hide chat lines with the specified tag.");
    SubCommand tagShow = new SubCommand("show",
      "Show chat lines with the specified tag.");
    SubCommand tagList = new SubCommand("list",
      "List all chat message tags that are hidden from chat.");

    tag.addSubCommand(tagHelp);
    tag.addSubCommand(tagHide);
    tag.addSubCommand(tagShow);
    tag.addSubCommand(tagList);

    OrderedParameterSyntax tagParams = new OrderedParameterSyntax();
    tagParams.addParameter(new Parameter("tag", true, new StringParameterType(
      "tag")));
    tagHide.setParameterSyntax(tagParams);
    tagShow.setParameterSyntax(tagParams);

    ICommandAction hideShowAction = new ICommandAction()
    {
      @Override
      public String perform(Command command,
                            LinkedHashMap<String, Object> values)
      {
        try
        {
          ChatProcessor.getInstance().setChatTagVisible(
            (String) values.get("tag"), command.getName().equals("show"));
          return "";
        }
        catch (Exception ex)
        {
          return ex.getMessage();
        }
      }
    };

    tagHelp.setCommandAction(new MethodCommandAction(tag, "showHelp",
      new Class<?>[0]));
    tagHide.setCommandAction(hideShowAction);
    tagShow.setCommandAction(hideShowAction);
    tagList.setCommandAction(new MethodCommandAction(
      ChatProcessor.getInstance(), "listHiddenTags", new Class<?>[0]));
  } // configureTagCommands

  // --------------------------------------------------------------------------
  /**
   * Configure the Watson /hl command set.
   */
  private void configureHLCommands()
  {
    CompoundCommand hl = new CompoundCommand("/hl",
      "Control colour highlighting of chat lines.");
    _cm.addCommand(hl);

    SubCommand hlHelp = new SubCommand("help", "Show this help message.");
    SubCommand hlAdd = new SubCommand("add", "Add a new highlight action.");
    SubCommand hlRemove = new SubCommand("remove",
      "Remove a highlight by number.");
    SubCommand hlList = new SubCommand("list",
      "List all of the highlight patterns.");

    hl.addSubCommand(hlHelp);
    hl.addSubCommand(hlAdd);
    hl.addSubCommand(hlRemove);
    hl.addSubCommand(hlList);

    OrderedParameterSyntax addParams = new OrderedParameterSyntax();
    addParams.addParameter(new Parameter("colour", true,
      new StringParameterType("colour")));
    addParams.addParameter(new Parameter("pattern", true,
      new StringParameterType("pattern")));
    hlAdd.setParameterSyntax(addParams);

    OrderedParameterSyntax removeParams = new OrderedParameterSyntax();
    removeParams.addParameter(new Parameter("index", true,
      new IntegerParameterType()));
    hlRemove.setParameterSyntax(removeParams);

    hlHelp.setCommandAction(new MethodCommandAction(hl, "showHelp",
      new Class<?>[0]));
    hlAdd.setCommandAction(new MethodCommandAction(
      Controller.instance.getChatHighlighter(), "addHighlight", String.class,
      String.class));
    hlRemove.setCommandAction(new MethodCommandAction(
      Controller.instance.getChatHighlighter(), "removeHighlight", Integer.TYPE));
    hlList.setCommandAction(new MethodCommandAction(
      Controller.instance.getChatHighlighter(), "listHighlights",
      new Class<?>[0]));
  } // configureHLCommands

  // --------------------------------------------------------------------------
  /**
   * Add a single, mandatory on/off parameter to a SubCommand.
   * 
   * @param subCommand the SubCommand.
   * @param name the name of the parameter.
   */
  private void addSingleOnOffParameter(SubCommand subCommand, String name)
  {
    OrderedParameterSyntax params = new OrderedParameterSyntax();
    params.addParameter(new Parameter(name, true, new OnOffParameterType()));
    subCommand.setParameterSyntax(params);
  }

  // --------------------------------------------------------------------------
  /**
   * Handles knowledge of accepted commands.
   */
  CommandManager _cm = new CommandManager();
} // class CLI