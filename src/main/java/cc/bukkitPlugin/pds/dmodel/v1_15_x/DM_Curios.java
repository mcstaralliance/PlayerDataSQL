package cc.bukkitPlugin.pds.dmodel.v1_15_x;

import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.dmodel.v1_12_2.ADM_CapabilityProvider;

public class DM_Curios extends ADM_CapabilityProvider {

    public DM_Curios(PlayerDataSQL pPlugin) {
        super(pPlugin);

        this.addCapabilityP("top.theillusivec4.curios.common.capability.CapCurioInventory$Provider");
    }

    @Override
    public String getModelId() {
        return "Curios_1_15_x";
    }

    @Override
    public String getDesc() {
        return "Curios";
    }

}
