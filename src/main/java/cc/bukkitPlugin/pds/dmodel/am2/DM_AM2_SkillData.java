package cc.bukkitPlugin.pds.dmodel.am2;

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
    protected boolean initOnce() throws Exception{
        this.initExProp();
        super.initOnce();

        // AM2 Skill TAG
        this.mModelTags.add("SpellKnowledge");

        return true;
    }

}
