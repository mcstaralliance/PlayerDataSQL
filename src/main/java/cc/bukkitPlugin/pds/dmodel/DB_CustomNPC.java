package cc.bukkitPlugin.pds.dmodel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.bukkitPlugin.pds.util.PDSNBTUtil;
import cc.commons.util.reflect.ClassUtil;
import cc.commons.util.reflect.FieldUtil;
import cc.commons.util.reflect.MethodUtil;

public class DB_CustomNPC extends ADM_InVanilla{

    private Object value_PlayerDataController_instance;
    private Method method_PlayerDataController_getPlayerData;
    /** public NBTTagCompound getNBT() */
    private Method method_PlayerData_getNBT;
    /** public void readNBT(NBTTagCompound) */
    private Method method_PlayerData_readNBT;

    public DB_CustomNPC(PlayerDataSQL pPlugin){
        super(pPlugin,"noppes.npcs.controllers.PlayerData","CustomNpcsData");
    }

    @Override
    public String getModelId(){
        return "CustomNPC";
    }

    @Override
    public String getDesc(){
        return "自定义NPC";
    }

    @Override
    protected boolean initOnce() throws Exception{
        this.initExProp();

        this.mModelTags.add("CustomNpcsId");
        this.mModelTags.add("CustomNpcsData");

        Class<?> tClazz=Class.forName("noppes.npcs.controllers.PlayerDataController");
        this.method_PlayerDataController_getPlayerData=MethodUtil.getMethod(tClazz,"getPlayerData",NMSUtil.clazz_EntityPlayer,true);
        Field tField=FieldUtil.getField(tClazz,tClazz,-1,true).get(0);
        this.value_PlayerDataController_instance=FieldUtil.getStaticFieldValue(tField);
        if(this.value_PlayerDataController_instance==null){
            this.value_PlayerDataController_instance=ClassUtil.newInstance(tClazz);
            tField.setAccessible(true);
            tField.set(null,this.value_PlayerDataController_instance);
        }

        this.method_PlayerData_getNBT=MethodUtil.getMethod(this.mExPropClazz,"getNBT",true);

        if(MethodUtil.isMethodExist(this.mExPropClazz,"readNBT",NBTUtil.clazz_NBTTagCompound,true)){
            this.method_PlayerData_readNBT=MethodUtil.getMethod(this.mExPropClazz,"readNBT",NBTUtil.clazz_NBTTagCompound,true);
        }else{
            this.method_PlayerData_readNBT=MethodUtil.getMethod(this.mExPropClazz,"setNBT",NBTUtil.clazz_NBTTagCompound,true);
        }

        return true;
    }

    @Override
    public byte[] getData(CPlayer pPlayer,Map<String,byte[]> pLoadedData) throws Exception{
        Object tData=this.getExProp(pPlayer);
        if(tData==null) return new byte[0];

        return PDSNBTUtil.compressNBT(MethodUtil.invokeMethod(this.method_PlayerData_getNBT,tData));
    }

    @Override
    public void restore(CPlayer pPlayer,byte[] pData) throws Exception{
        this.cleanData(pPlayer);
        if(pData.length==0) return;

        Object tExProp=this.getExProp(pPlayer);
        if(tExProp==null) return;

        MethodUtil.invokeMethod(this.method_PlayerData_readNBT,tExProp,PDSNBTUtil.decompressNBT(pData));
        this.updateToAround(pPlayer,tExProp);
    }

    @Override
    protected Object getExProp(CPlayer pPlayer){
        return MethodUtil.invokeMethod(this.method_PlayerDataController_getPlayerData,this.value_PlayerDataController_instance,pPlayer.getNMSPlayer());
    }

    @Override
    protected void registerExProp(CPlayer pPlayer){
        this.getExProp(pPlayer);
    }

    @Override
    protected void updateToAround(CPlayer pPlayer,Object pExProp){}

}
