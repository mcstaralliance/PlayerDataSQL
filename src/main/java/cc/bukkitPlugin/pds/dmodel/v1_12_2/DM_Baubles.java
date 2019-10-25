package cc.bukkitPlugin.pds.dmodel.v1_12_2;

import java.lang.reflect.Method;

import cc.bukkitPlugin.pds.PlayerDataSQL;

public class DM_Baubles extends ADM_CapabilityProvider {

    private Method method_BaublesApi_getBaubles = null;
    private Method method_InventoryBaubles_readNBT = null;
    private Method method_InventoryBaubles_saveNBT = null;
    private Method method_InventoryBaubles_syncSlotToClients = null;

    private Boolean mInit = null;

    public DM_Baubles(PlayerDataSQL pPlugin) {
        super(pPlugin);

        this.addModCheckClass("baubles.Baubles");
        this.addCapabilityP("baubles.api.cap.BaublesContainerProvider");
    }

    @Override
    public String getModelId() {
        return "Baubles_v1_12_2";
    }

    @Override
    public String getDesc() {
        return "饰品背包";
    }

    @Override
    public boolean initOnce() throws Exception {
        Class.forName("baubles.common.Baubles");

        return super.initOnce();
    }

}
