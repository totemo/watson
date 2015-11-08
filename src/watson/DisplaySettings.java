package watson;

import net.minecraft.world.WorldSettings;
import watson.chat.Chat;

// ----------------------------------------------------------------------------
/**
 * Records the current settings that affect the Watson displays.
 */
public class DisplaySettings
{
  // --------------------------------------------------------------------------
  /**
   * This method configures the initial display settings based on the server
   * being connected to and the game type.
   */
  public void configure(@SuppressWarnings("unused") String serverIP, WorldSettings.GameType gameType)
  {
    // The Watson display defaults to on. On survival servers, assume the
    // presence of ModMode and its associated notifications to turn on or off
    // the display.
    _displayed = gameType.isCreative();
    _minVectorLength = Configuration.instance.getVectorLength();
  }

  // --------------------------------------------------------------------------
  /**
   * Turn on or off all Watson displays.
   *
   * @param displayed true if Watson draws stuff; false otherwise.
   */
  public void setDisplayed(boolean displayed)
  {
    _displayed = displayed;
    Chat.localOutput("Watson display " + (displayed ? "enabled." : "disabled."));
    _displayVisibilityChanged = true;
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if Watson draws stuff; false otherwise.
   *
   * @return true if Watson draws stuff; false otherwise.
   */
  public boolean isDisplayed()
  {
    return _displayed;
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the Watson display's visibility setting has changed since
   * the last time this method was called.
   *
   * This is used by the Watson Macro/Keybind Support mod to determine when to
   * dispatch the corresponding event.
   *
   * @return true if isDisplayed() has changed.
   */
  public boolean isDisplayVisibilityChanged()
  {
    boolean result = _displayVisibilityChanged;
    _displayVisibilityChanged = false;
    return result;
  }

  // --------------------------------------------------------------------------
  /**
   * Turn on or off the wireframe block outline display.
   *
   * @param outlineShown if true, block outlines are drawn.
   */
  public void setOutlineShown(boolean outlineShown)
  {
    _outlineShown = outlineShown;
    Chat.localOutput("Outline display " + (outlineShown ? "enabled." : "disabled."));
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if block outines should be drawn.
   *
   * @return true if block outines should be drawn.
   */
  public boolean areOutlineShown()
  {
    return _outlineShown;
  }

  // --------------------------------------------------------------------------
  /**
   * Turn on or off the annotation display.
   *
   * @param annotationsShown if true, annotations are drawn.
   */
  public void setAnnotationsShown(boolean annotationsShown)
  {
    _annotationsShown = annotationsShown;
    Chat.localOutput("Annotation display " + (annotationsShown ? "enabled."
      : "disabled."));
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if block annotations should be drawn.
   *
   * @return true if block annotations should be drawn.
   */
  public boolean areAnnotationsShown()
  {
    return _annotationsShown;
  }

  // --------------------------------------------------------------------------
  /**
   * Turn on or off the ore deposit number labels.
   *
   * @param labelsShown if true, labels are shown.
   */
  public void setLabelsShown(boolean labelsShown)
  {
    _labelsShown = labelsShown;
    Chat.localOutput("Ore deposit label display " + (labelsShown ? "enabled." : "disabled."));
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if ore deposit labels should be drawn.
   *
   * @return true if ore deposit labels should be drawn.
   */
  public boolean areLabelsShown()
  {
    return _labelsShown;
  }

  // --------------------------------------------------------------------------
  /**
   * Turn on or off highlighting of the currently selected edit or position.
   *
   * @param selectionShown if true, the selection is highlighted.
   */
  public void setSelectionShown(boolean selectionShown)
  {
    _selectionShown = selectionShown;
    Chat.localOutput("Current selection display " + (selectionShown ? "enabled." : "disabled."));
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the current selection should be drawn.
   *
   * The selection is either a single edit, or a position.
   *
   * @return true if the current selection should be drawn.
   */
  public boolean isSelectionShown()
  {
    return _selectionShown;
  }

  // --------------------------------------------------------------------------
  /**
   * Turn on or off the wireframe vector display.
   *
   * @param outlineShown if true, vectors between sufficiently spaced blocks are
   *          drawn.
   */
  public void setVectorsShown(boolean vectorsShown)
  {
    Configuration.instance.setVectorsShown(vectorsShown);
    Chat.localOutput("Vector display " + (vectorsShown ? "enabled." : "disabled."));
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if block outines should be drawn.
   *
   * @return true if block outines should be drawn.
   */
  public boolean areVectorsShown()
  {
    return Configuration.instance.getVectorsShown();
  }

  // --------------------------------------------------------------------------
  /**
   * Control whether block creations are linked by vectors (when they are
   * shown).
   *
   * @param linkedCreations if true, links are drawn.
   */
  public void setLinkedCreations(boolean linkedCreations)
  {
    _linkedCreations = linkedCreations;
    Chat.localOutput("Vectors between block creations will be "
                     + (linkedCreations ? "shown." : "hidden."));
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if vectors are drawn between block creations.
   *
   * @return true if vectors are drawn between block creations.
   */
  public boolean isLinkedCreations()
  {
    return _linkedCreations;
  }

  // --------------------------------------------------------------------------
  /**
   * Control whether block destructions are linked by vectors (when they are
   * shown).
   *
   * @param linkedDestructions if true, links are drawn.
   */
  public void setLinkedDestructions(boolean linkedDestructions)
  {
    _linkedDestructions = linkedDestructions;
    Chat.localOutput("Vectors between block destructions will be "
                     + (linkedDestructions ? "shown." : "hidden."));
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if vectors are drawn between block destructions.
   *
   * @return true if vectors are drawn between block destructions.
   */
  public boolean isLinkedDestructions()
  {
    return _linkedDestructions;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the minimum length of a vector between edits for it to be drawn.
   *
   * @param minVectorLength the minimum length of a vector between edits for it
   *          to be drawn.
   * @param showInChat if true a message indicating the new minimum vector
   *          length is shown in chat.
   */
  public void setMinVectorLength(float minVectorLength, boolean showInChat)
  {
    _minVectorLength = minVectorLength;
    if (showInChat)
    {

      Chat.localOutput("Minimum vector length set to " + minVectorLength + ".");
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Return the minimum length of a vector between edits for it to be drawn.
   *
   * @return the minimum length of a vector between edits for it to be drawn.
   */
  public float getMinVectorLength()
  {
    return _minVectorLength;
  }

  // --------------------------------------------------------------------------
  /**
   * True if all Watson displays can be drawn. Other flags disable individual
   * displays.
   */
  protected boolean _displayed          = true;

  /**
   * True if _displayed has changed.
   */
  protected boolean _displayVisibilityChanged;

  /**
   * True if wireframe block outlines should be drawn.
   */
  protected boolean _outlineShown       = true;

  /**
   * True if annotations should be drawn.
   */
  protected boolean _annotationsShown   = true;

  /**
   * True if ore deposit labels should be drawn.
   */
  protected boolean _labelsShown        = true;

  /**
   * True if the current selection should be drawn.
   */
  protected boolean _selectionShown     = true;

  /**
   * If true, creation edits are linked by vectors.
   */
  protected boolean _linkedCreations    = true;

  /**
   * If true, destruction edits are linked by vectors.
   */
  protected boolean _linkedDestructions = true;

  /**
   * The minimum length of a vector between edits for it to be drawn.
   */
  protected float   _minVectorLength    = 4.0f;

} // class DisplaySettings