package cc.bukkitPlugin.pds.dmodel;

import java.lang.reflect.Method;
import java.util.Map;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.commons.util.reflect.MethodUtil;

public class DM_Dynamicswordskills extends ADM_InVanilla{

    public static final String EXT_PROP_NAME="DSSPlayerInfo";

    private Method method_DSSPlayerInfo_onJoinWorld;

    public DM_Dynamicswordskills(PlayerDataSQL pPlugin){
        super(pPlugin,"dynamicswordskills.entity.DSSPlayerInfo",EXT_PROP_NAME);
    }

    @Override
    public String getModelId(){
        return "dynamicswordskills";
    }

    @Override
    public String getDesc(){
        return "动态剑技";
    }

    @Override
    public boolean initOnce(){
        if(this.mInit!=null)
            return this.mInit.booleanValue();

        try{

            this.initExProp();

            this.method_DSSPlayerInfo_onJoinWorld=MethodUtil.getMethod(this.mExPropClazz,"onJoinWorld",true);

            // 动态剑技的TAG
            this.mModelTags.add("DynamicSwordSkills");
            this.mModelTags.add("receivedGear");
        }catch(Exception exp){
            if(!(exp instanceof ClassNotFoundException))
                Log.severe("模块 "+this.getDesc()+" 初始化时发生了错误",exp);
            return (this.mInit=false);
        }
        return (this.mInit=true);
    }

    @Override
    public Object correctNBTData(Object pNBTTag){
        Map<String,Object> tMapValue=NBTUtil.getNBTTagCompoundValue(pNBTTag);
        tMapValue.put("DynamicSwordSkills",NBTUtil.newNBTTagList());
        return tMapValue;
    }

    @Override
    protected void updateToAround(Object pNMSPlayer,Object pExProp){
        MethodUtil.invokeMethod(this.method_DSSPlayerInfo_onJoinWorld,pExProp);
    }

}
