package cc.bukkitPlugin.pds.manager;

import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.TConfigManager;
import cc.bukkitPlugin.pds.PlayerDataSQL;

public class ConfigManager extends TConfigManager<PlayerDataSQL>{

    public int mSyncDelay=10;
    public boolean mKickOnReadSQLError=true;
    public boolean mNoRestoreIfSQLDataNotExist=true;

    public ConfigManager(PlayerDataSQL pPlugin){
        super(pPlugin,"1.0");
    }

    @Override
    public void addDefaults(){
        super.addDefaults();

        this.mConfig.addDefault("Plugin.SyncDelay",this.mSyncDelay,"背包还原延迟(tick),重要");
        this.mConfig.addDefault("Plugin.KickOnReadSQLError",this.mKickOnReadSQLError,"读取SQL错误时踢出玩家");
        this.mConfig.addDefault("Plugin.NoRestoreIfSQLDataNotExist",this.mNoRestoreIfSQLDataNotExist,
                "如果数据库玩家数据不存在(在等待足够长时间后),PDS将不会从数据库还原玩家数据",
                "此项配置的目的为可以自动导入数据未在数据库中的玩家,如果未开启,PDS将使用空白数据还原玩家",
                "如果想使用此功能导入玩家数据,注意,数据库内一定要不存在该玩家的数据文件,并且玩家首次登录并且使用了此插件的服务器一定要有玩家的正确存档");

    }

    @Override
    public boolean reloadConfig(CommandSender pSender){
        if(!super.reloadConfig(pSender)){
            Log.severe(pSender,C("MsgErrorHappendWhenReloadConfig"));
            return false;
        }
        this.checkUpdate();
        this.addDefaults();

        this.mSyncDelay=this.mConfig.getInt("Plugin.SyncDelay",this.mSyncDelay);
        this.mKickOnReadSQLError=this.mConfig.getBoolean("Plugin.KickOnReadSQLError",this.mKickOnReadSQLError);
        this.mNoRestoreIfSQLDataNotExist=this.mConfig.getBoolean("Plugin.NoRestoreIfSQLDataNotExist",this.mNoRestoreIfSQLDataNotExist);

        this.reloadModles(pSender);
        Log.info(pSender,C("MsgConfigReloaded"));
        return this.saveConfig(null);
    }
}
