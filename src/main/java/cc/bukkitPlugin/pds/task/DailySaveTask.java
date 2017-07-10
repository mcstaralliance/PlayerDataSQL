package cc.bukkitPlugin.pds.task;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.pds.user.User;
import cc.bukkitPlugin.pds.user.UserManager;

/**
 * Created on 16-1-4.
 */
public class DailySaveTask implements Runnable{

    public final OfflinePlayer mPlayer;
    private String mName;
    private UserManager mUserMan;

    private int mTaskId;
    private int mSaveCount;

    public DailySaveTask(OfflinePlayer pPlayer,UserManager pUserMan){
        this.mPlayer=pPlayer;
        this.mName=pPlayer.getName();
        this.mUserMan=pUserMan;
    }

    @Override
    public synchronized void run(){
        User tUser=this.mUserMan.getUserData(this.mPlayer,false);
        if(tUser==null){
            Log.debug("Cancel task for "+this.mName+" offline!");
            Bukkit.getScheduler().cancelTask(mTaskId);
        }else{
            this.mSaveCount++;
            Log.debug("Save user "+this.mName+" count "+this.mSaveCount+'.');
            Bukkit.getScheduler().runTaskAsynchronously(this.mUserMan.getPlugin(),()->this.mUserMan.saveUser(tUser,true));
        }
    }

    public void setTaskId(int taskId){
        this.mTaskId=taskId;
    }

}
