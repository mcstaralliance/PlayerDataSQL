package cc.bukkitPlugin.pds.dmodel.v1_7_10.am2;

import cc.bukkitPlugin.pds.PlayerDataSQL;

public class DM_ArsMagica2 extends ADM_AM2 {

    public DM_ArsMagica2(PlayerDataSQL pPlugin) {
        super(pPlugin, "am2.playerextensions.ExtendedProperties", "ArsMagicaExProps");
    }

    @Override
    public String getModelId() {
        return "ArsMagica2";
    }

    @Override
    public String getDesc() {
        return "魔法艺术2";
    }

    @Override
    protected boolean initOnce() throws Exception {
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

        return true;
    }

}
