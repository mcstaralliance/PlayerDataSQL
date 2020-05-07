package cc.bukkitPlugin.pds.dmodel.v1_12_2;

import java.lang.reflect.Method;

import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.commons.util.reflect.MethodUtil;

public class DM_ElectroblobsWizardry extends ADM_CapabilityProvider {

    /** static WizardData get(EntityPlayer) */
    private Method method_WizardData_get;
    /** void sync() */
    private Method method_WizardData_sync;

    public DM_ElectroblobsWizardry(PlayerDataSQL pPlugin) {
        super(pPlugin);

        this.addModCheckClass("electroblob.wizardry.Wizardry");
        this.addCapabilityP("electroblob.wizardry.data.WizardData$Provider");
    }

    @Override
    public String getModelId() {
        return "ElectroblobsWizardry_v1_12_2";
    }

    @Override
    public String getDesc() {
        return "巫术学";
    }

    @Override
    protected boolean initCapability() throws Exception {
        Class<?> tClazz = Class.forName("electroblob.wizardry.data.WizardData");

        method_WizardData_get = MethodUtil.getDeclaredMethod(tClazz, "get", NMSUtil.clazz_EntityPlayer);
        method_WizardData_sync = MethodUtil.getDeclaredMethod(tClazz, "sync");

        return super.initCapability();
    }

    @Override
    public void updateAround(CPlayer pPlayer, Class<?> pProvider) {
        MethodUtil.invokeMethod(method_WizardData_sync,
                MethodUtil.invokeStaticMethod(method_WizardData_get, pPlayer.getNMSPlayer()));
    }

}
