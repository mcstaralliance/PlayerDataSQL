package cc.bukkitPlugin.pds.dmodel;

public interface IConfigedModel {

    default boolean doNotify() {
        return false;
    }

}
