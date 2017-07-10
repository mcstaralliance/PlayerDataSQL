package cc.bukkitPlugin.pds.storage;

import java.sql.SQLException;

import org.bukkit.OfflinePlayer;

import cc.bukkitPlugin.pds.user.User;

public interface IStorage{

    /**
     * 根据名字获取用户数据
     * 
     * @param pName
     *            用户名字,忽略大小写
     * @return 用户数据,如果不存在,则返回null
     * @throws SQLException
     *             读写数据库时发生异常
     */
    public User get(OfflinePlayer pPlayer) throws SQLException;

    /**
     * 使用用户数据更新或插入到数据库中
     * 
     * @param pUser
     *            用户数据
     * @return 插入是否成功,对于数据库则表示为产生影响的列数不为0
     * @throws SQLException
     *             读写数据库时发生异常
     */
    public boolean update(User pUser) throws SQLException;

    /**
     * 使用部分数据更新或插入到数据库中
     * <p>
     * 列名与值数量必须相同<br>
     * 如果未设置主键,会自动添加主键
     * </p>
     * 
     * @param pName
     *            用户名
     * @param pCols
     *            列名
     * @param pValues
     *            值
     * @return 插入是否成功,对于数据库则表示为产生影响的列数不为0
     * @throws SQLException
     *             读写数据库时发生异常
     */
    public boolean update(OfflinePlayer pPlayer,String[] pCols,Object...pValues) throws SQLException;

}
