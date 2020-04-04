package cc.bukkitPlugin.pds.api;

import java.io.IOException;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import cc.bukkitPlugin.pds.util.CPlayer;

public interface IDataModel {

    public Plugin getPlugin();

    /**
     * 获取模块ID,用于注册,以及序列化时使用
     * 
     * @return 模块标识,不能重复
     */
    public String getModelId();

    /**
     * 获取模块描述
     * 
     * @return 描述
     */
    public String getDesc();

    /**
     * 模块进行一次初始化,初始化成功则启用模块
     * <p>
     * 注意,此函数可能会被多次调用
     * </p>
     * 
     * @return 是否初始化成功
     */
    public boolean init();

    /**
     * 获取该模块用户的序列化数据,用于存储
     * 
     * @param pPlayer
     *            来源于谁的数据
     * @param pLoadedData
     *            已经载入的数据
     * @return 序列化的数据
     * @throws Exception
     *             序列化数据时发生异常
     */
    public byte[] getData(CPlayer pPlayer, Map<String, byte[]> pLoadedData) throws Exception;

    /**
     * 使用序列化的数据还原玩家数据
     * 
     * @param pPlayer
     *            要还原的玩家
     * @param pData
     *            数据
     * @throws Exception
     *             反序列化与设置数据过程中发生异常
     */
    public void restore(CPlayer pPlayer, byte[] pData) throws Exception;

    /**
     * 载入玩家的文件数据
     * 
     * @param pPlayer
     *            玩家
     * @param pLoadedData
     *            已经载入的数据
     * @return 序列化的数据,格式与{@link #getData(Player)}相同
     * @throws IOException
     *             读取文件时发生异常
     */
    public byte[] loadFileData(CPlayer pPlayer, Map<String, byte[]> pLoadedData) throws IOException;

    /**
     * 清理玩家此模块的数据
     * <p>
     * 通常在还原数据时使用
     * </p>
     * 
     * @param pPlayer
     *            要清理数据的玩家
     * @throws Exception
     */
    public void cleanData(CPlayer pPlayer) throws Exception;

}
