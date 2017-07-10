package cc.bukkitPlugin.pds.dmodel;

import java.io.File;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.api.IDataModel;

public abstract class ADataModel implements IDataModel{

    protected PlayerDataSQL mPlugin;

    protected File mServerDir;
    protected File mPlayerDataDir;

    public ADataModel(PlayerDataSQL pPlugin){
        this.mPlugin=pPlugin;

        this.mServerDir=pPlugin.getDataFolder().getAbsoluteFile().getParentFile().getParentFile();
        this.mPlayerDataDir=new File(this.mServerDir,"world"+File.separator+"playerdata");
    }

    @Override
    public Plugin getPlugin(){
        return this.mPlugin;
    }

    /**
     * 获取以UUID或者玩家名字为模式命令的数据文件
     * 
     * @param pPlayer
     *            玩家
     * @param pDir
     *            数据文件夹
     * @param pNameParam
     *            文件名模式,%name%为被替换的UUID或Name参数
     * @return 玩家数据文件
     */
    public File getUUIDOrNameFile(OfflinePlayer pPlayer,File pDir,String pNameParam){
        File tDataFile=new File(pDir,pNameParam.replace("%name%",pPlayer.getUniqueId().toString()));
        if(!tDataFile.isFile()){
            tDataFile=new File(pDir,pNameParam.replace("%name%",pPlayer.getName().toString()));
        }
        return tDataFile;
    }

}
