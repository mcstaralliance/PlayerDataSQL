package cc.bukkitPlugin.pds.util;

import java.nio.charset.Charset;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;

public class CPlayer{

    public static final String INVALID_NAME="InvalidUsername";
    public static final UUID INVALID_UUID=UUID.nameUUIDFromBytes(INVALID_NAME.getBytes(Charset.forName("UTF_8")));

    public static CPlayer fromNameOrUUID(String pUUIDOrName){
        try{
            return new CPlayer(UUID.fromString(pUUIDOrName));
        }catch(IllegalArgumentException exp){
            return new CPlayer(pUUIDOrName);
        }
    }

    /** 玩家名字 */
    protected String mName;
    /** 玩家UUID标识 */
    protected UUID mUUID;
    /** 玩家,可能为null */
    protected Player mPlayer=null;
    /** NMS玩家 */
    protected Object mNMSPlayer=null;

    public CPlayer(UUID pPlayer){
        this(pPlayer==null?null:Bukkit.getOfflinePlayer(pPlayer));
    }

    public CPlayer(String pPlayer){
        this(pPlayer==null?null:Bukkit.getOfflinePlayer(pPlayer));
    }

    public CPlayer(OfflinePlayer pPlayer){
        if(pPlayer!=null){
            this.mName=pPlayer.getName();
            this.mUUID=pPlayer.getUniqueId();
            this.mPlayer=pPlayer.getPlayer();
        }else{
            this.mName=INVALID_NAME;
            this.mUUID=INVALID_UUID;
        }
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
     * 根据配置UUID模式来获取玩家名字或UUID
     * 
     * @return UUID或名字
     */
    public String getUUIDOrName(){
        return (PlayerDataSQL.getInstance().getConfigManager().mUUIDMode&&this.mUUID!=INVALID_UUID)?this.mUUID.toString():this.mName;
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
        return this.isValid()?this.mPlayer.getWorld():null;
    }

    public boolean isOnline(){
        return this.getPlayer()!=null&&this.getPlayer().isOnline();
    }

    public boolean isValid(){
        return this.getPlayer()!=null&&this.getUniqueId()!=INVALID_UUID;
    }

    @Override
    public String toString(){
        return this.getName();
    }

}
