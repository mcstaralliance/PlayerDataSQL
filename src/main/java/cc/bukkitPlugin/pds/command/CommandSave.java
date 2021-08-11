package cc.bukkitPlugin.pds.command;

import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.plugin.command.TACommandBase;
import cc.bukkitPlugin.commons.util.BukkitUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.user.User;
import cc.bukkitPlugin.pds.user.UserManager;
import cc.bukkitPlugin.pds.util.CPlayer;

public class CommandSave extends TACommandBase<PlayerDataSQL, CommandExc> {

    public CommandSave(CommandExc pExector) {
        super(pExector, "save", 2);
    }

    @Override
    public boolean execute(CommandSender pSender, String pLabel, String[] pArgs) {
        if (!hasCmdPermission(pSender))
            return noPermission(pSender, this.mLastConstructPermisson);

        if (pArgs.length == 0) return help(pSender, pLabel);
        if (pArgs.length > 2) return errorArgsNumber(pSender, pArgs.length);

        Player tSaveFrom;
        OfflinePlayer tSaveTo;
        int tSaveToIndex;
        if (pArgs.length == 1) {
            tSaveToIndex = 0;
            if (!(pSender instanceof Player))
                return send(pSender, C("MsgCannotSaveDataForConsole"));

            tSaveFrom = (Player)pSender;
        } else {
            tSaveToIndex = 1;
            tSaveFrom = Bukkit.getPlayerExact(pArgs[0]);
            if (tSaveFrom == null)
                return send(pSender, C("MsgPlayerNotOnline", "%player%", pArgs[0]));
        }
        tSaveTo = Bukkit.getOfflinePlayer(pArgs[tSaveToIndex]);
        if (tSaveTo == null)
            return send(pSender, C("MsgPlayerNotExist", "%player%", pArgs[tSaveToIndex]));

        UserManager tUserMan = this.mPlugin.getUserManager();
        User tUserData = tUserMan.getUserData(new CPlayer(tSaveFrom), false);
        Bukkit.getScheduler().runTaskAsynchronously(this.mPlugin, () -> {
            User tOldData = null;
            try {
                tOldData = tUserMan.loadUser(new CPlayer(tSaveTo));
            } catch (SQLException e) {
                send(pSender, C("MsgErrorOnLoadSQLData", "%player%", tSaveTo.getName()) + ": " + e.getLocalizedMessage());
                return;
            }

            if (tOldData != null && tOldData.isLocked() && tSaveTo != tSaveFrom) {
                send(pSender, C("MsgPlayerDataLockSaveNotAllow", "%player%", tSaveTo.getName()));
                return;
            }

            tUserData.setPlayer(tSaveTo.getName());
            if (tUserMan.saveUser(tUserData, false, pSender)) {
                send(pSender, C("MsgSaveDataForPlayer",
                        new String[]{"%from%", "%to%"},
                        new Object[]{tSaveFrom == pSender ? C("WordYou") : tSaveFrom.getName(), tSaveTo == pSender ? C("WordYou") : tSaveTo.getName()}));
            }
        });

        return true;
    }

    @Override
    public ArrayList<String> getHelp(CommandSender pSender, String pLabel) {
        ArrayList<String> tHelps = new ArrayList<>();
        if (hasCmdPermission(pSender)) {
            tHelps.add(constructCmdUsage(C("WordPlayer")));
            tHelps.add(this.mExector.getCmdDescPrefix() + C("HelpCmdSave"));
            tHelps.add(constructCmdUsage(C("WordPlayer"), C("WordPlayer")));
            tHelps.add(this.mExector.getCmdDescPrefix() + C("HelpCmdSaveToPlayer"));
        }
        return tHelps;
    }

    @Override
    public ArrayList<String> getTabSubCmd(CommandSender pSender, String pLabel, String[] pArgs) {
        ArrayList<String> tTabs = null;
        if (hasCmdPermission(pSender)) {
            tTabs = new ArrayList<>();
            if (pArgs.length <= 2) {
                tTabs.addAll(BukkitUtil.getOnlinePlayersName());
            }
        }

        return tTabs;
    }

}
