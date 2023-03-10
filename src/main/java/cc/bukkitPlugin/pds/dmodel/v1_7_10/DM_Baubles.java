package cc.bukkitPlugin.pds.dmodel.v1_7_10;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import org.bukkit.inventory.Inventory;

import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.dmodel.ADataModel;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.bukkitPlugin.pds.util.PDSNBTUtil;
import cc.commons.util.FileUtil;
import cc.commons.util.reflect.ClassUtil;
import cc.commons.util.reflect.MethodUtil;

public class DM_Baubles extends ADataModel {

    private Method method_BaublesApi_getBaubles = null;
    private Method method_InventoryBaubles_readNBT = null;
    private Method method_InventoryBaubles_saveNBT = null;
    private Method method_InventoryBaubles_syncSlotToClients = null;

    private Boolean mInit = null;

    public DM_Baubles(PlayerDataSQL pPlugin) {
        super(pPlugin);
    }

    @Override
    public String getModelId() {
        return "Baubles";
    }

    @Override
    public String getDesc() {
        return "饰品背包";
    }

    @Override
    public boolean initOnce() throws Exception {
        Class<?> tClazz = null;
        Class.forName("baubles.common.Baubles");
        tClazz = Class.forName("baubles.api.BaublesApi");
        this.method_BaublesApi_getBaubles = tClazz.getMethod("getBaubles", NMSUtil.clazz_EntityPlayer);
        tClazz = Class.forName("baubles.common.container.InventoryBaubles");
        this.method_InventoryBaubles_readNBT = MethodUtil.getMethod(tClazz, "readNBT", NBTUtil.clazz_NBTTagCompound, true);
        this.method_InventoryBaubles_saveNBT = MethodUtil.getMethod(tClazz, "saveNBT", NBTUtil.clazz_NBTTagCompound, true);
        this.method_InventoryBaubles_syncSlotToClients = MethodUtil.getMethod(tClazz, "syncSlotToClients", int.class, true);
        return true;
    }

    @Override
    public byte[] getData(CPlayer pPlayer, Map<String, byte[]> pLoadedData) throws Exception {
        Object tNMSInv = this.getBaublesNMSInv(pPlayer);
        Object tNBTTag = NBTUtil.newNBTTagCompound();
        MethodUtil.invokeMethod(this.method_InventoryBaubles_saveNBT, tNMSInv, tNBTTag);
        return PDSNBTUtil.compressNBT(tNBTTag);
    }

    @Override
    public void restore(CPlayer pPlayer, byte[] pData) throws Exception {
        this.cleanData(pPlayer);
        Object tNMSInv = this.getBaublesNMSInv(pPlayer);
        MethodUtil.invokeMethod(this.method_InventoryBaubles_readNBT, tNMSInv, PDSNBTUtil.decompressNBT(pData));
        this.updateToAround(pPlayer.getNMSPlayer(), tNMSInv);
    }

    @Override
    public byte[] loadFileData(CPlayer pPlayer, Map<String, byte[]> pLoadedData) throws IOException {
        File tDataFile = this.getUUIDOrNameFile(pPlayer, this.mPlayerDataDir, "%name%.baub");
        if (!tDataFile.isFile()) return new byte[0];

        return FileUtil.readData(tDataFile);
    }

    @Override
    public void cleanData(CPlayer pPlayer) {
        Object tNMSInv = this.getBaublesNMSInv(pPlayer);
        Inventory tInv = (Inventory)ClassUtil.newInstance(NMSUtil.clazz_CraftInventory, NMSUtil.clazz_IInventory, tNMSInv);
        for (int i = 0; i < tInv.getSize(); i++) {
            tInv.setItem(i, null);
        }
    }

    protected Object getBaublesNMSInv(CPlayer pPlayer) {
        return MethodUtil.invokeStaticMethod(this.method_BaublesApi_getBaubles, pPlayer.getNMSPlayer());
    }

    protected void updateToAround(Object pNMSPlayer, Object pBaublesInv) {
        for (int i = 0; i < 4; i++) {
            MethodUtil.invokeMethod(method_InventoryBaubles_syncSlotToClients, pBaublesInv, i);
        }
    }

}
