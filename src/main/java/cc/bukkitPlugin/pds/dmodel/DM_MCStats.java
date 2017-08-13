package cc.bukkitPlugin.pds.dmodel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.commons.util.CollUtil;
import cc.commons.util.FileUtil;
import cc.commons.util.reflect.FieldUtil;
import cc.commons.util.reflect.MethodUtil;

public class DM_MCStats extends ADataModel{

    private static final Charset UTF_8=Charset.forName("UTF-8");

    private Boolean mInit=null;
    private Method method_EntityPlayerMP_getStatisticMan;
    /** Map loadStatistic(String) */
    private Method method_StatisticsFile_loadStatistic;
    /** String saveStatistic(Map) */
    private Method method_StatisticsFile_saveStatistic;
    private Field field_StatFileWriter_stats;

    private File mDataDir;

    public DM_MCStats(PlayerDataSQL pPlugin){
        super(pPlugin);

        this.mDataDir=new File(this.mServerDir,"world"+File.separator+"stats");
    }

    @Override
    public String getModelId(){
        return "MinecraftStats";
    }

    @Override
    public String getDesc(){
        return "Minecraft成就数据";
    }

    @Override
    public boolean initOnce(){
        if(this.mInit!=null)
            return this.mInit.booleanValue();

        try{
            for(Method sMethod : NMSUtil.clazz_EntityPlayerMP.getDeclaredMethods()){
                if(CollUtil.isEmpty(sMethod.getParameterTypes())&&sMethod.getReturnType().getSimpleName().toLowerCase().contains("statistic")){
                    this.method_EntityPlayerMP_getStatisticMan=sMethod;
                    break;
                }
            }
            if(this.method_EntityPlayerMP_getStatisticMan==null)
                return (this.mInit=false);

            Class<?> tClazz=this.method_EntityPlayerMP_getStatisticMan.getReturnType();
            this.method_StatisticsFile_loadStatistic=MethodUtil.getUnknowMethod(tClazz,Map.class,String.class,true).get(0);
            this.method_StatisticsFile_saveStatistic=MethodUtil.getUnknowMethod(tClazz,String.class,Map.class,true).get(0);
            this.field_StatFileWriter_stats=FieldUtil.getField(tClazz.getSuperclass(),Map.class,-1,true).get(0);
        }catch(Throwable exp){
            if(!(exp instanceof ClassNotFoundException))
                Log.severe("模块 "+this.getDesc()+" 初始化时发生了错误",exp);
            return (this.mInit=false);
        }
        return (this.mInit=true);
    }

    @Override
    public byte[] getData(Player pPlayer,Map<String,byte[]> pLoadedData) throws Exception{
        Object tStatMan=this.getStatMan(pPlayer);
        String tJson=(String)MethodUtil.invokeMethod(this.method_StatisticsFile_saveStatistic,tStatMan,this.getManStatValue(tStatMan));
        return tJson.getBytes(UTF_8);
    }

    @Override
    public void restore(Player pPlayer,byte[] pData) throws Exception{
        Object tStatMan=this.getStatMan(pPlayer);
        Map<Object,Object> tPlayerStatValue=this.getManStatValue(tStatMan);
        tPlayerStatValue.clear();
        tPlayerStatValue.putAll((Map<Object,Object>)MethodUtil.invokeMethod(this.method_StatisticsFile_loadStatistic,tStatMan,new String(pData,UTF_8)));
    }

    @Override
    public byte[] loadFileData(OfflinePlayer pPlayer,Map<String,byte[]> pLoadedData) throws IOException{
        File tDataFile=this.getUUIDOrNameFile(pPlayer,this.mDataDir,"%name%.json");
        if(!tDataFile.isFile()) return new byte[0];

        return FileUtil.readData(tDataFile);
    }

    @Override
    public void cleanData(Player pPlayer){
        Object tStatMan=this.getStatMan(pPlayer);
        Map<Object,Object> tPlayerStatValue=this.getManStatValue(tStatMan);
        tPlayerStatValue.clear();
    }

    protected void loadDataFromString(Player pToPlayer,String pData){
        Object tStatMan=this.getStatMan(pToPlayer);
        Map<Object,Object> tPlayerStatValue=this.getManStatValue(tStatMan);
        tPlayerStatValue.clear();
        tPlayerStatValue.putAll((Map<Object,Object>)MethodUtil.invokeMethod(this.method_StatisticsFile_loadStatistic,tStatMan,pData));
    }

    private Object getStatMan(Player pPlayer){
        return MethodUtil.invokeMethod(this.method_EntityPlayerMP_getStatisticMan,NMSUtil.getNMSPlayer(pPlayer));
    }

    private Map<Object,Object> getManStatValue(Object pStatMan){
        return (Map<Object,Object>)FieldUtil.getFieldValue(this.field_StatFileWriter_stats,pStatMan);
    }

}
