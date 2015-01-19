package watson;

import com.mumfrey.liteloader.core.runtime.Obf;
import com.mumfrey.liteloader.util.PrivateFields;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.EnumChatFormatting;

/**
 * Created by guntherdw on 12/01/15.
 */
// public class PrivateFieldsClient<P, T> extends PrivateFields<P, T>
public class PrivateFieldsWatson<P, T> extends PrivateFields<P, T> {
    /**
     * Creates a new private field entry
     *
     * @param owner
     * @param obf
     */
    protected PrivateFieldsWatson(Class<P> owner, Obf obf) {
        super(owner, obf);
    }

    public static final PrivateFieldsWatson<EnumChatFormatting, Character>       formattingCode = new PrivateFieldsWatson<EnumChatFormatting, Character>(EnumChatFormatting.class, WatsonObf.EnumChatFormatting_formattingCode);
    public static final PrivateFieldsWatson<RenderManager, Double>               renderPosX     = new PrivateFieldsWatson<RenderManager, Double>(RenderManager.class, WatsonObf.RenderManager_renderPosX);
    public static final PrivateFieldsWatson<RenderManager, Double>               renderPosY     = new PrivateFieldsWatson<RenderManager, Double>(RenderManager.class, WatsonObf.RenderManager_renderPosY);
    public static final PrivateFieldsWatson<RenderManager, Double>               renderPosZ     = new PrivateFieldsWatson<RenderManager, Double>(RenderManager.class, WatsonObf.RenderManager_renderPosZ);
}
