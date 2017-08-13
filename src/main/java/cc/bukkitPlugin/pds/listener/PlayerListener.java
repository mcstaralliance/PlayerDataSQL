package cc.bukkitPlugin.pds.listener;

import static org.bukkit.event.EventPriority.MONITOR;

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.AListener;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.task.LoadUserTask;
import cc.bukkitPlugin.pds.user.User;
import cc.bukkitPlugin.pds.user.UserManager;

public class PlayerListener extends AListener<PlayerDataSQL>{

    private UserManager mUserMan;

    public PlayerListener(PlayerDataSQL pPlugin,UserManager pUserMan){
        super(pPlugin);

        this.mUserMan=pUserMan;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent pEvent){
        Player tPlayer=pEvent.getPlayer();
        Log.debug("Lock user "+tPlayer.getName()+" done!");
        this.mUserMan.lockUser(tPlayer.getName());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent pEvent){
        this.mUserMan.cleanPlayerData(pEvent.getPlayer());
        Bukkit.getScheduler().runTaskAsynchronously(this.mPlugin,new LoadUserTask(pEvent.getPlayer(),this.mUserMan));
    }

    @EventHandler(priority=MONITOR)
    public void onQuit(PlayerQuitEvent pEvent){
        String tPlayer=pEvent.getPlayer().getName();
        Log.debug("Handle player quit");
        if(this.mUserMan.isNotLocked(tPlayer)){
            this.mUserMan.cancelSaveTask(tPlayer);
            User tUser=this.mUserMan.getUserData(pEvent.getPlayer(),true);
            Bukkit.getScheduler().runTaskAsynchronously(this.mPlugin,()->{
                int i=3;
                do{
                    if(this.mUserMan.saveUser(tUser,false)) break;
                }while(--i>0);
                if(i<=0){
                    Log.debug("Fail to save player data,try times 3!");
                }
                this.mUserMan.unlockUser(tPlayer,true);
            });
        }else{
            this.mUserMan.unlockUser(tPlayer,false);

            // Data not loaded, check if is use file data import
            User tUser=this.mUserMan.getUserData(pEvent.getPlayer(),true);
            Bukkit.getScheduler().runTaskAsynchronously(this.mPlugin,()->{
                int i=3;
                while(i-->0){
                    try{
                        if(this.mUserMan.loadUser(tPlayer)==null){
                            if(!this.mPlugin.getConfigManager().mNoRestoreIfSQLDataNotExist){
                                tUser.getDataMap(true).clear();
                            }
                            if(this.mUserMan.saveUser(tUser,false)){
                                break;
                            }
                        }
                    }catch(SQLException ignore){
                    }
                }
                if(i<0){
                    Log.warn("Error! fail to op database, this may cause a playerdata reset fo user "+tPlayer);
                }
            });

        }
    }

}
