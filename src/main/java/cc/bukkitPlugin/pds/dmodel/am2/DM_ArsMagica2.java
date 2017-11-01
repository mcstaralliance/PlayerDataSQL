package cc.bukkitPlugin.pds.dmodel.am2;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.pds.PlayerDataSQL;

public class DM_ArsMagica2 extends ADM_AM2{

    public DM_ArsMagica2(PlayerDataSQL pPlugin){
        super(pPlugin,"am2.playerextensions.ExtendedProperties","ArsMagicaExProps");
    }

    @Override
    public String getModelId(){
        return "ArsMagica2";
    }

    @Override
    public String getDesc(){
        return "魔法艺术2";
    }

    @Override
    public boolean initOnce(){
        if(this.mInit!=null)
            return this.mInit.booleanValue();

        try{
            this.initExProp();
            super.initOnce();

            // AM2 TAG
            this.mModelTags.add("magicLevel");
            this.mModelTags.add("curMana");
            this.mModelTags.add("curFatigue");
            this.mModelTags.add("armorCooldowns");
            this.mModelTags.add("isShrunk");
            this.mModelTags.add("isCritical");
            this.mModelTags.add("magicXP");
            this.mModelTags.add("marklocationx");
            this.mModelTags.add("marklocationy");
            this.mModelTags.add("marklocationz");
            this.mModelTags.add("markdimension");
            this.mModelTags.add("contingency_type");
            this.mModelTags.add("contingency_effect");
        }catch(Exception exp){
            if(!(exp instanceof ClassNotFoundException))
                Log.severe("模块 "+this.getDesc()+" 初始化时发生了错误",exp);
            return (this.mInit=false);
        }
        return (this.mInit=true);
    }

}
