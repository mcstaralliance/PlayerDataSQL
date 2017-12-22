package cc.bukkitPlugin.pds.util;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.nmsutil.NMSUtil;

public class CPlayer{

    /** 玩家名字 */
    protected final String mName;
    /** 玩家UUID标识 */
    protected final UUID mUUID;
    /** 玩家,可能为null */
    protected Player mPlayer=null;
    /** NMS玩家 */
    protected Object mNMSPlayer=null;

    public CPlayer(OfflinePlayer pPlayer){
        this.mName=pPlayer.getName();
        this.mUUID=pPlayer.getUniqueId();
        this.mPlayer=pPlayer.getPlayer();
    }

    public CPlayer(Player pPlayer){
        this((OfflinePlayer)pPlayer);
        this.mPlayer=pPlayer;
    }

    public String getName(){
        return this.mName;
    }

    public UUID getUniqueId(){
        return this.mUUID;
    }

    /**
     * 获取玩家,可能为null
     * 
     * @return 玩家
     */
    public Player getPlayer(){
        return this.mPlayer;
    }

    /**
     * 获取Minecraft的玩家
     * 
     * @return Minecraft的玩家
     */
    public Object getNMSPlayer(){
        if(this.mNMSPlayer==null&&this.mPlayer!=null){
            this.mNMSPlayer=NMSUtil.getNMSPlayer(this.mPlayer);
        }

        return this.mNMSPlayer;
    }

    public World getWorld(){
        return this.mPlayer==null?null:this.mPlayer.getWorld();
    }

}
