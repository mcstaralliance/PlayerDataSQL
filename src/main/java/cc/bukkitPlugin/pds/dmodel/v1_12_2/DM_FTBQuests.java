package cc.bukkitPlugin.pds.dmodel.v1_12_2;

import java.lang.reflect.Method;

import javax.annotation.Nullable;

import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.bukkitPlugin.pds.util.PDSNBTUtil;
import cc.commons.util.reflect.MethodUtil;

public class DM_FTBQuests extends ADM_FTBLib {

    /** public static ServerQuestData get(ForgeTeam) */
    private Method method_ServerQuestData_get = null;
    /** public void markDirty() */
    private Method method_ServerQuestData_markDirty = null;
    /** private void readData(NBTTagCompound) */
    private Method method_ServerQuestData_readData = null;
    /** private void writeData(NBTTagCompound) */
    private Method method_ServerQuestData_writeData = null;

    public DM_FTBQuests(PlayerDataSQL pPlugin) {
        super(pPlugin);

        this.mDataModelsStr.add("com.feed_the_beast.ftbquests.util.ServerQuestData");
    }

    @Override
    public String getModelId() {
        return "FTB_Quests";
    }

    @Override
    public String getDesc() {
        return "FTB Quests";
    }

    @Override
    protected boolean initOnce() throws Exception {
        boolean tSuccess = super.initOnce();
        if (!tSuccess) return false;

        Class<?> tClazz = Class.forName("com.feed_the_beast.ftbquests.util.ServerQuestData");
        method_ServerQuestData_get = MethodUtil.getDeclaredMethod(tClazz, "get", field_ForgePlayer_team.getType());
        method_ServerQuestData_markDirty = MethodUtil.getDeclaredMethod(tClazz, "markDirty");

        method_ServerQuestData_readData = MethodUtil.getDeclaredMethod(tClazz, "readData", NBTUtil.clazz_NBTTagCompound);
        method_ServerQuestData_writeData = MethodUtil.getDeclaredMethod(tClazz, "writeData", NBTUtil.clazz_NBTTagCompound);

        return true;
    }

    @Override
    public void restore(CPlayer pPlayer, byte[] pData) throws Exception {
        super.restore(pPlayer, pData);

        postForgePlayerLoggedInEvent(pPlayer);
        MethodUtil.invokeMethod(method_ServerQuestData_markDirty, getServerQuestData(pPlayer));
    }

    @Override
    @Nullable
    public Object getDataModel(CPlayer pPlayer, Class<?> pModelClazz) {
        if (pModelClazz.getSimpleName().equals("ServerQuestData")) {
            return getServerQuestData(pPlayer);
        }

        return null;
    }

    public Object getServerQuestData(CPlayer pPlayer) {
        return MethodUtil.invokeStaticMethod(method_ServerQuestData_get, getForgeTeam(pPlayer));
    }

    @Override
    public byte[] getDataModelData(CPlayer pPlayer, Object pDataModel) {
        Object tTag = NBTUtil.newNBTTagCompound();
        MethodUtil.invokeMethod(method_ServerQuestData_writeData, pDataModel, tTag);
        return PDSNBTUtil.compressNBT(tTag);
    }

    @Override
    public void restoreDataModelData(CPlayer pPlayer, Object pDataModel, byte[] pData) {
        postForgeTeamCreatedEvent(pPlayer);

        Object tTag;
        if (pData == null || pData.length == 0) {
            tTag = NBTUtil.newNBTTagCompound();
        } else tTag = PDSNBTUtil.decompressNBT(pData);

        MethodUtil.invokeMethod(method_ServerQuestData_readData, pDataModel, tTag);
    }

}
