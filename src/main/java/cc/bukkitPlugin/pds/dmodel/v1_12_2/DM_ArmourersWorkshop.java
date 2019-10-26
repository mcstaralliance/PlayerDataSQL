package cc.bukkitPlugin.pds.dmodel.v1_12_2;

import cc.bukkitPlugin.pds.PlayerDataSQL;

public class DM_ArmourersWorkshop extends ADM_CapabilityProvider {

    public DM_ArmourersWorkshop(PlayerDataSQL pPlugin) {
        super(pPlugin);

        this.addModCheckClass("moe.plushie.armourers_workshop.ArmourersWorkshop");
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

}
