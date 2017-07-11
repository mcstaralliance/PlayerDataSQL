package cc.bukkitPlugin.pds.dmodel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.PDSNBTUtil;
import cc.commons.util.FileUtil;
import cc.commons.util.reflect.ClassUtil;
import cc.commons.util.reflect.FieldUtil;
import cc.commons.util.reflect.MethodUtil;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategoryList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.research.ResearchManager;

public class DM_Thaumcraft extends ADataModel{

    public static final String TAG_Eldritch="Thaumcraft.eldritch";
    public static final String TAG_Eldritch_Counter="Thaumcraft.eldritch.counter";
    public static final String TAG_Eldritch_Sticky="Thaumcraft.eldritch.sticky";
    public static final String TAG_Eldritch_Temp="Thaumcraft.eldritch.temp";

    private Method method_ResearchManager_saveAspectNBT;
    private Method method_ResearchManager_saveResearchNBT;
    private Method method_ResearchManager_saveScannedNBT;
    private Method method_ResearchManager_loadAspectNBT;
    private Method method_ResearchManager_loadResearchNBT;
    private Method method_ResearchManager_loadScannedNBT;
    private Method method_ResearchManager_completeResearch;

    private Boolean mInit=null;

    public DM_Thaumcraft(PlayerDataSQL pPlugin){
        super(pPlugin);
    }

    @Override
    public String getModelId(){
        return "Thaumcraft";
    }

    @Override
    public String getDesc(){
        return "神秘时代研究";
    }

    @Override
    public boolean initOnce(){
        if(this.mInit!=null)
            return this.mInit.booleanValue();

        try{
            Class<?> tClazz=null;
            Class.forName("thaumcraft.common.Thaumcraft");
            Class.forName("thaumcraft.common.CommonProxy");

            tClazz=Class.forName("thaumcraft.common.lib.research.PlayerKnowledge");
            this.checkMethod(tClazz,new String[]{"getWarpCounter","getWarpPerm","getWarpSticky","getWarpTemp","wipePlayerKnowledge"},String.class);

            tClazz=Class.forName("thaumcraft.common.lib.research.ResearchManager");
            this.method_ResearchManager_loadAspectNBT=tClazz.getDeclaredMethod("loadAspectNBT",NBTUtil.clazz_NBTTagCompound,NMSUtil.clazz_EntityPlayer);
            this.method_ResearchManager_loadResearchNBT=tClazz.getDeclaredMethod("loadResearchNBT",NBTUtil.clazz_NBTTagCompound,NMSUtil.clazz_EntityPlayer);
            this.method_ResearchManager_loadScannedNBT=tClazz.getDeclaredMethod("loadScannedNBT",NBTUtil.clazz_NBTTagCompound,NMSUtil.clazz_EntityPlayer);
            this.method_ResearchManager_saveAspectNBT=tClazz.getDeclaredMethod("saveAspectNBT",NBTUtil.clazz_NBTTagCompound,NMSUtil.clazz_EntityPlayer);
            this.method_ResearchManager_saveResearchNBT=tClazz.getDeclaredMethod("saveResearchNBT",NBTUtil.clazz_NBTTagCompound,NMSUtil.clazz_EntityPlayer);
            this.method_ResearchManager_saveScannedNBT=tClazz.getDeclaredMethod("saveScannedNBT",NBTUtil.clazz_NBTTagCompound,NMSUtil.clazz_EntityPlayer);
            this.method_ResearchManager_completeResearch=tClazz.getDeclaredMethod("completeResearch",NMSUtil.clazz_EntityPlayer,String.class);
        }catch(Exception exp){
            if(!(exp instanceof ClassNotFoundException)&&!(exp instanceof NoSuchMethodException))
                Log.severe("模块 "+this.getDesc()+" 初始化时发生了错误",exp);
            return (this.mInit=false);
        }
        return (this.mInit=true);
    }

    @Override
    public byte[] getData(Player pPlayer,Map<String,byte[]> pLoadedData) throws Exception{
        Object tNBTTagCompound=NBTUtil.newNBTTagCompound();
        Object tNMSPlayer=NMSUtil.getNMSPlayer(pPlayer);
        MethodUtil.invokeMethod(this.method_ResearchManager_saveAspectNBT,null,new Object[]{tNBTTagCompound,tNMSPlayer});
        MethodUtil.invokeMethod(this.method_ResearchManager_saveResearchNBT,null,new Object[]{tNBTTagCompound,tNMSPlayer});
        MethodUtil.invokeMethod(this.method_ResearchManager_saveScannedNBT,null,new Object[]{tNBTTagCompound,tNMSPlayer});

        String tPlayerName=pPlayer.getName();
        Map<String,Object> tTagMap=NBTUtil.getNBTTagCompoundValue(tNBTTagCompound);
        tTagMap.put(TAG_Eldritch,ClassUtil.newInstance(NBTUtil.clazz_NBTTagInt,int.class,Thaumcraft.proxy.getPlayerKnowledge().getWarpPerm(tPlayerName)));
        tTagMap.put(TAG_Eldritch_Counter,ClassUtil.newInstance(NBTUtil.clazz_NBTTagInt,int.class,Thaumcraft.proxy.getPlayerKnowledge().getWarpCounter(tPlayerName)));
        tTagMap.put(TAG_Eldritch_Sticky,ClassUtil.newInstance(NBTUtil.clazz_NBTTagInt,int.class,Thaumcraft.proxy.getPlayerKnowledge().getWarpSticky(tPlayerName)));
        tTagMap.put(TAG_Eldritch_Temp,ClassUtil.newInstance(NBTUtil.clazz_NBTTagInt,int.class,Thaumcraft.proxy.getPlayerKnowledge().getWarpTemp(tPlayerName)));

        return PDSNBTUtil.compressNBT(tNBTTagCompound);
    }

    @Override
    public void restore(Player pPlayer,byte[] pData) throws Exception{
        Thaumcraft.proxy.getPlayerKnowledge().wipePlayerKnowledge(pPlayer.getName());

        Object tNMSPlayer=NMSUtil.getNMSPlayer(pPlayer);
        Object tNBT=PDSNBTUtil.decompressNBT(pData);
        MethodUtil.invokeMethod(this.method_ResearchManager_loadAspectNBT,null,new Object[]{tNBT,tNMSPlayer});
        MethodUtil.invokeMethod(this.method_ResearchManager_loadResearchNBT,null,new Object[]{tNBT,tNMSPlayer});
        MethodUtil.invokeMethod(this.method_ResearchManager_loadScannedNBT,null,new Object[]{tNBT,tNMSPlayer});

        String tPlayerName=pPlayer.getName();
        Map<String,Object> tTagMap=NBTUtil.getNBTTagCompoundValue(tNBT);

        Object tValue=tTagMap.get(TAG_Eldritch);
        if(NBTUtil.isNBTTagInt(tValue)){
            Thaumcraft.proxy.getPlayerKnowledge().setWarpPerm(tPlayerName,(int)FieldUtil.getFieldValue(NBTUtil.field_NBTTagInt_value,tValue));
        }
        tValue=tTagMap.get(TAG_Eldritch_Counter);
        if(NBTUtil.isNBTTagInt(tValue)){
            Thaumcraft.proxy.getPlayerKnowledge().setWarpCounter(tPlayerName,(int)FieldUtil.getFieldValue(NBTUtil.field_NBTTagInt_value,tValue));
        }
        tValue=tTagMap.get(TAG_Eldritch_Sticky);
        if(NBTUtil.isNBTTagInt(tValue)){
            Thaumcraft.proxy.getPlayerKnowledge().setWarpSticky(tPlayerName,(int)FieldUtil.getFieldValue(NBTUtil.field_NBTTagInt_value,tValue));
        }
        tValue=tTagMap.get(TAG_Eldritch_Temp);
        if(NBTUtil.isNBTTagInt(tValue)){
            Thaumcraft.proxy.getPlayerKnowledge().setWarpTemp(tPlayerName,(int)FieldUtil.getFieldValue(NBTUtil.field_NBTTagInt_value,tValue));
        }
        
        // 完成自动解锁的研究
        ResearchManager tMan=Thaumcraft.proxy.getResearchManager();
        Collection<ResearchCategoryList> tRCs=ResearchCategories.researchCategories.values();
        for(ResearchCategoryList sRCL : tRCs){
            Collection<ResearchItem> tRIs=sRCL.research.values();
            for(ResearchItem sRI : tRIs){
                if(sRI.isAutoUnlock()){
                    MethodUtil.invokeMethod(this.method_ResearchManager_completeResearch,tMan,new Object[]{tNMSPlayer,sRI.key});
                }
            }
        }
    }

    @Override
    public byte[] loadFileData(OfflinePlayer pPlayer,Map<String,byte[]> pLoadedData) throws IOException{
        File tDataFile=this.getUUIDOrNameFile(pPlayer,this.mPlayerDataDir,"%name%.thaum");
        if(!tDataFile.isFile()) return new byte[0];

        return FileUtil.readData(tDataFile);
    }

    private void checkMethod(Class<?> pClazz,String[] pMethods,Class<?>...pArgsClazz) throws NoSuchMethodException{
        for(String sMethod : pMethods)
            pClazz.getDeclaredMethod(sMethod,pArgsClazz);
    }

}
