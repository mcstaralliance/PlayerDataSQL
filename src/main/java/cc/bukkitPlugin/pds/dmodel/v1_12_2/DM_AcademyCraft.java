package cc.bukkitPlugin.pds.dmodel.v1_12_2;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.bukkitPlugin.pds.util.CapabilityHelper;
import cc.commons.util.reflect.FieldUtil;
import cc.commons.util.reflect.MethodUtil;

public class DM_AcademyCraft extends ADM_CapabilityProvider {

    /** private final ImmutableMap<Class, DataPart> constructed; */
    private Field field_EntityData_constructed;
    /** private void onQuerySync(EntityPlayerMP) */
    private Method method_DataPart_onQuerySync;
    private Object DATA_PART_CAPABILITY;

    public DM_AcademyCraft(PlayerDataSQL pPlugin) {
        super(pPlugin);

        this.addModCheckClass("cn.academy.AcademyCraft");
        this.addInnerCapabilityP("cn.lambdalib2.datapart.CapDataPartHandler");
    }

    @Override
    public String getModelId() {
        return "AcademyCraft_v1_12_2";
    }

    @Override
    public String getDesc() {
        return "学园都市超能力";
    }

    @Override
    protected boolean initCapability() throws Exception {
        Class<?> tClazz = Class.forName("cn.lambdalib2.datapart.EntityData");
        this.field_EntityData_constructed = FieldUtil.getDeclaredField(tClazz, "constructed");

        tClazz = Class.forName("cn.lambdalib2.datapart.DataPart");
        this.method_DataPart_onQuerySync = MethodUtil.getDeclaredMethod(tClazz, "onQuerySync", NMSUtil.clazz_EntityPlayerMP);

        tClazz = Class.forName("cn.lambdalib2.datapart.CapDataPartHandler");
        DATA_PART_CAPABILITY = FieldUtil.getStaticDeclaredFieldValue(tClazz, "DATA_PART_CAPABILITY");
        return true;
    }

    @Override
    public void updateAround(CPlayer pPlayer, Class<?> pProvider) {
        Object tNMSPlayer = pPlayer.getNMSPlayer();
        Object tEntityData = CapabilityHelper.getCapability(tNMSPlayer, DATA_PART_CAPABILITY, null);
        if (tEntityData == null) return;
        for (Object sObj : ((Map)FieldUtil.getFieldValue(field_EntityData_constructed, tEntityData)).values()) {
            MethodUtil.invokeMethod(method_DataPart_onQuerySync, sObj, tNMSPlayer);
        }
    }

}
