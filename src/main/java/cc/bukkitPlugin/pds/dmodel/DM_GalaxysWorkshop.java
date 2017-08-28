package cc.bukkitPlugin.pds.dmodel;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.commons.util.reflect.MethodUtil;

import java.lang.reflect.Method;

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
    public boolean initOnce(){
        if(this.mInit!=null)
            return this.mInit.booleanValue();

        try{

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
        }catch(Exception exp){
            if(!(exp instanceof ClassNotFoundException))
                Log.severe("模块 "+this.getDesc()+" 初始化时发生了错误",exp);
            return (this.mInit=false);
        }
        return (this.mInit=true);
    }

    @Override
    protected void updateToAround(Object pNMSPlayer,Object pExProp){
        MethodUtil.invokeMethod(method_ExPropsPlayerEquipmentData_updateEquipmentDataToPlayersAround,pExProp);
        MethodUtil.invokeMethod(method_ExPropsPlayerEquipmentData_sendSkinData,pExProp);
    }

}
