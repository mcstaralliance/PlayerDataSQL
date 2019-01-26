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
import cc.bukkitPlugin.pds.util.Pair;
import cc.commons.util.reflect.FieldUtil;

public abstract class ADM_ForgeCapability extends ADataModel {

    /** CapabilityLoc (key:class/value:field) */
    protected HashSet<Pair<String, String>> mCapabilityLoc = new HashSet<>();
    /** Capability */
    protected HashMap<String, Object> mCapability = new HashMap<>();

    public ADM_ForgeCapability(PlayerDataSQL pPlugin) {
        super(pPlugin);
    }

    protected void addCapability(String pClazz, String pField) {
        this.mCapabilityLoc.add(new Pair<String, String>(pClazz, pField));
    }

    @Override
    protected boolean initOnce() throws Exception {
        for (Pair<String, String> sPair : this.mCapabilityLoc) {
            Class tClazz;
            try {
                tClazz = Class.forName(sPair.getKey());
            } catch (IllegalStateException exp) {
                if (exp.getCause() instanceof NoSuchFieldException) {
                    Log.debug("no capability class found named \"" + sPair.getKey() + "\" at model " + this.getModelId());
                }
                continue;
            }
            try {
                this.mCapability.put(sPair.getKey() + "#" + sPair.getValue(), FieldUtil.getStaticFieldValue(tClazz, sPair.getKey()));
            } catch (IllegalStateException exp) {
                if (exp.getCause() instanceof NoSuchFieldException) {
                    Log.debug("no field named \"" + sPair.getValue() + "\" for class " + sPair.getKey() + " at model " + this.getModelId());
                }
            }
        }
        return true;
    }

    @Override
    public byte[] getData(CPlayer pPlayer, Map<String, byte[]> pLoadedData) throws Exception {
        ByteArrayOutputStream tBAOStream = new ByteArrayOutputStream();
        DataOutputStream tDOStream = new DataOutputStream(tBAOStream);
        tDOStream.write(this.mCapability.size());

        for (Map.Entry<String, Object> sEntry : this.mCapability.entrySet()) {
            tDOStream.writeUTF(sEntry.getKey());

            byte[] tData = PDSNBTUtil.compressNBT(CapabilityHelper.writeCapabilityNBT(pPlayer.getNMSPlayer(),
                    sEntry.getValue(),
                    this.getFacing(pPlayer, sEntry.getValue())));
            tDOStream.writeInt(tData.length);
            tDOStream.write(tData);
        }

        tBAOStream.flush();
        return tBAOStream.toByteArray();
    }

    @Override
    public void restore(CPlayer pPlayer, byte[] pData) throws Exception {
        if (pData.length == 0) {
            for (Object sObj : this.mCapability.values()) {
                Object tNBT = this.correctNBTData(sObj, NBTUtil.newNBTTagCompound());
                CapabilityHelper.readCapabilityNBT(pPlayer.getNMSPlayer(), sObj, this.getFacing(pPlayer, sObj), tNBT);
            }
        } else {
            ByteArrayInputStream tBAIStream = new ByteArrayInputStream(pData);
            DataInputStream tDIStream=new DataInputStream(tBAIStream);
            int tAmount = tDIStream.read();
            for (int i = 0; i < tAmount; i++) {
                Object tCapability = this.mCapability.get(tDIStream.readUTF());

                byte[] tData = new byte[tDIStream.readInt()];
                tDIStream.read(tData);
                if (tCapability != null) {
                    Object tNBT = this.correctNBTData(tCapability, PDSNBTUtil.decompressNBT(tData));
                    CapabilityHelper.readCapabilityNBT(pPlayer.getNMSPlayer(), tCapability, this.getFacing(pPlayer, tCapability), tNBT);
                }
            }
        }
    }

    @Override
    public void cleanData(CPlayer pPlayer) throws Exception {
        this.restore(pPlayer, new byte[0]);
    }

    public Object getFacing(CPlayer pPlayer, Object pCapability) {
        return null;
    }

    /**
     * 修正mod数据的NBT值,防止还原数据的时候报错
     * 
     * @param pNBTTag
     *            要修正的NBT数据
     * @return 修正后的数据
     */
    public Object correctNBTData(Object pCapability, Object pNBTTag) {
        return pNBTTag;
    }

}
