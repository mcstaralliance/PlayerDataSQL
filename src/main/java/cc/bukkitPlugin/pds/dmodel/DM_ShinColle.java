package cc.bukkitPlugin.pds.dmodel;

import java.lang.reflect.Method;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.commons.util.reflect.ClassUtil;
import cc.commons.util.reflect.MethodUtil;

public class DM_ShinColle extends ADM_InVanilla{

    private Method method_ExtendPlayerProps_syncShips=null;

    public DM_ShinColle(PlayerDataSQL pPlugin){
        super(pPlugin,"com.lulan.shincolle.entity.ExtendPlayerProps","TeitokuExtProps");
    }

    @Override
    public String getModelId(){
        return "ShinColle";
    }

    @Override
    public String getDesc(){
        return "舰队收藏";
    }

    @Override
    public boolean initOnce(){
        if(this.mInit!=null)
            return this.mInit.booleanValue();

        try{

            this.initExProp();

            this.method_ExtendPlayerProps_syncShips=MethodUtil.getMethod(this.mExPropClazz,"syncShips",true);

            // 舰娘的TAG
            this.mModelTags.add(this.mExPropName);
        }catch(Exception exp){
            if(!(exp instanceof ClassNotFoundException))
                Log.severe("模块 "+this.getDesc()+" 初始化时发生了错误",exp);
            return (this.mInit=false);
        }
        return (this.mInit=true);
    }

    @Override
    public Object newExProp(Object pNMSPlayer,Object pNMSWorld){
        return ClassUtil.newInstance(this.mExPropClazz);
    }

    @Override
    protected void updateToAround(Object pNMSPlayer,Object pExProp){
        MethodUtil.invokeMethod(this.method_ExtendPlayerProps_syncShips,pExProp);
    }

}
