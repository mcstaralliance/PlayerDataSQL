package cc.bukkitPlugin.pds.dmodel.v1_12_2;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.dmodel.ADataModel;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.bukkitPlugin.pds.util.CapabilityHelper;
import cc.bukkitPlugin.pds.util.PDSNBTUtil;
import cc.commons.util.reflect.FieldUtil;
import cc.commons.util.reflect.MethodUtil;

public class DM_Pixelmon_ZZ extends ADataModel {

    /** Pixelmon.storageManager */
    private Object fieldValue_storageManager;
    private Collection<?> fieldValue_ReforgedStorageManager_playersWithSyncedPCs;

    /** PlayerPartyStorage IStorageManager.getParty(UUID) */
    private Method method_IStorageManager_getParty;
    /** PCStorage IStorageManager.getPCForPlayer(UUID) */
    private Method method_IStorageManager_getPCForPlayer;
    /** void IStorageManager.initializePCForPlayer(EntityPlayer,PCStorage) */
    private Method method_IStorageManager_initializePCForPlayer;

    /** <? extends PokemonStorage> PokemonStorage.readFromNBT(NBTTagCompound) */
    private Method method_PokemonStorage_readFromNBT;
    /** NBTTagCompound PokemonStorage.writeToNBT(NBTTagCompound) */
    private Method method_PokemonStorage_writeToNBT;
    /** int PokemonStorage.countAll() */
    private Method method_PokemonStorage_countAll;
    private Field field_PokemonStorage_hasChanged;
    private Field field_PokemonStorage_shouldSendUpdates;

    /** static void TickHandler.deregisterStarterList(EntityPlayerMP) */
    private Method method_TickHandler_deregisterStarterList;
    /** static void TickHandler.registerStarterList(EntityPlayerMP) */
    private Method method_TickHandler_registerStarterList;

    private Method method_PixelmonPlayerTracker_onPlayerLogin;

    public DM_Pixelmon_ZZ(PlayerDataSQL pPlugin) {
        super(pPlugin);
    }

    @Override
    public String getModelId() {
        return "Pixelmon_ZZ";
    }

    @Override
    public String getDesc() {
        return "宝可梦(重铸)";
    }

    @Override
    public byte[] getData(CPlayer pPlayer, Map<String, byte[]> pLoadedData) throws Exception {
        ArrayList<Object> tStorages = this.getPlayerAllStorage(pPlayer);
        Object tNBT = NBTUtil.newNBTTagCompound();
        Map<String, Object> tValue = NBTUtil.getNBTTagCompoundValue(tNBT);
        for (Object sObj : tStorages) {
            Object tOut = NBTUtil.newNBTTagCompound();
            MethodUtil.invokeMethod(method_PokemonStorage_writeToNBT, sObj, tOut);
            tValue.put(sObj.getClass().getName(), tOut);
        }

        // 防止被宝可梦标记为已同步数据
        this.fieldValue_ReforgedStorageManager_playersWithSyncedPCs.remove(pPlayer.getUniqueId());

        return PDSNBTUtil.compressNBT(tNBT);
    }

    @Override
    public void restore(CPlayer pPlayer, byte[] pData) throws Exception {
        Object tNBTData = PDSNBTUtil.decompressNBT(pData);
        Map<String, Object> tValue = NBTUtil.getNBTTagCompoundValue(tNBTData);

        Object tPPS = this.getPartyStorage(pPlayer);
        boolean tLocalhas = this.getPixelmonCount(tPPS) != 0;
        this.readStorageData(tValue, tPPS);
        // 同步快捷栏宝可梦到客户端
        MethodUtil.invokeStaticMethod(method_PixelmonPlayerTracker_onPlayerLogin, CapabilityHelper.newLoginEvent(pPlayer));

        this.readStorageData(tValue, this.getPCStorage(pPlayer));
        // 同步PC宝可梦到客户端
        MethodUtil.invokeMethod(method_IStorageManager_initializePCForPlayer, fieldValue_storageManager,
                pPlayer.getNMSPlayer(), this.getPCStorage(pPlayer));

        boolean tDBhas = this.getPixelmonCount(tPPS) != 0;
        if (tLocalhas && !tDBhas) {
            // 注册--宝可梦选择界面发送
            MethodUtil.invokeStaticMethod(method_TickHandler_registerStarterList, pPlayer.getNMSPlayer());
        } else if (!tLocalhas && tDBhas) {
            // 注销--宝可梦选择界面发送
            MethodUtil.invokeStaticMethod(method_TickHandler_deregisterStarterList, pPlayer.getNMSPlayer());
            pPlayer.getPlayer().closeInventory();
        }
    }

    protected void readStorageData(Map<String, Object> pDatas, Object pStorage) {
        Object tStorageData = pDatas.get(pStorage.getClass().getName());
        if (tStorageData != null) {
            MethodUtil.invokeMethod(method_PokemonStorage_readFromNBT, pStorage, tStorageData);
        } else this.cleanStorage(pStorage);

        FieldUtil.setFieldValue(field_PokemonStorage_hasChanged, pStorage, true);
        FieldUtil.setFieldValue(field_PokemonStorage_shouldSendUpdates, pStorage, true);
    }

    @Override
    public void cleanData(CPlayer pPlayer) throws Exception {
        // TODO 注意特殊情况
        this.restore(pPlayer, new byte[0]);
    }

    public void cleanStorage(Object pStorage) {
        MethodUtil.invokeMethod(method_PokemonStorage_readFromNBT, pStorage, NBTUtil.newNBTTagCompound());
    }

    @Override
    protected boolean initOnce() throws Exception {
        Class<?> tClazz = Class.forName("com.pixelmonmod.pixelmon.Pixelmon");
        fieldValue_storageManager = FieldUtil.getStaticFieldValue(tClazz, "storageManager");
        fieldValue_ReforgedStorageManager_playersWithSyncedPCs = (Collection<?>)FieldUtil.getDeclaredFieldValue(
                fieldValue_storageManager.getClass(), "playersWithSyncedPCs", fieldValue_storageManager);

        tClazz = Class.forName("com.pixelmonmod.pixelmon.api.storage.IStorageManager");
        method_IStorageManager_getParty = MethodUtil.getDeclaredMethod(tClazz, "getParty", UUID.class);
        method_IStorageManager_getPCForPlayer = MethodUtil.getDeclaredMethod(tClazz, "getPCForPlayer", UUID.class);
        method_IStorageManager_initializePCForPlayer = MethodUtil.getMethodIgnoreParam(tClazz, "initializePCForPlayer", true).oneGet();

        tClazz = Class.forName("com.pixelmonmod.pixelmon.api.storage.PokemonStorage");
        method_PokemonStorage_readFromNBT = MethodUtil.getDeclaredMethod(tClazz, "readFromNBT", NBTUtil.clazz_NBTTagCompound);
        method_PokemonStorage_writeToNBT = MethodUtil.getDeclaredMethod(tClazz, "writeToNBT", NBTUtil.clazz_NBTTagCompound);
        method_PokemonStorage_countAll = MethodUtil.getDeclaredMethod(tClazz, "countAll");
        field_PokemonStorage_hasChanged = FieldUtil.getField(tClazz, "hasChanged");
        field_PokemonStorage_shouldSendUpdates = FieldUtil.getField(tClazz, "shouldSendUpdates");

        method_PixelmonPlayerTracker_onPlayerLogin = MethodUtil.getMethodIgnoreParam(
                Class.forName("com.pixelmonmod.pixelmon.listener.PixelmonPlayerTracker"),
                "onPlayerLogin", true).oneGet();

        tClazz = Class.forName("com.pixelmonmod.pixelmon.TickHandler");
        method_TickHandler_deregisterStarterList = MethodUtil.getDeclaredMethod(tClazz,
                "deregisterStarterList", NMSUtil.clazz_EntityPlayerMP);
        method_TickHandler_registerStarterList = MethodUtil.getDeclaredMethod(tClazz,
                "registerStarterList", NMSUtil.clazz_EntityPlayerMP);

        return true;
    }

    public ArrayList<Object> getPlayerAllStorage(CPlayer pPlayer) {
        ArrayList<Object> tStorages = new ArrayList<Object>();
        tStorages.add(this.getPartyStorage(pPlayer));
        tStorages.add(this.getPCStorage(pPlayer));
        return tStorages;
    }

    public Object getPartyStorage(CPlayer pPlayer) {
        return MethodUtil.invokeMethod(method_IStorageManager_getParty, fieldValue_storageManager, pPlayer.getUniqueId());
    }

    public Object getPCStorage(CPlayer pPlayer) {
        return MethodUtil.invokeMethod(method_IStorageManager_getPCForPlayer, fieldValue_storageManager, pPlayer.getUniqueId());
    }

    public int getPixelmonCount(Object pPokemonStorage) {
        return (int)MethodUtil.invokeMethod(method_PokemonStorage_countAll, pPokemonStorage);
    }

}
