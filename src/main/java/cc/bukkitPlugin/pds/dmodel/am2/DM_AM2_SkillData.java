package cc.bukkitPlugin.pds.dmodel.am2;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.pds.PlayerDataSQL;

public class DM_AM2_SkillData extends ADM_AM2{

    public DM_AM2_SkillData(PlayerDataSQL pPlugin){
        super(pPlugin,"am2.playerextensions.SkillData","SpellKnowledgeData");
    }

    @Override
    public String getModelId(){
        return "ArsMagica2_Skill";
    }

    @Override
    public String getDesc(){
        return "魔法艺术2-技能";
    }

    @Override
    public boolean initOnce(){
        if(this.mInit!=null)
            return this.mInit.booleanValue();

        try{
            this.initExProp();
            super.initOnce();

            // AM2 Skill TAG
            this.mModelTags.add("SpellKnowledge");
        }catch(Exception exp){
            if(!(exp instanceof ClassNotFoundException))
                Log.severe("模块 "+this.getDesc()+" 初始化时发生了错误",exp);
            return (this.mInit=false);
        }
        return (this.mInit=true);
    }

}
