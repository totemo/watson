package watson.macro;

import net.eq2online.macros.scripting.ScriptCore;
import net.eq2online.macros.scripting.api.APIVersion;
import net.eq2online.macros.scripting.api.IMacro;
import net.eq2online.macros.scripting.api.IMacroAction;
import net.eq2online.macros.scripting.api.IMacroActionProcessor;
import net.eq2online.macros.scripting.api.IMacroActionStackEntry;
import net.eq2online.macros.scripting.api.IScriptAction;
import net.eq2online.macros.scripting.api.IScriptActionProvider;
import net.minecraft.src.mod_ClientCommands;
import watson.debug.Log;

// package net.eq2online.macros.modules;

// --------------------------------------------------------------------------
/**
 * A Macro/Keybind Mod script action for a WATSON(&lt;string&gt;) command.
 */
@APIVersion(9)
public class ScriptActionWatsonCommand implements IScriptAction
{
  // --------------------------------------------------------------------------
  /**
   * This method is called by reflection when Watson is loaded in order to
   * attempt to register custom script actions.
   * 
   * @throws ClassNotFoundException if the Macro/Keybind Mod is not loaded.
   */
  public static void initialise()
  {
    new ScriptActionWatsonCommand().OnInit();
  }

  // --------------------------------------------------------------------------
  /**
   * Return the name of the script action as it would be written in scripts,
   * e.g. $${WATSON("/w clear")}$$.
   * 
   * @return the name of the script action as it would be written in scripts.
   */
  @Override
  public String toString()
  {
    return "watson";
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IMacrosAPIModule#OnInit()
   */
  @Override
  public void OnInit()
  {
    ScriptCore.RegisterScriptAction(this);
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IScriptAction#CanBePoppedBy(net.eq2online.macros.scripting.api.IScriptAction)
   */
  @Override
  public boolean CanBePoppedBy(IScriptAction arg0)
  {
    return false;
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IScriptAction#CanBreak(net.eq2online.macros.scripting.api.IMacroActionProcessor,
   *      net.eq2online.macros.scripting.api.IScriptActionProvider,
   *      net.eq2online.macros.scripting.api.IMacro,
   *      net.eq2online.macros.scripting.api.IMacroAction,
   *      net.eq2online.macros.scripting.api.IMacroAction)
   */
  @Override
  public boolean CanBreak(IMacroActionProcessor arg0,
                          IScriptActionProvider arg1, IMacro arg2,
                          IMacroAction arg3, IMacroAction arg4)
  {
    return false;
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IScriptAction#CanExecuteNow(net.eq2online.macros.scripting.api.IScriptActionProvider,
   *      net.eq2online.macros.scripting.api.IMacro,
   *      net.eq2online.macros.scripting.api.IMacroAction, java.lang.String,
   *      java.lang.String[])
   */
  @Override
  public boolean CanExecuteNow(IScriptActionProvider arg0, IMacro arg1,
                               IMacroAction arg2, String arg3, String[] arg4)
  {
    return true;
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IScriptAction#CheckExecutePermission()
   */
  @Override
  public boolean CheckExecutePermission()
  {
    return true;
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IScriptAction#CheckPermission(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public boolean CheckPermission(String arg0, String arg1)
  {
    return true;
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IScriptAction#Execute(net.eq2online.macros.scripting.api.IScriptActionProvider,
   *      net.eq2online.macros.scripting.api.IMacro,
   *      net.eq2online.macros.scripting.api.IMacroAction, java.lang.String,
   *      java.lang.String[])
   */
  @Override
  public String Execute(IScriptActionProvider provider, IMacro macro,
                        IMacroAction action, String command, String[] args)
  {
    Log.debug("Macro: " + command);
    mod_ClientCommands.getInstance().handleClientCommand(command);
    return null;
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IScriptAction#ExecuteConditional(net.eq2online.macros.scripting.api.IScriptActionProvider,
   *      net.eq2online.macros.scripting.api.IMacro,
   *      net.eq2online.macros.scripting.api.IMacroAction, java.lang.String,
   *      java.lang.String[])
   */
  @Override
  public boolean ExecuteConditional(IScriptActionProvider arg0, IMacro arg1,
                                    IMacroAction arg2, String arg3,
                                    String[] arg4)
  {
    return false;
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IScriptAction#ExecuteConditionalElse(net.eq2online.macros.scripting.api.IScriptActionProvider,
   *      net.eq2online.macros.scripting.api.IMacro,
   *      net.eq2online.macros.scripting.api.IMacroAction, java.lang.String,
   *      java.lang.String[],
   *      net.eq2online.macros.scripting.api.IMacroActionStackEntry)
   */
  @Override
  public void ExecuteConditionalElse(IScriptActionProvider arg0, IMacro arg1,
                                     IMacroAction arg2, String arg3,
                                     String[] arg4, IMacroActionStackEntry arg5)
  {
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IScriptAction#ExecuteStackPop(net.eq2online.macros.scripting.api.IScriptActionProvider,
   *      net.eq2online.macros.scripting.api.IMacro,
   *      net.eq2online.macros.scripting.api.IMacroAction, java.lang.String,
   *      java.lang.String[], net.eq2online.macros.scripting.api.IMacroAction)
   */
  @Override
  public boolean ExecuteStackPop(IScriptActionProvider arg0, IMacro arg1,
                                 IMacroAction arg2, String arg3, String[] arg4,
                                 IMacroAction arg5)
  {
    return false;
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IScriptAction#ExecuteStackPush(net.eq2online.macros.scripting.api.IScriptActionProvider,
   *      net.eq2online.macros.scripting.api.IMacro,
   *      net.eq2online.macros.scripting.api.IMacroAction, java.lang.String,
   *      java.lang.String[])
   */
  @Override
  public boolean ExecuteStackPush(IScriptActionProvider arg0, IMacro arg1,
                                  IMacroAction arg2, String arg3, String[] arg4)
  {
    return false;
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IScriptAction#GetPermissionGroup()
   */
  @Override
  public String GetPermissionGroup()
  {
    return null;
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IScriptAction#IsClocked()
   */
  @Override
  public boolean IsClocked()
  {
    return false;
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IScriptAction#IsConditionalElseOperator(net.eq2online.macros.scripting.api.IScriptAction)
   */
  @Override
  public boolean IsConditionalElseOperator(IScriptAction arg0)
  {
    return false;
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IScriptAction#IsConditionalOperator()
   */
  @Override
  public boolean IsConditionalOperator()
  {
    return false;
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IScriptAction#IsPermissable()
   */
  @Override
  public boolean IsPermissable()
  {
    return false;
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IScriptAction#IsStackPopOperator()
   */
  @Override
  public boolean IsStackPopOperator()
  {
    return false;
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IScriptAction#IsStackPushOperator()
   */
  @Override
  public boolean IsStackPushOperator()
  {
    return false;
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IScriptAction#MatchesConditionalOperator(net.eq2online.macros.scripting.api.IScriptAction)
   */
  @Override
  public boolean MatchesConditionalOperator(IScriptAction arg0)
  {
    return false;
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IScriptAction#OnStopped(net.eq2online.macros.scripting.api.IScriptActionProvider,
   *      net.eq2online.macros.scripting.api.IMacro,
   *      net.eq2online.macros.scripting.api.IMacroAction)
   */
  @Override
  public void OnStopped(IScriptActionProvider arg0, IMacro arg1,
                        IMacroAction arg2)
  {
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IScriptAction#RegisterPermissions(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void RegisterPermissions(String arg0, String arg1)
  {
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IScriptAction#Tick(net.eq2online.macros.scripting.api.IScriptActionProvider)
   */
  @Override
  public int Tick(IScriptActionProvider arg0)
  {
    return 0;
  }
} // class ScriptSctionWatsonCommand