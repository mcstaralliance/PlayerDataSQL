package cc.bukkitPlugin.pds.dmodel.v1_7_10;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.commons.util.reflect.ClassUtil;
import cc.commons.util.reflect.MethodUtil;

public class DM_ArmourersWorkshop extends ADM_InVanilla {

    public static final String EXT_PROP_NAME = "playerCustomEquipmentData";

    protected Method method_ExPropsPlayerEquipmentData_updateEquipmentDataToPlayersAround;
    protected Method method_ExPropsPlayerEquipmentData_sendSkinData;

    private static String getExpClass() {
        List<String> tExpClazz = Arrays.asList("riskyken.armourersWorkshop.common.skin.ExPropsPlayerEquipmentData", "riskyken.armourersWorkshop.common.skin.ExPropsPlayerSkinData", "riskyken.armourersWorkshop.common.wardrobe.ExPropsPlayerSkinData");

        for (String sStr : tExpClazz) {
            if (ClassUtil.isClassLoaded(sStr)) return sStr;
        }

        return tExpClazz.iterator().next();
    }

    public DM_ArmourersWorkshop(PlayerDataSQL pPlugin) {
        super(pPlugin, getExpClass(), EXT_PROP_NAME);
    }

    public DM_ArmourersWorkshop(PlayerDataSQL pPlugin, String pExPropClass, String pExPropName) {
        super(pPlugin, pExPropClass, pExPropName);
    }

    @Override
    public String getModelId() {
        return "ArmourersWorkshoh";
    }

    @Override
    public String getDesc() {
        return "时装工坊";
    }

    @Override
    protected boolean initOnce() throws Exception {
        this.initExProp();

        this.method_ExPropsPlayerEquipmentData_updateEquipmentDataToPlayersAround = MethodUtil.getMethod(this.mExPropClazz, "updateEquipmentDataToPlayersAround", true);
        this.method_ExPropsPlayerEquipmentData_sendSkinData = MethodUtil.getMethod(this.mExPropClazz, "sendSkinData", true);

        // 时装的TAG
        this.mModelTags.add("wardrobeContainer");
        this.mModelTags.add("items");

        this.mModelTags.add("skinColour");
        this.mModelTags.add("hairColour");
        for (int i = 0; i < 4; i++) {
            this.mModelTags.add("armourOverride" + i);
        }
        this.mModelTags.add("headOverlay");
        this.mModelTags.add("limitLimbs");
        this.mModelTags.add("slotsUnlocked");

        this.mModelTags.add("lastXmasYear");

        return true;
    }

    @Override
    protected void updateToAround(CPlayer pPlayer, Object pExProp) {
        MethodUtil.invokeMethod(method_ExPropsPlayerEquipmentData_updateEquipmentDataToPlayersAround, pExProp);
        MethodUtil.invokeMethod(method_ExPropsPlayerEquipmentData_sendSkinData, pExProp);
    }

}
