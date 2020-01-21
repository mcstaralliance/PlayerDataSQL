package cc.bukkitPlugin.pds.dmodel.v1_12_2;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.dmodel.ADataModel;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.bukkitPlugin.pds.util.PDSNBTUtil;
import cc.commons.util.reflect.ClassUtil;
import cc.commons.util.reflect.FieldUtil;
import cc.commons.util.reflect.MethodUtil;

public class DM_Pixelmon_SD extends ADataModel {

    /** static void TickHandler.deregisterStarterList(EntityPlayerMP) */
    private Method method_TickHandler_deregisterStarterList;
    /** static void TickHandler.registerStarterList(EntityPlayerMP) */
    private Method method_TickHandler_registerStarterList;

    /** static void PlayerStorage.removeFromPartyPlayer(int) */
    private Method method_PlayerStorage_removeFromPartyPlayer;

    /** 神奇宝贝(世代)的各个存储管理 */
    protected ArrayList<StorageManager> mStorageMs = new ArrayList<>();

    public DM_Pixelmon_SD(PlayerDataSQL pPlugin) {
        super(pPlugin);
    }

    @Override
    public String getModelId() {
        return "Pixelmon_SD";
    }

    @Override
    public String getDesc() {
        return "宝可梦(世代)";
    }

    @Override
    public byte[] getData(CPlayer pPlayer, Map<String, byte[]> pLoadedData) throws Exception {
        Object tNBT = NBTUtil.newNBTTagCompound();
        Map<String, Object> tValue = NBTUtil.getNBTTagCompoundValue(tNBT);
        for (StorageManager sSMan : this.mStorageMs) {
            tValue.put(sSMan.getID(), sSMan.writeToNBT(pPlayer));
        }

        return PDSNBTUtil.compressNBT(tNBT);
    }

    @Override
    public void restore(CPlayer pPlayer, byte[] pData) throws Exception {
        boolean tLocalhas = this.getPixelmonCount(pPlayer) != 0;
        Object tNBTData = PDSNBTUtil.decompressNBT(pData);
        Map<String, Object> tValue = NBTUtil.getNBTTagCompoundValue(tNBTData);
        for (StorageManager sSMan : this.mStorageMs) {
            Object tStorageNBT = tValue.get(sSMan.getID());
            if (tStorageNBT == null) {
                if (this.mPlugin.getConfigManager().mNoRestoreIfSQLDataNotExist)
                    continue;
                else tStorageNBT = NBTUtil.newNBTTagCompound();
            }

            // 清空背包里的神奇背包并发送清空包到客户端,防止虚数据
            Object tStorage = sSMan.getPlayerStorage(pPlayer);
            if (method_PlayerStorage_removeFromPartyPlayer.getDeclaringClass().isInstance(tStorage)) {
                for (int i = 0; i < 6; i++)
                    MethodUtil.invokeMethod(method_PlayerStorage_removeFromPartyPlayer, tStorage, i);
            }

            sSMan.clearStorage(pPlayer);
            sSMan.readFromNBT(pPlayer, tStorageNBT);
        }

        boolean tDBhas = this.getPixelmonCount(pPlayer) != 0;
        if (tLocalhas && !tDBhas) {
            // 注册--宝可梦选择界面发送
            MethodUtil.invokeStaticMethod(method_TickHandler_registerStarterList, pPlayer.getNMSPlayer());
        } else if (!tLocalhas && tDBhas) {
            // 注销--宝可梦选择界面发送
            MethodUtil.invokeStaticMethod(method_TickHandler_deregisterStarterList, pPlayer.getNMSPlayer());
            pPlayer.getPlayer().closeInventory();
        }
    }

    @Override
    public void cleanData(CPlayer pPlayer) throws Exception {
        for (StorageManager sSMan : this.mStorageMs) {
            sSMan.clearStorage(pPlayer);
        }
    }

    @Override
    protected boolean initOnce() throws Exception {
        Class.forName("com.pixelmonmod.pixelmon.Pixelmon");

        Class<?> tClazz = Class.forName("com.pixelmonmod.pixelmon.storage.PixelmonStorage");
        this.mStorageMs.add(new StorageManager(FieldUtil.getStaticDeclaredFieldValue(tClazz, "pokeBallManager"), "playerMap",
                Class.forName("com.pixelmonmod.pixelmon.storage.PlayerStorage")));
        this.mStorageMs.add(new StorageManager(FieldUtil.getStaticDeclaredFieldValue(tClazz, "computerManager"), "playerComputerList",
                Class.forName("com.pixelmonmod.pixelmon.storage.PlayerComputerStorage")));

        tClazz = Class.forName("com.pixelmonmod.pixelmon.TickHandler");
        method_TickHandler_deregisterStarterList = MethodUtil.getDeclaredMethod(tClazz,
                "deregisterStarterList", NMSUtil.clazz_EntityPlayerMP);
        method_TickHandler_registerStarterList = MethodUtil.getDeclaredMethod(tClazz,
                "registerStarterList", NMSUtil.clazz_EntityPlayerMP);

        tClazz = Class.forName("com.pixelmonmod.pixelmon.storage.PlayerStorage");
        method_PlayerStorage_removeFromPartyPlayer = MethodUtil.getDeclaredMethod(tClazz, "removeFromPartyPlayer", int.class);

        return true;
    }

    public int getPixelmonCount(CPlayer pPlayer) {
        int tCount = 0;
        for (StorageManager sSMan : this.mStorageMs) {
            tCount += sSMan.getPixelmonCount(pPlayer);
        }
        return tCount;
    }

    public static class StorageManager {

        /** 存储管理实例 */
        public final Object mManagerInstance;
        /** 存储类 */
        public final Class<?> mStorageClazz;

        /** 存储管理获取存储实例的方法 */
        public final Method method_Manager_getPlayerStorage;

        public final Method method_Storage_writeToNBT;
        public final Method method_Storage_readFromNBT;
        public final Method method_Storage_count;

        private Object mStorages;

        public StorageManager(Object pStorageMan, String pStoragesField, Class<?> pStorage) throws ClassNotFoundException {
            this(pStorageMan, pStoragesField, pStorage, "getPlayerStorage");
        }

        /**
         * @param pStorageMan
         *            存储管理实例
         * @param pStorage
         *            存储类
         * @param pStoragesField
         *            存储管理中用于保存各个存储实例的字段名字
         * @param pMethodName
         *            存储实例通过存储管理获取的方法名
         * @throws ClassNotFoundException
         */
        public StorageManager(Object pStorageMan, String pStoragesField, Class<?> pStorage, String pMethodName) throws ClassNotFoundException {
            this.mManagerInstance = pStorageMan;
            this.mStorageClazz = pStorage;

            this.method_Manager_getPlayerStorage = MethodUtil.getDeclaredMethod(this.mManagerInstance.getClass(),
                    pMethodName, NMSUtil.clazz_EntityPlayerMP);

            this.method_Storage_writeToNBT = MethodUtil.getDeclaredMethod(pStorage, "writeToNBT", NBTUtil.clazz_NBTTagCompound);
            this.method_Storage_readFromNBT = MethodUtil.getDeclaredMethod(pStorage, "readFromNBT", NBTUtil.clazz_NBTTagCompound);
            this.method_Storage_count = MethodUtil.getDeclaredMethod(pStorage, "count");

            this.mStorages = FieldUtil.getDeclaredFieldValue(this.mManagerInstance.getClass(), pStoragesField, this.mManagerInstance);
        }

        public Object getPlayerStorage(CPlayer pPlayer) {
            Object tPlayer = pPlayer.getNMSPlayer();
            if (!NMSUtil.clazz_EntityPlayerMP.isInstance(tPlayer)) return null;
            Object tStorage = MethodUtil.invokeMethod(method_Manager_getPlayerStorage, this.mManagerInstance, tPlayer);
            if (tStorage instanceof Optional) {
                Optional tTmp = (Optional)tStorage;
                if (tTmp == null || !tTmp.isPresent()) {
                    tStorage = null;
                } else {
                    tStorage = tTmp.get();
                }
            }
            return tStorage;
        }

        /**
         * 将玩家数据写入到NBTTagCompound中
         * 
         * @param pPlayer
         *            玩家
         * @return NBTTagCompound实例
         */
        public Object writeToNBT(CPlayer pPlayer) {
            Object tNBTTag = NBTUtil.newNBTTagCompound();
            Object tStorage = this.getPlayerStorage(pPlayer);
            if (tStorage == null) return tNBTTag;

            MethodUtil.invokeMethod(method_Storage_writeToNBT, tStorage, tNBTTag);
            return tNBTTag;
        }

        /**
         * 将玩家数据写入到NBTTagCompound中
         * 
         * @param pPlayer
         *            玩家
         * @param pNBTTag
         *            存储序列化的数据
         * @return NBTTagCompound实例
         */
        public void readFromNBT(CPlayer pPlayer, Object pNBTTag) {
            Object tStorage = this.getPlayerStorage(pPlayer);
            if (tStorage == null) return;

            MethodUtil.invokeMethod(method_Storage_readFromNBT, tStorage, pNBTTag);
        }

        public void clearStorage(CPlayer pPlayer) {
            if (!NMSUtil.clazz_EntityPlayerMP.isInstance(pPlayer.getNMSPlayer())) return;
            Object tNewStorage = ClassUtil.newInstance(this.mStorageClazz, NMSUtil.clazz_EntityPlayerMP, pPlayer.getNMSPlayer());

            Object tOldStorage = this.getPlayerStorage(pPlayer);
            if (this.mStorages instanceof List) {
                List tListStorage = (List)this.mStorages;
                tListStorage.remove(tOldStorage);
                tListStorage.add(tNewStorage);
            } else if (this.mStorages instanceof Map) {
                Map tMapStorage = (Map)this.mStorages;
                tMapStorage.remove(pPlayer.getUniqueId());
                tMapStorage.put(pPlayer.getUniqueId(), tNewStorage);
            }
        }

        public int getPixelmonCount(CPlayer pPlayer) {
            return (int)MethodUtil.invokeMethod(method_Storage_count, this.getPlayerStorage(pPlayer));
        }

        public String getID() {
            return this.mManagerInstance.getClass().getName();
        }
    }

}
