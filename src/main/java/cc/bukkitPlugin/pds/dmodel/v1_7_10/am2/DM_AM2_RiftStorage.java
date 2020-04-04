package cc.bukkitPlugin.pds.dmodel.v1_7_10.am2;

import cc.bukkitPlugin.pds.PlayerDataSQL;

public class DM_AM2_RiftStorage extends ADM_AM2 {

    public DM_AM2_RiftStorage(PlayerDataSQL pPlugin) {
        super(pPlugin, "am2.playerextensions.RiftStorage", "ArsMagicaVoidStorage");
    }

    @Override
    public String getModelId() {
        return "ArsMagica2_RiftStorage";
    }

    @Override
    public String getDesc() {
        return "魔法艺术2-RiftStorage";
    }

    @Override
    protected boolean initOnce() throws Exception {
        this.initExProp();
        super.initOnce();

        // AM2 Affinity TAG
        this.mModelTags.add("PlayerRiftStorage");

        return true;
    }

}
