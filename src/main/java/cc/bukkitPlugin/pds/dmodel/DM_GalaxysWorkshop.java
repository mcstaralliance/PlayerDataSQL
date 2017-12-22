package cc.bukkitPlugin.pds.dmodel;

import cc.bukkitPlugin.pds.PlayerDataSQL;

public class DM_GalaxysWorkshop extends DM_ArmourersWorkshop{

    public static final String EXT_PROP_NAME="playerCustomEquipmentData";

    public DM_GalaxysWorkshop(PlayerDataSQL pPlugin){
        super(pPlugin,"wang.magick.galaxyworkshop.skin.ExPropsPlayerEquipmentData",EXT_PROP_NAME);
    }

    @Override
    public String getModelId(){
        return "GalaxysWorkshop";
    }

    @Override
    public String getDesc(){
        return "银河时装工坊";
    }

}
