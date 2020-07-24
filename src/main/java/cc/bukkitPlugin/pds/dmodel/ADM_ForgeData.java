package cc.bukkitPlugin.pds.dmodel;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;

import javax.annotation.Nullable;

import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.bukkitPlugin.pds.util.PDSNBTUtil;
import cc.commons.util.reflect.MethodUtil;

public abstract class ADM_ForgeData extends ADataModel {

    protected final HashSet<String> mModelTags = new HashSet<>();
    protected final HashSet<String> mChechClass = new HashSet<>();

    protected Method method_Entity_getEntityData;

    public ADM_ForgeData(PlayerDataSQL pPlugin) {
        super(pPlugin);
    }

    @Override
    public byte[] getData(CPlayer pPlayer, Map<String, byte[]> pLoadedData) throws Exception {
        Object tNBTStore = NBTUtil.newNBTTagCompound();
        Object tNBTEData = this.getEntityData(pPlayer);
        Map<String, Object> tValues = NBTUtil.getNBTTagCompoundValue(tNBTEData);
        this.mModelTags.forEach(str -> {
            Object tValue = tValues.get(str);
            if (tValue != null) {
                tValue = MethodUtil.invokeMethod(NBTUtil.method_NBTBase_copy, tValue);
                NBTUtil.invokeNBTTagCompound_set(tNBTStore, str, tValue);
            }
        });

        return PDSNBTUtil.compressNBT(tNBTStore);
    }

    @Override
    public void restore(CPlayer pPlayer, byte[] pData) throws Exception {
        Object tNBTStore = PDSNBTUtil.decompressNBT(pData);
        Object tNBTEData = this.getEntityData(pPlayer);
        Map<String, Object> tValues = NBTUtil.getNBTTagCompoundValue(tNBTStore);

        this.mModelTags.forEach(str -> {
            Object tValue = fixNBT(pPlayer, str, tValues.get(str));
            if (tValue != null) {
                tValue = MethodUtil.invokeMethod(NBTUtil.method_NBTBase_copy, tValue);
                NBTUtil.invokeNBTTagCompound_set(tNBTEData, str, tValue);
            }
        });

        this.updateToClient(pPlayer);
    }

    @Override
    public void cleanData(CPlayer pPlayer) throws Exception {
        Object tNBTEData = this.getEntityData(pPlayer);
        Map<String, Object> tValues = NBTUtil.getNBTTagCompoundValue(tNBTEData);
        this.mModelTags.forEach(str -> {
            tValues.remove(str);
            Object tValue = fixNBT(pPlayer, str, null);
            if (tValue != null) {
                tValue = MethodUtil.invokeMethod(NBTUtil.method_NBTBase_copy, tValue);
                NBTUtil.invokeNBTTagCompound_set(tNBTEData, str, tValue);
            }
        });
    }

    public abstract void updateToClient(CPlayer pPlayer);

    public Object fixNBT(CPlayer pPlayer, String pTag, @Nullable Object pNBT) {
        return pNBT;
    }

    public Object getEntityData(CPlayer pPlayer) {
        return MethodUtil.invokeMethod(method_Entity_getEntityData, pPlayer.getNMSPlayer());
    }

    @Override
    protected boolean initOnce() throws Exception {
        for (String sClass : this.mChechClass)
            Class.forName(sClass);

        method_Entity_getEntityData = MethodUtil.getDeclaredMethod(NMSUtil.clazz_NMSEntity, "getEntityData");
        return true;
    }

}
