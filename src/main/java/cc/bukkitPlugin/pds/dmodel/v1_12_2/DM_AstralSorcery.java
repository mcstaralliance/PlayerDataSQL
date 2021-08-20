package cc.bukkitPlugin.pds.dmodel.v1_12_2;

import java.lang.reflect.Method;
import java.util.Map;

import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.dmodel.ADataModel;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.bukkitPlugin.pds.util.PDSNBTUtil;
import cc.commons.util.reflect.MethodUtil;

public class DM_AstralSorcery extends ADataModel {

    private Class<?> clazz_PlayerProgress;
    /** public void load(NBTTagCompound) */
    private Method method_PlayerProgress_load;
    /** public void store(NBTTagCompound) */
    private Method method_PlayerProgress_store;
    //    /** static void saveNow(UUID,PlayerProgress) { */
    //    private Method method_ResearchIOThread_saveNow;
    /** public static PlayerProgress getProgress(EntityPlayer) { */
    private Method method_ResearchManager_getProgress;
    /** private static void pushProgressToClientUnsafe(EntityPlayerMP) { */
    private Method method_ResearchManager_pushProgressToClientUnsafe;

    public DM_AstralSorcery(PlayerDataSQL pPlugin) {
        super(pPlugin);
    }

    @Override
    public String getModelId() {
        return "AstralSorcery";
    }

    @Override
    public String getDesc() {
        return "星辉魔法";
    }

    @Override
    public byte[] getData(CPlayer pPlayer, Map<String, byte[]> pLoadedData) throws Exception {
        Object tProgress = this.getProgress(pPlayer), tNBT = NBTUtil.newNBTTagCompound();
        MethodUtil.invokeMethod(method_PlayerProgress_store, tProgress, tNBT);

        return PDSNBTUtil.compressNBT(tNBT);
    }

    @Override
    public void restore(CPlayer pPlayer, byte[] pData) throws Exception {
        Object tProgress = this.getProgress(pPlayer), tNBT = PDSNBTUtil.decompressNBT(pData);
        MethodUtil.invokeMethod(method_PlayerProgress_load, tProgress, tNBT);

        MethodUtil.invokeStaticMethod(method_ResearchManager_pushProgressToClientUnsafe, pPlayer.getNMSPlayer());
    }

    @Override
    public void cleanData(CPlayer pPlayer) throws Exception {
        this.restore(pPlayer, new byte[0]);
    }

    protected Object getProgress(CPlayer pPlayer) {
        return MethodUtil.invokeStaticMethod(method_ResearchManager_getProgress, pPlayer.getNMSPlayer());
    }

    @Override
    protected boolean initOnce() throws Exception {
        Class<?> tClazz = Class.forName("hellfirepvp.astralsorcery.common.data.research.PlayerProgress");
        this.clazz_PlayerProgress = tClazz;
        this.method_PlayerProgress_load = MethodUtil.getDeclaredMethod(tClazz, "load", NBTUtil.clazz_NBTTagCompound);
        this.method_PlayerProgress_store = MethodUtil.getDeclaredMethod(tClazz, "store", NBTUtil.clazz_NBTTagCompound);

//        tClazz = Class.forName("hellfirepvp.astralsorcery.common.data.research.ResearchIOThread");
//        this.method_ResearchIOThread_saveNow = MethodUtil.getMethodIgnoreParam(tClazz, "saveNow", true).oneGet();

        tClazz = Class.forName("hellfirepvp.astralsorcery.common.data.research.ResearchManager");
        this.method_ResearchManager_getProgress = MethodUtil.getDeclaredMethod(tClazz, "getProgress", NMSUtil.clazz_EntityPlayer);
        this.method_ResearchManager_pushProgressToClientUnsafe = MethodUtil.getDeclaredMethod(tClazz, "pushProgressToClientUnsafe",
                NMSUtil.clazz_EntityPlayerMP);

        return true;
    }

}
