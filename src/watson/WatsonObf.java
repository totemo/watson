package watson;

import com.mumfrey.liteloader.core.runtime.Obf;

/**
 * Created by guntherdw on 12/01/15.
 */
public class WatsonObf extends Obf {
    /**
     * @param seargeName
     * @param obfName
     * @param mcpName
     */
    protected WatsonObf(String seargeName, String obfName, String mcpName) {
        super(seargeName, obfName, mcpName);
    }

    /**
     * @param seargeName
     * @param obfName
     */
    public WatsonObf(String seargeName, String obfName) {
        super(seargeName, obfName, seargeName);
    }

    protected static WatsonObf EnumChatFormatting_formattingCode = new WatsonObf("field_96329_z", "z", "formattingCode");
    protected static WatsonObf RenderManager_renderPosX = new WatsonObf("field_78725_b", "o", "renderPosX");
    protected static WatsonObf RenderManager_renderPosY = new WatsonObf("field_78726_c", "p", "renderPosY");
    protected static WatsonObf RenderManager_renderPosZ = new WatsonObf("field_78723_d", "q", "renderPosZ");
}
