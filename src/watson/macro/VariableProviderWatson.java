package watson.macro;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;

import net.eq2online.macros.scripting.ScriptCore;
import net.eq2online.macros.scripting.VariableCache;
import net.eq2online.macros.scripting.api.APIVersion;
import net.eq2online.macros.scripting.api.IVariableListener;
import net.eq2online.macros.scripting.api.IVariableProvider;
import watson.Configuration;
import watson.Controller;
import watson.DisplaySettings;
import watson.debug.Log;

// ----------------------------------------------------------------------------
/**
 * An IVariableProvider implementation that provides variables reflecting
 * Watson's current state.
 */
@APIVersion(9)
public class VariableProviderWatson extends VariableCache
  implements
    IVariableProvider
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
    new VariableProviderWatson().OnInit();
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IMacrosAPIModule#OnInit()
   */
  @Override
  public void OnInit()
  {
    ScriptCore.RegisterVariableProvider(this);
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IVariableProvider#UpdateVariables(boolean)
   */
  @Override
  public void UpdateVariables(boolean clock)
  {
    try
    {
      if (Configuration.instance.isEnabled())
      {
        DisplaySettings ds = Controller.instance.getDisplaySettings();
        HashMap<String, Object> variables = Controller.instance.getVariables();
        updateVariable("WATSON_DISPLAY", ds.isDisplayed());
        updateVariable("WATSON_OUTLINE", ds.isOutlineShown());
        updateVariable("WATSON_VECTOR", ds.areVectorsShown());
        updateVariable("WATSON_LABEL", ds.areLabelsShown());
        updateVariable("WATSON_ANNOTATION", ds.areAnnotationsShown());

        updateVariable("WATSON_PLAYER", (String) variables.get("player"));
        updateVariable("WATSON_ID", (Integer) variables.get("id"));
        updateVariable("WATSON_DATA", (Integer) variables.get("data"));
        updateVariable("WATSON_BLOCK", (String) variables.get("block"));

        Boolean creation = (Boolean) variables.get("creation");
        String action = (creation == null) ? ""
          : (creation.booleanValue() ? "created" : "destroyed");
        updateVariable("WATSON_ACTION", action);

        Integer x = (Integer) variables.get("x");
        Integer y = (Integer) variables.get("y");
        Integer z = (Integer) variables.get("z");
        updateVariable("WATSON_X", x);
        updateVariable("WATSON_Y", y);
        updateVariable("WATSON_Z", z);

        if (x == null || y == null || z == null)
        {
          updateVariable("WATSON_XYZ", "");
        }
        else
        {
          updateVariable("WATSON_XYZ",
            String.format(Locale.US, "%d, %d, %d", x, y, z));
        }

        Long timeStamp = (Long) variables.get("time");
        if (timeStamp == null)
        {
          updateVariable("WATSON_DATE", "");
          updateVariable("WATSON_TIME", "");
          updateVariable("WATSON_DATETIME", "");
        }
        else
        {
          String date = _dateFormat.format(timeStamp);
          String time = _timeFormat.format(timeStamp);
          StringBuilder dateTime = new StringBuilder();
          dateTime.append(date).append(' ').append(time);
          updateVariable("WATSON_DATE", date);
          updateVariable("WATSON_TIME", time);
          updateVariable("WATSON_DATETIME", dateTime.toString());
        }
      }
    }
    catch (Exception ex)
    {
      Log.exception(Level.WARNING,
        "exception updating WATSON_... macro variables", ex);
    }
  } // UpdateVariables

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IVariableProvider#ProvideVariables(net.eq2online.macros.scripting.api.IVariableListener)
   */
  @Override
  public void ProvideVariables(IVariableListener variableListener)
  {
    ProvideCachedVariables(variableListener);
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.eq2online.macros.scripting.api.IVariableProvider#GetVariable(java.lang.String)
   */
  @Override
  public Object GetVariable(String variableName)
  {
    return GetCachedValue(variableName);
  }

  // --------------------------------------------------------------------------
  /**
   * Update a string variable.
   * 
   * @param name the variable name.
   * @para value the String value; if null, defaults to "".
   */
  protected void updateVariable(String name, String value)
  {
    SetCachedVariable(name, value == null ? "" : value);
  }

  // --------------------------------------------------------------------------
  /**
   * Update an integer variable.
   * 
   * @param name the variable name.
   * @para value the Integer value; if null, defaults to 0.
   */
  protected void updateVariable(String name, Integer value)
  {
    SetCachedVariable(name, value == null ? 0 : value);
  }

  // --------------------------------------------------------------------------
  /**
   * Update a boolean variable.
   * 
   * @param name the variable name.
   * @para value the boolean value; if null, defaults to false.
   */
  protected void updateVariable(String name, Boolean value)
  {
    SetCachedVariable(name, value == null ? false : value);
  }

  // --------------------------------------------------------------------------
  /**
   * Used to format the WATSON_DATE variable.
   */
  protected SimpleDateFormat _dateFormat = new SimpleDateFormat("yyyy-MM-dd");

  /**
   * Used to format the WATSON_TIME variable.
   */
  protected SimpleDateFormat _timeFormat = new SimpleDateFormat("HH:mm:ss");
} // class VariableProviderWatson