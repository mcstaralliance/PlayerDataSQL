package cc.bukkitPlugin.pds.dmodel.v1_12_2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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

public abstract class ADM_ForgeCapabilityProvider extends ADataModel {

    protected HashSet<String> mCapabilityPs_name = new HashSet<>();
    private HashMap<String, Class<?>> mCapabilityPs = new HashMap<>();

    public ADM_ForgeCapabilityProvider(PlayerDataSQL pPlugin) {
        super(pPlugin);
    }

    protected void addCapabilityP(String pClass) {
        this.mCapabilityPs_name.add(pClass);
    }

    @Override
    protected boolean initOnce() throws Exception {
        if (!CapabilityHelper.isInisSuccess()) return false;

        Class<?> tImp = Class.forName("net.minecraftforge.common.capabilities.ICapabilityProvider");
        for (String sName : mCapabilityPs_name) {
            Class<?> tClazz;
            try {
                tClazz = Class.forName(sName);
            } catch (ClassNotFoundException exp) {
                Log.severe("模块 " + getModelId() + " 注册的CapabilityProvider " + sName + " 不存在");
                continue;
            }

            if (!tImp.isAssignableFrom(tClazz)) {
                Log.severe("模块 " + getModelId() + " 注册了一个名为 " + sName + " 的非CapabilityProvider模块");
            } else this.mCapabilityPs.put(sName, tClazz);
        }
        return !this.mCapabilityPs.isEmpty();
    }

    @Override
    public byte[] getData(CPlayer pPlayer, Map<String, byte[]> pLoadedData) throws Exception {
        ByteArrayOutputStream tBAOStream = new ByteArrayOutputStream();
        DataOutputStream tDOStream = new DataOutputStream(tBAOStream);
        tDOStream.write(this.mCapabilityPs.size());
        for (Map.Entry<String, Class<?>> sEntry : this.mCapabilityPs.entrySet()) {
            tDOStream.writeUTF(sEntry.getKey());

            byte[] tData = PDSNBTUtil.compressNBT(CapabilityHelper.serializeCapability(pPlayer.getNMSPlayer(), sEntry.getValue()));
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
                    Object tNBT = this.correctNBTData(tProviderName, PDSNBTUtil.decompressNBT(tData));
                    CapabilityHelper.deserializeCapability(pPlayer.getNMSPlayer(), tProvider, tNBT);
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

}
