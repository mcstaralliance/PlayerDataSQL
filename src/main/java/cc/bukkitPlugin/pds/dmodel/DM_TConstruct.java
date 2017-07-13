package cc.bukkitPlugin.pds.dmodel;

import java.util.Map;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;

public class DM_TConstruct extends ADM_InVanilla{

    public static final String EXT_PROP_NAME="TConstruct";
    public static final String TAG_MAIN="TConstruct";

    public DM_TConstruct(PlayerDataSQL pPlugin){
        super(pPlugin,"tconstruct.armor.player.TPlayerStats",EXT_PROP_NAME);
    }

    @Override
    public String getModelId(){
        return "TConstruct";
    }

    @Override
    public String getDesc(){
        return "将魂背包";
    }

    @Override
    public boolean initOnce(){
        if(this.mInit!=null)
            return this.mInit.booleanValue();

        try{
            this.initExProp();

            // 将魂TAG
            this.mModelTags.add(TAG_MAIN);
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
        if(!tMapValue.containsKey(TAG_MAIN)){
            tMapValue.put(TAG_MAIN,NBTUtil.newNBTTagCompound());
        }
        return pNBTTag;
    }

    @Override
    protected void updateToAround(Object pNMSPlayer,Object pExProp){}

}
