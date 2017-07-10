package cc.bukkitPlugin.pds.manager;

import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.TConfigManager;
import cc.bukkitPlugin.pds.PlayerDataSQL;

public class ConfigManager extends TConfigManager<PlayerDataSQL>{

    private int mSyncDelay=10;
    private boolean mKickOnReadSQLError=true;

    public ConfigManager(PlayerDataSQL pPlugin){
        super(pPlugin,"1.0");
    }

    @Override
    public void addDefaults(){
        super.addDefaults();

        this.mConfig.addDefault("Plugin.SyncDelay",this.mSyncDelay,"背包还原延迟(tick),重要");
        this.mConfig.addDefault("Plugin.KickOnReadSQLError",this.mKickOnReadSQLError,"读取SQL错误时踢出玩家");

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

        this.reloadModles(pSender);
        Log.info(pSender,C("MsgConfigReloaded"));
        return this.saveConfig(null);
    }

    public int getSyncDelay(){
        return this.mSyncDelay;
    }

    public boolean isKickOnSQLReadError(){
        return this.mKickOnReadSQLError;
    }
}
