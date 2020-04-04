package cc.bukkitPlugin.pds.dmodel.v1_7_10.am2;

import java.lang.reflect.Method;

import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.dmodel.v1_7_10.ADM_InVanilla;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.commons.util.reflect.MethodUtil;

public abstract class ADM_AM2 extends ADM_InVanilla {

    protected Method method_ExtendedProperties_forceSync = null;

    public ADM_AM2(PlayerDataSQL pPlugin, String pExPropClass, String pExPropName) {
        super(pPlugin, pExPropClass, pExPropName);
    }

    @Override
    protected boolean initOnce() throws Exception {
        try {
            method_ExtendedProperties_forceSync = MethodUtil.getMethodIgnoreParam(this.mExPropClazz, "forceSync", true).get(0);
        } catch (IllegalStateException ignore) {
        }

        try {
            this.method_ExProp_get = MethodUtil.getMethodIgnoreParam(this.mExPropClazz, "For", true).get(0);
        } catch (IllegalStateException ignore) {
        }
        return true;
    }

    @Override
    protected void updateToAround(CPlayer pPlayer, Object pExProp) {
        if (method_ExtendedProperties_forceSync != null) {
            MethodUtil.invokeMethod(method_ExtendedProperties_forceSync, pExProp);
        }
    }

}
