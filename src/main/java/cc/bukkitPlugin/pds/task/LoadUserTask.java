package cc.bukkitPlugin.pds.task;

import cc.bukkitPlugin.pds.events.PlayerDataLoadCompleteEvent;

import java.util.concurrent.Executors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.user.User;
import cc.bukkitPlugin.pds.user.UserManager;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.commons.util.ToolKit;

/**
 * Created on 16-1-2.
 */
public class LoadUserTask implements Runnable {

    private final int RETRY_COUNT = 5;

    private final CPlayer mPlayer;
    private final String mName;
    private UserManager mUserMan;

    protected int mRetry;
    protected PlayerDataSQL mPlugin;
    protected boolean mDone = false;
    /** 毫秒 */
    private int mInterval;

    public LoadUserTask(Player pForPlayer, UserManager pUserMan) {
        this(new CPlayer(pForPlayer), pUserMan);
    }

    public LoadUserTask(String pForPlayer, UserManager pUserMan) {
        this(new CPlayer(pForPlayer), pUserMan);
    }

    public LoadUserTask(CPlayer pForPlayer, UserManager pUserMan) {
        this.mPlayer = pForPlayer;
        this.mName = pForPlayer.getName();

        this.mUserMan = pUserMan;
        this.mPlugin = pUserMan.getPlugin();
        this.mInterval = ToolKit.between(1, 200, this.mPlugin.getConfigManager().mSyncDelay) * 50;
    }

    @Override
    public void run() {
        if (!this.mPlayer.isValid()) return;

        while (this.mUserMan.isLocked(this.mPlayer.getPlayer())) {
            try {
                Thread.sleep(this.mInterval);
            } catch (InterruptedException exp) {
                Log.severe(this.mPlugin.C("MsgErrorOnLoadingDataWait"), exp);
            }
            if (this.mDone || this.mRetry > RETRY_COUNT || !this.mPlugin.isEnabled()) return;
            ++this.mRetry;

            User tUser = null;
            try {
                tUser = this.mUserMan.loadUser(this.mPlayer);
            } catch (Throwable exp) {
                Log.severe("载入玩家 " + this.mName + " 的数据时发生错误", exp);
                if (this.tryKickIfError()) return;
                continue;
            }

            try {
                if ((tUser == null || tUser.isLocked()) && this.mRetry <= RETRY_COUNT) {
                    Log.debug("Load user data " + this.mName + " fail " + mRetry + (tUser == null ? "(no data and wait)" : "(Locked)"));
                } else {
                    if (tUser != null && tUser.isLocked()) {
                        if (!this.mPlugin.getConfigManager().mForceUseLockData) {
                            if (this.tryKickIfError()) {
                                Log.info("用户数据锁定,踢出玩家");
                                return;
                            }
                        }
                        Log.warn("Use locked data to restore user " + this.mName);
                    }
                    this.restoreUser(tUser, false);
                    Bukkit.getScheduler().callSyncMethod(this.mPlugin, Executors.callable(()->
                            Bukkit.getPluginManager().callEvent(new PlayerDataLoadCompleteEvent(this.mPlayer.getPlayer()))));
                    break;
                }
            } catch (Throwable exp) {
                Log.severe(exp);
            }
        }

        if (!this.mDone && !this.mUserMan.isLocked(this.mPlayer.getPlayer())) { // maybe player quit the game
            Log.debug("Cancel load data task for " + this.mName + ", player may already quit game");
        }

    }

    /**
     * 根据配置,如果数据读取错误,踢出玩家
     * 
     * @return 是否踢出玩家
     */
    protected boolean tryKickIfError() {
        if (this.mPlugin.getConfigManager().mKickOnReadSQLError) {
            PlayerDataSQL.kickPlayerOnError(this.mPlayer);
            this.mDone = true;
            return true;
        }
        return false;
    }

    protected void restoreUser(User pUser, boolean pException) {
        this.mDone = true;

        boolean tNoRestore = this.mPlugin.getConfigManager().mNoRestoreIfSQLDataNotExist;
        if (pUser == null && (pException || !tNoRestore)) {
            Log.debug("Use blank data restore for player " + this.mName);
            pUser = new User(this.mPlayer);
        }

        if (pUser != null) {
            this.mUserMan.restoreUser(this.mPlayer, pUser);
        } else {
            Log.debug("duce setting NoRestoreIfSQLDataNotExist=true,plugin skip load user data");
        }

        if (this.mUserMan.isLocked(this.mPlayer.getPlayer())) { //skip fo player quit
            this.mUserMan.createSaveTask(this.mPlayer);
        }
    }

}
