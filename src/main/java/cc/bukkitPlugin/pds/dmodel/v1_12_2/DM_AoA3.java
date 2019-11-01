package cc.bukkitPlugin.pds.dmodel.v1_12_2;

import java.lang.reflect.Method;

import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.bukkitPlugin.pds.util.CapabilityHelper;
import cc.commons.util.reflect.ClassUtil;
import cc.commons.util.reflect.MethodUtil;

public class DM_AoA3 extends ADM_CapabilityProvider {

    private Object instance_GlobalEvents;
    private Method method_GlobalEvents_onPlayerLogin;

    public DM_AoA3(PlayerDataSQL pPlugin) {
        super(pPlugin);

        this.addModCheckClass("net.tslat.aoa3.advent.AdventOfAscension");
        this.addCapabilityP("net.tslat.aoa3.capabilities.providers.AdventPlayerProvider");
    }

    @Override
    public String getModelId() {
        return "AoA3";
    }

    @Override
    public String getDesc() {
        return "虚无3";
    }

    @Override
    protected boolean initCapability() throws Exception {
        Class<?> tClazz = Class.forName("net.tslat.aoa3.event.GlobalEvents");
        this.instance_GlobalEvents = ClassUtil.newInstance(tClazz);
        this.method_GlobalEvents_onPlayerLogin = MethodUtil.getMethodIgnoreParam(tClazz, "onPlayerLogin", true).oneGet();

        return super.initCapability();
    }

    @Override
    public void updateAround(CPlayer pPlayer, Class<?> pProvider) {
        MethodUtil.invokeMethod(method_GlobalEvents_onPlayerLogin, instance_GlobalEvents, CapabilityHelper.newLoginEvent(pPlayer));

        super.updateAround(pPlayer, pProvider);
    }

}
