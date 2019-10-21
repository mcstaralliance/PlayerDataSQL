package cc.bukkitPlugin.pds.dmodel.v1_12_2;

import cc.bukkitPlugin.pds.PlayerDataSQL;

public class DM_ArmourersWorkshop extends ADM_ForgeCapabilityProvider{

    public DM_ArmourersWorkshop(PlayerDataSQL pPlugin) {
        super(pPlugin);

        this.addCapabilityP("moe.plushie.armourers_workshop.common.capability.entityskin.EntitySkinProvider");
        this.addCapabilityP("moe.plushie.armourers_workshop.common.capability.wardrobe.player.PlayerWardrobeProvider");
        this.addCapabilityP("moe.plushie.armourers_workshop.common.capability.holiday.HolidayTrackCap$Provider");
    }

    @Override
    public String getModelId() {
        return "ArmourersWorkshop_v1_12_2";
    }

    @Override
    public String getDesc() {
        return "时装工坊";
    }
    
    @Override
    public boolean initOnce() throws Exception {
        Class.forName("moe.plushie.armourers_workshop.ArmourersWorkshop");

        return super.initOnce();
    }

}
