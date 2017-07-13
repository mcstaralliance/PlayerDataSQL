package cc.bukkitPlugin.pds.command;

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.plugin.command.TACommandBase;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.user.User;
import cc.bukkitPlugin.pds.user.UserManager;

public class CommandSave extends TACommandBase<PlayerDataSQL,CommandExc>{

    public CommandSave(CommandExc pExector){
        super(pExector,"save",2);
    }

    @Override
    public boolean execute(CommandSender pSender,String pLabel,String[] pArgs){
        if(!hasCmdPermission(pSender))
            return noPermission(pSender,this.mLastConstructPermisson);

        if(pArgs.length==0) return help(pSender,pLabel);
        if(pArgs.length>2) return errorArgsNumber(pSender,pArgs.length);

        Player tSaveTo,tSaveFrom;
        int tSaveToIndex;
        if(pArgs.length==1){
            tSaveToIndex=0;
            if(!(pSender instanceof Player))
                return send(pSender,C("MsgCannotSaveDataForConsole"));

            tSaveFrom=(Player)pSender;
        }else{
            tSaveToIndex=1;
            tSaveFrom=Bukkit.getPlayerExact(pArgs[0]);
            if(tSaveFrom==null)
                return send(pSender,C("MsgPlayerNotOnline","%player%",pArgs[0]));
        }
        tSaveTo=Bukkit.getPlayerExact(pArgs[tSaveToIndex]);
        if(tSaveTo==null)
            return send(pSender,C("MsgPlayerNotExist","%player%",pArgs[0]));

        UserManager tUserMan=this.mPlugin.getUserManager();
        User tUserData=tUserMan.getUserData(tSaveFrom,false);
        Bukkit.getScheduler().runTaskAsynchronously(this.mPlugin,()->{
            User tOldData=null;
            try{
                tOldData=tUserMan.loadUser(tSaveTo);
            }catch(SQLException e){
                send(pSender,C("MsgErrorOnLoadSQLData","%player%",tSaveTo.getName())+": "+e.getLocalizedMessage());
                return;
            }

            if(tOldData!=null&&tOldData.isLocked()&&tSaveTo!=tSaveFrom){
                send(pSender,C("MsgPlayerDataLockSaveNotAllow","%player%",tSaveTo.getName()));
                return;
            }

            tUserData.setPlayer(tSaveTo);
            if(tUserMan.saveUser(tUserData,false,pSender)){
                send(pSender,C("MsgSaveDataForPlayer",
                        new String[]{"%from%","%to%"},
                        new Object[]{tSaveFrom==pSender?C("WordYou"):tSaveFrom.getName(),tSaveTo==pSender?C("WordYou"):tSaveTo.getName()}));
            }
        });

        return true;
    }

}
