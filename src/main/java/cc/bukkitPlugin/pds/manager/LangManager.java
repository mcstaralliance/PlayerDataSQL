package cc.bukkitPlugin.pds.manager;

import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.TLangManager;
import cc.bukkitPlugin.pds.PlayerDataSQL;

public class LangManager extends TLangManager<PlayerDataSQL> {

    public LangManager(PlayerDataSQL pPlugin) {
        super(pPlugin, "lang.yml", "1.0");
    }

    @Override
    public boolean reloadConfig(CommandSender pSender) {
        if (!super.reloadConfig(pSender)) {
            Log.warn(pSender, C("MsgErrorHappendWhenReloadLang"));
            return false;
        }
        this.checkUpdate();
        this.addDefaults();
        this.reloadModles();
        Log.info(pSender, C("MsgLangReloaded"));
        return this.saveConfig(null);
    }

    @Override
    protected void addDefaults() {
        super.addDefaults();
        this.mConfig.addDefault("HelpCmdLoad", "载入指定玩家数据库的数据到自己的身上");
        this.mConfig.addDefault("HelpCmdLoadForPlayer", "为玩家载入指定玩家的数据库数据");
        this.mConfig.addDefault("HelpCmdCopy", "复制指定玩家的数据到自己身上");
        this.mConfig.addDefault("HelpCmdCopyForPlayer", "为玩家复制指定玩家的数据");
        this.mConfig.addDefault("HelpCmdSave", "保存你的数据到指定玩家");
        this.mConfig.addDefault("HelpCmdSaveToPlayer", "保存前者玩家数据到指定玩家");
        this.mConfig.addDefault("HelpCmdRunScript", "执行脚本操作玩家数据");
        this.mConfig.addDefault("HelpCmdUnlock", "执行一个或全部玩家的数据解锁操作");
        this.mConfig.addDefault("MsgDataExpection", "数据库背包数据异常,请稍候再试");
        this.mConfig.addDefault("MsgCannotLoadDataForConsole", "§c不能为控制台载入数据");
        this.mConfig.addDefault("MsgErrorOnLoadingDataWait", "§c载入数据等待期间发生错误");
        this.mConfig.addDefault("MsgCannotCopyDataForConsole", "§c不能为控制台复制数据");
        this.mConfig.addDefault("MsgCannotSaveDataForConsole", "§c不能为控制台保存数据");
        this.mConfig.addDefault("MsgPlayerDataLockSaveNotAllow", "§c玩家 §4§l%player% §c的数据已被锁定,玩家可能在线,数据可能会被覆盖");
        this.mConfig.addDefault("MsgPlayerDataNotExist", "§c玩家 §4§l%player% §c的数据不存在,请检查大小写");
        this.mConfig.addDefault("MsgPlayerNotOnline", "§c玩家 §4§l%player% §c未在线");
        this.mConfig.addDefault("MsgPlayerNotExist", "§c玩家 §4§l%player% §c不存在");
        this.mConfig.addDefault("MsgRunScriptSuccess", "§c运行脚本§b%script_name% §c成功，成功操作了§b%pcount%§c个玩家。");
        this.mConfig.addDefault("MsgErrorOnRunScriptControlPlayer", "§c运行脚本§b%script_name%操作§4§l%player%玩家时 §c时发生了错误");
        this.mConfig.addDefault("MsgErrorOnRunScript", "§c运行脚本%script_name% §c时发生了错误");
        this.mConfig.addDefault("MsgErrorNotFoundScript", "§c§4§l%script_name% §c这个脚本不存在");
        this.mConfig.addDefault("MsgErrorOnLoadSQLData", "§c从数据库载入 §4§l%player% §c的数据时发生了错误");
        this.mConfig.addDefault("MsgErrorOnUpdateSQLData", "§c对数据库更新玩家 §4§l%player% §c的数据时发生了错误");
        this.mConfig.addDefault("MsgErrorOnUnlockPlayer", "§c解锁玩家数据时发生了错误");
        this.mConfig.addDefault("MsgModelErrorOnClearData", "§c模块 %model% 在清理 %player% 的数据时发生了错误");
        this.mConfig.addDefault("MsgModelErrorOnSerializeData", "§c模块 %model% 在序列化 %player% 的数据时发生了错误");
        this.mConfig.addDefault("MsgModelErrorOndeserializeData", "§c模块 %model% 在反序列化 %player% 的数据时发生了错误");
        this.mConfig.addDefault("MsgLoadDataForPlayer", "已经为 %for% 载入 %from% 的数据");
        this.mConfig.addDefault("MsgCopyDataForPlayer", "已经复制  %from% 的数据到  %for%");
        this.mConfig.addDefault("MsgSaveDataForPlayer", "已经保存  %from% 的数据到  %to%");
        this.mConfig.addDefault("MsgModifyDataForPlayer", "已经修改 %from% 的数据到  %to%");
        this.mConfig.addDefault("MsgSuccessConnectToDB", "成功连接到数据库");
        this.mConfig.addDefault("MsgUnableConnectToDB", "§c无法连接到数据库");
        this.mConfig.addDefault("MsgOnlinePlayerNoUnlock", "§c在线用户无法解锁");
        this.mConfig.addDefault("MsgUnlockNoChange", "§c未解锁任何数据");
        this.mConfig.addDefault("MsgUnlockedPlayer", "共解锁了 %amount% 条玩家数据");
        this.mConfig.addDefault("WordFor", "为谁");
        this.mConfig.addDefault("WordFrom", "来源");
        this.mConfig.addDefault("WordPlayer", "玩家");
        this.mConfig.addDefault("WordYou", "你");
        this.mConfig.addDefault("WordScriptName", "脚本名称");

    }

}
