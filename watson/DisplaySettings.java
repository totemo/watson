package watson;

// --------------------------------------------------------------------------
/**
 * Records the current settings that affect the Watson displays.
 */
public class DisplaySettings
{
  // --------------------------------------------------------------------------
  /**
   * Turn on or off all Watson displays.
   * 
   * @param displayed true if Watson draws stuff; false otherwise.
   */
  public void setDisplayed(boolean displayed)
  {
    _displayed = displayed;
    Controller.instance.localChat("Watson display "
                                  + (displayed ? "enabled." : "disabled."));
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
   * Turn on or off the wireframe block outline display.
   * 
   * @param outlineShown if true, block outlines are drawn.
   */
  public void setOutlineShown(boolean outlineShown)
  {
    _outlineShown = outlineShown;
    Controller.instance.localChat("Outline display "
                                  + (outlineShown ? "enabled." : "disabled."));
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if block outines should be drawn.
   * 
   * This method takes into account the last calls to both setOutlineShown() and
   * setDisplayed(). It will return false if outlines are disabled or if the
   * overall Watson display is turned off.
   * 
   * @return true if block outines should be drawn.
   */
  public boolean isOutlineShown()
  {
    return _displayed && _outlineShown;
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
    Controller.instance.localChat("Annotation display "
                                  + (annotationsShown ? "enabled."
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
    return _displayed && _annotationsShown;
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
    _vectorsShown = vectorsShown;
    Controller.instance.localChat("Vector display "
                                  + (vectorsShown ? "enabled." : "disabled."));
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if block outines should be drawn.
   * 
   * This method takes into account the last calls to both setOutlineShown() and
   * setDisplayed(). It will return false if outlines are disabled or if the
   * overall Watson display is turned off.
   * 
   * @return true if block outines should be drawn.
   */
  public boolean areVectorsShown()
  {
    return _displayed && _vectorsShown;
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
    Controller.instance.localChat("Vectors between block creations will be "
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
    Controller.instance.localChat("Vectors between block destructions will be "
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
   * @param minVectorLength
   */
  public void setMinVectorLength(float minVectorLength)
  {
    _minVectorLength = minVectorLength;
    Controller.instance.localChat("Minimum vector length set to "
                                  + minVectorLength);
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
   * True if wireframe block outlines should be drawn.
   */
  protected boolean _outlineShown       = true;

  /**
   * True if wireframe block outlines should be drawn.
   */
  protected boolean _annotationsShown   = true;

  /**
   * True if vectors between blocks that are more than getMinVectorLength()
   * blocks apart should be drawn.
   */
  protected boolean _vectorsShown       = true;

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