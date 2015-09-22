package watson.transformer;

import watson.WatsonObf;

import com.mumfrey.liteloader.transformers.event.Event;
import com.mumfrey.liteloader.transformers.event.EventInjectionTransformer;
import com.mumfrey.liteloader.transformers.event.MethodInfo;
import com.mumfrey.liteloader.transformers.event.inject.MethodHead;

// ----------------------------------------------------------------------------
/**
 * An EventInjectionTransformer implementation that hooks mouse and keyboard
 * events on behalf of Watson. It is not possible to use
 * EventInjectionTransformer to hook LWJGL methods such as Mouse.next() and
 * Keyboard.next(), so the hooked methods are in Mojang code.
 */
public class WatsonTransformer extends EventInjectionTransformer
{
  // --------------------------------------------------------------------------
  /**
   * @see com.mumfrey.liteloader.transformers.event.EventInjectionTransformer#addEvents()
   */
  @Override
  protected void addEvents()
  {
    try
    {
      Event onKeyBindingOnTick = Event.getOrCreate("onKeyBindingOnTick", true);
      addEvent(onKeyBindingOnTick,
               new MethodInfo(WatsonObf.KeyBinding, WatsonObf.KeyBinding_onTick, "(I)V"),
               new MethodHead());
      onKeyBindingOnTick.addListener(new MethodInfo("watson.LiteModWatson", "onKeyBindingOnTick"));

      Event onKeyBindingSetKeyBindState = Event.getOrCreate("onKeyBindingSetKeyBindState", true);
      addEvent(onKeyBindingSetKeyBindState,
               new MethodInfo(WatsonObf.KeyBinding, WatsonObf.KeyBinding_setKeyBindState, "(IZ)V"),
               new MethodHead());
      onKeyBindingSetKeyBindState.addListener(new MethodInfo("watson.LiteModWatson", "onKeyBindingSetKeyBindState"));

      Event onInventoryPlayerChangeCurrentItem = Event.getOrCreate("onInventoryPlayerChangeCurrentItem", true);
      addEvent(onInventoryPlayerChangeCurrentItem,
               new MethodInfo(WatsonObf.InventoryPlayer, WatsonObf.InventoryPlayer_changeCurrentItem, "(I)V"),
               new MethodHead());
      onInventoryPlayerChangeCurrentItem.addListener(new MethodInfo("watson.LiteModWatson",
                                                                    "onInventoryPlayerChangeCurrentItem"));
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
} // class WatsonTransformer