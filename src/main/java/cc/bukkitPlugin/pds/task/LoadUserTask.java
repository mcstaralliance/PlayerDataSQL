package cc.bukkitPlugin.pds.task;

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.user.User;
import cc.bukkitPlugin.pds.user.UserManager;

/**
 * Created on 16-1-2.
 */
public class LoadUserTask implements Runnable{

    private final int RETRY_COUNT=5;

    private final String mName;
    private UserManager mUserMan;

    private int mTaskId;
    protected int mRetry;

    protected PlayerDataSQL mPlugin;
    protected final Player mPlayer;

    public LoadUserTask(OfflinePlayer pPlayer,UserManager pUserMan){
        this.mName=pPlayer.getName();
        this.mUserMan=pUserMan;

        this.mPlugin=pUserMan.getPlugin();
        this.mPlayer=pPlayer.getPlayer();
    }

    @Override
    public synchronized void run(){
        if(this.mPlayer==null||!this.mPlayer.isOnline()){
            this.cancelTask();
            return;
        }

        User tUser=null;

        try{
            tUser=this.mUserMan.loadUser(this.mPlayer);
        }catch(SQLException exp){
            if(this.mPlugin.getConfigManager().isKickOnSQLReadError()){
                this.mPlayer.kickPlayer(this.mPlugin.C("MsgDataExpection"));
                this.cancelTask();
            }
            this.handleExcetion(exp);
            return;
        }catch(Throwable exp){
            Log.severe("载入玩家 "+this.mName+" 的数据时发生错误",exp);
            this.handleExcetion(exp);
            return;
        }

        if((tUser==null||tUser.isLocked())&&(++this.mRetry)<RETRY_COUNT){
            Log.debug("Load user data "+this.mName+" fail "+mRetry+(tUser==null?"(no data and wait)":"(Locked)"));
        }else{
            this.restoreUser(tUser);
        }

    }

    protected void handleExcetion(Throwable pExp){
        Log.debug("Load user data "+this.mName+" fail "+(++this.mRetry)+'.');
        if(this.mRetry>RETRY_COUNT) this.restoreUser((User)null);
    }

    protected void restoreUser(User pUser){
        Bukkit.getScheduler().cancelTask(this.mTaskId);

        if(pUser==null){
            Log.debug("Use blank data restore for player "+this.mName);
            pUser=new User(this.mPlayer);
        }

        this.mUserMan.restoreUser(pUser);
        Log.debug("Load user data "+this.mName+" done.");
        this.mUserMan.lockUserData(this.mPlayer);
    }

    protected void cancelTask(){
        Bukkit.getScheduler().cancelTask(this.mTaskId);
    }

    public void setTaskId(int taskId){
        this.mTaskId=taskId;
    }

}
