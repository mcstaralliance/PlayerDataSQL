package cc.bukkitPlugin.pds.listener;

import static org.bukkit.event.EventPriority.HIGHEST;
import static org.bukkit.event.EventPriority.LOWEST;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import cc.bukkitPlugin.commons.plugin.AListener;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.user.UserManager;

public class PreventListener extends AListener<PlayerDataSQL>{

    private UserManager mUserMan;

    public PreventListener(PlayerDataSQL pPlugin,UserManager pUserMan){
        super(pPlugin);
        this.mUserMan=pUserMan;

    }

    @EventHandler(priority=LOWEST)
    public void pre(AsyncPlayerChatEvent pEvent){
        this.post(pEvent);
    }

    @EventHandler(ignoreCancelled=true,priority=HIGHEST)
    public void post(AsyncPlayerChatEvent pEvent){
        this.handle(pEvent.getPlayer(),pEvent);
    }

    @EventHandler(priority=LOWEST)
    public void pre(EntityDamageEvent pEvent){
        post(pEvent);
    }

    @EventHandler(ignoreCancelled=true,priority=HIGHEST)
    public void post(EntityDamageEvent pEvent){
        if(pEvent.getEntity() instanceof Player){
            this.handle((Player)pEvent.getEntity(),pEvent);
        }
    }

    @EventHandler(priority=LOWEST)
    public void pre(InventoryClickEvent pEvent){
        post(pEvent);
    }

    @EventHandler(ignoreCancelled=true,priority=HIGHEST)
    public void post(InventoryClickEvent pEvent){
        HumanEntity tWho=pEvent.getWhoClicked();
        if(tWho instanceof Player&&this.mUserMan.isLocked(tWho.getName())){
            pEvent.setCancelled(true);
            tWho.closeInventory();
        }
    }

    @EventHandler(priority=LOWEST)
    public void pre(PlayerPickupItemEvent pEvent){
        this.post(pEvent);
    }

    @EventHandler(ignoreCancelled=true,priority=HIGHEST)
    public void post(PlayerPickupItemEvent pEvent){
        this.handle(pEvent.getPlayer(),pEvent);
    }

    @EventHandler(priority=LOWEST)
    public void pre(PlayerDropItemEvent pEvent){
        this.post(pEvent);
    }

    @EventHandler(ignoreCancelled=true,priority=HIGHEST)
    public void post(PlayerDropItemEvent pEvent){
        this.handle(pEvent.getPlayer(),pEvent);
    }

    @EventHandler(priority=LOWEST)
    public void pre(PlayerInteractEntityEvent pEvent){
        this.post(pEvent);
    }

    @EventHandler(ignoreCancelled=true,priority=HIGHEST)
    public void post(PlayerInteractEntityEvent pEvent){
        this.handle(pEvent.getPlayer(),pEvent);
    }

    @EventHandler(priority=LOWEST)
    public void pre(PlayerInteractEvent pEvent){
        this.post(pEvent);
    }

    @EventHandler(priority=HIGHEST)
    public void post(PlayerInteractEvent pEvent){
        this.handle(pEvent.getPlayer(),pEvent);
    }

    @EventHandler(priority=LOWEST)
    public void pre(PlayerCommandPreprocessEvent pEvent){
        this.post(pEvent);
    }

    @EventHandler(ignoreCancelled=true,priority=HIGHEST)
    public void post(PlayerCommandPreprocessEvent pEvent){
        this.handle(pEvent.getPlayer(),pEvent);
    }

    @EventHandler(priority=LOWEST)
    public void pre(PlayerToggleSneakEvent pEvent){
        this.post(pEvent);
    }

    @EventHandler(ignoreCancelled=true,priority=HIGHEST)
    public void post(PlayerToggleSneakEvent pEvent){
        this.handle(pEvent.getPlayer(),pEvent);
    }

    @EventHandler
    public void handle(PlayerMoveEvent pEvent){
        if(this.mUserMan.isLocked(pEvent.getPlayer().getName())){
            Location tFromLoc=pEvent.getFrom();
            Location tToLoc=pEvent.getTo();
            tFromLoc.setYaw(tToLoc.getYaw());
            tFromLoc.setPitch(tToLoc.getPitch());
            pEvent.setTo(tFromLoc);
        }
    }

    /**
     * 处理事件
     * 
     * @param pUuid
     *            玩家UUID
     * @param pEvent
     *            事件
     * @return 是否取消
     */
    protected boolean handle(OfflinePlayer pPlayer,Cancellable pEvent){
        if(this.mUserMan.isLocked(pPlayer.getName())){
            pEvent.setCancelled(true);
            return true;
        }
        return false;
    }

    public UserManager getManager(){
        return this.mUserMan;
    }

}
