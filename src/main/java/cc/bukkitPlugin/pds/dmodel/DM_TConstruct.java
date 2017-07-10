package cc.bukkitPlugin.pds.dmodel;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.PDSNBTUtil;
import cc.commons.util.reflect.ClassUtil;
import cc.commons.util.reflect.MethodUtil;

public class DM_TConstruct extends DM_MCStats{

    public static final String TAG_MAIN="TConstruct";

    private Method method_TConstructAPI_getInventoryWrapper;
    private Method method_IPlayerExtendedInventoryWrapper_getKnapsackInventory;
    private Method method_IPlayerExtendedInventoryWrapper_getAccessoryInventory;
    private Method method_TPlayerStats_loadNBTData;
    private Method method_TPlayerStats_saveNBTData;

    private Boolean mInit=null;

    public DM_TConstruct(PlayerDataSQL pPlugin){
        super(pPlugin);
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

        Class<?> tClazz=null;
        try{
            Class.forName("tconstruct.TConstruct");
            tClazz=Class.forName("tconstruct.api.TConstructAPI");
            method_TConstructAPI_getInventoryWrapper=tClazz.getMethod("getInventoryWrapper",NMSUtil.clazz_EntityPlayer);

            tClazz=method_TConstructAPI_getInventoryWrapper.getReturnType();
            method_IPlayerExtendedInventoryWrapper_getKnapsackInventory=tClazz.getMethod("getKnapsackInventory",NMSUtil.clazz_EntityPlayer);
            method_IPlayerExtendedInventoryWrapper_getAccessoryInventory=tClazz.getMethod("getAccessoryInventory",NMSUtil.clazz_EntityPlayer);

            tClazz=Class.forName("tconstruct.armor.player.TPlayerStats");
            this.method_TPlayerStats_saveNBTData=MethodUtil.getMethod(tClazz,"saveNBTData",NBTUtil.clazz_NBTTagCompound,true);
            this.method_TPlayerStats_loadNBTData=MethodUtil.getMethod(tClazz,"loadNBTData",NBTUtil.clazz_NBTTagCompound,true);
        }catch(Exception exp){
            if(!(exp instanceof ClassNotFoundException))
                Log.severe("模块 "+this.getDesc()+" 初始化时发生了错误",exp);
            return (this.mInit=false);
        }
        return (this.mInit=true);
    }

    @Override
    public byte[] getData(Player pPlayer,Map<String,byte[]> pLoadedData) throws Exception{
        Object tNBT=NBTUtil.newNBTTagCompound();
        MethodUtil.invokeMethod(this.method_TPlayerStats_saveNBTData,this.getPlayerData(pPlayer),tNBT);
        return PDSNBTUtil.compressNBT(tNBT);
    }

    @Override
    public void restore(Player pPlayer,byte[] pData) throws Exception{
        this.reset(pPlayer);
        MethodUtil.invokeMethod(this.method_TPlayerStats_loadNBTData,this.getPlayerData(pPlayer),fixNBT(PDSNBTUtil.decompressNBT(pData)));
    }

    @Override
    public byte[] loadFileData(OfflinePlayer pPlayer,Map<String,byte[]> pLoadedData) throws IOException{
        byte[] tData=pLoadedData.get(DM_Minecraft.ID.toLowerCase());
        if(tData==null) tData=super.loadFileData(pPlayer,pLoadedData);

        Object tNBT=PDSNBTUtil.decompressNBT(tData);
        Map<String,Object> tNBTValue=NBTUtil.getNBTTagCompoundValue(tNBT);
        Object tTCNBT=tNBTValue.remove(TAG_MAIN);
        tNBTValue.clear();
        tNBTValue.put(TAG_MAIN,tTCNBT);
        return PDSNBTUtil.compressNBT(tNBT);
    }

    private Object getPlayerData(Player pPlayer){
        return MethodUtil.invokeStaticMethod(this.method_TConstructAPI_getInventoryWrapper,NMSUtil.getNMSPlayer(pPlayer));
    }

    public boolean reset(Player pTargetPlayer){
        Object tNMSPlayer=NMSUtil.getNMSPlayer(pTargetPlayer);
        Object tPlayerData=MethodUtil.invokeMethod(this.method_TConstructAPI_getInventoryWrapper,null,tNMSPlayer);

        Object tNBTTag=fixNBT(NBTUtil.newNBTTagCompound());
        MethodUtil.invokeMethod(this.method_TPlayerStats_loadNBTData,tPlayerData,tNBTTag);
        HashSet<Object> NMSInvs=new HashSet<>();
        NMSInvs.add(MethodUtil.invokeMethod(method_IPlayerExtendedInventoryWrapper_getKnapsackInventory,tPlayerData,tNMSPlayer));
        NMSInvs.add(MethodUtil.invokeMethod(method_IPlayerExtendedInventoryWrapper_getAccessoryInventory,tPlayerData,tNMSPlayer));
        for(Object sNMSInv : NMSInvs){
            if(sNMSInv==null)
                continue;
            Inventory tInv=(Inventory)ClassUtil.newInstance(NMSUtil.clazz_CraftInventory,NMSUtil.clazz_IInventory,sNMSInv);
            for(int i=0;i<tInv.getSize();i++){
                tInv.setItem(i,null);
            }
        }
        return true;
    }

    public static Object fixNBT(Object pNBTTag){
        Map<String,Object> tMapValue=NBTUtil.getNBTTagCompoundValue(pNBTTag);
        if(!tMapValue.containsKey(TAG_MAIN)){
            tMapValue.put(TAG_MAIN,NBTUtil.newNBTTagCompound());
        }
        return pNBTTag;
    }

}
