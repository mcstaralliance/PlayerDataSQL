package cc.bukkitPlugin.pds.listener;

import static org.bukkit.event.EventPriority.MONITOR;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.AListener;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.task.LoadUserTask;
import cc.bukkitPlugin.pds.user.User;
import cc.bukkitPlugin.pds.user.UserManager;
import cc.bukkitPlugin.pds.util.CPlayer;

public class PlayerListener extends AListener<PlayerDataSQL> {

    private UserManager mUserMan;
    /** 用于存储已关闭的背包,防止其他插件重复关闭背包导致的刷物品 */
    public static final Set<Inventory> ClosedInvs = Collections.newSetFromMap(new WeakHashMap<Inventory, Boolean>());

    public PlayerListener(PlayerDataSQL pPlugin, UserManager pUserMan) {
        super(pPlugin);

        this.mUserMan = pUserMan;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent pEvent) {
        Player tPlayer = pEvent.getPlayer();
        Log.debug("Lock user " + tPlayer.getName() + " done!");
        this.mUserMan.lockUser(tPlayer);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent pEvent) {
        if (!this.mPlugin.getConfigManager().mNoRestoreIfSQLDataNotExist) {
            this.mUserMan.cleanPlayerData(pEvent.getPlayer());
        }
        Bukkit.getScheduler().runTaskAsynchronously(this.mPlugin, new LoadUserTask(pEvent.getPlayer(), this.mUserMan));
    }

    @EventHandler(priority = MONITOR)
    public void onQuit(PlayerQuitEvent pEvent) {
        Player tPlayer = pEvent.getPlayer();
        Log.debug("Handle player quit");
        if (this.mUserMan.isNotLocked(tPlayer)) {
            this.mUserMan.cancelSaveTask(tPlayer);
            User tUser = this.mUserMan.getUserData(new CPlayer(pEvent.getPlayer()), true);
            Bukkit.getScheduler().runTaskAsynchronously(this.mPlugin, () -> {
                int i = 3;
                do {
                    if (this.mUserMan.saveUser(tUser, false)) break;
                } while (--i > 0);
                if (i <= 0) {
                    Log.debug("Fail to save player data,try times 3!");
                }
                this.mUserMan.unlockUser(tPlayer, true);
            });
        } else this.mUserMan.unlockUser(tPlayer, false);
    }

    @EventHandler(priority = MONITOR)
    public void onInvClose(InventoryCloseEvent pEvent) {
        ClosedInvs.add(pEvent.getInventory());
    }

}
