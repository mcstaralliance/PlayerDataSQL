package cc.bukkitPlugin.pds.dmodel.v1_12_2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.dmodel.ADataModel;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.bukkitPlugin.pds.util.CapabilityHelper;
import cc.bukkitPlugin.pds.util.PDSNBTUtil;

public abstract class ADM_CapabilityProvider extends ADataModel {

    /** Provider的类全名 */
    protected HashSet<String> mCapabilityPs_name = new HashSet<>();
    /** 根据Provider的类全名获的Provider类 */
    protected HashMap<String, Class<?>> mCapabilityPs = new HashMap<>();
    /** 判断mod是否加载的类全名 */
    protected HashSet<String> mModClass = new HashSet<String>();

    public ADM_CapabilityProvider(PlayerDataSQL pPlugin) {
        super(pPlugin);
    }

    protected void addCapabilityP(String pClass) {
        this.mCapabilityPs_name.add(pClass);
    }

    /**
     * 添加此类下的匿名类为特性提供器
     * <p>
     * 插件会判定匿名类是否继承自特性提供器
     * </p>
     * 
     * @param pClass
     *            类
     */
    protected void addInnerCapabilityP(String pClass) {
        if (!CapabilityHelper.isInisSuccess()) return;
        try {
            Class<?> tImp = Class.forName("net.minecraftforge.common.capabilities.ICapabilityProvider");
            for (int i = 1;; i++) {
                String tClazzPName = pClass + "$" + i;
                Class<?> tClazzP = Class.forName(tClazzPName);
                if (tImp.isAssignableFrom(tClazzP)) this.addCapabilityP(tClazzPName);
            }
        } catch (ClassNotFoundException exp) {
        }
    }

    protected void addModCheckClass(String pClazz) {
        this.mModClass.add(pClazz);
    }

    @Override
    protected boolean initOnce() throws Exception {
        if (!CapabilityHelper.isInisSuccess()) return false;

        try {
            for (String sClazz : this.mModClass) {
                Class.forName(sClazz);
            }
        } catch (ClassNotFoundException exp) {
            Log.developInfo("Model " + this.getModelId() + " check fail by \"" + exp.getMessage() + "\"");
            throw exp;
        }

        Class<?> tImp = Class.forName("net.minecraftforge.common.capabilities.ICapabilityProvider");
        for (String sName : mCapabilityPs_name) {
            Class<?> tClazz;
            try {
                tClazz = Class.forName(sName);
            } catch (ClassNotFoundException exp) {
                Log.debug("§4模块 " + getModelId() + " 注册的CapabilityProvider " + sName + " 不存在");
                continue;
            }

            if (!tImp.isAssignableFrom(tClazz)) {
                Log.debug("§4模块 " + getModelId() + " 注册了一个名为 " + sName + " 的非CapabilityProvider模块");
            } else this.mCapabilityPs.put(sName, tClazz);
        }
        return !this.mCapabilityPs.isEmpty() && this.initCapability();
    }

    @Override
    public byte[] getData(CPlayer pPlayer, Map<String, byte[]> pLoadedData) throws Exception {
        ByteArrayOutputStream tBAOStream = new ByteArrayOutputStream();
        DataOutputStream tDOStream = new DataOutputStream(tBAOStream);
        tDOStream.write(this.mCapabilityPs.size());
        for (Map.Entry<String, Class<?>> sEntry : this.mCapabilityPs.entrySet()) {
            tDOStream.writeUTF(sEntry.getKey());

            byte[] tData = PDSNBTUtil.compressNBT(CapabilityHelper.wrapNBT(
                    CapabilityHelper.serializeCapability(pPlayer.getNMSPlayer(), sEntry.getValue())));
            tDOStream.writeInt(tData.length);
            tDOStream.write(tData);
        }

        tDOStream.flush();
        return tBAOStream.toByteArray();
    }

    @Override
    public void restore(CPlayer pPlayer, byte[] pData) throws Exception {
        if (pData.length == 0) {
            for (Map.Entry<String, Class<?>> sEntry : this.mCapabilityPs.entrySet()) {
                Object tNBT = this.correctNBTData(sEntry.getKey(), NBTUtil.newNBTTagCompound());
                CapabilityHelper.deserializeCapability(pPlayer.getNMSPlayer(), sEntry.getValue(), tNBT);
            }
        } else {
            ByteArrayInputStream tBAIStream = new ByteArrayInputStream(pData);
            DataInputStream tDIStream = new DataInputStream(tBAIStream);
            int tAmount = tDIStream.read();
            for (int i = 0; i < tAmount; i++) {
                String tProviderName = tDIStream.readUTF();

                byte[] tData = new byte[tDIStream.readInt()];
                tDIStream.read(tData);
                Class<?> tProvider = this.mCapabilityPs.get(tProviderName);
                if (tProvider != null) {
                    Object tNBT = this.correctNBTData(tProviderName, CapabilityHelper.unwrapNBT(PDSNBTUtil.decompressNBT(tData)));
                    CapabilityHelper.deserializeCapability(pPlayer.getNMSPlayer(), tProvider, tNBT);
                    this.updateAround(pPlayer, tProvider);
                }
            }
        }
    }

    @Override
    public void cleanData(CPlayer pPlayer) throws Exception {
        this.restore(pPlayer, new byte[0]);
    }

    /**
     * 修正mod数据的NBT值,防止还原数据的时候报错
     * 
     * @param pNBTTag
     *            要修正的NBT数据
     * @return 修正后的数据
     */
    public Object correctNBTData(String pProvider, Object pNBTTag) {
        return pNBTTag;
    }

    /**
     * 用于在数据还原后,同步到客户端时调用
     * 
     * @param pPlayer
     *            玩家
     * @param pProvider
     *            特性提供器
     */
    public void updateAround(CPlayer pPlayer, Class<?> pProvider) {}

    /**
     * 获取当前模块所注册的所有特性提供器全限定名
     * 
     * @return 特性提供器全限定名,无法编辑
     */
    public Collection<String> getProviderClazz() {
        return Collections.unmodifiableCollection(this.mCapabilityPs_name);
    }

    /**
     * 初始化模块
     * <p>
     * 子模块的初始化代码应写在此处
     * </p>
     */
    protected boolean initCapability() throws Exception {
        return true;
    }

}
