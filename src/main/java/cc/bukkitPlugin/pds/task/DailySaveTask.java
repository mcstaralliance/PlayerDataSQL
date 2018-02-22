package cc.bukkitPlugin.pds.task;

import org.bukkit.Bukkit;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.pds.user.User;
import cc.bukkitPlugin.pds.user.UserManager;
import cc.bukkitPlugin.pds.util.CPlayer;

/**
 * Created on 16-1-4.
 */
public class DailySaveTask implements Runnable{

    public final CPlayer mPlayer;
    private UserManager pPlayer;

    private int mTaskId;
    private int mSaveCount;
    private UserManager mUserMan;

    public DailySaveTask(CPlayer pPlayer,UserManager pUserMan){
        this.mPlayer=pPlayer;
        this.mUserMan=pUserMan;
    }

    @Override
    public synchronized void run(){
        User tUser=this.mUserMan.getUserData(this.mPlayer,false);
        if(tUser==null){
            Log.debug("Cancel task for "+this.mPlayer.getName()+" offline!");
            Bukkit.getScheduler().cancelTask(mTaskId);
        }else{
            this.mSaveCount++;
            Log.debug("Save user "+this.mPlayer.getName()+" count "+this.mSaveCount+'.');
            Bukkit.getScheduler().runTaskAsynchronously(this.mUserMan.getPlugin(),()->this.mUserMan.saveUser(tUser,true));
        }
    }

    public void setTaskId(int taskId){
        this.mTaskId=taskId;
    }

}
