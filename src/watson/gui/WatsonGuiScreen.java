package watson.gui;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.resources.I18n;
import watson.Configuration;
import watson.Controller;
import watson.DisplaySettings;

// ----------------------------------------------------------------------------
/**
 * The Watson in-game GUI screen.
 */
public class WatsonGuiScreen extends GuiScreen
{
  // --------------------------------------------------------------------------
  /**
   * @see GuiScreen#initGui()
   */
  @SuppressWarnings("unchecked")
  @Override
  public void initGui()
  {
    Minecraft mc = Minecraft.getMinecraft();
    FontRenderer fr = mc.fontRendererObj;
    _rowHeight = (int) (fr.FONT_HEIGHT * 2.75);
    int startY = height / 2 - 4 * _rowHeight;
    int currentRow = 0;

    buttonList.clear();
    buttonList.add(new GuiButton(ID_SHOW_WATSON, width / 2 - 175, startY + currentRow * _rowHeight, 150, 20,
                                 getButtonLabel(ID_SHOW_WATSON)));
    buttonList.add(new GuiButton(ID_CLEAR_EDITS, width / 2 + 25, startY + currentRow++ * _rowHeight, 150, 20,
                                 getButtonLabel(ID_CLEAR_EDITS)));
    buttonList.add(new GuiButton(ID_SHOW_VECTORS, width / 2 - 175, startY + currentRow * _rowHeight, 150, 20,
                                 getButtonLabel(ID_SHOW_VECTORS)));

    GuiResponder sliderResponder = new GuiPageButtonList.GuiResponder()
    {
      @Override
      public void onTick(int id, float value)
      {
        // Update the display when the slider slides. Save the new length in
        // the configuration when the GUI closes.
        DisplaySettings ds = Controller.instance.getDisplaySettings();
        ds.setMinVectorLength((int) value, false);
      }

      @Override
      public void func_175321_a(int id, boolean value)
      {
      }

      @Override
      public void func_175319_a(int id, String value)
      {
      }
    };

    Configuration config = Configuration.instance;
    GuiSlider slider = new GuiSlider(sliderResponder,
                                     ID_MIN_VECTOR_LENGTH, width / 2 + 25, startY + currentRow++ * _rowHeight,
                                     "Min Vector Length", 0, 20,
                                     config.getVectorLength(),
                                     new GuiSlider.FormatHelper()
                                     {
                                       @Override
                                       public String getText(int id, String name, float value)
                                       {
                                         return "Min Vector Length: " + Integer.toString((int) value);
                                       }
                                     });
    buttonList.add(slider);
    buttonList.add(new GuiButton(ID_SHOW_LABELS, width / 2 - 175, startY + currentRow * _rowHeight, 150, 20,
                                 getButtonLabel(ID_SHOW_LABELS)));
    buttonList.add(new GuiButton(ID_LABEL_ORDER, width / 2 + 25, startY + currentRow++ * _rowHeight, 150, 20,
                                 getButtonLabel(ID_LABEL_ORDER)));
    buttonList.add(new GuiButton(ID_SHOW_ANNOTATIONS, width / 2 - 175, startY + currentRow * _rowHeight, 150, 20,
                                 getButtonLabel(ID_SHOW_ANNOTATIONS)));
    buttonList.add(new GuiButton(ID_SHOW_SELECTION, width / 2 + 25, startY + currentRow++ * _rowHeight, 150, 20,
                                 getButtonLabel(ID_SHOW_SELECTION)));

    buttonList.add(new GuiOptionButton(ID_DONE, width / 2 - 40, startY + (currentRow + 2) * _rowHeight, 80, 20,
                                       I18n.format("gui.done", new Object[0])));
    enableButtons();
  } // initGui

  // --------------------------------------------------------------------------
  /**
   * @see GuiScreen#onGuiClosed()
   */
  @Override
  public void onGuiClosed()
  {
    Configuration config = Configuration.instance;
    DisplaySettings ds = Controller.instance.getDisplaySettings();
    if ((int) ds.getMinVectorLength() != (int) config.getVectorLength())
    {
      config.setVectorLength((int) ds.getMinVectorLength(), true);
    }
  }

  // --------------------------------------------------------------------------
  /**
   * @see GuiScreen#drawScreen(int, int, float)
   */
  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks)
  {
    drawRect(0, 0, width, height, 0xA0000000);
    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  // --------------------------------------------------------------------------
  /**
   * Enable the buttons that show or hide various part of the Watson display
   * only when the Watson display is visible.
   */
  protected void enableButtons()
  {
    DisplaySettings ds = Controller.instance.getDisplaySettings();
    for (Object o : buttonList)
    {
      GuiButton control = (GuiButton) o;
      if (control.id >= ID_SHOW_VECTORS && control.id <= ID_SHOW_SELECTION)
      {
        control.enabled = ds.isDisplayed();
      }
    }
  }

  // --------------------------------------------------------------------------
  /**
   * @see GuiScreen#actionPeformed(GuiButton)
   *
   *      When a button is clicked, update the corresponding display sections
   *      and the button label.
   */
  @Override
  protected void actionPerformed(GuiButton button)
    throws IOException
  {
    DisplaySettings ds = Controller.instance.getDisplaySettings();
    Configuration config = Configuration.instance;
    switch (button.id)
    {
      case ID_CLEAR_EDITS:
        Controller.instance.clearBlockEditSet();
        break;
      case ID_SHOW_WATSON:
        ds.setDisplayed(!ds.isDisplayed());
        button.displayString = getButtonLabel(button.id);
        enableButtons();
        break;
      case ID_SHOW_VECTORS:
        ds.setVectorsShown(!ds.areVectorsShown());
        button.displayString = getButtonLabel(button.id);
        break;
      case ID_MIN_VECTOR_LENGTH:
        break;
      case ID_SHOW_LABELS:
        ds.setLabelsShown(!ds.areLabelsShown());
        button.displayString = getButtonLabel(button.id);
        break;
      case ID_LABEL_ORDER:
        config.setTimeOrderedDeposits(!config.timeOrderedDeposits());
        button.displayString = getButtonLabel(button.id);
        break;
      case ID_SHOW_ANNOTATIONS:
        ds.setAnnotationsShown(!ds.areAnnotationsShown());
        button.displayString = getButtonLabel(button.id);
        break;
      case ID_SHOW_SELECTION:
        ds.setSelectionShown(!ds.isSelectionShown());
        button.displayString = getButtonLabel(button.id);
        break;
      case ID_DONE:
        mc.displayGuiScreen(null);
        break;
    }
  } // actionPerformed

  // --------------------------------------------------------------------------
  /**
   * Return the label text for a toggle button, based on its ID.
   *
   * @param id the control ID.
   * @return the label text for a toggle button, based on its ID.
   */
  protected String getButtonLabel(int id)
  {
    DisplaySettings ds = Controller.instance.getDisplaySettings();
    Configuration config = Configuration.instance;
    switch (id)
    {
      case ID_CLEAR_EDITS:
        return "Clear Edits";
      case ID_SHOW_WATSON:
        return "Watson Display: " + (ds.isDisplayed() ? "ON" : "OFF");
      case ID_SHOW_VECTORS:
        return "Show Vectors: " + (ds.areVectorsShown() ? "ON" : "OFF");
      case ID_MIN_VECTOR_LENGTH:
        // Slider uses a callback to set text.
        return "";
      case ID_SHOW_LABELS:
        return "Show Labels: " + (ds.areLabelsShown() ? "ON" : "OFF");
      case ID_LABEL_ORDER:
        return "Label Order: " + (config.timeOrderedDeposits() ? "TIMESTAMPS" : "IMPORTANCE");
      case ID_SHOW_ANNOTATIONS:
        return "Show Annotations: " + (ds.areAnnotationsShown() ? "ON" : "OFF");
      case ID_SHOW_SELECTION:
        return "Show Selection: " + (ds.isSelectionShown() ? "ON" : "OFF");
      default:
        return "";
    }
  } // getButtonLabel

  // --------------------------------------------------------------------------
  /**
   * The distance interval, in pixels between the top of one row of controls and
   * the top of the next.
   *
   * This is based on FontRenderer.FONT_HEIGHT, which is the height of the
   * current font. A default button in Minecraft is 200 wide and 20 tall, which
   * is near enough to 2 * FontRenderer.FONT_HEIGHT (= 2 * 9) pixels tall.
   */
  private int              _rowHeight;

  // IDs of the various controls.
  private static final int ID_CLEAR_EDITS       = 1;
  private static final int ID_SHOW_WATSON       = 2;
  private static final int ID_SHOW_VECTORS      = 3;
  private static final int ID_MIN_VECTOR_LENGTH = 4;
  private static final int ID_SHOW_LABELS       = 5;
  private static final int ID_LABEL_ORDER       = 6;
  private static final int ID_SHOW_ANNOTATIONS  = 7;
  private static final int ID_SHOW_SELECTION    = 8;
  private static final int ID_DONE              = 100;
} // class WatsonGuiScreen