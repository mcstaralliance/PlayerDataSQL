package cc.bukkitPlugin.pds.dmodel;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import org.bukkit.Bukkit;

import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.bukkitPlugin.pds.util.PDSNBTUtil;
import cc.commons.util.reflect.ClassUtil;
import cc.commons.util.reflect.MethodUtil;
import cc.commons.util.reflect.filter.MethodFilter;

public abstract class ADM_WorldData extends ADataModel{

    /** WorldSavedData WorldSavedData loadItemData(Class;String) */
    protected static Method method_World_loadItemData=null;
    /** void setItemData(String;WorldSavedData) */
    protected static Method method_World_setItemData=null;

    /** void readFromNBT(NBTTagCompound) */
    protected static Method method_WorldSaeData_readFromNBT=null;
    /** void writeToNBT(NBTTagCompound) */
    protected static Method method_WorldSaeData_writeToNBT=null;
    /** void setDirty(boolean) */
    protected static Method method_WorldSaeData_setDirty=null;

    static{
        String tClazzName="net.minecraft.world.WorldSavedData";
        if(ClassUtil.isClassLoaded(tClazzName)){
            try{
                Class<?> tClazz=ClassUtil.getClass(tClazzName);
                Object tNMSWorld=NMSUtil.getNMSWorld(Bukkit.getWorlds().get(0));
                method_World_loadItemData=MethodUtil.getDeclaredMethod(tNMSWorld.getClass(),
                        MethodFilter.rpt(tClazz,Class.class,String.class)).first();
                method_World_setItemData=MethodUtil.getDeclaredMethod(tNMSWorld.getClass(),
                        MethodFilter.rpt(void.class,String.class,tClazz)).first();

                method_WorldSaeData_readFromNBT=MethodUtil.getDeclaredMethod(tClazz,
                        MethodFilter.pn("func_76184_a","readFromNBT")).first();
                method_WorldSaeData_readFromNBT=MethodUtil.getDeclaredMethod(tClazz,
                        MethodFilter.pn("func_76187_b","writeToNBT")).first();
                method_WorldSaeData_setDirty=MethodUtil.getDeclaredMethod(tClazz,
                        MethodFilter.rpt(void.class,boolean.class)).first();
            }catch(IllegalStateException ignore){
            }
        }
    }

    protected final String mWSDClassName;
    protected Class<?> mWSDClass;

    protected Object mMainNMSWorld;

    public ADM_WorldData(PlayerDataSQL pPlugin,String pWSDClassName){
        super(pPlugin);

        this.mWSDClassName=pWSDClassName;
        this.mMainNMSWorld=NMSUtil.getNMSWorld(Bukkit.getWorlds().get(0));
    }

    protected void initWSD() throws Exception{
        if(method_World_loadItemData==null) throw new ClassNotFoundException();
        this.mWSDClass=Class.forName(this.mWSDClassName);
    }

    @Override
    public byte[] getData(CPlayer pPlayer,Map<String,byte[]> pLoadedData) throws Exception{
        return PDSNBTUtil.compressNBT(
                MethodUtil.invokeMethod(method_WorldSaeData_writeToNBT,this.loadWorldData(pPlayer),NBTUtil.newNBTTagCompound()));
    }

    @Override
    public void restore(CPlayer pPlayer,byte[] pData) throws Exception{
        Object tNBTData=PDSNBTUtil.decompressNBT(pData);
        Object tWorldData=this.loadWorldData(pPlayer);
        MethodUtil.invokeMethod(method_WorldSaeData_readFromNBT,tWorldData,tNBTData);
        MethodUtil.invokeMethod(method_WorldSaeData_setDirty,tWorldData,true);
        this.syncToClient(pPlayer,tWorldData);
    }

    @Override
    public byte[] loadFileData(CPlayer pPlayer,Map<String,byte[]> pLoadedData) throws IOException{
        return PDSNBTUtil.compressNBT(
                MethodUtil.invokeMethod(method_WorldSaeData_writeToNBT,this.loadWorldData(pPlayer),NBTUtil.newNBTTagCompound()));
    }

    @Override
    public void cleanData(CPlayer pPlayer){
        Object tData=this.newWorldData(pPlayer);
        this.saveWorldData(pPlayer,tData);
    }

    public String getDataKey(CPlayer pPlayer){
        return pPlayer.getName();
    }

    /**
     * 载入数据
     * 
     * @param pPlayer
     *            玩家
     * @return 数据
     */
    public Object loadWorldData(CPlayer pPlayer){
        Object tData=MethodUtil.invokeMethod(method_World_loadItemData,this.mMainNMSWorld,this.mWSDClass,this.getDataKey(pPlayer));
        if(tData==null){
            tData=this.newWorldData(pPlayer);
            this.saveWorldData(pPlayer,tData);
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
    public void saveWorldData(CPlayer pPlayer,Object pWData){
        MethodUtil.invokeMethod(method_World_setItemData,this.mMainNMSWorld,this.getDataKey(pPlayer),pWData);
        MethodUtil.invokeMethod(method_WorldSaeData_setDirty,pWData,true);
    }

    /**
     * 创建一个新的数据
     * 
     * @param pPlayer
     *            玩家
     * @return 数据模型
     */
    public Object newWorldData(CPlayer pPlayer){
        return ClassUtil.newInstance(this.mWSDClass,String.class,pPlayer.getName());
    }

    /**
     * 修正NBT数据,确保mod中的读取函数不会因为NBT数据缺失某个条目而报错
     * 
     * @param pNBTData
     *            NBT数据
     * @return 修正后的NBT数据
     */
    public Object fixNBT(Object pNBTData){
        return pNBTData;
    }

    public void syncToClient(CPlayer pPlayer,Object pData){}

}
