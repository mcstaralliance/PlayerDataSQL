package cc.bukkitPlugin.pds.dmodel.v1_12_2;

import java.lang.reflect.Method;

import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.commons.util.reflect.MethodUtil;

public class DM_DynamicSwordSkills extends ADM_CapabilityProvider {

    /** public static final DSSPlayerInfo get(EntityPlayer) { */
    private Method method_DSSPlayerInfo_get;
    /** public void onJoinWorld() */
    private Method method_DSSPlayerInfo_onJoinWorld;

    public DM_DynamicSwordSkills(PlayerDataSQL pPlugin) {
        super(pPlugin);

        this.addModCheckClass("dynamicswordskills.DynamicSwordSkills");
        this.addCapabilityP("dynamicswordskills.capability.SimpleCapabilityProvider");
    }

    @Override
    public String getModelId() {
        return "DynamicSwordSkills_v1_12_2";
    }

    @Override
    public String getDesc() {
        return "动态剑技";
    }

    @Override
    protected boolean initCapability() throws Exception {
        Class<?> tClazz = Class.forName("dynamicswordskills.entity.DSSPlayerInfo");
        this.method_DSSPlayerInfo_get = MethodUtil.getDeclaredMethod(tClazz, "get", NMSUtil.clazz_EntityPlayer);
        this.method_DSSPlayerInfo_onJoinWorld = MethodUtil.getDeclaredMethod(tClazz, "onJoinWorld");

        return super.initCapability();
    }

    @Override
    public void updateAround(CPlayer pPlayer, Class<?> pProvider) {
        Object tIn_DSS = MethodUtil.invokeStaticMethod(method_DSSPlayerInfo_get, pPlayer.getNMSPlayer());
        if (tIn_DSS != null) {
            MethodUtil.invokeMethod(method_DSSPlayerInfo_onJoinWorld, tIn_DSS);
        }

        super.updateAround(pPlayer, pProvider);
    }

}
