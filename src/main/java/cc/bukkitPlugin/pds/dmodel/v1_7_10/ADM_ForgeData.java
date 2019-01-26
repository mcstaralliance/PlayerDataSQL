package cc.bukkitPlugin.pds.dmodel;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.bukkitPlugin.pds.util.PDSNBTUtil;
import cc.commons.util.reflect.MethodUtil;

public abstract class ADM_ForgeData extends ADataModel{

    protected final HashSet<String> mModelTags=new HashSet<>();

    private static Method method_Entity_getEntityData;

    static{
        try{
            method_Entity_getEntityData=MethodUtil.getMethodIgnoreParam(NMSUtil.clazz_NMSEntity,"getEntityData",true).get(0);
        }catch(IllegalStateException ignore){
        }
    }

    public ADM_ForgeData(PlayerDataSQL pPlugin){
        super(pPlugin);
    }

    public Object getEntityData(CPlayer pPlayer){
        return MethodUtil.invokeMethod(method_Entity_getEntityData,pPlayer.getNMSPlayer());
    }

    @Override
    public byte[] getData(CPlayer pPlayer,Map<String,byte[]> pLoadedData) throws Exception{
        Object tNBT=this.getEntityData(pPlayer);
        Map<String,Object> tNBTValue=NBTUtil.getNBTTagCompoundValue(tNBT);
        Map<String,Object> tRemoved=new HashMap<>();
        for(String sKey : this.mModelTags){
            Object tValue=tNBTValue.remove(sKey);
            if(tValue!=null) tRemoved.put(sKey,tValue);
        }

        tNBTValue.clear();
        tNBTValue.putAll(tRemoved);
        return PDSNBTUtil.compressNBT(tNBT);
    }

    @Override
    public void restore(CPlayer pPlayer,byte[] pData) throws Exception{
        this.cleanData(pPlayer);
        if(pData.length==0) return;

        Object tNBT=this.getEntityData(pPlayer);
        Map<String,Object> tNBTValue=NBTUtil.getNBTTagCompoundValue(tNBT);
        Map<String,Object> tRestoreNBTValue=NBTUtil.getNBTTagCompoundValue(this.correctNBTData(PDSNBTUtil.decompressNBT(pData)));

        tNBTValue.putAll(tRestoreNBTValue);
        this.updateToAround(pPlayer);
    }

    @Override
    public byte[] loadFileData(CPlayer pPlayer,Map<String,byte[]> pLoadedData) throws IOException{
        throw new UnsupportedOperationException();
    }

    @Override
    public void cleanData(CPlayer pPlayer){
        Object tNBT=this.getEntityData(pPlayer);
        Map<String,Object> tNBTValue=NBTUtil.getNBTTagCompoundValue(tNBT);
        for(String sKey : this.mModelTags){
            tNBTValue.remove(sKey);
        }
        
        this.correctNBTData(tNBT);
    }

    /**
     * 修正mod数据的NBT值,防止还原数据的时候报错
     * 
     * @param pNBTTag
     *            要修正的NBT数据
     * @return 修正后的数据
     */
    public Object correctNBTData(Object pNBTTag){
        return pNBTTag;
    }

    protected abstract void updateToAround(CPlayer pPlayer);

}
