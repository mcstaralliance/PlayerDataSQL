package cc.bukkitPlugin.pds.dmodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.PDSNBTUtil;
import cc.commons.util.IOUtil;
import cc.commons.util.reflect.ClassUtil;
import cc.commons.util.reflect.MethodUtil;

public class DM_AcademyCraft extends DM_Minecraft{

    public static class SubM{

        public final Class<?> mModelClazz;
        public final String mModelClazzStr;
        public final Method mInstnceMethod;
        /** 初始化的数据,类型为NBTTagCompound */
        public final Object mInitialData;

        public SubM(Class<?> pModelClazz){
            this.mModelClazz=pModelClazz;
            this.mModelClazzStr=pModelClazz.getName();
            this.mInstnceMethod=MethodUtil.getUnknowMethod(pModelClazz,pModelClazz,NMSUtil.clazz_EntityPlayer,true).get(0);
            if(!Modifier.isStatic(this.mInstnceMethod.getModifiers())){
                throw new IllegalStateException();
            }

            this.mInitialData=NBTUtil.newNBTTagCompound();
            Object tInstance=ClassUtil.newInstance(pModelClazz);
            MethodUtil.invokeMethod(method_DataPart_toNBT,tInstance,this.mInitialData);
        }

    }

    public static Method method_DataPart_sync=null;
    public static Method method_DataPart_fromNBT=null;
    public static Method method_DataPart_toNBT=null;
    private List<SubM> mDataModels=new ArrayList<>();

    private Boolean mInit=null;

    public DM_AcademyCraft(PlayerDataSQL pPlugin){
        super(pPlugin);
    }

    @Override
    public String getModelId(){
        return "AcademyCraft";
    }

    @Override
    public String getDesc(){
        return "超能力";
    }

    @Override
    public boolean initOnce(){
        if(this.mInit!=null)
            return this.mInit.booleanValue();

        try{
            Class.forName("cn.academy.core.AcademyCraft");
            Class<?> tClazz=Class.forName("cn.lambdalib.util.datapart.DataPart");

            method_DataPart_sync=tClazz.getDeclaredMethod("sync");
            method_DataPart_fromNBT=tClazz.getDeclaredMethod("fromNBT",NBTUtil.clazz_NBTTagCompound);
            method_DataPart_toNBT=tClazz.getDeclaredMethod("toNBT",NBTUtil.clazz_NBTTagCompound);

            List<Class<?>> tClazzes=ClassUtil.getPackageClasses("cn.academy",true,(pClazz)->{
                if(tClazz.isAssignableFrom(pClazz)){
                    for(Annotation sAnnotation : pClazz.getAnnotations()){
                        if(sAnnotation.annotationType().getSimpleName().equals("SideOnly")&&sAnnotation.toString().contains("CLIENT")){
                            return false;
                        }
                    }
                    return true;
                }
                return false;
            });

            for(Class<?> sClazz : tClazzes){
                Log.developInfo("Find sub model \""+sClazz.getName()+"\" of data model "+this.getModelId());
                try{
                    this.mDataModels.add(new SubM(sClazz));
                }catch(IllegalStateException exp){
                    Log.developInfo("The sub model of Data Model "+this.getModelId()+" don't have method to instance");
                }
            }
        }catch(Exception exp){
            if(!(exp instanceof ClassNotFoundException))
                Log.severe("模块 "+this.getDesc()+" 初始化时发生了错误",exp);
            return (this.mInit=false);
        }
        return (this.mInit=true);
    }

    @Override
    public byte[] getData(Player pPlayer,Map<String,byte[]> pLoadedData) throws Exception{
        Object tNMSPlayer=NMSUtil.getNMSPlayer(pPlayer);
        HashMap<String,Object> tNBTTags=new HashMap<>();
        for(SubM sSubM : this.mDataModels){
            Object tModelInstance=MethodUtil.invokeStaticMethod(sSubM.mInstnceMethod,tNMSPlayer);
            if(tModelInstance==null) continue;

            Object tNBT=NBTUtil.newNBTTagCompound();
            MethodUtil.invokeMethod(method_DataPart_toNBT,tModelInstance,tNBT);
            tNBTTags.put(sSubM.mModelClazzStr,tNBT);
        }

        return serializeNBTMap(tNBTTags);
    }

    @Override
    public void restore(Player pPlayer,byte[] pData) throws Exception{
        HashMap<String,Object> tNBTs=new HashMap<>();
        if(pData.length>0){
            ByteArrayInputStream tBAIStream=new ByteArrayInputStream(pData);
            DataInputStream tDIStream=new DataInputStream(tBAIStream);
            int tSize=tDIStream.readInt();
            for(int i=0;i<tSize;i++){
                String tModelName=tDIStream.readUTF();
                int tDataLen=tDIStream.readInt();
                byte[] tData=new byte[tDataLen];
                if(tDataLen>0){
                    tDIStream.readFully(tData);
                }
                tNBTs.put(tModelName,PDSNBTUtil.decompressNBT(tData));
            }
        }

        Object tNMSPlayer=NMSUtil.getNMSPlayer(pPlayer);
        for(SubM sSubM : this.mDataModels){
            Object tModelInstance=MethodUtil.invokeStaticMethod(sSubM.mInstnceMethod,tNMSPlayer);
            if(tModelInstance==null) continue;

            Object tNBT=tNBTs.get(sSubM.mModelClazzStr);
            if(tNBT==null){ // 如果未空直接使用初始数据还原
                tNBT=PDSNBTUtil.invokeNBTTagCompound_clone(sSubM.mInitialData);
            }else{ // 不为空,先用初始数据清空玩家数据
                MethodUtil.invokeMethod(method_DataPart_fromNBT,tModelInstance,PDSNBTUtil.invokeNBTTagCompound_clone(sSubM.mInitialData));
            }
            MethodUtil.invokeMethod(method_DataPart_fromNBT,tModelInstance,tNBT);
            MethodUtil.invokeMethod(method_DataPart_sync,tModelInstance);
        }

    }

    @Override
    public byte[] loadFileData(OfflinePlayer pPlayer,Map<String,byte[]> pLoadedData) throws IOException{
        byte[] tData=pLoadedData.get(DM_Minecraft.ID.toLowerCase());
        if(tData==null) tData=super.loadFileData(pPlayer,pLoadedData);

        Object tNBT=PDSNBTUtil.decompressNBT(tData);
        tNBT=NBTUtil.invokeNBTTagCompound_get(tNBT,"ForgeData");
        HashMap<String,Object> tNBTTags=new HashMap<>();
        if(NBTUtil.isNBTTagCompound(tNBT)){
            Map<String,Object> tNBTValue=NBTUtil.getNBTTagCompoundValue(tNBT);
            for(SubM sSubM : this.mDataModels){
                Object tNBTTag=tNBTValue.get(sSubM.mModelClazzStr);
                if(tNBTTag!=null){
                    tNBTTags.put(sSubM.mModelClazzStr,tNBTTag);
                }
            }
        }
        return serializeNBTMap(tNBTTags);
    }

    @Override
    public void cleanData(Player pPlayer){
        Object tNMSPlayer=NMSUtil.getNMSPlayer(pPlayer);
        for(SubM sSubM : this.mDataModels){
            Object tModelInstance=MethodUtil.invokeStaticMethod(sSubM.mInstnceMethod,tNMSPlayer);
            if(tModelInstance==null) continue;

            // 如果未空直接使用初始数据还原
            Object tNBT=PDSNBTUtil.invokeNBTTagCompound_clone(sSubM.mInitialData);
            MethodUtil.invokeMethod(method_DataPart_fromNBT,tModelInstance,tNBT);
        }
    }

    public static byte[] serializeNBTMap(Map<String,Object> pNBTTags) throws IOException{
        ByteArrayOutputStream tBAOStream=new ByteArrayOutputStream();
        DataOutputStream tDOStream=null;
        try{
            tDOStream=new DataOutputStream(tBAOStream);
            tDOStream.writeInt(pNBTTags.size());
            for(Map.Entry<String,Object> sEntry : pNBTTags.entrySet()){
                tDOStream.writeUTF(sEntry.getKey());
                byte[] tCompressNBT=PDSNBTUtil.compressNBT(sEntry.getValue());
                tDOStream.writeInt(tCompressNBT.length);
                tDOStream.write(tCompressNBT);
            }
        }finally{
            IOUtil.closeStream(tDOStream);
        }
        return tBAOStream.toByteArray();
    }

}
