package cc.bukkitPlugin.pds.task;

import java.sql.SQLException;

import org.bukkit.Bukkit;
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
    /** 载入数据到 */
    protected final Player mLoadFor;

    protected boolean mDone=false;

    public LoadUserTask(Player pForPlayer,UserManager pUserMan){
        this.mName=pForPlayer.getName();
        this.mLoadFor=pForPlayer;

        this.mUserMan=pUserMan;
        this.mPlugin=pUserMan.getPlugin();
    }

    @Override
    public synchronized void run(){
        if(this.mLoadFor==null||!this.mLoadFor.isOnline()||this.mDone){
            this.cancelTask();
            return;
        }

        User tUser=null;

        try{
            tUser=this.mUserMan.loadUser(this.mLoadFor);
        }catch(SQLException exp){

            this.handleExcetion(exp);
            return;
        }catch(Throwable exp){
            Log.severe("载入玩家 "+this.mName+" 的数据时发生错误",exp);
            this.handleExcetion(exp);
            return;
        }

        if((tUser==null||tUser.isLocked())&&(++this.mRetry)<=RETRY_COUNT){
            Log.debug("Load user data "+this.mName+" fail "+mRetry+(tUser==null?"(no data and wait)":"(Locked)"));
        }else{
            this.restoreUser(tUser,false);
        }

    }

    protected void handleExcetion(Throwable pExp){
        if(this.mPlugin.getConfigManager().mKickOnReadSQLError){
            this.mLoadFor.kickPlayer(this.mPlugin.C("MsgDataExpection"));
            this.cancelTask();
            return;
        }
        Log.debug("Load user data "+this.mName+" fail "+(++this.mRetry)+'.');
        if(this.mRetry>RETRY_COUNT) this.restoreUser((User)null,true);
    }

    protected void restoreUser(User pUser,boolean pException){
        this.cancelTask();

        boolean tNoRestore=this.mPlugin.getConfigManager().mNoRestoreIfSQLDataNotExist;
        if(pUser==null&&(pException||!tNoRestore)){
            Log.debug("Use blank data restore for player "+this.mName);
            pUser=new User(this.mLoadFor);
        }

        if(pUser!=null){
            this.mUserMan.restoreUser(pUser,this.mLoadFor);
        }

        this.mUserMan.unlockUser(this.mLoadFor,false);
        this.mUserMan.lockUserData(this.mLoadFor);
        this.mUserMan.createSaveTask(this.mLoadFor);
    }

    protected void cancelTask(){
        Bukkit.getScheduler().cancelTask(this.mTaskId);
        this.mDone=true;
    }

    public void setTaskId(int taskId){
        this.mTaskId=taskId;
    }

}
