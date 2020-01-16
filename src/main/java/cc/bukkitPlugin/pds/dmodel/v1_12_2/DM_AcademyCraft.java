package cc.bukkitPlugin.pds.dmodel.v1_12_2;

import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.CapabilityHelper;

public class DM_AcademyCraft extends ADM_CapabilityProvider {

    public DM_AcademyCraft(PlayerDataSQL pPlugin) {
        super(pPlugin);

        this.addModCheckClass("cn.academy.AcademyCraft");

        if (!CapabilityHelper.isInisSuccess()) return;
        try {
            Class<?> tImp = Class.forName("net.minecraftforge.common.capabilities.ICapabilityProvider");
            for (int i = 1;; i++) {
                String tClazzPName = "cn.lambdalib2.datapart.CapDataPartHandler$" + i;
                Class<?> tClazzP = Class.forName(tClazzPName);
                if (tImp.isAssignableFrom(tClazzP)) this.addCapabilityP(tClazzPName);
            }
        } catch (ClassNotFoundException exp) {
        }
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
