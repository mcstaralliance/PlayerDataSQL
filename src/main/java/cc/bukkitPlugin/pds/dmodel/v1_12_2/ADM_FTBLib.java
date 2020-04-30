package cc.bukkitPlugin.pds.dmodel.v1_12_2;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.annotation.Nullable;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.dmodel.ADataModel;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.bukkitPlugin.pds.util.DataHelper;
import cc.commons.util.reflect.ClassUtil;
import cc.commons.util.reflect.FieldUtil;
import cc.commons.util.reflect.MethodUtil;
import cc.commons.util.reflect.filter.MethodFilter;

public abstract class ADM_FTBLib extends ADataModel {

    private static Boolean INIT_STATUS = null;

    protected static Class<?> clazz_Universe = null;
    @Deprecated
    protected static Object instance_Universe = null;
    /** Nullable public ForgePlayer getPlayer(UUID) { */
    protected static Method method_Universe_getPlayer = null;
    protected static Field field_ForgePlayer_team = null;

    /**  */
    protected static Class<?> clazz_EventBase = null;
    /** public boolean post() */
    protected static Method method_EventBase_post = null;
    /** public ForgePlayerLoggedInEvent(ForgePlayer) */
    protected static Constructor<?> construct_ForgePlayerLoggedInEvent = null;

    protected static Class<?> clazz_NBTDataStorage_Data = null;
    /** NBTTagCompound serializeNBT() */
    protected static Method method_Data_serializeNBT = null;
    /** void deserializeNBT(NBTTagCompound) */
    protected static Method method_Data_deserializeNBT = null;
    /** void clearCache() */
    protected static Method method_Data_clear = null;

    protected HashSet<String> mDataModelsStr = new HashSet<>();
    protected HashMap<String, Class> mDataModelsClazz = new HashMap<>();

    public ADM_FTBLib(PlayerDataSQL pPlugin) {
        super(pPlugin);
    }

    @Override
    public byte[] getData(CPlayer pPlayer, Map<String, byte[]> pLoadedData) throws Exception {
        return DataHelper.writeMapData(this.mDataModelsClazz.entrySet(), (pDOStream, pEntry) -> {
            DataHelper.writeStr(pDOStream, pEntry.getKey());
            DataHelper.writeBytes(pDOStream,
                    this.getDataModelData(this.getDataModel(pPlayer, pEntry.getValue())));
        });
    }

    @Override
    public void restore(CPlayer pPlayer, byte[] pData) throws Exception {
        DataHelper.readMapData(pData, (pDIStream) -> {
            String pModelClazzStr = DataHelper.readStr(pDIStream);
            byte[] tModelData = DataHelper.readBytes(pDIStream);
            Class<?> tModelClazz = this.mDataModelsClazz.get(pModelClazzStr);
            if (tModelClazz != null) {
                this.restoreDataModelData(this.getDataModel(pPlayer, tModelClazz), tModelData);
            }
        });
    }

    @Override
    public void cleanData(CPlayer pPlayer) throws Exception {
        this.mDataModelsClazz.values().forEach(clazz -> this.clearDataModelData(getDataModel(pPlayer, clazz)));
    }

    @Override
    protected boolean initOnce() throws Exception {
        if (INIT_STATUS != null) return INIT_STATUS.booleanValue();
        INIT_STATUS = false;

        clazz_Universe = Class.forName("com.feed_the_beast.ftblib.lib.data.Universe");
        method_Universe_getPlayer = MethodUtil.getDeclaredMethod(clazz_Universe, MethodFilter.c().setName("getPlayer").addFilter((method) -> {
            return method.getParameterCount() == 1 && method.getParameterTypes()[0].isAssignableFrom(NMSUtil.clazz_EntityPlayer);
        })).oneGet();

        field_ForgePlayer_team = FieldUtil.getDeclaredField(method_Universe_getPlayer.getReturnType(), "team");

        Class<?> tClazz = Class.forName("com.feed_the_beast.ftblib.events.player.ForgePlayerLoggedInEvent");
        construct_ForgePlayerLoggedInEvent = ClassUtil.getConstrouctor(tClazz, method_Universe_getPlayer.getReturnType());

        clazz_EventBase = Class.forName("com.feed_the_beast.ftblib.lib.EventBase");
        method_EventBase_post = MethodUtil.getDeclaredMethod(clazz_EventBase, "post");

        clazz_NBTDataStorage_Data = Class.forName("com.feed_the_beast.ftblib.lib.data.NBTDataStorage$Data");
        method_Data_serializeNBT = MethodUtil.getDeclaredMethod(clazz_NBTDataStorage_Data, "serializeNBT");
        method_Data_deserializeNBT = MethodUtil.getDeclaredMethod(clazz_NBTDataStorage_Data, "deserializeNBT",
                NBTUtil.clazz_NBTTagCompound);;
        method_Data_clear = MethodUtil.getDeclaredMethod(clazz_NBTDataStorage_Data, "clearCache");

        for (String sStr : this.mDataModelsStr) {
            tClazz = Class.forName(sStr);
            if (clazz_NBTDataStorage_Data.isAssignableFrom(tClazz)) {
                this.mDataModelsClazz.put(sStr, tClazz);
            } else {
                Log.severe("FTB模块 " + this.getModelId() + " 注册了一个非Data类型的类");
            }
        }

        return INIT_STATUS = true;
    }

    public Object getUniverse() {
        if (instance_Universe == null) {
            instance_Universe = FieldUtil.getStaticDeclaredFieldValue(clazz_Universe, "INSTANCE");
        }
        return instance_Universe;
    }

    public Object getForgePlayer(CPlayer pPlayer) {
        return MethodUtil.invokeMethod(method_Universe_getPlayer, getUniverse(), pPlayer.getNMSPlayer());
    }

    public Object getForgeTeam(CPlayer pPlayer) {
        return FieldUtil.getFieldValue(field_ForgePlayer_team, getForgePlayer(pPlayer));
    }

    public void postForgePlayerLoggedInEvent(CPlayer pPlayer) {
        Object tForgePlayer = getForgePlayer(pPlayer);
        if (tForgePlayer == null) return;

        MethodUtil.invokeMethod(method_EventBase_post,
                ClassUtil.newInstance(construct_ForgePlayerLoggedInEvent, tForgePlayer));
    }

    /**
     * 根据FTB数据模型类获取FTB数据模型实例
     * 
     * @param pPlayer
     *            所属玩家
     * @param pModelClazz
     *            数据模块类
     * @return
     */
    @Nullable
    public abstract Object getDataModel(CPlayer pPlayer, Class<?> pModelClazz);

    /**
     * 用于获取各个模块数据时使用
     * 
     * @param pDataModel
     *            数据模块实例
     * @return
     */
    public abstract byte[] getDataModelData(Object pDataModel);

    /**
     * 用于还原各个模块数据时使用
     * 
     * @param pDataModel
     *            数据模块实例
     * @param pData
     *            数据
     */
    public abstract void restoreDataModelData(Object pDataModel, byte[] pData);

    /**
     * 用于清除各个模块数据时使用
     * 
     * @param pDataModel
     *            数据模块实例
     */
    public void clearDataModelData(Object pDataModel) {
        this.restoreDataModelData(pDataModel, new byte[0]);
    }

}
