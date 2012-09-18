package watson.cli;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.logging.Level;

import watson.debug.Log;

// --------------------------------------------------------------------------
/**
 * An implementation of {@link ICommandAction} that uses reflection to call a
 * method.
 */
public class MethodCommandAction implements ICommandAction
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param target the instance whose method is called.
   * @param methodName the method name.
   * @param parameterTypes the format parameter types, in declaration order.
   */
  public MethodCommandAction(Object target, String methodName,
                             Class<?>... parameterTypes)
  {
    _target = target;
    try
    {
      _method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
      _method.setAccessible(true);
    }
    catch (Exception ex)
    {
      Log.exception(Level.SEVERE,
        "exception setting up command line handler: ", ex);
    }
  } // MethodCommandACtion

  // --------------------------------------------------------------------------
  /**
   * This method is called when this {@link ICommandAction} is triggered by
   * successful parsing of the corresponding command line.
   * 
   * The method specified at construction time is called.
   */
  @Override
  public String perform(Command command, LinkedHashMap<String, Object> args)
  {
    try
    {
      Object result = _method.invoke(_target, args.values().toArray());
      return (result != null) ? result.toString() : "";
    }
    catch (Exception ex)
    {
      return ex.getMessage();
    }
  } // perform

  // --------------------------------------------------------------------------
  /**
   * The target of the reflective method call.
   */
  protected Object _target;

  /**
   * The method to call.
   */
  protected Method _method;
} // class MethodCommandAction