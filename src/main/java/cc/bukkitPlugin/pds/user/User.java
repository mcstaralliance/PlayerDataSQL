package cc.bukkitPlugin.pds.user;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.commons.util.ByteUtil;
import cc.commons.util.IOUtil;

public class User{

    public static final String COL_NAME="name";
    public static final String COL_LOCK="locked";
    public static final String COL_DATA="data";

    /** 标识,玩家名字 */
    private String mName;
    /** 数据是否被锁定 */
    public boolean mLocked;
    /** 数据,key请保证小写 */
    private ConcurrentHashMap<String,byte[]> mData=new ConcurrentHashMap<>();
    /** {@link #mData}的数据序列化缓存 */
    private transient byte[] mDataCache=null;

    public User(String pPlayer){
        this.setPlayer(pPlayer);
    }

    public void setPlayer(String pPlayer){
        this.mName=pPlayer;
    }

    public byte[] getData(){
        if(this.mDataCache==null){
            ByteArrayOutputStream tBAOStream=new ByteArrayOutputStream();
            DataOutputStream tDOStream=null;
            try{
                tDOStream=new DataOutputStream(new GZIPOutputStream(tBAOStream));
                tDOStream.writeInt(this.mData.size());
                for(Map.Entry<String,byte[]> sEntry : this.mData.entrySet()){
                    tDOStream.writeUTF(sEntry.getKey().toLowerCase());
                    byte[] tValue=sEntry.getValue();
                    tDOStream.writeInt(tValue.length);
                    if(tValue.length>0){
                        tDOStream.write(tValue);
                    }
                }
            }catch(IOException exp){
                Log.severe("序列化数据到SQL时发生错误",exp);
            }finally{
                IOUtil.closeStream(tDOStream);
            }
            this.mDataCache=tBAOStream.toByteArray();
        }
        return this.mDataCache;
    }

    public void setData(byte[] pData){
        if(pData==null){
            // 0 zero length Gzip byte data
            pData=ByteUtil.base64ToByte("H4sIAAAAAAAAAGNgYGAAABzfRCEEAAAA");
        }

        this.mDataCache=pData;
        this.mData.clear();
        ByteArrayInputStream tBAIStream=new ByteArrayInputStream(pData);
        DataInputStream tDIStream=null;
        try{
            tDIStream=new DataInputStream(new GZIPInputStream(tBAIStream));
            int tCount=tDIStream.readInt();

            for(int i=0;i<tCount;i++){
                String tName=tDIStream.readUTF().toLowerCase();
                int tSubDataSize=tDIStream.readInt();
                byte[] tSubData=new byte[tSubDataSize];
                if(tSubDataSize>0){
                    tDIStream.readFully(tSubData);
                }
                this.mData.put(tName,tSubData);
            }
        }catch(IOException exp){
            Log.severe("从SQL反序列化数据时发生错误",exp);
            if(PlayerDataSQL.getInstance().getConfigManager().mKickOnReadSQLError){
                PlayerDataSQL.kickPlayerOnError(this.mName);
            }
        }finally{
            IOUtil.closeStream(tDIStream);
        }
    }

    /**
     * 获取模块序列化的数据,可以一编辑
     * 
     * @param pResetCache
     *            是否重置序列化到SQL数据的缓存
     * @return 模块序列化数据
     */
    public Map<String,byte[]> getDataMap(boolean pResetCache){
        if(pResetCache){
            this.mDataCache=null;
        }
        return this.mData;
    }

    public String getName(){
        return this.mName;
    }

    public boolean isLocked(){
        return this.mLocked;
    }

}
