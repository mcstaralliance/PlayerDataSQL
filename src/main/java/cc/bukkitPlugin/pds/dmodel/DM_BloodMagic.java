package cc.bukkitPlugin.pds.dmodel;

import cc.bukkitPlugin.commons.Log;
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
    public boolean initOnce(){
        if(this.mInit!=null)
            return this.mInit.booleanValue();

        try{
            this.initWSD();
        }catch(Exception exp){
            if(!(exp instanceof ClassNotFoundException))
                Log.severe("模块 "+this.getDesc()+" 初始化时发生了错误",exp);
            return (this.mInit=false);
        }
        return (this.mInit=true);
    }

}
