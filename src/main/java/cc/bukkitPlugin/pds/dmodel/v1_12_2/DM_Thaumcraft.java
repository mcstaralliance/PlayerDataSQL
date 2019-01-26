package cc.bukkitPlugin.pds.dmodel.v1_12_2;

import cc.bukkitPlugin.pds.PlayerDataSQL;

public class DM_Thaumcraft extends ADM_ForgeCapability {

    public DM_Thaumcraft(PlayerDataSQL pPlugin) {
        super(pPlugin);

        this.addCapability("thaumcraft.api.capabilities.ThaumcraftCapabilities", "KNOWLEDGE");
        this.addCapability("thaumcraft.api.capabilities.ThaumcraftCapabilities", "WARP");
    }

    @Override
    public String getModelId() {
        return "Thaumcraft_v1_12_2";
    }

    @Override
    public String getDesc() {
        return "神秘时代研究";
    }

}
