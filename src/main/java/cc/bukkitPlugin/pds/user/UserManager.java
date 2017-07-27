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
    public void restoreUser(User pUser,String pPlayer){
        this.restoreUser(pUser,pPlayer,(CommandSender)null);
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
    public void restoreUser(User pUser,String pPlayer,CommandSender pReciver){
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
    public User loadUser(String pPlayer) throws SQLException{
        try{
            return this.mPlugin.getStorage().get(pPlayer);
        }catch(SQLException exp){
            Log.severe(this.mPlugin.C("MsgErrorOnLoadSQLData","player",pPlayer),exp);
            throw exp;
        }
    }

    /**
     * 保存当前用户的数据
     * 
     * @param pPlayer
     * @param pLock
     */
    public boolean saveUser(Player pPlayer,boolean pLock){
        return this.saveUser(getUserData(pPlayer,false),pLock);
    }

    /**
     * 保存用户数据
     * 
     * @param pUser
     *            用户数据
     * @param pLock
     *            是否锁定
     */
    public boolean saveUser(User pUser,boolean pLock){
        return this.saveUser(pUser,pLock,(CommandSender)null);
    }

    /**
     * 保存用户数据
     * 
     * @param pUser
     *            用户数据
     * @param pLock
     *            是否锁定
     * @param pReciver
     *            消息接收者
     */
    public boolean saveUser(User pUser,boolean pLock,CommandSender pReciver){
        pUser.mLocked=pLock;
        boolean tSuccess;
        try{
            tSuccess=this.mPlugin.getStorage().update(pUser);
        }catch(SQLException exp){
            Log.severe(this.mPlugin.C("MsgErrorOnUpdateSQLData","%player%",pUser.getName()),exp);
            return false;
        }
        Log.debug("Save user data "+pUser.getName()+" done!");
        return tSuccess;
    }

    /**
     * 锁定数据库用户数据
     * 
     * @param pPlayer
     *            用户
     */
    public void lockUserData(String pPlayer){
        if(Bukkit.isPrimaryThread()){
            Bukkit.getScheduler().runTask(this.mPlugin,()->this.lockUserData0(pPlayer));
        }else{
            this.lockUserData0(null);
        }
    }

    public void lockUserData0(String pPlayer){
        boolean tResult=false;

        try{
            tResult=this.mPlugin.getStorage().update(pPlayer,new String[]{User.COL_LOCK},new Object[]{true});
        }catch(SQLException exp){
            Log.severe(this.mPlugin.C("MsgErrorOnUpdateSQLData","%player%",pPlayer),exp);
        }

        if(tResult){
            Log.debug("Lock user data "+pPlayer+" done.");
        }else{
            Log.debug("Lock user data "+pPlayer+" faid, may be no data in db !");
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
    public User getUserData(Player pPlayer,boolean pCloseInv,CommandSender pReciver){
        if(pPlayer==null||!pPlayer.isOnline()) return null;
        // if(!Bukkit.isPrimaryThread()) throw new IllegalStateException("请勿在异步线程调用此方法");

        if(pCloseInv){
            InventoryView tView=pPlayer.getOpenInventory();
            if(tView!=null&&BukkitUtil.isValidItem(tView.getCursor())){
                ItemStack tCursor=tView.getCursor().clone();
                tView.setCursor(new ItemStack(Material.AIR));
                BukkitUtil.giveItem(pPlayer,tCursor);
            }
            pPlayer.closeInventory();
        }

        User tUser=new User(pPlayer.getName());
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
    protected void restoreUser0(User pUser,String pPlayer,CommandSender pReciver){
        Player tPlayer=Bukkit.getPlayerExact(pPlayer);
        if(tPlayer!=null&&tPlayer.isOnline()){
            Log.debug("Start restore data for user "+pUser.getName());
            Map<String,byte[]> tDatas=pUser.getDataMap(false);
            for(IDataModel sModel : PDSAPI.getEnableModel()){
                byte[] tData=tDatas.get(sModel.getModelId().toLowerCase());
                if(tData==null) tData=new byte[0];

                try{
                    sModel.restore(tPlayer,tData);
                }catch(Throwable exp){
                    Log.severe(pReciver,this.mPlugin.C("MsgModelErrorOndeserializeData",new String[]{"%model%","%player%"},sModel.getDesc(),pUser.getName()),exp);
                }
            }
        }else{
            Log.debug("User "+pUser.getName()+" not online! cancel restore");
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

    public void createSaveTask(String pPlayer){
        if(this.mSaveInterval<=0) return;

        if(Bukkit.isPrimaryThread()){
            this.createSaveTask0(pPlayer);
        }else{
            Bukkit.getScheduler().runTask(this.mPlugin,()->this.createSaveTask0(pPlayer));
        }
    }

    protected void createSaveTask0(String pPlayer){
        this.lockUserData(pPlayer);

        Log.debug("Scheduling daily save task for user "+pPlayer+'.');
        DailySaveTask tSaveTask=new DailySaveTask(pPlayer,this);
        BukkitTask tTask=Bukkit.getScheduler().runTaskTimer(this.mPlugin,tSaveTask,this.mSaveInterval,this.mSaveInterval);
        tSaveTask.setTaskId(tTask.getTaskId());
        tTask=this.mTaskMap.put(pPlayer.toLowerCase(),tTask);
        if(tTask!=null){
            tTask.cancel();
            Log.debug("Old save task canceled for "+pPlayer+'!');
        }
    }

}
