package cc.bukkitPlugin.pds.user;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import cc.bukkitPlugin.pds.util.CPlayer;
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
     */
    public void restoreUser(User pUser){
        this.restoreUser(pUser.getOwner(),pUser,(CommandSender)null);
    }

    /**
     * 使用数据还原用户
     * 
     * @param pUser
     *            用户数据
     * @param pPlayer
     *            还原的用户
     */
    public void restoreUser(CPlayer pPlayer,User pUser){
        this.restoreUser(pPlayer,pUser,(CommandSender)null);
    }

    /**
     * 使用数据还原用户
     * 
     * @param pUser
     *            用户数据
     * @param pPlayer
     *            还原的用户
     * @param pReciver
     *            消息接收者
     */
    public void restoreUser(CPlayer pPlayer,User pUser,CommandSender pReciver){
        if(Bukkit.isPrimaryThread()){
            this.restoreUser0(pUser,pPlayer,pReciver);
        }else{
            Bukkit.getScheduler().runTask(this.mPlugin,()->restoreUser0(pUser,pPlayer,pReciver));
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
    public User loadUser(CPlayer pPlayer) throws SQLException{
        try{
            return this.mPlugin.getStorage().get(pPlayer);
        }catch(SQLException exp){
            Log.severe(this.mPlugin.C("MsgErrorOnLoadSQLData","%player%",pPlayer),exp);
            throw exp;
        }
    }

    /**
     * 保存当前用户的数据
     * 
     * @param pPlayer
     * @param pLock
     */
    public boolean saveUser(User pData,boolean pLock){
        return this.saveUser(pData,pLock,(CommandSender)null);
    }

    /**
     * 保存当前用户的数据
     * 
     * @param pPlayer
     * @param pLock
     */
    public boolean saveUser(User pData,boolean pLock,CommandSender pSender){
        return this.saveUser(pData.getOwner(),pData,pLock);
    }

    /**
     * 保存当前用户的数据
     * 
     * @param pPlayer
     * @param pLock
     */
    public boolean saveUser(CPlayer pPlayer,boolean pLock){
        return this.saveUser(pPlayer,getUserData(pPlayer,false),pLock);
    }

    /**
     * 保存用户数据
     * 
     * @param pPlayer
     *            保存数据到谁
     * @param pUser
     *            用户数据
     * @param pLock
     *            是否锁定
     */
    public boolean saveUser(CPlayer pPlayer,User pUser,boolean pLock){
        return this.saveUser(pPlayer,pUser,pLock,(CommandSender)null);
    }

    /**
     * 保存用户数据
     * 
     * @param pPlayer
     *            保存数据到谁
     * @param pUser
     *            用户数据
     * @param pLock
     *            是否锁定
     * @param pReciver
     *            消息接收者
     */
    public boolean saveUser(CPlayer pPlayer,User pUser,boolean pLock,CommandSender pReciver){
        pUser.mLocked=pLock;
        boolean tSuccess;
        try{
            tSuccess=this.mPlugin.getStorage().update(pPlayer,pUser);
        }catch(SQLException exp){
            Log.severe(this.mPlugin.C("MsgErrorOnUpdateSQLData","%player%",pUser.getOwnerName()),exp);
            return false;
        }
        Log.debug("Save user data "+pUser.getOwnerName()+" done!");
        return tSuccess;
    }

    /**
     * 锁定数据库用户数据
     * 
     * @param pPlayer
     *            用户
     */
    public void lockUserData(CPlayer pPlayer){
        if(Bukkit.isPrimaryThread()){
            Bukkit.getScheduler().runTask(this.mPlugin,()->this.lockUserData0(pPlayer));
        }else{
            this.lockUserData0(pPlayer);
        }
    }

    public void lockUserData0(CPlayer pPlayer){
        boolean tResult=false;

        try{
            tResult=this.mPlugin.getStorage().update(pPlayer,new String[]{User.COL_LOCK},new Object[]{true});
        }catch(SQLException exp){
            Log.severe(this.mPlugin.C("MsgErrorOnUpdateSQLData","%player%",pPlayer),exp);
        }

        if(tResult){
            Log.debug("Lock user data "+pPlayer.getName()+" done.");
        }else{
            Log.debug("Lock user data "+pPlayer.getName()+" faid, may be no data in db !");
        }
    }

    /**
     * 根据获取所有用户数据
     *
     * @throws SQLException
     *             读写数据库时发生异常
     */
    public ArrayList<User> getall() throws SQLException{
        ArrayList<User> users=new ArrayList(Bukkit.getOnlinePlayers().size());
        for(Player p : Bukkit.getOnlinePlayers()){
            users.add(getUserData(new CPlayer(p),true));
        }
        users.addAll(this.mPlugin.getStorage().getall());
        return users;
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
    public User getUserData(CPlayer pPlayer,boolean pCloseInv){
        return this.getUserData(pPlayer,pCloseInv,(CommandSender)null);
    }

    /**
     * 获取用户当前序列化的数据,请只在主线程调用
     * 
     * @param pPlayer
     *            用户
     * @param pCloseInv
     *            是否关闭背包
     * @param pReciver
     *            消息接收者
     * @return 用户的数据
     */
    public User getUserData(CPlayer pPlayer,boolean pCloseInv,CommandSender pReciver){
        if(!pPlayer.isOnline()) return null;
        // if(!Bukkit.isPrimaryThread()) throw new IllegalStateException("请勿在异步线程调用此方法");

        if(pCloseInv){
            Player tBPlayer=pPlayer.getPlayer();
            InventoryView tView=tBPlayer.getOpenInventory();
            if(tView!=null&&BukkitUtil.isValidItem(tView.getCursor())){
                ItemStack tCursor=tView.getCursor().clone();
                tView.setCursor(new ItemStack(Material.AIR));
                BukkitUtil.giveItem(tBPlayer,tCursor);
            }
            tBPlayer.closeInventory();
        }

        User tUser=new User(pPlayer);
        Map<String,byte[]> tDatas=tUser.getDataMap(true);
        for(IDataModel sModel : PDSAPI.getEnableModel()){
            try{
                tDatas.put(sModel.getModelId().toLowerCase(),sModel.getData(pPlayer,tDatas));
            }catch(Throwable exp){
                Log.severe(pReciver,this.mPlugin.C("MsgModelErrorOnSerializeData",new String[]{"%model%","%player%"},sModel.getDesc(),pPlayer.getName()),exp);
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
    protected void restoreUser0(User pUser,CPlayer pPlayer,CommandSender pReciver){
        Player tPlayer=pPlayer.getPlayer();
        if(tPlayer!=null&&tPlayer.isOnline()){
            Log.debug("Start restore data for user "+pPlayer.getName());
            Map<String,byte[]> tDatas=pUser.getDataMap(false);
            for(IDataModel sModel : PDSAPI.getEnableModel()){
                byte[] tData=tDatas.get(sModel.getModelId().toLowerCase());
                if(tData==null){
                    if(this.mPlugin.getConfigManager().mNoRestoreIfSQLDataNotExist) continue;
                    tData=new byte[0];
                }

                try{
                    sModel.restore(pPlayer,tData);
                }catch(Throwable exp){
                    Log.severe(pReciver,this.mPlugin.C("MsgModelErrorOndeserializeData",new String[]{"%model%","%player%"},sModel.getDesc(),pUser.getOwnerName()),exp);
                }
            }
        }else{
            Log.debug("User "+pPlayer.getName()+" not online! cancel restore");
        }
    }

    public boolean isLocked(String pPlayer){
        return this.mLocked.contains(pPlayer.toLowerCase());
    }

    public boolean isNotLocked(String pPlayer){
        return !this.isLocked(pPlayer);
    }

    public void lockUser(String pPlayer){
        this.mLocked.add(pPlayer.toLowerCase());
    }

    public void unlockUser(String pPlayer,boolean pScheduled){
        if(pScheduled){
            Bukkit.getScheduler().runTask(this.mPlugin,()->unlockUser(pPlayer));
        }else{
            this.unlockUser(pPlayer);
        }
    }

    private void unlockUser(String pPlayer){
        while(this.isLocked(pPlayer)){
            this.mLocked.remove(pPlayer.toLowerCase());
        }
    }

    public void cancelSaveTask(String pPlayer){
        BukkitTask tTask=this.mTaskMap.remove(pPlayer.toLowerCase());
        if(tTask!=null){
            tTask.cancel();
            Log.debug("Save task canceled for "+pPlayer+'!');
        }
    }

    public void createSaveTask(CPlayer pPlayer){
        if(this.mSaveInterval<=0) return;

        if(Bukkit.isPrimaryThread()){
            this.createSaveTask0(pPlayer);
        }else{
            Bukkit.getScheduler().runTask(this.mPlugin,()->this.createSaveTask0(pPlayer));
        }
    }

    protected void createSaveTask0(CPlayer pPlayer){
        this.lockUserData(pPlayer);

        Log.debug("Scheduling daily save task for user "+pPlayer+'.');
        DailySaveTask tSaveTask=new DailySaveTask(pPlayer,this);
        BukkitTask tTask=Bukkit.getScheduler().runTaskTimer(this.mPlugin,tSaveTask,this.mSaveInterval,this.mSaveInterval);
        tSaveTask.setTaskId(tTask.getTaskId());
        tTask=this.mTaskMap.put(pPlayer.getName().toLowerCase(),tTask);
        if(tTask!=null){
            tTask.cancel();
            Log.debug("Old save task canceled for "+pPlayer+'!');
        }
    }

    /**
     * 清理/清空玩家所有模块的数据
     * 
     * @param pPlayer
     *            玩家
     */
    public void cleanPlayerData(Player pPlayer){
        CPlayer tPlayer=new CPlayer(pPlayer);
        for(IDataModel sModel : PDSAPI.getEnableModel()){
            try{
                sModel.cleanData(tPlayer);
            }catch(Throwable exp){
                Log.severe(this.mPlugin.C("MsgModelErrorOnClearData",new String[]{"%model%","%player%"},sModel.getDesc(),pPlayer.getName()),exp);
            }
        }
    }

}
