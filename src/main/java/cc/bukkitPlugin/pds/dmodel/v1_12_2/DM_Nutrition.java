package cc.bukkitPlugin.pds.dmodel.v1_12_2;

import java.lang.reflect.Method;

import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.commons.util.reflect.MethodUtil;

public class DM_Nutrition extends ADM_CapabilityProvider {

    private Method method_Sync_serverRequest;

    public DM_Nutrition(PlayerDataSQL pPlugin) {
        super(pPlugin);

        this.addModCheckClass("ca.wescook.nutrition.Nutrition");
        this.addCapabilityP("ca.wescook.nutrition.capabilities.CapabilityManager$Provider");
    }

    @Override
    public String getModelId() {
        return "Nutrition_v1_12_2";
    }

    @Override
    public String getDesc() {
        return "营养学";
    }

    @Override
    protected boolean initCapability() throws Exception {
        Class<?> tClazz = Class.forName("ca.wescook.nutrition.network.Sync");
        this.method_Sync_serverRequest = MethodUtil.getMethodIgnoreParam(tClazz, "serverRequest", true).oneGet();

        return super.initCapability();
    }

    @Override
    public void updateAround(CPlayer pPlayer, Class<?> pProvider) {
        MethodUtil.invokeStaticMethod(method_Sync_serverRequest, pPlayer.getNMSPlayer());

        super.updateAround(pPlayer, pProvider);
    }

}
