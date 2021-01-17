package cc.bukkitPlugin.pds.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.commons.util.reflect.ClassUtil;
import cc.commons.util.reflect.FieldUtil;
import cc.commons.util.reflect.MethodUtil;

public class CapabilityHelper {

    private static boolean mInitSuccess = true;

    /** CapabilityDispatcher capabilities */
    private static Field field_NMSEntity_capabilities;
    /** ICapabilityProvider[] caps; */
    private static Field field_CapabilityDispatcher_caps;
    /** INBTSerializable<NBTBase>[] writers; */
    private static Field field_CapabilityDispatcher_writers;
    /** FastCapability CatServer */
    private static Field field_CapabilityDispatcher_fastCapabilities;
    private static BiFunction<Object, Class<?>, Object> cap_getter;
    private static BiFunction<Object, Class<?>, Object> writer_getter;

    /** T serializeNBT(); */
    private static Method method_INBTSerializable_serializeNBT;
    /** void deserializeNBT(T); */
    private static Method method_INBTSerializable_deserializeNBT;

    /** T getCapability(Capability<T>,EnumFacing) */
    private static Method method_NMSEntity_getCapability;
    /** NBTBase writeNBT(T instance, EnumFacing side); */
    private static Method method_Capability_writeNBT;
    /** void readNBT(T instance, EnumFacing side, NBTBase nbt); */
    private static Method method_Capability_readNBT;
    @Nullable
    public final static Class<?> clazz_PlayerLoggedInEvent;

    static {
        boolean tIsForge = ClassUtil.isClassLoaded("net.minecraftforge.common.MinecraftForge");
        mInitSuccess = tIsForge;

        String tStatus = "field_NMSEntity_capabilities";
        try {
            field_NMSEntity_capabilities = FieldUtil.getField(NMSUtil.clazz_NMSEntity, "capabilities");
        } catch (IllegalStateException exp) {
            mInitSuccess = false;
        }

        if (mInitSuccess) {
            tStatus = "caps getter, writers getter";

            try {
                Class<?> tClazz = field_NMSEntity_capabilities.getType();
                if (FieldUtil.isDeclaredFieldExist(tClazz, "caps")) {
                    field_CapabilityDispatcher_caps = FieldUtil.getDeclaredField(tClazz, "caps");
                    cap_getter = (Object pNMSEntity, Class<?> pProvider) -> {
                        // capabilities
                        Object tObj = FieldUtil.getFieldValue(field_NMSEntity_capabilities, pNMSEntity);
                        // providers
                        tObj = FieldUtil.getFieldValue(field_CapabilityDispatcher_caps, tObj);
                        for (Object sObj : (Object[])tObj) {
                            if (pProvider.isInstance(sObj)) return sObj;
                        }

                        return null;
                    };
                    field_CapabilityDispatcher_writers = FieldUtil.getDeclaredField(tClazz, "writers");
                    writer_getter = (Object pNMSEntity, Class<?> pProvider) -> {
                        // capabilities
                        Object tObj = FieldUtil.getFieldValue(field_NMSEntity_capabilities, pNMSEntity);
                        // writers
                        tObj = FieldUtil.getFieldValue(field_CapabilityDispatcher_writers, tObj);
                        for (Object sObj : (Object[])tObj) {
                            if (pProvider.isInstance(sObj)) return sObj;
                        }

                        return null;
                    };
                } else { // Catserver FastCapability
                    field_CapabilityDispatcher_fastCapabilities = FieldUtil.getDeclaredField(tClazz, "fastCapabilities");
                    tClazz = field_CapabilityDispatcher_fastCapabilities.getType().getComponentType();
                    field_CapabilityDispatcher_caps = FieldUtil.getDeclaredField(tClazz, "cap");
                    cap_getter = (Object pNMSEntity, Class<?> pProvider) -> {
                        // capabilities
                        Object tObj = FieldUtil.getFieldValue(field_NMSEntity_capabilities, pNMSEntity);
                        // FastCapability
                        tObj = FieldUtil.getFieldValue(field_CapabilityDispatcher_fastCapabilities, tObj);
                        for (Object sObj : (Object[])tObj) {
                            Object tCap = FieldUtil.getFieldValue(field_CapabilityDispatcher_caps, sObj);
                            if (pProvider.isInstance(tCap)) return tCap;
                        }

                        return null;
                    };
                    field_CapabilityDispatcher_writers = FieldUtil.getDeclaredField(tClazz, "writer");
                    writer_getter = (Object pNMSEntity, Class<?> pProvider) -> {
                        // capabilities
                        Object tObj = FieldUtil.getFieldValue(field_NMSEntity_capabilities, pNMSEntity);
                        // FastCapability
                        tObj = FieldUtil.getFieldValue(field_CapabilityDispatcher_fastCapabilities, tObj);
                        for (Object sObj : (Object[])tObj) {
                            Object tWriter = FieldUtil.getFieldValue(field_CapabilityDispatcher_writers, sObj);
                            if (pProvider.isInstance(tWriter)) return tWriter;
                        }

                        return null;
                    };
                }
            } catch (IllegalStateException | NullPointerException exp) {
                mInitSuccess = false;
            }
        }

        if (mInitSuccess) {
            tStatus = "INBTSerializable serializeNBT/deserializeNBT";

            try {
                Class<?> tClazz = ClassUtil.getClass("net.minecraftforge.common.util.INBTSerializable");
                method_INBTSerializable_serializeNBT = MethodUtil.getMethodIgnoreParam(tClazz, "serializeNBT", true).oneGet();
                method_INBTSerializable_deserializeNBT = MethodUtil.getMethodIgnoreParam(tClazz, "deserializeNBT", true).oneGet();
            } catch (IllegalStateException | NullPointerException exp) {
                mInitSuccess = false;
            }
        }

        if (mInitSuccess) {
            tStatus = "getCapability";

            try {
                method_NMSEntity_getCapability = MethodUtil.getMethodIgnoreParam(NMSUtil.clazz_NMSEntity, "getCapability", false).oneGet();
            } catch (IllegalStateException exp) {
                mInitSuccess = false;
            }
        }

        if (mInitSuccess) {
            tStatus = "Capability writeNBT/readNBT";

            try {
                Class<?> tClazz = ClassUtil.getClass("net.minecraftforge.common.capabilities.Capability");
                method_Capability_writeNBT = MethodUtil.getMethodIgnoreParam(tClazz, "writeNBT", true).oneGet();
                method_Capability_readNBT = MethodUtil.getMethodIgnoreParam(tClazz, "readNBT", true).oneGet();
            } catch (IllegalStateException | NullPointerException exp) {
                mInitSuccess = false;
            }
        }

        Class<?> tClazz = null;
        if (mInitSuccess) {
            try {
                String tStr = "net.minecraftforge.fml.common.gameevent.PlayerEvent$PlayerLoggedInEvent";//1.12.x
                if (ClassUtil.isClassLoaded(tStr)) {
                    tClazz = ClassUtil.getClass(tStr);
                } else {
                    tStr = "net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent";//1.15.x
                    tClazz = ClassUtil.getClass(tStr);
                }
            } catch (IllegalStateException | NullPointerException exp) {
                tClazz = null;
            }

            if (tClazz == null) {
                Log.warn("未能找到玩家登陆事件类,部分模块数据可能会存在服务器与客户端不同的情况");
            }
        }
        clazz_PlayerLoggedInEvent = tClazz;

        if (!mInitSuccess && tIsForge) {
            Log.severe("CapabilityHelper 在\"" + tStatus + "\"过程时初始化失败,部分模块可能无法启用");
        }
    }

    public static boolean isInisSuccess() {
        return CapabilityHelper.mInitSuccess;
    }

    public static Object getCapabilityProvider(Object pNMSEntity, Class<?> pProvider) {
        if (!isInisSuccess()) return null;

        return cap_getter.apply(pNMSEntity, pProvider);
    }

    public static Object getCapabilityStorage(Object pNMSEntity, Class<?> pProvider) {
        if (!isInisSuccess()) return null;

        return writer_getter.apply(pNMSEntity, pProvider);
    }

    public static Object serializeCapability(Object pNMSEntity, Class<?> pProvider) {
        if (!isInisSuccess()) return NBTUtil.newNBTTagCompound();

        Object tStorage = getCapabilityStorage(pNMSEntity, pProvider);
        if (tStorage != null) {
            return MethodUtil.invokeMethod(method_INBTSerializable_serializeNBT, tStorage);
        }

        return NBTUtil.newNBTTagCompound();
    }

    public static void deserializeCapability(Object pNMSEntity, Class<?> pProvider, Object pNBT) {
        if (!isInisSuccess()) return;

        Object tStorage = getCapabilityStorage(pNMSEntity, pProvider);
        if (tStorage != null) {
            MethodUtil.invokeMethod(method_INBTSerializable_deserializeNBT, tStorage, pNBT);
        }
    }

    /**
     * @param pNMSEntity
     *            NMS实体实例
     * @param pCapabilityEntry
     *            Capability的Key(net.minecraftforge.common.capabilities.Capability)
     * @param pFacing
     *            朝向(EnumFacing)
     * @return
     */
    public static Object getCapability(Object pNMSEntity, Object pCapabilityEntry, @Nullable Object pFacing) {
        if (!isInisSuccess()) return null;

        return MethodUtil.invokeMethod(method_NMSEntity_getCapability, pNMSEntity, pCapabilityEntry, pFacing);
    }

    /**
     * 从NBT还原Capability的数据
     * 
     * @param pNMSEntity
     *            NMS实体实例
     * @param pCapabilityEntry
     *            Capability的Key(net.minecraftforge.common.capabilities.Capability)
     * @param pFacing
     *            朝向(EnumFacing)
     * @return
     */
    public static void readCapabilityNBT(Object pNMSEntity, Object pCapabilityEntry, @Nullable Object pFacing, Object pNBT) {
        if (!isInisSuccess()) return;

        Object tCap = getCapability(pNMSEntity, pCapabilityEntry, pFacing);
        if (tCap == null) return;
        MethodUtil.invokeMethod(method_Capability_readNBT, pCapabilityEntry, tCap, pFacing, pNBT);
    }

    /**
     * 将Capability的数据序列化到NBT中
     * 
     * @param pNMSEntity
     *            NMS实体实例
     * @param pCapabilityEntry
     *            Capability的Key(net.minecraftforge.common.capabilities.Capability)
     * @param pFacing
     *            朝向(EnumFacing)
     * @return
     */
    public static Object writeCapabilityNBT(Object pNMSEntity, Object pCapabilityEntry, @Nullable Object pFacing) {
        if (!isInisSuccess()) return NBTUtil.newNBTTagCompound();

        Object tCap = getCapability(pNMSEntity, pCapabilityEntry, pFacing);
        if (tCap == null) return NBTUtil.newNBTTagCompound();
        return MethodUtil.invokeMethod(method_Capability_writeNBT, pCapabilityEntry, tCap, pFacing);
    }

    @Nullable
    public static Object newLoginEvent(CPlayer pPlayer) {
        return clazz_PlayerLoggedInEvent == null ? null
                : ClassUtil.newInstance(clazz_PlayerLoggedInEvent, NMSUtil.clazz_EntityPlayer, pPlayer.getNMSPlayer());
    }

}
