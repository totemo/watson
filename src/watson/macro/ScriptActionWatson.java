package watson.macro;

import net.eq2online.macros.scripting.ScriptAction;
import net.eq2online.macros.scripting.ScriptCore;
import net.eq2online.macros.scripting.api.IMacro;
import net.eq2online.macros.scripting.api.IMacroAction;
import net.eq2online.macros.scripting.api.IReturnValue;
import net.eq2online.macros.scripting.api.IScriptActionProvider;
import net.minecraft.src.mod_ClientCommands;

// ----------------------------------------------------------------------------
/**
 * A Macro/Keybind Mod script action for a WATSON(&lt;string&gt;) command.
 */
// I think the APIVersion annotation is only needed in a module.
// @APIVersion(10)
public class ScriptActionWatson extends ScriptAction
{
  // --------------------------------------------------------------------------
  /**
   * This method is called by reflection when Watson is loaded in order to
   * attempt to register custom script actions.
   * 
   * @throws NoClassFoundDefError if the Macro/Keybind Mod is not loaded.
   */
  public static void initialise()
  {
    new ScriptActionWatson().onInit();
  }

  // --------------------------------------------------------------------------
  /**
   * Default constructor.
   */
  public ScriptActionWatson()
  {
    super("watson");
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IMacrosAPIModule#OnInit()
   */
  @Override
  public void onInit()
  {
    ScriptCore.registerScriptAction(this);
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IScriptAction#Execute(net.eq2online.macros.scripting.api.IScriptActionProvider,
   *      net.eq2online.macros.scripting.api.IMacro,
   *      net.eq2online.macros.scripting.api.IMacroAction, java.lang.String,
   *      java.lang.String[])
   */
  @Override
  public IReturnValue execute(IScriptActionProvider provider, IMacro macro,
                              IMacroAction action, String command, String[] args)
  {
    mod_ClientCommands.getInstance().handleClientCommand(command);
    return null;
  }
} // class ScriptSctionWatsonCommand
