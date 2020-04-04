package cc.bukkitPlugin.pds.dmodel.v1_7_10;

import java.lang.reflect.Method;
import java.util.Map;

import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.commons.util.reflect.MethodUtil;

public class DM_ThirstMod extends ADM_ForgeData {

    protected Class<?> clazz_PlayerContainer;

    protected Method method_PlayerContainer_addPlayer;
    protected Method method_PlayerContainer_getPlayer;
    protected Method method_PlayerContainer_respawnPlayer;
    protected Method method_PlayerContainer_getStats;

    protected Method method_ThirstLogic_readData;
    protected Method method_ThirstLogic_writeData;

    public DM_ThirstMod(PlayerDataSQL pPlugin) {
        super(pPlugin);
    }

    @Override
    public String getModelId() {
        return "ThirstMod";
    }

    @Override
    public String getDesc() {
        return "饮水";
    }

    @Override
    protected boolean initOnce() throws Exception {
        Class.forName("com.thetorine.thirstmod.core.main.ThirstMod");

        clazz_PlayerContainer = Class.forName("com.thetorine.thirstmod.core.player.PlayerContainer");
        method_PlayerContainer_addPlayer = MethodUtil.getMethod(clazz_PlayerContainer, "addPlayer", NMSUtil.clazz_EntityPlayer, true);
        method_PlayerContainer_getPlayer = MethodUtil.getMethod(clazz_PlayerContainer, "getPlayer", NMSUtil.clazz_EntityPlayer, true);
        method_PlayerContainer_respawnPlayer = MethodUtil.getMethod(clazz_PlayerContainer, "respawnPlayer", true);
        method_PlayerContainer_getStats = MethodUtil.getMethod(clazz_PlayerContainer, "getStats", true);

        Class<?> tClazz = method_PlayerContainer_getStats.getReturnType();
        method_ThirstLogic_readData = MethodUtil.getMethodIgnoreParam(tClazz, "readData", true).get(0);
        method_ThirstLogic_writeData = MethodUtil.getMethodIgnoreParam(tClazz, "writeData", true).get(0);

        this.mModelTags.add("ThirstMod");

        return true;
    }

    @Override
    public byte[] getData(CPlayer pPlayer, Map<String, byte[]> pLoadedData) throws Exception {
        MethodUtil.invokeMethod(method_ThirstLogic_writeData, this.getThirstLogic(pPlayer));
        return super.getData(pPlayer, pLoadedData);
    }

    @Override
    public void restore(CPlayer pPlayer, byte[] pData) throws Exception {
        super.restore(pPlayer, pData);
        MethodUtil.invokeMethod(method_ThirstLogic_readData, this.getThirstLogic(pPlayer));
    }

    @Override
    public void cleanData(CPlayer pPlayer) {
        MethodUtil.invokeMethod(method_PlayerContainer_respawnPlayer, this.getOrCreatePlayerContainer(pPlayer));
    }

    public Object getThirstLogic(CPlayer pPlayer) {
        return MethodUtil.invokeMethod(method_PlayerContainer_getStats, this.getOrCreatePlayerContainer(pPlayer));
    }

    public Object getOrCreatePlayerContainer(CPlayer pPlayer) {
        while (true) {
            Object tPlayerContainer = MethodUtil.invokeStaticMethod(method_PlayerContainer_getPlayer, pPlayer.getNMSPlayer());
            if (tPlayerContainer != null) return tPlayerContainer;

            MethodUtil.invokeStaticMethod(method_PlayerContainer_addPlayer, pPlayer.getNMSPlayer());
        }
    }

    @Override
    protected void updateToAround(CPlayer pPlayer) {
        // 自动每Tick发送一次数据
    }

}
