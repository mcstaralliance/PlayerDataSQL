package cc.bukkitPlugin.pds.dmodel.v1_7_10;

import cc.bukkitPlugin.pds.PlayerDataSQL;

public class DM_BloodMagic extends ADM_WorldData{

    public DM_BloodMagic(PlayerDataSQL pPlugin){
        super(pPlugin,"WayofTime.alchemicalWizardry.api.soulNetwork.LifeEssenceNetwork");
    }

    @Override
    public String getModelId(){
        return "BloodMagic";
    }

    @Override
    public String getDesc(){
        return "血魔法";
    }

    @Override
    protected boolean initOnce() throws Exception{
        this.initWSD();

        return true;
    }

}
