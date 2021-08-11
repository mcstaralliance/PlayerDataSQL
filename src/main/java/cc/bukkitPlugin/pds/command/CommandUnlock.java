package cc.bukkitPlugin.pds.command;

import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.command.TACommandBase;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.user.User;
import cc.bukkitPlugin.pds.util.CPlayer;

public class CommandUnlock extends TACommandBase<PlayerDataSQL, CommandExc> {

    public CommandUnlock(CommandExc pExector) {
        super(pExector, 1);
    }

    @Override
    public boolean execute(CommandSender pSender, String pLabel, String[] pArgs) {
        if (!hasCmdPermission(pSender))
            return noPermission(pSender, this.mLastConstructPermisson);

        if (pArgs.length == 0) return help(pSender, pLabel);
        if (pArgs.length > 2) return errorArgsNumber(pSender, pArgs.length);

        CPlayer tPlayer = CPlayer.fromNameOrUUID(pArgs[0]);
        if (tPlayer.isOnline()) return send(pSender, C("MsgOnlinePlayerNoUnlock"));

        Bukkit.getScheduler().runTaskAsynchronously(this.mPlugin, () -> {
            try {
                int tResult = 0;
                if (pArgs[0].equals("*")) {
                    tResult = this.mPlugin.getStorage().update("UPDATE %table_name% SET "
                            + User.COL_LOCK + "= false");
                } else {
                    tResult = this.mPlugin.getStorage().update(tPlayer, new String[]{User.COL_LOCK}, new Object[]{false}) ? 1 : 0;
                }

                int tRT = tResult;
                Bukkit.getScheduler().scheduleSyncDelayedTask(this.mPlugin, () -> {
                    if (tRT == 0) Log.info(pSender, C("MsgUnlockNoChange"));
                    else Log.info(pSender, C("MsgUnlockedPlayer","%amount%",tRT));
                });
            } catch (SQLException e) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(this.mPlugin, () -> {
                    Log.severe(null, C("MsgErrorOnUnlockPlayer"), e);
                    if (pSender instanceof Player) send(pSender, C("MsgErrorOnUnlockPlayer"));
                });
            }
        });

        return true;
    }

    @Override
    public ArrayList<String> getHelp(CommandSender pSender, String pLabel) {
        ArrayList<String> tHelps = new ArrayList<>();
        if (hasCmdPermission(pSender)) {
            tHelps.add(constructCmdUsage("<"+C("WordPlayer")+"|*>"));
            tHelps.add(this.mExector.getCmdDescPrefix() + C("HelpCmdUnlock"));
        }
        return tHelps;
    }

}
