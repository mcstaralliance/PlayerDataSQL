package cc.bukkitPlugin.pds.task;

import java.sql.SQLException;

import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.user.User;
import cc.bukkitPlugin.pds.user.UserManager;
import cc.commons.util.ToolKit;

/**
 * Created on 16-1-2.
 */
public class LoadUserTask implements Runnable{

    private final int RETRY_COUNT=5;

    private final String mName;
    private UserManager mUserMan;

    protected int mRetry;
    protected PlayerDataSQL mPlugin;
    protected boolean mDone=false;
    /** 毫秒 */
    private int mInterval;

    public LoadUserTask(Player pForPlayer,UserManager pUserMan){
        this(pForPlayer==null?null:pForPlayer.getName(),pUserMan);
    }

    public LoadUserTask(String pForPlayer,UserManager pUserMan){
        this.mName=pForPlayer;

        this.mUserMan=pUserMan;
        this.mPlugin=pUserMan.getPlugin();
        this.mInterval=ToolKit.between(1,200,this.mPlugin.getConfigManager().mSyncDelay)*50;
    }

    @Override
    public void run(){
        if(this.mName==null) return;

        while(this.mUserMan.isLocked(this.mName)){
            try{
                Thread.sleep(this.mInterval);
            }catch(InterruptedException exp){
                Log.severe(this.mPlugin.C("MsgErrorOnLoadingDataWait"),exp);
            }
            if(this.mDone||this.mRetry>RETRY_COUNT) return;

            User tUser=null;
            try{
                tUser=this.mUserMan.loadUser(this.mName);
            }catch(SQLException exp){
                this.handleExcetion(exp);
                if(this.mDone) return;
                continue;
            }catch(Throwable exp){
                Log.severe("载入玩家 "+this.mName+" 的数据时发生错误",exp);
                this.handleExcetion(exp);
                if(this.mDone) return;
                continue;
            }

            try{
                if((tUser==null||tUser.isLocked())&&(++this.mRetry)<=RETRY_COUNT){
                    Log.debug("Load user data "+this.mName+" fail "+mRetry+(tUser==null?"(no data and wait)":"(Locked)"));
                }else{
                    if(tUser!=null&&tUser.isLocked()){
                        Log.warn("Use locked data to restore user "+this.mName);
                    }
                    this.restoreUser(tUser,false);
                    break;
                }
            }catch(Throwable exp){
                Log.severe(exp);
            }
        }

        if(!this.mDone&&this.mUserMan.isNotLocked(this.mName)){ // maybe player quit the game
            Log.debug("Cancel load data task for "+this.mName+", player may already quit game");
        }

    }

    protected void handleExcetion(Throwable pExp){
        if(this.mPlugin.getConfigManager().mKickOnReadSQLError){
            PlayerDataSQL.kickPlayerOnError(this.mName);
            this.mDone=true;
            return;
        }
        Log.debug("Load user data "+this.mName+" fail "+(++this.mRetry)+'.');
        if(this.mRetry>RETRY_COUNT) this.restoreUser((User)null,true);
    }

    protected void restoreUser(User pUser,boolean pException){
        this.mDone=true;

        boolean tNoRestore=this.mPlugin.getConfigManager().mNoRestoreIfSQLDataNotExist;
        if(pUser==null&&(pException||!tNoRestore)){
            Log.debug("Use blank data restore for player "+this.mName);
            pUser=new User(this.mName);
        }

        if(pUser!=null){
            this.mUserMan.restoreUser(pUser,this.mName);
        }else{
            Log.debug("duce setting NoRestoreIfSQLDataNotExist=true,plugin skip load user data");
        }

        if(this.mUserMan.isLocked(this.mName)){ //skip fo player quit
            this.mUserMan.unlockUser(this.mName,false);
            this.mUserMan.createSaveTask(this.mName);
        }
    }

}
