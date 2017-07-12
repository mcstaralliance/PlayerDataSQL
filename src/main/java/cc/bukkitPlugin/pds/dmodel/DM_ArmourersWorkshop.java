package cc.bukkitPlugin.pds.dmodel;

import java.lang.reflect.Method;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.commons.util.reflect.MethodUtil;

public class DM_ArmourersWorkshop extends DataInValidModel{

    public static final String EXT_PROP_NAME="playerCustomEquipmentData";

    private Method method_ExPropsPlayerEquipmentData_get;
    private Method method_ExPropsPlayerEquipmentData_register;

    private Method method_ExPropsPlayerEquipmentData_updateEquipmentDataToPlayersAround;

    public DM_ArmourersWorkshop(PlayerDataSQL pPlugin){
        super(pPlugin,"riskyken.armourersWorkshop.common.skin.ExPropsPlayerEquipmentData",EXT_PROP_NAME);
    }

    @Override
    public String getModelId(){
        return "ArmourersWorksho";
    }

    @Override
    public String getDesc(){
        return "时装工坊";
    }

    @Override
    public boolean initOnce(){
        if(this.mInit!=null)
            return this.mInit.booleanValue();

        try{
            
            this.initExProp();

            Class<?> tClazz=Class.forName(this.mExPropClass);
            this.method_ExPropsPlayerEquipmentData_get=MethodUtil.getMethod(tClazz,"get",NMSUtil.clazz_EntityPlayer,true);
            this.method_ExPropsPlayerEquipmentData_register=MethodUtil.getMethod(tClazz,"register",NMSUtil.clazz_EntityPlayer,true);

            this.method_ExPropsPlayerEquipmentData_updateEquipmentDataToPlayersAround=MethodUtil.getMethod(tClazz,"updateEquipmentDataToPlayersAround",true);

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
    protected Object getExProp(Object pNMSPlayer){
        return MethodUtil.invokeStaticMethod(method_ExPropsPlayerEquipmentData_get,pNMSPlayer);
    }

    @Override
    protected void registerExProp(Object pNMSPlayer){
        MethodUtil.invokeStaticMethod(method_ExPropsPlayerEquipmentData_register,pNMSPlayer);
    }

    @Override
    protected void updateToAround(Object pNMSPlayer,Object pExProp){
        MethodUtil.invokeMethod(method_ExPropsPlayerEquipmentData_updateEquipmentDataToPlayersAround,pExProp);
    }

}
