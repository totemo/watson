package watson.gui;

import net.minecraft.client.gui.GuiButton;

// --------------------------------------------------------------------------
/**
 * A custom GuiButton that sets a {@link ModifiedKeyBinding}.
 */
public class KeyBindingButton extends GuiButton
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   *
   * @param buttonId control ID.
   * @param x X position.
   * @param y Y position.
   * @param width width.
   * @param height height.
   * @param keyBinding {@link ModifiedKeyBinding} edited by this button.
   */
  public KeyBindingButton(int buttonId, int x, int y, int widthIn, int heightIn, ModifiedKeyBinding keyBinding)
  {
    super(buttonId, x, y, widthIn, heightIn, keyBinding.toString());
    _keyBinding = keyBinding;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the {@link ModifiedKeyBinding}.
   *
   * @return the {@link ModifiedKeyBinding}.
   */
  public ModifiedKeyBinding getKeyBinding()
  {
    return _keyBinding;
  }

  // --------------------------------------------------------------------------
  /**
   * The {@link ModifiedKeyBinding} edited by this button.
   */
  protected ModifiedKeyBinding _keyBinding;
} // class KeyBindingButton