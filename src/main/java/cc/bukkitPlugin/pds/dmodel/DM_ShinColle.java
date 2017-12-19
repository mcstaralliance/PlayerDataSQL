package cc.bukkitPlugin.pds.dmodel;

import java.lang.reflect.Method;

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
    protected boolean initOnce() throws Exception{
        this.initExProp();

        this.method_ExtendPlayerProps_syncShips=MethodUtil.getMethod(this.mExPropClazz,"syncShips",true);

        // 舰娘的TAG
        this.mModelTags.add(this.mExPropName);

        return true;
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
