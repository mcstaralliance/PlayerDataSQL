package cc.bukkitPlugin.pds.dmodel;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.bukkitPlugin.pds.util.PDSNBTUtil;
import cc.commons.util.reflect.ClassUtil;
import cc.commons.util.reflect.FieldUtil;
import cc.commons.util.reflect.MethodUtil;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import travellersgear.api.TGSaveData;
import travellersgear.api.TravellersGearAPI;

public class DM_TravellersGear extends ADataModel{

    /** static NBTTagCompound getPlayerData(EntityPlayer) */
    private Method method_TravellersGearAPI_getTravellersNBTData;
    /** static void setPlayerData(EntityPlayer;NBTTagCompound) */
    private Method method_TGSaveData_setPlayerData;

    private TGSaveData mTGSDInstance;
    private HashMap<UUID,Object> mTGSDMap=new HashMap<>();

    private Boolean mInit=null;
    /** 用于发送同步包 */
    private SimpleNetworkWrapper mPacketHandler;
    /** void sendTo(IMessage;EntityPlayerMP) */
    private Method method_SimpleNetworkWrapper_sendTo;
    private Class<?> class_MessageNBTSync;

    public DM_TravellersGear(PlayerDataSQL pPlugin){
        super(pPlugin);
    }

    @Override
    public String getModelId(){
        return "TravellersGear";
    }

    @Override
    public String getDesc(){
        return "旅行者背包";
    }

    @Override
    protected boolean initOnce() throws Exception{
        Class tClazz=Class.forName("travellersgear.TravellersGear");
        this.mPacketHandler=(SimpleNetworkWrapper)FieldUtil.getFieldValue(tClazz,"packetHandler",true,null);
        this.method_SimpleNetworkWrapper_sendTo=MethodUtil.getMethodIgnoreParam(SimpleNetworkWrapper.class,"sendTo",true).get(0);

        this.class_MessageNBTSync=Class.forName("travellersgear.common.network.MessageNBTSync");

        Class.forName("travellersgear.api.TravellersGearAPI");
        this.method_TravellersGearAPI_getTravellersNBTData=MethodUtil.getMethod(TravellersGearAPI.class,
                "getTravellersNBTData",
                NMSUtil.clazz_EntityPlayer,
                true);

        Class.forName("travellersgear.api.TGSaveData");
        this.method_TGSaveData_setPlayerData=MethodUtil.getMethod(TGSaveData.class,
                "setPlayerData",
                new Class<?>[]{NMSUtil.clazz_EntityPlayer,NBTUtil.clazz_NBTTagCompound},
                true);

        this.mTGSDInstance=(TGSaveData)FieldUtil.getStaticFieldValue(FieldUtil.getField(TGSaveData.class,TGSaveData.class,-1,true).get(0));
        this.mTGSDMap=(HashMap<UUID,Object>)FieldUtil.getFieldValue(TGSaveData.class,"playerData",true,this.mTGSDInstance);

        return true;
    }

    @Override
    public byte[] getData(CPlayer pPlayer,Map<String,byte[]> pLoadedData) throws Exception{
        return this.getData0(pPlayer);
    }

    @Override
    public void restore(CPlayer pPlayer,byte[] pData) throws Exception{
        Object tNBT=PDSNBTUtil.decompressNBT(pData);
        this.mTGSDMap.put(pPlayer.getUniqueId(),tNBT);
        TGSaveData.setDirty();
        this.syncToClient(pPlayer);
    }

    @Override
    public byte[] loadFileData(CPlayer pPlayer,Map<String,byte[]> pLoadedData) throws IOException{
        return this.getData0(pPlayer);
    }

    private byte[] getData0(CPlayer pPlayer){
        Object tNBT=this.mTGSDMap.get(pPlayer.getUniqueId());
        if(tNBT==null) tNBT=NBTUtil.newNBTTagCompound();
        return PDSNBTUtil.compressNBT(tNBT);
    }

    @Override
    public void cleanData(CPlayer pPlayer){
        Object tRemoved=this.mTGSDMap.remove(pPlayer.getUniqueId());
        if(tRemoved!=null) TGSaveData.setDirty();
    }

    public void syncToClient(CPlayer pPlayer){
        Object tNMSPlayer=pPlayer.getNMSPlayer();
        if(tNMSPlayer!=null){
            Object tNBTSyncMsg=ClassUtil.newInstance(this.class_MessageNBTSync,NMSUtil.clazz_EntityPlayer,tNMSPlayer);
            MethodUtil.invokeMethod(method_SimpleNetworkWrapper_sendTo,this.mPacketHandler,tNBTSyncMsg,tNMSPlayer);
        }
    }

}
