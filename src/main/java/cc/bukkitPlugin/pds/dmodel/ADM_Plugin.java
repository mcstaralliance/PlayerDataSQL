package cc.bukkitPlugin.pds.dmodel;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import cc.bukkitPlugin.pds.PlayerDataSQL;

public abstract class ADM_Plugin extends ADataModel {

    protected final String mPlugin;

    public ADM_Plugin(PlayerDataSQL pMain, String pPluginName) {
        super(pMain);

        this.mPlugin = pPluginName;
    }

    @Override
    public String getModelId() {
        return this.mPlugin;
    }

    public String getDependentPluginN() {
        return this.mPlugin;
    }

    @Nullable
    public Plugin getDependentPluginI() {
        return Bukkit.getPluginManager().getPlugin(this.getDependentPluginN());
    }

    @Override
    protected boolean initOnce() throws Exception {
        return Bukkit.getPluginManager().isPluginEnabled(this.getDependentPluginN());
    }

}
