package watson;

import com.mumfrey.liteloader.core.runtime.Obf;

// ----------------------------------------------------------------------------
/**
 * Describes the obfuscation mappings for private accessed through
 * {@link PrivateFieldsWatson} and for methods modified by
 * {@link watson.transformer.WatsonTransformer}.
 *
 * When accessing a private field via reflection, its name will vary depending
 * on the runtime environment. In the IDE, the fully-deobfuscated MCP name will
 * be available. Running as a deployed mod, the field will have its fully
 * obfuscated name, e.g. "a", unless Forge ModLoader is running, in which case
 * the field will take its Searge name, e.g. field_12345_a.
 *
 * For classes, MCP always uses the class name assigned by Searge.
 */
public class WatsonObf extends Obf
{
  // --------------------------------------------------------------------------
  /**
   * Constructor for fields.
   *
   * @param seargeName the Searge name of the member.
   * @param obfName the fully obfuscated name of the member.
   * @param mcpName the MCP-assigned name of the member.
   */
  protected WatsonObf(String seargeName, String obfName, String mcpName)
  {
    super(seargeName, obfName, mcpName);
  }

  // --------------------------------------------------------------------------
  /**
   * Constructor for classes.
   *
   * @param seargeName the Searge name of the member.
   * @param obfName the fully obfuscated name of the member.
   */
  public WatsonObf(String seargeName, String obfName)
  {
    super(seargeName, obfName, seargeName);
  }

  // --------------------------------------------------------------------------
  // Obfuscation data for net.minecraft.client.settings.KeyBinding
  // and methods thereof.

  public static WatsonObf    KeyBinding                        = new WatsonObf("net.minecraft.client.settings.KeyBinding",
                                                                               "bsr");
  public static WatsonObf    KeyBinding_onTick                 = new WatsonObf("func_74507_a", "a", "onTick");
  public static WatsonObf    KeyBinding_setKeyBindState        = new WatsonObf("func_74510_a", "a", "setKeyBindState");

  // --------------------------------------------------------------------------
  // Obfuscation data for net.minecraft.entity.player.InventoryPlayer
  // and methods thereof.

  public static WatsonObf    InventoryPlayer                   = new WatsonObf("net.minecraft.entity.player.InventoryPlayer",
                                                                               "ahb");
  public static WatsonObf    InventoryPlayer_changeCurrentItem = new WatsonObf("func_70453_c", "d", "changeCurrentItem");

  // --------------------------------------------------------------------------
  // Private fields accessed by reflection.

  protected static WatsonObf EnumChatFormatting_formattingCode = new WatsonObf("field_96329_z", "z", "formattingCode");
  protected static WatsonObf RenderManager_renderPosX          = new WatsonObf("field_78725_b", "o", "renderPosX");
  protected static WatsonObf RenderManager_renderPosY          = new WatsonObf("field_78726_c", "p", "renderPosY");
  protected static WatsonObf RenderManager_renderPosZ          = new WatsonObf("field_78723_d", "q", "renderPosZ");
} // class WatsonObf
