package watson;

import com.mumfrey.liteloader.core.runtime.Obf;
import com.mumfrey.liteloader.util.PrivateFields;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.EnumChatFormatting;

// ----------------------------------------------------------------------------
/**
 * An object that provides access to private fields in Mojang code that lack
 * public access methods.
 */
public class PrivateFieldsWatson<P, T> extends PrivateFields<P, T>
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param owner class that owns the field to be accessed.
   * @param obf the obfuscation entry describing the name of the field in
   *          various run-time environments - unobfuscated,
   */
  protected PrivateFieldsWatson(Class<P> owner, Obf obf)
  {
    super(owner, obf);
  }

  // --------------------------------------------------------------------------

  public static final PrivateFieldsWatson<EnumChatFormatting, Character> formattingCode = new PrivateFieldsWatson<EnumChatFormatting, Character>(
                                                                                          EnumChatFormatting.class,
                                                                                          WatsonObf.EnumChatFormatting_formattingCode);
  public static final PrivateFieldsWatson<RenderManager, Double>         renderPosX     = new PrivateFieldsWatson<RenderManager, Double>(
                                                                                          RenderManager.class,
                                                                                          WatsonObf.RenderManager_renderPosX);
  public static final PrivateFieldsWatson<RenderManager, Double>         renderPosY     = new PrivateFieldsWatson<RenderManager, Double>(
                                                                                          RenderManager.class,
                                                                                          WatsonObf.RenderManager_renderPosY);
  public static final PrivateFieldsWatson<RenderManager, Double>         renderPosZ     = new PrivateFieldsWatson<RenderManager, Double>(
                                                                                          RenderManager.class,
                                                                                          WatsonObf.RenderManager_renderPosZ);
} // class PrivateFieldsWatson<P, T>
