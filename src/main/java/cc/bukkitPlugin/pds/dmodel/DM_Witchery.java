package cc.bukkitPlugin.pds.dmodel;

import java.lang.reflect.Method;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.pds.PlayerDataSQL;
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
    public boolean initOnce(){
        if(this.mInit!=null)
            return this.mInit.booleanValue();

        try{
            Class.forName("com.emoniph.witchery.Witchery");

            this.initExProp();

            method_ExtendedPlayer_sync=MethodUtil.getMethod(this.mExPropClazz,"sync",true);

            // 巫术TAG
            this.mModelTags.add(EXT_PROP_NAME);
        }catch(Exception exp){
            if(!(exp instanceof ClassNotFoundException))
                Log.severe("模块 "+this.getDesc()+" 初始化时发生了错误",exp);
            return (this.mInit=false);
        }
        return (this.mInit=true);
    }

    @Override
    protected void updateToAround(Object pNMSPlayer,Object pExProp){
        MethodUtil.invokeMethod(method_ExtendedPlayer_sync,pExProp);
    }

}
