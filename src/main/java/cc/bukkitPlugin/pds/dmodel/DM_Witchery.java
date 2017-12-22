package cc.bukkitPlugin.pds.dmodel;

import java.lang.reflect.Method;

import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.commons.util.reflect.MethodUtil;

public class DM_Witchery extends ADM_InVanilla{

    public static final String EXT_PROP_NAME="WitcheryExtendedPlayer";
    /** void sync() */
    private static Method method_ExtendedPlayer_sync;

    public DM_Witchery(PlayerDataSQL pPlugin){
        super(pPlugin,"com.emoniph.witchery.common.ExtendedPlayer",EXT_PROP_NAME);
    }

    @Override
    public String getModelId(){
        return "Witchery";
    }

    @Override
    public String getDesc(){
        return "巫术";
    }

    @Override
    protected boolean initOnce() throws Exception{
        this.initExProp();

        method_ExtendedPlayer_sync=MethodUtil.getMethod(this.mExPropClazz,"sync",true);

        // 巫术TAG
        this.mModelTags.add(EXT_PROP_NAME);

        return true;
    }

    @Override
    protected void updateToAround(CPlayer pPlayer,Object pExProp){
        MethodUtil.invokeMethod(method_ExtendedPlayer_sync,pExProp);
    }

}
