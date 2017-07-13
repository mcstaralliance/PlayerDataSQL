package cc.bukkitPlugin.pds.user;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.manager.AManager;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.IConfigModel;
import cc.bukkitPlugin.commons.util.BukkitUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.api.IDataModel;
import cc.bukkitPlugin.pds.api.PDSAPI;
import cc.bukkitPlugin.pds.task.DailySaveTask;
import cc.commons.commentedyaml.CommentedYamlConfig;

public class UserManager extends AManager<PlayerDataSQL> implements IConfigModel{

    /** 所有保存的任务 */
    private final Map<String,BukkitTask> mTaskMap;
    private final List<String> mLocked;

    private int mSaveInterval=6000;

    public UserManager(PlayerDataSQL pPlugin){
        super(pPlugin);

        this.mTaskMap=new ConcurrentHashMap<>();
        this.mLocked=new ArrayList<>();

        this.mPlugin.getConfigManager().registerConfigModel(this);
    }

    @Override
    public void addDefaults(CommentedYamlConfig pConfig){
        pConfig.addDefault("Plugin.SaveInterval",this.mSaveInterval,"用户数据保存时间间隔,单位tick,6000tick=5分钟");
    }

    @Override
    public void setConfig(CommandSender pSender,CommentedYamlConfig pConfig){
        this.mSaveInterval=pConfig.getInt("Plugin.SaveInterval",this.mSaveInterval);
    }

    /**
     * 使用数据还原用户
     * 
     * @param pUser
     *            用户数据
     * @param pPlayer
     *            还原的用户
     */
    public void restoreUser(User pUser,Player pPlayer){
        if(Bukkit.isPrimaryThread()){
            this.restoreUser0(pUser,pPlayer);
        }else{
            Bukkit.getScheduler().runTask(this.mPlugin,()->restoreUser0(pUser,pPlayer));
        }
    }

    /**
     * 为用户从数据库载入数据
     * 
     * @param pPlayer
     *            玩家
     * @return 用户数据或null
     * @throws SQLException
     *             读取数据库时发生异常
     */
    public User loadUser(OfflinePlayer pPlayer) throws SQLException{
        try{
            return this.mPlugin.getStorage().get(pPlayer);
        }catch(SQLException exp){
            Log.severe(this.mPlugin.C("MsgErrorOnLoadSQLData","player",pPlayer.getName()),exp);
            throw exp;
        }
    }

    /**
     * 保存当前用户的数据
     * 
     * @param pPlayer
     * @param pLock
     */
    public void saveUser(Player pPlayer,boolean pLock){
        this.saveUser(getUserData(pPlayer,false),pLock);
    }

    /**
     * 保存用户数据
     * 
     * @param pUser
     *            用户数据
     * @param pLock
     *            是否锁定
     */
    public void saveUser(User pUser,boolean pLock){
        pUser.mLocked=pLock;
        try{
            this.mPlugin.getStorage().update(pUser);
        }catch(SQLException exp){
            Log.severe(this.mPlugin.C("MsgErrorOnUpdateSQLData","%player%",pUser.getName()),exp);
            return;
        }
        Log.debug("Save user data "+pUser.getName()+" done!");
    }

    /**
     * 锁定数据库用户数据
     * 
     * @param pPlayer
     *            用户
     */
    public void lockUserData(OfflinePlayer pPlayer){
        boolean tResult=false;
        String tName=pPlayer.getName();

        try{
            tResult=this.mPlugin.getStorage().update(pPlayer,new String[]{User.COL_LOCK},new Object[]{true});
        }catch(SQLException exp){
            Log.severe(this.mPlugin.C("MsgErrorOnUpdateSQLData","%player%",pPlayer.getName()),exp);
        }

        if(tResult){
            Log.debug("Lock user data "+tName+" done.");
        }else{
            Log.debug("Lock user data "+tName+" faid!");
        }

    }

    /**
     * 获取用户当前序列化的数据
     * 
     * @param pPlayer
     *            用户
     * @param pCloseInv
     *            是否关闭背包
     * @return 用户的数据
     */
    public User getUserData(Player pPlayer,boolean pCloseInv){
        if(pCloseInv){
            InventoryView tView=pPlayer.getOpenInventory();
            if(tView!=null&&BukkitUtil.isValidItem(tView.getCursor())){
                ItemStack tCursor=tView.getCursor().clone();
                tView.setCursor(new ItemStack(Material.AIR));
                BukkitUtil.giveItem(pPlayer,tCursor);
            }
            pPlayer.closeInventory();
        }

        User tUser=new User(pPlayer);
        Map<String,byte[]> tDatas=tUser.getDataMap(true);
        for(IDataModel sModel : PDSAPI.getEnableModel()){
            try{
                tDatas.put(sModel.getModelId().toLowerCase(),sModel.getData(pPlayer,tDatas));
            }catch(Throwable exp){
                Log.severe(this.mPlugin.C("MsgModelErrorOnSerializeData",new String[]{"%model%","%player%"},sModel.getDesc(),pPlayer.getName()),exp);
            }
        }

        return tUser;
    }

    /**
     * 还原用户数据
     * <p>
     * 请只在服务器线程调用此方法
     * </p>
     * 
     * @param pUser
     *            用户,包含序列化的数据
     */
    protected void restoreUser0(User pUser,Player pPlayer){
        if(pPlayer!=null&&pPlayer.isOnline()){
            Log.debug("Start restore data for user "+pUser.getName());
            Map<String,byte[]> tDatas=pUser.getDataMap(false);
            for(IDataModel sModel : PDSAPI.getEnableModel()){
                byte[] tData=tDatas.get(sModel.getModelId().toLowerCase());
                if(tData==null) tData=new byte[0];

                try{
                    sModel.restore(pPlayer,tData);
                }catch(Throwable exp){
                    Log.severe(this.mPlugin.C("MsgModelErrorOndeserializeData",new String[]{"%model%","%player%"},sModel.getDesc(),pUser.getName()),exp);
                }
            }
        }else{
            Log.debug("User "+pUser.getName()+" not found!");
        }
    }

    public boolean isLocked(OfflinePlayer pPlayer){
        return this.mLocked.contains(pPlayer.getName().toLowerCase());
    }

    public boolean isNotLocked(OfflinePlayer pPlayer){
        return !this.isLocked(pPlayer);
    }

    public void lockUser(OfflinePlayer pPlayer){
        this.mLocked.add(pPlayer.getName().toLowerCase());
    }

    public void unlockUser(OfflinePlayer pPlayer,boolean pScheduled){
        if(pScheduled){
            Bukkit.getScheduler().runTask(this.mPlugin,()->unlockUser(pPlayer));
        }else{
            this.unlockUser(pPlayer);
        }
    }

    private void unlockUser(OfflinePlayer pPlayer){
        while(this.isLocked(pPlayer)){
            this.mLocked.remove(pPlayer.getName().toLowerCase());
        }
    }

    public void cancelSaveTask(OfflinePlayer pPlayer){
        String tName=pPlayer.getName();
        BukkitTask tTask=this.mTaskMap.remove(tName.toLowerCase());
        if(tTask!=null){
            tTask.cancel();
            Log.debug("Save task canceled for user "+tName+'!');
            Log.debug("Save task canceled for "+tName+'!');
        }
    }

    public void createSaveTask(Player pPlayer){
        if(this.mSaveInterval<=0) return;

        this.lockUserData(pPlayer);
        String tName=pPlayer.getName();

        Log.debug("Scheduling daily save task for user "+tName+'.');
        DailySaveTask tSaveTask=new DailySaveTask(pPlayer,this);
        BukkitTask tTask=Bukkit.getScheduler().runTaskTimer(this.mPlugin,tSaveTask,this.mSaveInterval,this.mSaveInterval);
        tSaveTask.setTaskId(tTask.getTaskId());

        this.cancelSaveTask(pPlayer);
    }

}
