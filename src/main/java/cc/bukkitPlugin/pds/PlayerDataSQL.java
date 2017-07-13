package cc.bukkitPlugin.pds;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.List;

import org.bukkit.Bukkit;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.ABukkitPlugin;
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

    private UserManager mUserMan;
    private IStorage mStorage;

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

        PDSAPI.checkModels(true);
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
