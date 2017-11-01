package cc.bukkitPlugin.pds.dmodel;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.PDSNBTUtil;
import cc.commons.util.reflect.FieldUtil;
import cc.commons.util.reflect.MethodUtil;

public abstract class ADM_InVanilla extends DM_Minecraft{

    protected static Method method_Entity_getExtendedProperties;
    protected static Method method_Entity_registerExtendedProperties;

    static{
        try{
            method_Entity_registerExtendedProperties=MethodUtil.getMethodIgnoreParam(NMSUtil.clazz_NMSEntity,"registerExtendedProperties",true).get(0);
            method_Entity_getExtendedProperties=MethodUtil.getMethodIgnoreParam(NMSUtil.clazz_NMSEntity,"getExtendedProperties",true).get(0);
        }catch(IllegalStateException ignore){
        }
    }

    protected final HashSet<String> mModelTags=new HashSet<>();
    protected final String mExPropClass;
    protected Class<?> mExPropClazz;
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

    protected Boolean mInit=null;
    protected Field field_NMSEntity_extendedProperties;

    public ADM_InVanilla(PlayerDataSQL pPlugin,String pExPropClass,String pExPropName){
        super(pPlugin);

        this.mExPropClass=pExPropClass;
        this.mExPropName=pExPropName;
    }

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
    public byte[] getData(Player pPlayer,Map<String,byte[]> pLoadedData) throws Exception{
        Object tNMSPlayer=NMSUtil.getNMSPlayer(pPlayer);
        Object tData=this.getOrCreateExProp(tNMSPlayer,pPlayer);
        if(tData==null) return new byte[0];

        Object tNBTTag=NBTUtil.newNBTTagCompound();
        MethodUtil.invokeMethod(this.method_ExProp_saveNBTData,tData,tNBTTag);
        return PDSNBTUtil.compressNBT(tNBTTag);
    }

    @Override
    public void restore(Player pPlayer,byte[] pData) throws Exception{
        Object tNMSPlayer=NMSUtil.getNMSPlayer(pPlayer);
        this.reset(tNMSPlayer,pPlayer);
        if(pData.length==0) return;

        Object tExProp=this.getOrCreateExProp(tNMSPlayer,pPlayer);
        if(tExProp==null) return;

        MethodUtil.invokeMethod(method_ExProp_loadNBTData,tExProp,PDSNBTUtil.decompressNBT(pData));
        this.updateToAround(tNMSPlayer,tExProp);
    }

    @Override
    public byte[] loadFileData(OfflinePlayer pPlayer,Map<String,byte[]> pLoadedData) throws IOException{
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

    public void reset(Object pNMSPlayer,Player pPlayer){
        Object tExProp=this.getExProp(pNMSPlayer);
        if(tExProp==null){
            this.registerExProp(pNMSPlayer,pPlayer);
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
        this.registerExProp(pNMSPlayer,pPlayer);
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
    public Object getOrCreateExProp(Object pNMSPlayer,Player pPlayer){
        Object tExProp=this.getExProp(pNMSPlayer);
        if(tExProp==null){
            this.registerExProp(pNMSPlayer,pPlayer);
            tExProp=this.getExProp(pNMSPlayer);
        }
        return tExProp;
    }

    /**
     * 获取当前Mod数据
     * 
     * @param pNMSPlayer
     *            NMS玩家,数据类型为EntityPlayer
     * @return Mod数据或null
     */
    protected Object getExProp(Object pNMSPlayer){
        if(this.method_ExProp_get==null){
            return this.getExPropNMS(pNMSPlayer);
        }else{
            return MethodUtil.invokeStaticMethod(this.method_ExProp_get,pNMSPlayer);
        }
    }

    /**
     * 使用NMS Player接口的方法获取当前Mod数据
     * 
     * @param pNMSPlayer
     *            NMS玩家,数据类型为EntityPlayer
     * @return Mod数据或null
     */
    public final Object getExPropNMS(Object pNMSPlayer){
        return MethodUtil.invokeMethod(method_Entity_getExtendedProperties,pNMSPlayer,this.mExPropName);
    }

    /**
     * 为Mod注册数据
     * 
     * @param pNMSPlayer
     *            NMS玩家,数据类型为EntityPlayer
     */
    protected void registerExProp(Object pNMSPlayer,Player pPlayer){
        if(this.method_ExProp_register==null){
            this.registerExPropNMS(pNMSPlayer,pPlayer);
        }else{
            MethodUtil.invokeStaticMethod(this.method_ExProp_register,pNMSPlayer);
            this.initExProp(pNMSPlayer,NMSUtil.getNMSWorld(pPlayer.getWorld()));
        }
    }

    /**
     * 使用NMS Player接口的方法为Mod注册数据
     * 
     * @param pNMSPlayer
     *            NMS玩家,数据类型为EntityPlayer
     */
    protected void registerExPropNMS(Object pNMSPlayer,Player pPlayer){
        Object tNMSWorld=NMSUtil.getNMSWorld(pPlayer.getWorld());
        MethodUtil.invokeMethod(method_Entity_registerExtendedProperties,pNMSPlayer,this.mExPropName,this.newExProp(pNMSPlayer,tNMSWorld));
        this.initExProp(pNMSPlayer,tNMSWorld);
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
    public Object newExProp(Object pNMSPlayer,Object pNMSWorld){
        try{
            Constructor<?> tConstructor=this.mExPropClazz.getDeclaredConstructor(NMSUtil.clazz_EntityPlayer);
            tConstructor.setAccessible(true);
            return tConstructor.newInstance(pNMSPlayer);
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
    public void initExProp(Object pNMSPlayer,Object pNMSWorld){
        MethodUtil.invokeMethod(method_ExProp_init,this.getExProp(pNMSPlayer),pNMSPlayer,pNMSWorld);
    }

    @Override
    public void cleanData(Player pPlayer){
        Object tNMSPlayer=NMSUtil.getNMSPlayer(pPlayer);
        this.reset(tNMSPlayer,pPlayer);
    }

    @Override
    public abstract String getModelId();

    @Override
    public abstract String getDesc();

    @Override
    public abstract boolean initOnce();

    protected abstract void updateToAround(Object pNMSPlayer,Object pExProp);

}
