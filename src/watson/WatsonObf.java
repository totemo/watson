package watson;

import com.mumfrey.liteloader.core.runtime.Obf;

// ----------------------------------------------------------------------------
/**
 * Describes the obfuscation mappings for private fields accessed through
 * {@link PrivateFieldsWatson}.
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
   * @param seargeName the Searge name of the field.
   * @param obfName the fully obfuscated name of the field.
   * @param mcpName the MCP-assigned name of the field.
   */
  protected WatsonObf(String seargeName, String obfName, String mcpName)
  {
    super(seargeName, obfName, mcpName);
  }

  // --------------------------------------------------------------------------
  /**
   * Constructor for classes.
   * 
   * @param seargeName
   * @param obfName
   */
  public WatsonObf(String seargeName, String obfName)
  {
    super(seargeName, obfName, seargeName);
  }

  // --------------------------------------------------------------------------

  protected static WatsonObf EnumChatFormatting_formattingCode = new WatsonObf("field_96329_z", "z", "formattingCode");
  protected static WatsonObf RenderManager_renderPosX          = new WatsonObf("field_78725_b", "o", "renderPosX");
  protected static WatsonObf RenderManager_renderPosY          = new WatsonObf("field_78726_c", "p", "renderPosY");
  protected static WatsonObf RenderManager_renderPosZ          = new WatsonObf("field_78723_d", "q", "renderPosZ");
} // class WatsonObf
