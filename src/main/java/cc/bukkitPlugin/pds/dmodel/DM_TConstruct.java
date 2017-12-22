package cc.bukkitPlugin.pds.dmodel;

import java.util.Map;

import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.CPlayer;

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
    protected boolean initOnce() throws Exception{
        this.initExProp();

        // 将魂TAG
        this.mModelTags.add(TAG_MAIN);

        return true;
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
    protected void updateToAround(CPlayer pPlayer,Object pExProp){}

}
