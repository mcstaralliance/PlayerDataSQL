package cc.bukkitPlugin.pds;

import java.sql.SQLException;

import org.bukkit.Bukkit;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.ABukkitPlugin;
import cc.bukkitPlugin.pds.api.PDSAPI;
import cc.bukkitPlugin.pds.api.event.CallDataModelRegisterEvent;
import cc.bukkitPlugin.pds.dmodel.DM_Baubles;
import cc.bukkitPlugin.pds.dmodel.DM_MCStats;
import cc.bukkitPlugin.pds.dmodel.DM_Minecraft;
import cc.bukkitPlugin.pds.dmodel.DM_TConstruct;
import cc.bukkitPlugin.pds.dmodel.DM_Thaumcraft;
import cc.bukkitPlugin.pds.listener.PlayerListener;
import cc.bukkitPlugin.pds.listener.PreventListener;
import cc.bukkitPlugin.pds.manager.ConfigManager;
import cc.bukkitPlugin.pds.manager.LangManager;
import cc.bukkitPlugin.pds.storage.IStorage;
import cc.bukkitPlugin.pds.storage.MySQL;
import cc.bukkitPlugin.pds.user.UserManager;

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

        PDSAPI.registerModel(new DM_Minecraft(this));
        PDSAPI.registerModel(new DM_MCStats(this));
        PDSAPI.registerModel(new DM_Baubles(this));
        PDSAPI.registerModel(new DM_Thaumcraft(this));
        PDSAPI.registerModel(new DM_TConstruct(this));
        Bukkit.getPluginManager().callEvent(new CallDataModelRegisterEvent());

        // 初始化管理器并载入配置
        this.reloadPlugin(null);

        PDSAPI.checkModels(true);

        try{
            ((MySQL)this.mStorage).getConn();
        }catch(SQLException exp){
            Log.warn("无法连接至数据库:"+exp.getMessage());
        }
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
