package watson.chat;

import java.lang.reflect.Method;
import java.util.logging.Level;

import watson.debug.Log;

// ----------------------------------------------------------------------------
/**
 * An IChatHandler implementation that uses reflection to dispatch classify()
 * and revise() calls to two methods named at the time of construction.
 */
public class MethodChatHandler implements IChatHandler
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * NOTE: reflective method calling will not work for obfuscated methods.
   * 
   * The method signatures are assumed to be:
   * <ul>
   * <li>void classify(watson.chat.ChatLine line) - NB: not the Minecraft
   * ChatLine class</li>
   * <li>void revise(watson.chat.ChatLine line, watson.chat.ChatLine line)</li>
   * <ul>
   * However, if a two-argument revise method is not found, a method with the
   * specified name but the same signature as classify() (single ChatLine
   * paramter) will be used instead, if found.
   * 
   * @param target the target object upon which the methods are invoked.
   * @param classifyMethod the method to call for IChatHandler.classify(); if
   *          null or "", do not call.
   * @param reviseMethod the method to call for IChatHandler.revise(); if null
   *          or "", do not call.
   */
  public MethodChatHandler(Object target, String classifyMethod,
                           String reviseMethod)
  {
    _target = target;

    Class<?> cls = _target.getClass();
    if (classifyMethod != null && classifyMethod.length() != 0)
    {
      try
      {
        _classifyMethod = cls.getDeclaredMethod(classifyMethod, ChatLine.class);
        _classifyMethod.setAccessible(true);
      }
      catch (Exception ex)
      {
        Log.exception(Level.SEVERE, "no such method: " + classifyMethod, ex);
      }
    }
  } // MethodChatHandler

  // --------------------------------------------------------------------------
  /**
   * Shorthand for constructing a MethodChatHandler that calls the same
   * single-argument method for both the classify() and revise() callbacks of
   * the IChatHandler interface.
   * 
   * @param target the target object upon which the methods are invoked.
   * @param method the method to call for IChatHandler.classify() and
   *          IChatHandler.revise(); if null or "", do not call.
   */
  public MethodChatHandler(Object target, String method)
  {
    this(target, method, method);
  }

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public void classify(ChatLine line)
  {
    if (_classifyMethod != null)
    {
      try
      {
        _classifyMethod.invoke(_target, line);
      }
      catch (Exception ex)
      {
        Log.exception(Level.SEVERE, "problem calling: " + _classifyMethod, ex);
      }
    }
  } // classify

  // --------------------------------------------------------------------------
  /**
   * The target object upon which the methods are invoked.
   */
  private Object _target;

  /**
   * The method to call for IChatHandler.classify().
   */
  private Method _classifyMethod;

  /**
   * The method to call for IChatHandler.revise().
   */
  private Method _reviseMethod;

  /**
   * True if the _reviseMethod takes a single argument instead of two.
   */
  boolean        _singleArgRevise;
} // class MethodChatHandler
