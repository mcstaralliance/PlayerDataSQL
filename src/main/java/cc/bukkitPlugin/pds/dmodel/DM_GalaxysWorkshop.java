package cc.bukkitPlugin.pds.dmodel;

import java.lang.reflect.Method;

import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.commons.util.reflect.MethodUtil;

public class DM_GalaxysWorkshop extends ADM_InVanilla{

    public static final String EXT_PROP_NAME="playerCustomEquipmentData";

    private Method method_ExPropsPlayerEquipmentData_updateEquipmentDataToPlayersAround;
    private Method method_ExPropsPlayerEquipmentData_sendSkinData;

    public DM_GalaxysWorkshop(PlayerDataSQL pPlugin){
        super(pPlugin,"wang.magick.galaxyworkshop.skin.ExPropsPlayerEquipmentData",EXT_PROP_NAME);
    }

    @Override
    public String getModelId(){
        return "GalaxysWorkshop";
    }

    @Override
    public String getDesc(){
        return "银河时装工坊";
    }

    @Override
    protected boolean initOnce() throws Exception{
        this.initExProp();
        this.method_ExPropsPlayerEquipmentData_updateEquipmentDataToPlayersAround=MethodUtil.getMethod(this.mExPropClazz,"updateEquipmentDataToPlayersAround",true);
        this.method_ExPropsPlayerEquipmentData_sendSkinData=MethodUtil.getMethod(this.mExPropClazz,"sendSkinData",true);

        // 时装的TAG
        this.mModelTags.add("wardrobeContainer");
        this.mModelTags.add("items");

        this.mModelTags.add("skinColour");
        this.mModelTags.add("hairColour");
        for(int i=0;i<4;i++){
            this.mModelTags.add("armourOverride"+i);
        }
        this.mModelTags.add("headOverlay");
        this.mModelTags.add("limitLimbs");
        this.mModelTags.add("slotsUnlocked");

        this.mModelTags.add("lastXmasYear");

        return true;
    }

    @Override
    protected void updateToAround(Object pNMSPlayer,Object pExProp){
        MethodUtil.invokeMethod(method_ExPropsPlayerEquipmentData_updateEquipmentDataToPlayersAround,pExProp);
        MethodUtil.invokeMethod(method_ExPropsPlayerEquipmentData_sendSkinData,pExProp);
    }

}
