package cc.bukkitPlugin.pds.manager;

import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.TLangManager;
import cc.bukkitPlugin.pds.PlayerDataSQL;

public class LangManager extends TLangManager<PlayerDataSQL>{

    public LangManager(PlayerDataSQL pPlugin){
        super(pPlugin,"lang.yml","1.0");
    }

    @Override
    public boolean reloadConfig(CommandSender pSender){
        if(!super.reloadConfig(pSender)){
            Log.warn(pSender,C("MsgErrorHappendWhenReloadLang"));
            return false;
        }
        this.checkUpdate();
        this.addDefaults();
        this.reloadModles();
        Log.info(pSender,C("MsgLangReloaded"));
        return this.saveConfig(null);
    }

    @Override
    protected void addDefaults(){
        super.addDefaults();

        this.mConfig.addDefault("MsgDataExpection","数据库背包数据异常,请稍候再试");

    }

}
