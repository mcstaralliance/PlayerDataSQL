package cc.bukkitPlugin.pds.dmodel.v1_12_2;

import cc.bukkitPlugin.pds.PlayerDataSQL;

public class DM_AcademyCraft extends ADM_CapabilityProvider {

    public DM_AcademyCraft(PlayerDataSQL pPlugin) {
        super(pPlugin);

        this.addModCheckClass("cn.academy.AcademyCraft");
        this.addInnerCapabilityP("cn.lambdalib2.datapart.CapDataPartHandler");
    }

    @Override
    public String getModelId() {
        return "AcademyCraft_v1_12_2";
    }

    @Override
    public String getDesc() {
        return "学园都市超能力";
    }

}
