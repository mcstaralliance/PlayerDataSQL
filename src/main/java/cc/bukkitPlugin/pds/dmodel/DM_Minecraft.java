package cc.bukkitPlugin.pds.dmodel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
//import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

//import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.bukkitPlugin.pds.util.PDSNBTUtil;
import cc.commons.util.FileUtil;
//import cc.commons.util.reflect.FieldUtil;
//import cc.commons.util.reflect.MethodUtil;

public class DM_Minecraft extends ADataModel {

    public static final String ID = "Minecraft";

    private Method method_EntityLivingBase_getAttributeMap = null;
    private HashSet<Field> mMapFields = new HashSet<>();

    private PlayerInventory Inventory;

    public DM_Minecraft(PlayerDataSQL pPlugin) {
        super(pPlugin);
    }

    @Override
    public String getModelId() {
        return DM_Minecraft.ID;
    }

    @Override
    public String getDesc() {
        return "Minecraft原版数据";
    }

    @Override
    protected boolean initOnce() throws Exception {
        return true;
    }

    @Override
    public byte[] getData(CPlayer pPlayer, Map<String, byte[]> pLoadedData) throws Exception {
        return PDSNBTUtil.compressNBT(PDSNBTUtil.getPlayerNBT(pPlayer.getPlayer()));
    }

    @Override
    public void restore(CPlayer pPlayer, byte[] pData) throws Exception {
        this.cleanData(pPlayer);

        Player tPlayer = pPlayer.getPlayer();
        GameMode tMode = tPlayer.getGameMode();
        PDSNBTUtil.setPlayerNBT(tPlayer, PDSNBTUtil.decompressNBT(pData));
        tPlayer.setGameMode(tMode);
        
        PlayerInventory tInv = tPlayer.getInventory();
        int tSlot = tInv.getHeldItemSlot();
        tInv.setHeldItemSlot(0);
        tInv.setHeldItemSlot(tSlot);
    }

    @Override
    public void cleanData(CPlayer pPlayer) {
        // clear buff
        Player tPlayer = pPlayer.getPlayer();
        for (PotionEffect sEffect : tPlayer.getActivePotionEffects()) {
            tPlayer.removePotionEffect(sEffect.getType());
        }
        //        // clear Attribute
        //        boolean tError=false;
        //        Object tNMSPlayer=NMSUtil.getNMSPlayer(pPlayer);
        //        Object tAttributeMap=null;
        //        if(this.method_EntityLivingBase_getAttributeMap==null){
        //            try{
        //                this.method_EntityLivingBase_getAttributeMap=MethodUtil.getMethod(NMSUtil.clazz_EntityPlayer,(pMethod)->{
        //                    return pMethod.getName().contains("Attribute")
        //                            &&pMethod.getParameterCount()==0
        //                            &&pMethod.getReturnType().getSimpleName().contains("Attribute");
        //                },false).get(0);
        //
        //                tAttributeMap=MethodUtil.invokeMethod(this.method_EntityLivingBase_getAttributeMap,tNMSPlayer);
        //
        //                this.mMapFields.addAll(FieldUtil.getField(tAttributeMap.getClass(),(pField)->{
        //                    return Collection.class.isAssignableFrom(pField.getType())||Map.class.isAssignableFrom(pField.getType());
        //                },false));
        //            }catch(IllegalStateException exp){
        //                tError=true;
        //            }
        //        }
        //        if(!tError){
        //            tAttributeMap=tAttributeMap==null?MethodUtil.invokeMethod(this.method_EntityLivingBase_getAttributeMap,tNMSPlayer):tAttributeMap;
        //            for(Field sField : this.mMapFields){
        //                Object tValue=FieldUtil.getFieldValue(sField,tAttributeMap);
        //                if(tValue instanceof Map){
        //                    ((Map)tValue).clear();
        //                }else if(tValue instanceof Collection){
        //                    ((Collection)tValue).clear();
        //                }
        //            }
        //        }
    }

    @Override
    public byte[] loadFileData(CPlayer pPlayer, Map<String, byte[]> pLoadedData) throws IOException {
        File tDataFile = this.getUUIDOrNameFile(pPlayer, this.mPlayerDataDir, "%name%.dat");
        if (!tDataFile.isFile()) return new byte[0];

        return FileUtil.readData(tDataFile);
    }

}
