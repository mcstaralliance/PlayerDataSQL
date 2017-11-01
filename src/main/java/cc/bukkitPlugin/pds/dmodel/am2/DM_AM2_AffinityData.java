package cc.bukkitPlugin.pds.dmodel.am2;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.pds.PlayerDataSQL;

public class DM_AM2_AffinityData extends ADM_AM2{

    public DM_AM2_AffinityData(PlayerDataSQL pPlugin){
        super(pPlugin,"am2.playerextensions.AffinityData","AffinityData");
    }

    @Override
    public String getModelId(){
        return "ArsMagica2_Affinity";
    }

    @Override
    public String getDesc(){
        return "魔法艺术2-亲和力";
    }

    @Override
    public boolean initOnce(){
        if(this.mInit!=null)
            return this.mInit.booleanValue();

        try{
            this.initExProp();
            super.initOnce();

            // AM2 Affinity TAG
            this.mModelTags.add("AffinityDepthData");
            this.mModelTags.add("AffinityLocked");
        }catch(Exception exp){
            if(!(exp instanceof ClassNotFoundException))
                Log.severe("模块 "+this.getDesc()+" 初始化时发生了错误",exp);
            return (this.mInit=false);
        }
        return (this.mInit=true);
    }

}
