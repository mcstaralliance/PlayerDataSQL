package cc.bukkitPlugin.pds.dmodel.v1_12_2;

import cc.bukkitPlugin.pds.PlayerDataSQL;

public class DM_Thaumcraft extends ADM_CapabilityProvider {

    public DM_Thaumcraft(PlayerDataSQL pPlugin) {
        super(pPlugin);

        this.addModCheckClass("thaumcraft.Thaumcraft");
        this.addCapabilityP("thaumcraft.common.lib.capabilities.PlayerKnowledge.Provider");
        this.addCapabilityP("thaumcraft.common.lib.capabilities.PlayerWarp.Provider");
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
