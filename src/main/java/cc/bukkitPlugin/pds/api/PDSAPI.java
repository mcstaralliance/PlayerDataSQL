package cc.bukkitPlugin.pds.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.IConfigModel;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.api.event.CallDataModelRegisterEvent;
import cc.commons.commentedyaml.CommentedSection;
import cc.commons.commentedyaml.CommentedYamlConfig;
import cc.commons.util.StringUtil;
import cc.commons.util.ValidData;

public class PDSAPI implements Listener,IConfigModel{

    /** 插件实例 */
    private static PlayerDataSQL mPlugin=null;

    /** 注册的模块 */
    private static LinkedHashMap<String,IDataModel> mRegistedModels=new LinkedHashMap<>();
    /** 启用的模块 */
    private static ArrayList<IDataModel> mEnabledModels=new ArrayList<>();
    /** 配置中启用的模块 */
    private static HashSet<String> mEnabledModelsStr=new HashSet<>();
    /** 启用的模块数组 */
    private static IDataModel[] mEnabledModelsA=null;

    /**
     * 获取插件实例
     * 
     * @return 插件实例
     * @throws IllegalStateException
     *             插件未实例化
     */
    public static PlayerDataSQL getPlugin(){
        synchronized(PDSAPI.class){
            if(PDSAPI.mPlugin==null){
                PDSAPI.mPlugin=PlayerDataSQL.getInstance();
            }
        }
        return PDSAPI.mPlugin;
    }

    /**
     * 注册一个数据模块
     * <p>
     * 外部注册请只在{@link CallDataModelRegisterEvent}事件被触发时注册<br>
     * 否则,请手动调用{@link #checkModels()}
     * </p>
     * 
     * @param pModel
     *            模块
     */
    public static void registerModel(IDataModel pModel){
        ValidData.notNull(pModel.getPlugin(),"模块的插件不能为空");
        ValidData.notEmpty(pModel.getModelId(),"模块名称不能为空");
        String tLowerCase=pModel.getModelId().toLowerCase();
        ValidData.valid(!PDSAPI.mRegistedModels.containsKey(tLowerCase),"模块名称 "+pModel.getModelId()+" 已经被注册");
        PDSAPI.mRegistedModels.put(tLowerCase,pModel);
    }

    /**
     * 检查注册的模块,提取激活的模块
     * 
     * @param pNotify
     *            是否发送模块注册成功通知
     */
    public static void checkModels(boolean pNotify){
        synchronized(PDSAPI.class){
            PDSAPI.mEnabledModels.clear();
            for(IDataModel sModel : PDSAPI.mRegistedModels.values()){
                try{
                    if(sModel.getPlugin().isEnabled()&&sModel.initOnce()&&PDSAPI.mEnabledModelsStr.contains(sModel.getModelId().toLowerCase())){
                        PDSAPI.mEnabledModels.add(sModel);
                        if(pNotify) Log.info("成功启用数据模块: "+sModel.getDesc());
                    }
                }catch(Throwable exp){
                    Log.severe("在初始化数据模块 "+sModel.getModelId()+" 时发生错误",exp);
                }
            }
            PDSAPI.mEnabledModelsA=null;
        }
    }

    public static IDataModel remove(String pModelName){
        IDataModel tRemoved=PDSAPI.mRegistedModels.remove(pModelName.toLowerCase());
        if(tRemoved!=null){
            synchronized(PDSAPI.class){
                PDSAPI.mEnabledModels.remove(tRemoved);
                PDSAPI.mEnabledModelsA=null;
            }
        }
        return tRemoved;
    }

    public static void remove(Plugin pPlugin){
        synchronized(PDSAPI.class){
            Iterator<Entry<String,IDataModel>> tIt=PDSAPI.mRegistedModels.entrySet().iterator();
            boolean tChange=false;
            while(tIt.hasNext()){
                IDataModel tModel=tIt.next().getValue();
                if(tModel.getPlugin()==pPlugin){
                    tIt.remove();
                    PDSAPI.mEnabledModels.remove(tModel);
                    tChange=true;
                }
            }

            if(tChange) PDSAPI.mEnabledModelsA=null;
        }
    }

    public static IDataModel[] getEnableModel(){
        synchronized(PDSAPI.class){
            if(PDSAPI.mEnabledModelsA==null){
                if(PDSAPI.mEnabledModels.isEmpty()) PDSAPI.checkModels(false);
                PDSAPI.mEnabledModelsA=PDSAPI.mEnabledModels.toArray(new IDataModel[PDSAPI.mEnabledModels.size()]);
            }
        }
        return PDSAPI.mEnabledModelsA;
    }

    private boolean mFirtInit=true;

    public PDSAPI(PlayerDataSQL pPlugin){
        pPlugin.registerEvents(this);
        pPlugin.getConfigManager().registerConfigModel(this);
    }

    @Override
    public void addDefaults(CommentedYamlConfig pConfig){
        CommentedSection tSecMain=pConfig.getOrCreateSection("Sync","启用哪些同步模块");
        for(IDataModel sModel : PDSAPI.mRegistedModels.values()){
            if(!sModel.initOnce()) continue;
            String[] tComments=null;
            if(StringUtil.isNotEmpty(sModel.getDesc())){
                tComments=sModel.getDesc().split("\n");
            }else{
                tComments=new String[0];
            }
            tSecMain.addDefault(sModel.getModelId(),true,tComments);
        }
    }

    @Override
    public void setConfig(CommandSender pSender,CommentedYamlConfig pConfig){
        CommentedSection tSecMain=pConfig.getOrCreateSection("Sync");
        PDSAPI.mEnabledModelsStr.clear();

        for(String sKey : tSecMain.getKeys(false)){
            if(tSecMain.getBoolean(sKey,false)){
                PDSAPI.mEnabledModelsStr.add(sKey.toLowerCase());
            }
        }

        PDSAPI.checkModels(!this.mFirtInit);
        this.mFirtInit=false;
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent pEvent){
        if(pEvent.getPlugin()==PDSAPI.getPlugin())
            return;

        PDSAPI.remove(pEvent.getPlugin());
    }
}
