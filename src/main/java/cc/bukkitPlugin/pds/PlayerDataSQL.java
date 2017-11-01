package cc.bukkitPlugin.pds;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.ABukkitPlugin;
import cc.bukkitPlugin.commons.util.BukkitUtil;
import cc.bukkitPlugin.pds.api.PDSAPI;
import cc.bukkitPlugin.pds.api.event.CallDataModelRegisterEvent;
import cc.bukkitPlugin.pds.command.CommandExc;
import cc.bukkitPlugin.pds.dmodel.ADataModel;
import cc.bukkitPlugin.pds.listener.PlayerListener;
import cc.bukkitPlugin.pds.listener.PreventListener;
import cc.bukkitPlugin.pds.manager.ConfigManager;
import cc.bukkitPlugin.pds.manager.LangManager;
import cc.bukkitPlugin.pds.storage.IStorage;
import cc.bukkitPlugin.pds.storage.MySQL;
import cc.bukkitPlugin.pds.user.UserManager;
import cc.commons.util.reflect.ClassUtil;

public class PlayerDataSQL extends ABukkitPlugin<PlayerDataSQL>{

    /**
     * 踢出玩家,线程安全的
     * 
     * @param pPlayer
     *            玩家名字
     */
    public static void kickPlayerOnError(String pPlayer){
        if(Bukkit.isPrimaryThread()){
            PlayerDataSQL.kickPlayerOnError0(pPlayer);
        }else{
            Bukkit.getScheduler().runTask(getInstance(),()->PlayerDataSQL.kickPlayerOnError0(pPlayer));
        }
    }

    private static void kickPlayerOnError0(String pPlayer){
        Player tPlayer=Bukkit.getPlayerExact(pPlayer);
        PlayerDataSQL tPlugin=PlayerDataSQL.getInstance();
        if(tPlayer!=null){
            tPlayer.getPlayer().kickPlayer(tPlugin.C("MsgDataExpection"));
        }
    }

    private UserManager mUserMan;
    private IStorage mStorage;
    @Getter
    private List<File> scripts=new ArrayList();

    /**
     * 此函数中不要进行模块间的互相调用<br />
     * 调用操作请在reload函数中进行
     */
    public void onEnable(){
        // 注册管理器
        this.setLangManager(new LangManager(this));
        this.registerManager(this.getLangManager());
        this.setConfigManager(new ConfigManager(this));
        this.registerManager(this.getConfigManager());
        this.registerManager(this.mUserMan=new UserManager(this));
        this.registerManager((MySQL)(this.mStorage=new MySQL(this)));

        new PDSAPI(this);

        // 注册监听器
        new PreventListener(this,this.mUserMan);
        new PlayerListener(this,this.mUserMan);

        // 绑定命令管理器
        new CommandExc(this);

        this.registerDM();
        Bukkit.getPluginManager().callEvent(new CallDataModelRegisterEvent());

        // 初始化管理器并载入配置
        this.reloadPlugin(null);

        PDSAPI.checkModels(true); // 先加载配置,再决定启用哪些配置

        for(Player sPlayer : BukkitUtil.getOnlinePlayers()){
            this.mUserMan.createSaveTask(sPlayer.getName());
        }
    }

    @Override
    public void reloadPlugin(CommandSender pSender){
        File tScriptDir=new File(getDataFolder(),"scripts");
        if(!tScriptDir.exists()) tScriptDir.mkdir();
        scripts.clear();
        File[] tScriptFiles=tScriptDir.listFiles();
        if(tScriptFiles!=null)
            for(File sFile : tScriptFiles){
            scripts.add(sFile);
            }
        super.reloadPlugin(pSender);
    }

    @Override
    public void onDisable(){
        for(Player sPlayer : BukkitUtil.getOnlinePlayers()){
            this.mUserMan.saveUser(sPlayer,false);
        }
        super.onDisable();
    }

    private void registerDM(){
        String tPackage=ADataModel.class.getPackage().getName();
        tPackage=tPackage==null?"cc.bukkitPlugin.pds.dmodel":tPackage;

        List<Class<?>> tDMClasses=null;
        try{
            tDMClasses=ClassUtil.getPackageClasses(tPackage,true);
        }catch(IOException exp){
            Log.severe("Error on auto register datamodel",exp);
            return;
        }

        int tRegisteredCount=0;
        for(Class<?> tClass : tDMClasses){
            if(tClass.isInterface()||Modifier.isAbstract(tClass.getModifiers())||!ADataModel.class.isAssignableFrom(tClass))
                continue;

            try{
                ADataModel tModel=ClassUtil.newInstance((Class<? extends ADataModel>)tClass,PlayerDataSQL.class,this);
                PDSAPI.registerModel(tModel);
                tRegisteredCount++;
            }catch(Throwable exp){
                Log.severe("Error on auto register datamodel class: "+tClass.getName(),exp);
            }
        }

        Log.debug("auto registered "+tRegisteredCount+" datamodel");
    }

    @Override
    public ConfigManager getConfigManager(){
        return (ConfigManager)super.getConfigManager();
    }

    @Override
    public LangManager getLangManager(){
        return (LangManager)super.getLangManager();
    }

    public UserManager getUserManager(){
        return this.mUserMan;
    }

    public IStorage getStorage(){
        return this.mStorage;
    }

    public static PlayerDataSQL getInstance(){
        return ABukkitPlugin.getInstance(PlayerDataSQL.class);
    }

}
