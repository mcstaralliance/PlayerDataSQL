package cc.bukkitPlugin.pds.dmodel.v1_7_10;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.dmodel.DM_Minecraft;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.bukkitPlugin.pds.util.PDSNBTUtil;
import cc.commons.util.reflect.FieldUtil;
import cc.commons.util.reflect.MethodUtil;

public abstract class ADM_InVanilla extends DM_Minecraft{

    protected static Method method_Entity_getExtendedProperties;
    protected static Method method_Entity_registerExtendedProperties;

    static{
        try{
            method_Entity_registerExtendedProperties=MethodUtil.getMethodIgnoreParam(NMSUtil.clazz_NMSEntity,"registerExtendedProperties",true).get(0);
            method_Entity_getExtendedProperties=MethodUtil.getMethod(NMSUtil.clazz_NMSEntity,"getExtendedProperties",String.class,true);
        }catch(IllegalStateException ignore){
        }
    }

    /**
     * 模组保存数据到NBT时所使用的根节点名字,用于文件载入数据功能
     * <p>
     * 由于Mod Exp的NBT数据保存级别与玩家的其他数据处于同一级别,因此需要此Mod<br>
     * 所使用的NBT根节点名字来从玩家的所有NBT数据中分离出Mod的数据
     * </p>
     */
    protected final HashSet<String> mModelTags=new HashSet<>();
    /** Mod Exp的类全名,在构造函数中初始化 */
    protected final String mExPropClass;
    /** Mod Exp的类全名,在构造函数中初始化 */
    protected Class<?> mExPropClazz;
    /** Mod Exp的注册名字 */
    protected final String mExPropName;

    /** void loadNBTData(NBTTagCompound) */
    protected Method method_ExProp_loadNBTData;
    /** void saveNBTData(NBTTagCompound) */
    protected Method method_ExProp_saveNBTData;
    /** void init(Entity;World) */
    protected Method method_ExProp_init;

    /** static T get(EntityPlayer) */
    protected Method method_ExProp_get;
    /** static void register(EntityPlayer) */
    protected Method method_ExProp_register;

    protected Field field_NMSEntity_extendedProperties;

    public ADM_InVanilla(PlayerDataSQL pPlugin,String pExPropClass,String pExPropName){
        super(pPlugin);

        this.mExPropClass=pExPropClass;
        this.mExPropName=pExPropName;
    }

    /**
     * 初始化Mod的ExProp相关方法
     * <p>
     * 此方法中会获取ExProp的loadNBTData与saveNBTData方法,并尝试获取静态的get与register方法<br>
     * get方法,用于获取Mod的ExProp,static T get(EntityPlayer)<br>
     * register方法,用于注册Mod的ExProp,static void register(EntityPlayer)
     * </p>
     * 
     * @throws Exception
     */
    protected void initExProp() throws Exception{
        this.mExPropClazz=Class.forName(this.mExPropClass);

        this.method_ExProp_loadNBTData=MethodUtil.getMethod(this.mExPropClazz,"loadNBTData",NBTUtil.clazz_NBTTagCompound,true);
        this.method_ExProp_saveNBTData=MethodUtil.getMethod(this.mExPropClazz,"saveNBTData",NBTUtil.clazz_NBTTagCompound,true);
        this.method_ExProp_init=MethodUtil.getMethodIgnoreParam(this.mExPropClazz,"init",true).get(0);

        try{
            this.method_ExProp_get=this.mExPropClazz.getDeclaredMethod("get",NMSUtil.clazz_EntityPlayer);
            this.method_ExProp_register=this.mExPropClazz.getDeclaredMethod("register",NMSUtil.clazz_EntityPlayer);
        }catch(NoSuchMethodException ignore){
        }

        try{
            this.field_NMSEntity_extendedProperties=NMSUtil.clazz_NMSEntity.getDeclaredField("extendedProperties");
        }catch(NoSuchFieldException ignore){
        }
    }

    @Override
    public byte[] getData(CPlayer pPlayer,Map<String,byte[]> pLoadedData) throws Exception{
        Object tData=this.getOrCreateExProp(pPlayer);
        if(tData==null) return new byte[0];

        Object tNBTTag=NBTUtil.newNBTTagCompound();
        MethodUtil.invokeMethod(this.method_ExProp_saveNBTData,tData,tNBTTag);
        return PDSNBTUtil.compressNBT(tNBTTag);
    }

    @Override
    public void restore(CPlayer pPlayer,byte[] pData) throws Exception{
        this.cleanData(pPlayer);
        if(pData.length==0) return;

        Object tExProp=this.getOrCreateExProp(pPlayer);
        if(tExProp==null) return;

        MethodUtil.invokeMethod(method_ExProp_loadNBTData,tExProp,PDSNBTUtil.decompressNBT(pData));
        this.updateToAround(pPlayer,tExProp);
    }

    @Override
    public byte[] loadFileData(CPlayer pPlayer,Map<String,byte[]> pLoadedData) throws IOException{
        byte[] tData=pLoadedData.get(DM_Minecraft.ID.toLowerCase());
        if(tData==null) tData=super.loadFileData(pPlayer,pLoadedData);

        Object tNBT=this.correctNBTData(PDSNBTUtil.decompressNBT(tData));
        Map<String,Object> tNBTValue=NBTUtil.getNBTTagCompoundValue(tNBT);
        Map<String,Object> tRemoved=new HashMap<>();
        for(String sKey : this.mModelTags){
            Object tValue=tNBTValue.remove(sKey);
            if(tValue!=null) tRemoved.put(sKey,tValue);
        }

        tNBTValue.clear();
        tNBTValue.putAll(tRemoved);
        return PDSNBTUtil.compressNBT(tNBT);
    }

    /**
     * 修正mod数据的NBT值,防止还原数据的时候报错
     * 
     * @param pNBTTag
     *            要修正的NBT数据
     * @return 修正后的数据
     */
    public Object correctNBTData(Object pNBTTag){
        return pNBTTag;
    }

    /**
     * 获取MOD的数据
     * 
     * @param pNMSPlayer
     *            NMS玩家,类型为EntityPlayer
     * @return MOD数据或null
     */
    public Object getOrCreateExProp(CPlayer pPlayer){
        Object tExProp=this.getExProp(pPlayer);
        if(tExProp==null){
            this.registerExProp(pPlayer);
            tExProp=this.getExProp(pPlayer);
        }
        return tExProp;
    }

    /**
     * 获取当前Mod数据
     * 
     * @param pPlayer
     *            NMS玩家,数据类型为EntityPlayer
     * @return Mod数据或null
     */
    protected Object getExProp(CPlayer pPlayer){
        if(this.method_ExProp_get==null){
            return this.getExPropNMS(pPlayer);
        }else{
            return MethodUtil.invokeStaticMethod(this.method_ExProp_get,pPlayer.getNMSPlayer());
        }
    }

    /**
     * 使用NMS Player接口的方法获取当前Mod数据
     * 
     * @param pPlayer
     *            NMS玩家,数据类型为EntityPlayer
     * @return Mod数据或null
     */
    public final Object getExPropNMS(CPlayer pPlayer){
        return MethodUtil.invokeMethod(method_Entity_getExtendedProperties,pPlayer.getNMSPlayer(),this.mExPropName);
    }

    /**
     * 为Mod注册数据
     * 
     * @param pNMSPlayer
     *            NMS玩家,数据类型为EntityPlayer
     */
    protected void registerExProp(CPlayer pPlayer){
        if(this.method_ExProp_register==null){
            this.registerExPropNMS(pPlayer);
        }else{
            Object tNMSPlayer=pPlayer.getNMSPlayer();
            if(tNMSPlayer!=null){
                MethodUtil.invokeStaticMethod(this.method_ExProp_register,tNMSPlayer);
                this.initExProp(pPlayer,NMSUtil.getNMSWorld(pPlayer.getWorld()));
            }
        }
    }

    /**
     * 使用NMS Player接口的方法为Mod注册数据
     * 
     * @param pNMSPlayer
     *            NMS玩家,数据类型为EntityPlayer
     */
    protected void registerExPropNMS(CPlayer pPlayer){
        Object tNMSWorld=NMSUtil.getNMSWorld(pPlayer.getWorld());
        if(tNMSWorld!=null){
            MethodUtil.invokeMethod(method_Entity_registerExtendedProperties,pPlayer.getNMSPlayer(),this.mExPropName,this.newExProp(pPlayer,tNMSWorld));
            this.initExProp(pPlayer,tNMSWorld);
        }
    }

    /**
     * 实例化一个Mod的附属数据
     * <p>
     * 本方法不一定会调用,只在{@link #method_ExProp_get}为null
     * 并调用{@link #registerExPropNMS(Object, Player)}时才会调用
     * </p>
     * 
     * @param pNMSPlayer
     *            NMS的玩家
     * @param pNMSWorld
     *            NMS的世界
     * @return 新的mod附属数据实例
     */
    public Object newExProp(CPlayer pPlayer,Object pNMSWorld){
        try{
            Constructor<?> tConstructor=this.mExPropClazz.getDeclaredConstructor(NMSUtil.clazz_EntityPlayer);
            tConstructor.setAccessible(true);
            return tConstructor.newInstance(pPlayer.getNMSPlayer());
        }catch(Throwable ignore){
        }

        try{
            Constructor<?> tConstructor=this.mExPropClazz.getDeclaredConstructor(String.class);
            tConstructor.setAccessible(true);
            return tConstructor.newInstance(pPlayer.getName());
        }catch(Throwable ignore){
        }

        try{
            Constructor<?> tConstructor=this.mExPropClazz.getDeclaredConstructor();
            tConstructor.setAccessible(true);
            return tConstructor.newInstance();
        }catch(Throwable ignore){
        }

        throw new IllegalStateException("Cannot instance ExProp for model "+this.getModelId());
    }

    /**
     * 初始化附属数据
     * 
     * @param pNMSPlayer
     *            NMS玩家
     * @param pNMSWorld
     *            NMS世界
     */
    public void initExProp(CPlayer pPlayer,Object pNMSWorld){
        MethodUtil.invokeMethod(method_ExProp_init,this.getExProp(pPlayer),pPlayer.getNMSPlayer(),pNMSWorld);
    }

    @Override
    public void cleanData(CPlayer pPlayer){
        Object pNMSPlayer=pPlayer.getNMSPlayer();
        if(pNMSPlayer==null) return;

        Object tExProp=this.getExProp(pPlayer);
        if(tExProp==null){
            this.registerExProp(pPlayer);
            return;
        }

        Map<?,?> tExProps=null;
        if(this.field_NMSEntity_extendedProperties==null){
            for(Field sField : NMSUtil.clazz_NMSEntity.getDeclaredFields()){
                if(!Map.class.isAssignableFrom(sField.getType()))
                    continue;

                tExProps=(Map<?,?>)FieldUtil.getFieldValue(sField,pNMSPlayer);
                if(tExProps.get(this.mExPropName)==tExProp){
                    this.field_NMSEntity_extendedProperties=sField;
                    break;
                }
            }
        }
        if(this.field_NMSEntity_extendedProperties==null) return; // not found
        if(tExProps==null) tExProps=(Map<?,?>)FieldUtil.getFieldValue(this.field_NMSEntity_extendedProperties,pNMSPlayer);

        tExProps.remove(this.mExPropName);
        this.registerExProp(pPlayer);
    }

    @Override
    public abstract String getModelId();

    @Override
    public abstract String getDesc();

    @Override
    protected abstract boolean initOnce() throws Exception;

    protected abstract void updateToAround(CPlayer pPlayer,Object pExProp);

}
