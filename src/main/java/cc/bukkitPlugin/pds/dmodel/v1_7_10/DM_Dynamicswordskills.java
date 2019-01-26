package cc.bukkitPlugin.pds.dmodel.v1_7_10;

import java.lang.reflect.Method;
import java.util.Map;

import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.CPlayer;
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
    protected boolean initOnce() throws Exception{
        this.initExProp();

        this.method_DSSPlayerInfo_onJoinWorld=MethodUtil.getMethod(this.mExPropClazz,"onJoinWorld",true);

        // 动态剑技的TAG
        this.mModelTags.add("DynamicSwordSkills");
        this.mModelTags.add("receivedGear");

        return true;
    }

    @Override
    public Object correctNBTData(Object pNBTTag){
        Map<String,Object> tMapValue=NBTUtil.getNBTTagCompoundValue(pNBTTag);
        tMapValue.put("DynamicSwordSkills",NBTUtil.newNBTTagList());
        return tMapValue;
    }

    @Override
    protected void updateToAround(CPlayer pPlayer,Object pExProp){
        MethodUtil.invokeMethod(this.method_DSSPlayerInfo_onJoinWorld,pExProp);
    }

}
