package cc.bukkitPlugin.pds.user;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import org.bukkit.Bukkit;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.storage.IStorage;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.commons.util.FileUtil;

public class UserLockMark {

    public static File LogFile = null;

    private static HashSet<String> Locks = new HashSet<String>();

    public UserLockMark(PlayerDataSQL pPlugin) {

    }

    public static boolean addLockedUser(CPlayer pPlayer) {
        return Locks.add(pPlayer.getUUIDOrName().toLowerCase());
    }

    public static boolean removeLockedUser(CPlayer pPlayer) {
        return Locks.remove(pPlayer.getUUIDOrName().toLowerCase());
    }

    public static boolean isSameLockState(CPlayer pPlayer, boolean pLock) {
        return Locks.contains(pPlayer.getUUIDOrName().toLowerCase()) == pLock;
    }

    public static boolean autoLockState(CPlayer pPlayer, boolean pLock) {
        String tId = pPlayer.getUUIDOrName().toLowerCase();
        if (pLock) return Locks.add(tId);
        else return Locks.remove(tId);
    }

    public static void clear() {
        Locks.clear();
    }

    public static void unlockLoggedUser(PlayerDataSQL pPlugin) {
        String tServerDir = pPlugin.getDataFolder().getAbsoluteFile().getParentFile().getParentFile().getName();
        LogFile = new File(pPlugin.getDataFolder().getAbsolutePath(), "LockUser_" + tServerDir + ".txt");
        String tContent = "";
        try {
            tContent = FileUtil.readContent(LogFile, "UTF-8");
        } catch (IOException e) {
            Log.severe(null, "读取锁定玩家缓存时出错", e);
        }

        IStorage tStorage = pPlugin.getStorage();
        int tAmount_L = 0, tAmount_uL = 0;
        for (String sName : tContent.split("\r?\n")) {
            if ((sName = sName.trim().toLowerCase()).isEmpty()) continue;
            tAmount_L++;
            CPlayer pPlayer = CPlayer.fromNameOrUUID(sName);
            if (pPlayer.isOnline()) continue;

            try {
                tAmount_uL += tStorage.update(pPlayer, new String[]{User.COL_LOCK}, new Object[]{false}) ? 1 : 0;
            } catch (SQLException e) {
                Log.severe(null, "解锁锁定玩家 " + sName + " 缓存时出错", e);
                Locks.add(sName);
            }
        }
        if (tAmount_L + tAmount_uL != 0)
            Log.info("读取了 " + tAmount_L + " 个数据锁定的玩家,实际解锁了 " + tAmount_L + " 个数据锁定的玩家");
    }

    public static boolean save() {
        if(Bukkit.isPrimaryThread()) return save0();
        try {
            return Bukkit.getScheduler().callSyncMethod(PlayerDataSQL.getInstance(), () -> save0()).get();
        } catch (InterruptedException | ExecutionException e) {
            Log.severe("error on invoke save task", e);
            return false;
        }
    }

    protected static boolean save0() {
        try {
            FileUtil.createNewFile(LogFile, true);

            StringBuilder tSBuilder = new StringBuilder();
            for (String sName : Locks) {
                tSBuilder.append(sName).append("\n");
            }

            if (!Locks.isEmpty()) FileUtil.writeData(LogFile, tSBuilder.toString().getBytes("UTF-8"));
            return true;
        } catch (IOException e) {
            Log.severe(null, "保存锁定玩家缓存时出错", e);
        }
        return false;
    }
}
