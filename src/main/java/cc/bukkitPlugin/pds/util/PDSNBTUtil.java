package cc.bukkitPlugin.pds.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.commons.util.reflect.ClassUtil;
import cc.commons.util.reflect.MethodUtil;
import cc.commons.util.reflect.filter.MethodFilter;

public class PDSNBTUtil extends NBTUtil{

    /** NBTTagCompound readCompressed(InputStream) */
    public static final Method method_NBTCompressedStreamTools_readCompressed;
    /** void writeCompressed(NBTTagCompound,OutputStream) */
    public static final Method method_NBTCompressedStreamTools_writeCompressed;

    public static final Method method_EntityPlayer_readEntityFromNBT;
    public static final Method method_EntityPlayer_writeEntityToNBT;

    static{
        String packetPath=ClassUtil.getClassPacket(clazz_NBTTagCompound.getName());
        Class<?> NBTCompressedStreamTools=null;
        if(ClassUtil.isClassLoaded(packetPath+"CompressedStreamTools")) // kc
            NBTCompressedStreamTools=ClassUtil.getClass(packetPath+"CompressedStreamTools");
        else NBTCompressedStreamTools=ClassUtil.getClass(packetPath+"NBTCompressedStreamTools"); // bukkit
        // NBT-END

        method_NBTCompressedStreamTools_readCompressed=MethodUtil.getDeclaredMethod(NBTCompressedStreamTools,
                MethodFilter.rpt(clazz_NBTTagCompound,InputStream.class)).first();
        method_NBTCompressedStreamTools_writeCompressed=MethodUtil.getDeclaredMethod(NBTCompressedStreamTools,
                MethodFilter.rpt(void.class,clazz_NBTTagCompound,OutputStream.class)).first();

        // Entity readFromNBT>>InvBack
        Class<?> clazz_EntityZombie=null;
        if(ClassUtil.isClassLoaded(packetPath+"EntityZombie")){
            clazz_EntityZombie=ClassUtil.getClass(packetPath+"EntityZombie");
        }else{
            clazz_EntityZombie=ClassUtil.getClass("net.minecraft.entity.monster.EntityZombie");
        }
        // 获取世界实例
        World tWorld=Bukkit.getWorlds().get(0);
        Method tMethod=MethodUtil.getMethod(tWorld.getClass(),"getHandle",true);
        Object tNMSWorld=MethodUtil.invokeMethod(tMethod,tWorld);
        Object tObj_EntityZombie=null;
        try{
            tObj_EntityZombie=clazz_EntityZombie.getDeclaredConstructors()[0].newInstance(tNMSWorld);
        }catch(InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException|SecurityException e){
            Log.severe(e);
        }
        Object tObj_NBTTagCompound=NBTUtil.newNBTTagCompound();
        ArrayList<Method> tms=MethodUtil.getDeclaredMethod(clazz_EntityZombie,MethodFilter.rpt(void.class,clazz_NBTTagCompound));
        int readMethodPos=0;
        MethodUtil.invokeMethod(tms.get(0),tObj_EntityZombie,tObj_NBTTagCompound);
        if(!NBTUtil.getNBTTagCompoundValue(tObj_NBTTagCompound).isEmpty()){
            readMethodPos=1;
        }else{
            readMethodPos=0;
        }
        method_EntityPlayer_readEntityFromNBT=MethodUtil.getMethod(NMSUtil.clazz_EntityPlayer,tms.get(readMethodPos).getName(),clazz_NBTTagCompound,false);
        method_EntityPlayer_writeEntityToNBT=MethodUtil.getMethod(NMSUtil.clazz_EntityPlayer,tms.get(1-readMethodPos).getName(),clazz_NBTTagCompound,false);
        // Entity readFromNBT-END
    }

    /**
     * 设置玩家的NBT
     * 
     * @param pPlayer
     *            玩家
     * @param pNBTTag
     *            NBT
     */
    public static void setPlayerNBT(Player pPlayer,Object pNBTTag){
        MethodUtil.invokeMethod(method_EntityPlayer_readEntityFromNBT,NMSUtil.getNMSPlayer(pPlayer),pNBTTag);
    }

    /**
     * 获取玩家的NBT
     * 
     * @param pPlayer
     *            玩家
     * @return NBT实例
     */
    public static Object getPlayerNBT(Player pPlayer){
        Object tNBTTag=NBTUtil.newNBTTagCompound();
        MethodUtil.invokeMethod(method_EntityPlayer_writeEntityToNBT,NMSUtil.getNMSPlayer(pPlayer),tNBTTag);
        return tNBTTag;
    }

    public static byte[] compressNBT(Object pNBTTag){
        if(pNBTTag==null||!NBTUtil.isNBTTagCompound(pNBTTag))
            return new byte[0];

        ByteArrayOutputStream tBAOStream=new ByteArrayOutputStream();
        MethodUtil.invokeStaticMethod(method_NBTCompressedStreamTools_writeCompressed,pNBTTag,tBAOStream);
        return tBAOStream.toByteArray();
    }

    public static Object decompressNBT(byte[] pData){
        if(pData==null||pData.length==0)
            return NBTUtil.newNBTTagCompound();

        ByteArrayInputStream tBAIStream=new ByteArrayInputStream(pData);
        return MethodUtil.invokeStaticMethod(method_NBTCompressedStreamTools_readCompressed,tBAIStream);
    }

}
