package cc.bukkitPlugin.pds.dmodel;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.Bukkit;

import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.bukkitPlugin.pds.util.PDSNBTUtil;
import cc.commons.util.extra.CList;
import cc.commons.util.reflect.ClassUtil;
import cc.commons.util.reflect.MethodUtil;
import cc.commons.util.reflect.filter.MethodFilter;

public abstract class ADM_WorldData extends ADataModel {

    /** WorldSavedData WorldSavedData loadItemData(Class;String) */
    protected static Method method_World_loadItemData = null;
    /** void setItemData(String;WorldSavedData) */
    protected static Method method_World_setItemData = null;

    /** void readFromNBT(NBTTagCompound) */
    protected static Method method_WorldSaeData_readFromNBT = null;
    /** NBTTagCompound writeToNBT(NBTTagCompound) */
    protected static Method method_WorldSaeData_writeToNBT = null;
    /** void setDirty(boolean) */
    protected static Method method_WorldSaeData_setDirty = null;

    public static final boolean mInitSuccess;
    public static String MULTI_DATA_MRAK = "MULTI_DATA_MRAK";

    static {
        boolean tSuccess = true;
        String tClazzName = "net.minecraft.world.WorldSavedData";
        if (!ClassUtil.isClassLoaded(tClazzName))
            tClazzName = "net.minecraft.world.storage.WorldSavedData";

        if (ClassUtil.isClassLoaded(tClazzName)) {
            try {
                Class<?> tClazz = ClassUtil.getClass(tClazzName);
                Object tNMSWorld = NMSUtil.getNMSWorld(Bukkit.getWorlds().get(0));
                method_World_loadItemData = MethodUtil.getMethod(tNMSWorld.getClass(),
                        MethodFilter.rpt(tClazz, Class.class, String.class)).oneGet();
                method_World_setItemData = MethodUtil.getMethod(tNMSWorld.getClass(),
                        MethodFilter.rpt(void.class, String.class, tClazz)).oneGet();

                try {
                    method_WorldSaeData_readFromNBT = MethodUtil.getDeclaredMethod(tClazz,
                            MethodFilter.pn("func_76184_a", "readFromNBT")).first();
                } catch (IllegalStateException ignore) {
                    // catserver remapped method
                    CList<Method> tMethods = MethodUtil.getDeclaredMethod(tClazz, MethodFilter.rpt(void.class, NBTUtil.clazz_NBTTagCompound));
                    for (Method sMethod : tMethods) {
                        if (!sMethod.getName().equals("deserializeNBT")) {
                            method_WorldSaeData_readFromNBT = sMethod;
                            break;
                        }
                    }
                }
                try {
                    method_WorldSaeData_writeToNBT = MethodUtil.getDeclaredMethod(tClazz,
                            MethodFilter.pn("func_76187_b", "func_189551_b", "writeToNBT")).first();
                } catch (IllegalStateException ignore) {
                    // catserver remapped method
                    method_WorldSaeData_writeToNBT = MethodUtil.getDeclaredMethod(tClazz,
                            MethodFilter.rpt(NBTUtil.clazz_NBTTagCompound, NBTUtil.clazz_NBTTagCompound)).oneGet();
                }
                method_WorldSaeData_setDirty = MethodUtil.getDeclaredMethod(tClazz,
                        MethodFilter.rpt(void.class, boolean.class)).oneGet();
            } catch (IllegalStateException ignore) {
                tSuccess = false;
            }
        }

        mInitSuccess = tSuccess;
    }

    protected final HashSet<String> mWSDClassName = new HashSet<>();
    protected HashSet<Class<?>> mWSDClass = new HashSet<>();

    protected Object mMainNMSWorld;

    public ADM_WorldData(PlayerDataSQL pPlugin, String...pWSDClassName) {
        super(pPlugin);

        this.mWSDClassName.addAll(Arrays.asList(pWSDClassName));
        this.mMainNMSWorld = NMSUtil.getNMSWorld(Bukkit.getWorlds().get(0));
    }

    protected void initWSD() throws Exception {
        if (!mInitSuccess) throw new ClassNotFoundException();
        for (String sClazz : this.mWSDClassName) {
            this.mWSDClass.add(Class.forName(sClazz));
        }
    }

    @Override
    public byte[] getData(CPlayer pPlayer, Map<String, byte[]> pLoadedData) throws Exception {
        Object tData = NBTUtil.newNBTTagCompound();
        Map<String, Object> tValue = NBTUtil.getNBTTagCompoundValue(tData);
        tValue.put(MULTI_DATA_MRAK, NBTUtil.newNBTTagString(MULTI_DATA_MRAK));
        for (Class<?> sClazz : this.mWSDClass) {
            Object tNBT = NBTUtil.newNBTTagCompound();
            MethodUtil.invokeMethod(method_WorldSaeData_writeToNBT, this.loadWorldData(pPlayer, sClazz), tNBT);
            tValue.put(sClazz.getName(), tNBT);
        }

        return PDSNBTUtil.compressNBT(tData);
    }

    @Override
    public void restore(CPlayer pPlayer, byte[] pData) throws Exception {
        Object tNBTData = PDSNBTUtil.decompressNBT(pData);
        Map<String, Object> tValue = NBTUtil.getNBTTagCompoundValue(tNBTData);
        if (!tValue.containsKey(MULTI_DATA_MRAK)) {
            tValue = new HashMap<>();
            tValue.put(this.mWSDClass.iterator().next().getName(), tNBTData);
        }

        for (Class<?> sClazz : this.mWSDClass) {
            Object tWorldData = this.loadWorldData(pPlayer, sClazz);
            if (tWorldData == null) continue;
            Object tSQLData=tValue.get(sClazz.getName());
            if (tSQLData == null) continue;
            MethodUtil.invokeMethod(method_WorldSaeData_readFromNBT, tWorldData, tSQLData);
            MethodUtil.invokeMethod(method_WorldSaeData_setDirty, tWorldData, true);
            this.syncToClient(pPlayer, tWorldData);
        }
    }

    @Override
    public byte[] loadFileData(CPlayer pPlayer, Map<String, byte[]> pLoadedData) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cleanData(CPlayer pPlayer) {
        for (Class<?> sClazz : this.mWSDClass) {
            Object tData = this.newWorldData(pPlayer, sClazz);
            this.saveWorldData(pPlayer, tData);
        }
    }

    public String getDataKey(CPlayer pPlayer, Class<?> pWSDClazz) {
        return pPlayer.getName();
    }

    /**
     * 载入数据
     * 
     * @param pPlayer
     *            玩家
     * @return 数据
     */
    public Object loadWorldData(CPlayer pPlayer, Class<?> pWSDClazz) {
        Object tData = MethodUtil.invokeMethod(method_World_loadItemData, this.mMainNMSWorld, pWSDClazz, this.getDataKey(pPlayer, pWSDClazz));
        if (tData == null) {
            tData = this.newWorldData(pPlayer, pWSDClazz);
            this.saveWorldData(pPlayer, tData);
        }
        return tData;
    }

    /**
     * 设置并保存数据
     * 
     * @param pPlayer
     *            玩家
     * @param pWData
     *            数据
     */
    public void saveWorldData(CPlayer pPlayer, Object pWData) {
        if (pWData == null) return;
        MethodUtil.invokeMethod(method_World_setItemData, this.mMainNMSWorld, this.getDataKey(pPlayer, pWData.getClass()), pWData);
        MethodUtil.invokeMethod(method_WorldSaeData_setDirty, pWData, true);
    }

    /**
     * 创建一个新的数据
     * 
     * @param pPlayer
     *            玩家
     * @param WorldSaveData
     *            继承类
     * @return 数据模型
     */
    public Object newWorldData(CPlayer pPlayer, Class<?> pWSDClazz) {
        return ClassUtil.newInstance(pWSDClazz, String.class, this.getDataKey(pPlayer, pWSDClazz));
    }

    /**
     * 修正NBT数据,确保mod中的读取函数不会因为NBT数据缺失某个条目而报错
     * 
     * @param pNBTData
     *            NBT数据
     * @return 修正后的NBT数据
     */
    public Object fixNBT(Object pNBTData) {
        return pNBTData;
    }

    public void syncToClient(CPlayer pPlayer, Object pData) {}

}
