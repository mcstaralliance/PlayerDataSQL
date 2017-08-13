package cc.bukkitPlugin.pds.dmodel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.PDSNBTUtil;
import cc.commons.util.FileUtil;
import cc.commons.util.reflect.ClassUtil;
import cc.commons.util.reflect.MethodUtil;

public class DM_Baubles extends ADataModel{

    private Method method_BaublesApi_getBaubles=null;
    private Method method_InventoryBaubles_readNBT=null;
    private Method method_InventoryBaubles_saveNBT=null;
    private Method method_InventoryBaubles_syncSlotToClients=null;

    private Boolean mInit=null;

    public DM_Baubles(PlayerDataSQL pPlugin){
        super(pPlugin);
    }

    @Override
    public String getModelId(){
        return "Baubles";
    }

    @Override
    public String getDesc(){
        return "饰品背包";
    }

    @Override
    public boolean initOnce(){
        if(this.mInit!=null)
            return this.mInit.booleanValue();

        try{
            Class<?> tClazz=null;
            Class.forName("baubles.common.Baubles");
            tClazz=Class.forName("baubles.api.BaublesApi");
            this.method_BaublesApi_getBaubles=tClazz.getMethod("getBaubles",NMSUtil.clazz_EntityPlayer);
            tClazz=Class.forName("baubles.common.container.InventoryBaubles");
            this.method_InventoryBaubles_readNBT=MethodUtil.getMethod(tClazz,"readNBT",NBTUtil.clazz_NBTTagCompound,true);
            this.method_InventoryBaubles_saveNBT=MethodUtil.getMethod(tClazz,"saveNBT",NBTUtil.clazz_NBTTagCompound,true);
            this.method_InventoryBaubles_syncSlotToClients=MethodUtil.getMethod(tClazz,"syncSlotToClients",int.class,true);
        }catch(Exception exp){
            if(!(exp instanceof ClassNotFoundException))
                Log.severe("模块 "+this.getDesc()+" 初始化时发生了错误",exp);
            return (this.mInit=false);
        }
        return (this.mInit=true);
    }

    @Override
    public byte[] getData(Player pPlayer,Map<String,byte[]> pLoadedData) throws Exception{
        Object tNMSInv=this.getBaublesNMSInv(pPlayer);
        Object tNBTTag=NBTUtil.newNBTTagCompound();
        MethodUtil.invokeMethod(this.method_InventoryBaubles_saveNBT,tNMSInv,tNBTTag);
        return PDSNBTUtil.compressNBT(tNBTTag);
    }

    @Override
    public void restore(Player pPlayer,byte[] pData) throws Exception{
        this.cleanData(pPlayer);
        Object tNMSInv=this.getBaublesNMSInv(pPlayer);
        MethodUtil.invokeMethod(this.method_InventoryBaubles_readNBT,tNMSInv,PDSNBTUtil.decompressNBT(pData));
        this.updateToAround(NMSUtil.getNMSPlayer(pPlayer),tNMSInv);
    }

    @Override
    public byte[] loadFileData(OfflinePlayer pPlayer,Map<String,byte[]> pLoadedData) throws IOException{
        File tDataFile=this.getUUIDOrNameFile(pPlayer,this.mPlayerDataDir,"%name%.baub");
        if(!tDataFile.isFile()) return new byte[0];

        return FileUtil.readData(tDataFile);
    }

    @Override
    public void cleanData(Player pPlayer){
        Object tNMSInv=this.getBaublesNMSInv(pPlayer);
        Inventory tInv=(Inventory)ClassUtil.newInstance(NMSUtil.clazz_CraftInventory,NMSUtil.clazz_IInventory,tNMSInv);
        for(int i=0;i<tInv.getSize();i++){
            tInv.setItem(i,null);
        }
    }

    protected Object getBaublesNMSInv(Player pPlayer){
        Object tNMSPlayer=NMSUtil.getNMSPlayer(pPlayer);
        return MethodUtil.invokeStaticMethod(this.method_BaublesApi_getBaubles,tNMSPlayer);
    }

    protected void updateToAround(Object pNMSPlayer,Object pBaublesInv){
        for(int i=0;i<4;i++){
            MethodUtil.invokeMethod(method_InventoryBaubles_syncSlotToClients,pBaublesInv,i);
        }
    }

}
