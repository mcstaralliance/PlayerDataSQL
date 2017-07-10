package cc.bukkitPlugin.pds.dmodel;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.PDSNBTUtil;
import cc.commons.util.FileUtil;

public class DM_Minecraft extends ADataModel{

    public static final String ID="Minecraft";

    public DM_Minecraft(PlayerDataSQL pPlugin){
        super(pPlugin);
    }

    @Override
    public String getModelId(){
        return DM_Minecraft.ID;
    }

    @Override
    public String getDesc(){
        return "Minecraft原版数据";
    }

    @Override
    public boolean initOnce(){
        return true;
    }

    @Override
    public byte[] getData(Player pPlayer,Map<String,byte[]> pLoadedData) throws Exception{
        return PDSNBTUtil.compressNBT(PDSNBTUtil.getPlayerNBT(pPlayer));
    }

    @Override
    public void restore(Player pPlayer,byte[] pData) throws Exception{
        PDSNBTUtil.setPlayerNBT(pPlayer,PDSNBTUtil.decompressNBT(pData));
    }

    @Override
    public byte[] loadFileData(OfflinePlayer pPlayer,Map<String,byte[]> pLoadedData) throws IOException{
        File tDataFile=this.getUUIDOrNameFile(pPlayer,this.mPlayerDataDir,"%name%.dat");
        if(!tDataFile.isFile()) return new byte[0];

        return FileUtil.readData(tDataFile);
    }

}
