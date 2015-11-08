package watson.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import watson.Configuration;

import com.mumfrey.liteloader.modconfig.ConfigPanel;
import com.mumfrey.liteloader.modconfig.ConfigPanelHost;

// --------------------------------------------------------------------------
/**
 * Configuration GUI.
 */
public class WatsonConfigPanel extends Gui implements ConfigPanel
{
  // --------------------------------------------------------------------------
  /**
   * @see com.mumfrey.liteloader.modconfig.ConfigPanel#getPanelTitle()
   */
  @Override
  public String getPanelTitle()
  {
    return "Watson Configuration";
  }

  // --------------------------------------------------------------------------
  /**
   * @see com.mumfrey.liteloader.modconfig.ConfigPanel#getContentHeight()
   */
  @Override
  public int getContentHeight()
  {
    return (Configuration.instance.getAllModifiedKeyBindings().size() + 1) * getRowHeight();
  }

  // --------------------------------------------------------------------------
  /**
   * Initialise controls from the configuration settings when the panel is first
   * shown.
   *
   * @see com.mumfrey.liteloader.modconfig.ConfigPanel#onPanelShown(com.mumfrey.liteloader.modconfig.ConfigPanelHost)
   */
  @Override
  public void onPanelShown(ConfigPanelHost host)
  {
    int controlId = 0;
    for (ModifiedKeyBinding keyBinding : Configuration.instance.getAllModifiedKeyBindings())
    {
      final KeyBindingButton button = new KeyBindingButton(controlId, 135, (controlId + 1) * getRowHeight(),
                                                           180, 20, keyBinding);
      _keyButtons.add(button);

      Runnable handler = new Runnable()
      {
        @Override
        public void run()
        {
          // Clear any pre-accumulated mouse wheel delta as it will be detected
          // in onTick() as an attempt to bind to the scroll wheel.
          Mouse.getDWheel();

          _focusedControl = button;
          _focusedControl.enabled = false;
          _focusedControl.displayString = "Press a key or mouse combination.";
        }
      };
      _handlers.put(controlId, handler);
      ++controlId;
    }
  } // onPanelShown

  // --------------------------------------------------------------------------
  /**
   * @see com.mumfrey.liteloader.modconfig.ConfigPanel#onPanelResize(com.mumfrey.liteloader.modconfig.ConfigPanelHost)
   */
  @Override
  public void onPanelResize(ConfigPanelHost host)
  {
    // Nothing.
  }

  // --------------------------------------------------------------------------
  /**
   * @see com.mumfrey.liteloader.modconfig.ConfigPanel#onPanelHidden()
   */
  @Override
  public void onPanelHidden()
  {
    Configuration.instance.save();
  }

  // --------------------------------------------------------------------------
  /**
   * @see com.mumfrey.liteloader.modconfig.ConfigPanel#onTick(com.mumfrey.liteloader.modconfig.ConfigPanelHost)
   */
  @Override
  public void onTick(ConfigPanelHost host)
  {
    // When setting a key binding, monitor the mouse wheel on every tick.
    if (_focusedControl != null && _focusedControl instanceof KeyBindingButton)
    {
      KeyBindingButton button = (KeyBindingButton) _focusedControl;

      // Display current key modifiers on new key binding.
      boolean control = ModifiedKeyBinding.isCtrlDown();
      boolean alt = ModifiedKeyBinding.isAltDown();
      boolean shift = ModifiedKeyBinding.isShiftDown();
      if (control || alt || shift)
      {
        _showingModifiers = true;
      }
      if (_showingModifiers)
      {
        _focusedControl.displayString = ModifiedKeyBinding.getModifierString(control, alt, shift) + "???";
      }

      // Check for a mouse wheel movement and if so, set keybinding.
      int wheel = Mouse.getDWheel();
      // > 0 ==> Up
      if (wheel != 0)
      {
        setModifiedKeyBinding(button.getKeyBinding(),
                              (wheel > 0) ? MouseButton.SCROLL_UP.getCode() : MouseButton.SCROLL_DOWN.getCode());
      }
    }
  } // onTick

  // --------------------------------------------------------------------------
  /**
   * Draw the title and all the controls.
   *
   * @see com.mumfrey.liteloader.modconfig.ConfigPanel#drawPanel(com.mumfrey.liteloader.modconfig.ConfigPanelHost,
   *      int, int, float)
   */
  @Override
  public void drawPanel(ConfigPanelHost host, int mouseX, int mouseY, float partialTicks)
  {
    Minecraft mc = Minecraft.getMinecraft();
    FontRenderer fr = mc.fontRendererObj;
    int labelHeight = (int) (0.75 * fr.FONT_HEIGHT);
    drawCenteredString(fr, "Key Bindings", host.getWidth() / 2, labelHeight, 0xFFFFFF55);

    for (KeyBindingButton control : _keyButtons)
    {
      control.drawString(fr, control.getKeyBinding().getDescription(),
                         0, labelHeight + (control.id + 1) * _rowHeight, 0xFFFFFFFF);
      control.drawButton(mc, mouseX, mouseY);
    }
  }

  // --------------------------------------------------------------------------
  /**
   * @see com.mumfrey.liteloader.modconfig.ConfigPanel#mousePressed(com.mumfrey.liteloader.modconfig.ConfigPanelHost,
   *      int, int, int)
   */
  @Override
  public void mousePressed(ConfigPanelHost host, int mouseX, int mouseY, int mouseButton)
  {
    if (_focusedControl != null && _focusedControl instanceof KeyBindingButton)
    {
      KeyBindingButton button = (KeyBindingButton) _focusedControl;

      // A control is capturing all input. Set a bound key.
      int keyCode = (mouseButton == 0) ? MouseButton.MOUSE_LEFT.getCode() :
        (mouseButton == 1) ? MouseButton.MOUSE_RIGHT.getCode() : MouseButton.MOUSE_MIDDLE.getCode();
      setModifiedKeyBinding(button.getKeyBinding(), keyCode);
    }
    else
    {
      Minecraft mc = Minecraft.getMinecraft();
      // Iterate over all the controls and pass the mouse press to the control
      for (GuiButton control : _keyButtons)
      {
        // Note: GulButton.mousePressed() just returns true if the control is
        // enabled and visible, and the mouse coords are within the control.
        if (control.mousePressed(mc, mouseX, mouseY))
        {
          control.playPressSound(mc.getSoundHandler());
          Runnable handler = _handlers.get(control.id);
          if (handler != null)
          {
            handler.run();
          }
          break;
        }
      } // for
    } // no control has focus
  } // mousePressed

  // --------------------------------------------------------------------------
  /**
   * @see com.mumfrey.liteloader.modconfig.ConfigPanel#mouseReleased(com.mumfrey.liteloader.modconfig.ConfigPanelHost,
   *      int, int, int)
   */
  @Override
  public void mouseReleased(ConfigPanelHost host, int mouseX, int mouseY, int mouseButton)
  {
  }

  // --------------------------------------------------------------------------
  /**
   * @see com.mumfrey.liteloader.modconfig.ConfigPanel#mouseMoved(com.mumfrey.liteloader.modconfig.ConfigPanelHost,
   *      int, int)
   */
  @Override
  public void mouseMoved(ConfigPanelHost host, int mouseX, int mouseY)
  {
  }

  // --------------------------------------------------------------------------
  /**
   * @see com.mumfrey.liteloader.modconfig.ConfigPanel#keyPressed(com.mumfrey.liteloader.modconfig.ConfigPanelHost,
   *      char, int)
   */
  @Override
  public void keyPressed(ConfigPanelHost host, char keyChar, int keyCode)
  {
    if (_focusedControl != null && _focusedControl instanceof KeyBindingButton)
    {
      KeyBindingButton button = (KeyBindingButton) _focusedControl;

      boolean control = ModifiedKeyBinding.isCtrl(keyCode);
      boolean alt = ModifiedKeyBinding.isAlt(keyCode);
      boolean shift = ModifiedKeyBinding.isShift(keyCode);
      if (control || alt || shift)
      {
        // If just a modifier key, start updating the button label to display
        // currently held modifiers.
        _showingModifiers = true;
      }
      else
      {
        // Not a modifier.
        setModifiedKeyBinding(button.getKeyBinding(), keyCode);
      }
    }
    else
    {
      if (keyCode == Keyboard.KEY_ESCAPE)
      {
        host.close();
        return;
      }
    }
  } // keyPressed

  // --------------------------------------------------------------------------
  /**
   * This method is called when a key binding is finalised by pressing a key
   * that is not a modifier, clicking the mouse or scrolling the mouse wheel.
   *
   * If the user presses the Esc key, the key binding is set to NONE.
   *
   * @param binding the key binding.
   * @param keyCode the key code.
   */
  protected void setModifiedKeyBinding(ModifiedKeyBinding binding, int keyCode)
  {
    if (keyCode == Keyboard.KEY_ESCAPE)
    {
      binding.setCtrl(false);
      binding.setAlt(false);
      binding.setShift(false);
      binding.setKeyCode(Keyboard.KEY_NONE);
    }
    else
    {
      binding.setCtrl(ModifiedKeyBinding.isCtrlDown());
      binding.setAlt(ModifiedKeyBinding.isAltDown());
      binding.setShift(ModifiedKeyBinding.isShiftDown());
      binding.setKeyCode(keyCode);
    }
    _focusedControl.displayString = binding.toString();
    _focusedControl.enabled = true;
    _focusedControl = null;
    _showingModifiers = false;
  } // setModifiedKeyBinding

  // --------------------------------------------------------------------------
  /**
   * Return the distance in pixels between the top of one row of controls and
   * the top of the next.
   *
   * @return the distance in pixels between the top of one row of controls and
   *         the top of the next.
   */
  protected int getRowHeight()
  {
    if (_rowHeight == 0)
    {
      Minecraft mc = Minecraft.getMinecraft();
      FontRenderer fr = mc.fontRendererObj;
      _rowHeight = (int) (fr.FONT_HEIGHT * 2.75);
    }
    return _rowHeight;
  }

  // --------------------------------------------------------------------------
  /**
   * When non-null, all input is captured and handled by this control rather
   * than being passed to the control where the mouse pointer is located.
   */
  protected GuiButton                  _focusedControl;

  /**
   * Initially when a button is pressed to rebind a key, the button text is
   * changed to prompt for a key combination.
   *
   * When the first modifier key (Ctrl, Alt or Shift) is pressed, this flag is
   * set true, and the button text is updated thereafter on every tick to show
   * what modifier keys are currently held down.
   */
  protected boolean                    _showingModifiers;

  /**
   * Controls in this panel.
   */
  protected List<KeyBindingButton>     _keyButtons = new ArrayList<KeyBindingButton>();

  /**
   * The distance in pixels between the top of one row of controls and the top
   * of the next.
   *
   * This is based on FontRenderer.FONT_HEIGHT, which is the height of the
   * current font. A default button in Minecraft is 200 wide and 20 tall, which
   * is near enough to 2 * FontRenderer.FONT_HEIGHT (= 2 * 9) pixels tall.
   */
  protected int                        _rowHeight;

  /**
   * Map from control ID to handler for when it is clicked.
   */
  protected HashMap<Integer, Runnable> _handlers   = new HashMap<Integer, Runnable>();

} // class WatsonConfigPanel