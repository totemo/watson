package watson.macro;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;

import net.eq2online.macros.core.MacroModCore;
import net.eq2online.macros.core.Macros;
import net.eq2online.macros.event.MacroEventManager;
import net.eq2online.macros.gui.layout.LayoutPanel;
import net.eq2online.macros.gui.layout.LayoutPanelEvents;
import net.eq2online.macros.gui.layout.LayoutWidget;
import net.eq2online.macros.interfaces.ILayoutWidget;
import watson.debug.Log;

// ----------------------------------------------------------------------------
/**
 * Manages script interface objects and isolates and protects the rest of Watson
 * from {@link NoClassFoundDefError} in the event that the Macro/Keybind Mod is
 * not loaded.
 */
public class MacroIntegration
{
  /**
   * Name of the onWatsonDisplay event.
   */
  public static String ON_WATSON_DISPLAY      = "onWatsonDisplay";

  /**
   * ID of the onWatsonDisplay event.
   */
  public static int    ON_WATSON_DISPLAY_ID   = 1250;

  /**
   * Name of the onWatsonDisplay event.
   */
  public static String ON_WATSON_SELECTION    = "onWatsonSelection";

  /**
   * ID of the onWatsonSelection event.
   */
  public static int    ON_WATSON_SELECTION_ID = 1251;

  // --------------------------------------------------------------------------
  /**
   * Register custom Macro/Keybind Mod scripting support.
   * 
   * All calls are by reflection to prevent direct dependency of this class on
   * Macro/Keybind Mod classes.
   */
  public static void initialiseMacroKeybind()
  {
    if (callInitialise("watson.macro.ScriptActionWatson")
        && callInitialise("watson.macro.VariableProviderWatson")
        && defineEvent(ON_WATSON_DISPLAY, ON_WATSON_DISPLAY_ID)
        && defineEvent(ON_WATSON_SELECTION, ON_WATSON_SELECTION_ID))
    {
      Log.info("Macro/Keybind Mod support for Watson initialised.");
    }
    else
    {
      Log.warning("Unable to register Macro/Keybind Mod support for Watson");
    }
  } // initialiseMacroKeybind

  // --------------------------------------------------------------------------
  /**
   * Send the specified event, with optional arguments array.
   */
  public static void sendEvent(String eventName, String... eventArgs)
  {
    try
    {
      // Get a reference to the MacroEventManager.
      Macros macros = MacroModCore.getMacroManager();
      MacroEventManager eventManager = macros.getEventManager();

      // Immediate dispatch (priority 100) results in the event handler getting
      // called before variables are updated.
      eventManager.sendEvent(eventName, 0, eventArgs);
    }
    catch (Throwable ex)
    {
      // If the Macro/Keybind Mod is not loaded, there would be rather a lot of
      // these, so don't log.
    }
  } // sendEvent

  // --------------------------------------------------------------------------
  /**
   * Call the static initialise() method of the specified class.
   * 
   * @return true if the method was called; false should indicate that the
   *         Macro/Keybind Mod was not loaded.
   */
  protected static boolean callInitialise(String className)
  {
    try
    {
      Class<?> c = Class.forName(className);
      Method initialise = c.getMethod("initialise");
      initialise.invoke(null);
      Log.debug("Initialised " + className);
      return true;
    }
    catch (Throwable ex)
    {
      Log.debug(ex.getClass().getName() + " calling " + className
                + ".initialise()");
    }
    return false;
  } // callInitialise

  // --------------------------------------------------------------------------
  /**
   * Define a new Watson-specific event type.
   * 
   * @param name the name of the event.
   * @param suggestedID the suggested integer ID of the event; if it is already
   *          in use, higher numbers will be tried successively until an unused
   *          one is found or the maximum ID is reached.
   */
  protected static boolean defineEvent(String name, int suggestedID)
  {
    try
    {
      // Get a reference to the MacroEventManager.
      Macros macros = MacroModCore.getMacroManager();
      MacroEventManager eventManager = macros.getEventManager();

      // Find an unused event ID, starting at suggestedID.
      int id = suggestedID;
      while (eventManager.getEvent(id) != null && id < 2000)
      {
        ++id;
      }
      if (id >= 2000)
      {
        Log.debug("No Macro/Keybind Mod event IDs are available.");
        return false;
      }

      // Make MacroEventManager.AddEvent() callable and call it.
      Method addEvent = eventManager.getClass().getDeclaredMethod("addEvent",
        Integer.TYPE, String.class, Boolean.TYPE, String.class);
      addEvent.setAccessible(true);
      addEvent.invoke(eventManager, id, name, false, null);
      Log.debug(String.format("Defined event %s with ID %d.", name, id));

      return true; // fixEventsGUI(name, id);
    }
    catch (Throwable ex)
    {
      Log.exception(Level.FINE, "exception defining Macro/Keybind mod event",
        ex);
    }
    return false;
  } // defineEvent

  // --------------------------------------------------------------------------
  /**
   * Fix the label of the custom Watson events buttons in the
   * net.eq2online.macros.gui.layout.LayoutPanelEvents panel.
   * 
   * By default, MacroType.GetMacroName() gives these a generic name based on
   * the ID number minus 1000, e.g. "onEvent250".
   * 
   * As of Macro/Keybind Mod 0.9.9 for 1.5.2, this code is apparently no-longer
   * necessary. I'll keep it around until I'm satisfied that the events API is
   * finalised.
   */
  protected static boolean fixEventsGUI(String name, int id)
    throws Exception
  {
    // Get the LayoutPanelEvents instance.
    MacroModCore core = MacroModCore.getInstance();
    Field eventLayoutField = core.getClass().getDeclaredField("eventLayout");
    eventLayoutField.setAccessible(true);
    LayoutPanelEvents panel = (LayoutPanelEvents) eventLayoutField.get(core);

    // Get the inherited LayoutPanel.widgets[], field.
    Field widgetsField = LayoutPanel.class.getDeclaredField("widgets");
    widgetsField.setAccessible(true);
    ILayoutWidget[] widgets = (ILayoutWidget[]) widgetsField.get(panel);

    // In for a penny, in for a pound. I could just create a new widget and
    // place it in the array but I'm not sure whether the lowest layers are
    // registered somewhere. Instead, get the LayoutWidget.displayText field and
    // set it to the right value. A mutator for that field would be nice. -_-
    Field displayTextField = LayoutWidget.class.getDeclaredField("displayText");
    displayTextField.setAccessible(true);
    displayTextField.set(widgets[id], name);
    return true;
  } // fixEventsGUI
} // class MacroIntegration
