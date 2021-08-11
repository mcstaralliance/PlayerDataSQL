package cc.bukkitPlugin.pds.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.commons.plugin.command.TACommandBase;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.user.User;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.bukkitPlugin.pds.util.PDSNBTUtil;

/**
 * Created by xjboss on 2017/8/30.
 */
public class CommandRunscript extends TACommandBase<PlayerDataSQL, CommandExc> {

    public CommandRunscript(CommandExc pExector) {
        super(pExector, "run_script", 2);
    }

    @Override
    public ArrayList<String> onTabComplete(CommandSender pSender, String[] pArgs) {
        if (pArgs.length == 1) {
            ArrayList<String> scripts = new ArrayList(mPlugin.getScripts().size());
            for (File f : mPlugin.getScripts()) {
                scripts.add(f.getName());
            }
            return scripts;
        } else if (pArgs.length == 2) {
            ArrayList<String> players = new ArrayList();
            players.add("*");
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            return players;
        }
        return new ArrayList();
    }

    public void control(CommandSender pSender, User pUser, String pScriptName, ScriptEngine pEngine) {
        Invocable jsInvoke = (Invocable)pEngine;
        try {
            jsInvoke.invokeFunction("run", pUser);
        } catch (ScriptException | NoSuchMethodException e) {
            send(pSender, C("MsgErrorOnRunScriptControlPlayer", new String[]{"%player%", "%script_name%"}, pUser.getOwnerName(), pScriptName) + ": " + e.getLocalizedMessage());
        }
        if (Bukkit.getPlayerExact(pUser.getOwnerName()) != null) {
            mPlugin.getUserManager().restoreUser(pUser);
        }
        mPlugin.getUserManager().saveUser(pUser, pUser.mLocked);
    }

    @Override
    public boolean execute(CommandSender pSender, String pLabel, String[] pArgs) {
        if (!hasCmdPermission(pSender))
            return noPermission(pSender, this.mLastConstructPermisson);
        if (pArgs.length == 0) return help(pSender, pLabel);
        if (pArgs.length != 2) return errorArgsNumber(pSender, pArgs.length);
        ScriptEngine tJs_engine = new ScriptEngineManager(getClass().getClassLoader()).getEngineByName("javascript");
        try {
            Bindings bind = tJs_engine.createBindings();
            bind.put("NBTUtil", NBTUtil.class);
            bind.put("PDSNBTUtil", PDSNBTUtil.class);
            bind.put("dec", tJs_engine.eval("function(data){" +
                    "return PDSNBTUtil.static.decompressNBT(data);}"));
            bind.put("enc", tJs_engine.eval("function(data){" +
                    "return PDSNBTUtil.static.compressNBT(data);"
                    + "}"));
            bind.put("sender", pSender);
            tJs_engine.setBindings(bind, ScriptContext.GLOBAL_SCOPE);
            tJs_engine.eval(new FileReader(new File(mPlugin.getDataFolder(), "scripts" + File.separator + pArgs[0])));
            if (pArgs[1].equals("*")) {
                try {
                    int i = 0;
                    for (User u : mPlugin.getUserManager().getall()) {
                        control(pSender, u, pArgs[0], tJs_engine);
                        i++;
                    }
                    send(pSender, C("MsgRunScriptSuccess", new String[]{"%script_name%", "%pcount%"}, pArgs[0], i));
                } catch (SQLException e) {
                    send(pSender, C("MsgErrorOnLoadSQLData", "%player%", "*") + ": " + e.getLocalizedMessage());
                    return false;
                }
            } else {
                try {
                    User u = mPlugin.getStorage().get(new CPlayer(pArgs[1]));
                    if (u != null) {
                        control(pSender, u, pArgs[0], tJs_engine);
                        send(pSender, C("MsgRunScriptSuccess", new String[]{"%script_name%", "%pcount%"}, pArgs[0], 1));
                    }
                } catch (SQLException e) {
                    send(pSender, C("MsgErrorOnLoadSQLData", "%player%", pArgs[1]) + ": " + e.getLocalizedMessage());
                    return false;
                }
            }
        } catch (FileNotFoundException e) {
            send(pSender, C("MsgErrorNotFoundScript", "%script_name%", pArgs[0]));
        } catch (ScriptException e) {
            send(pSender, C("MsgErrorOnRunScript", "%player%", pSender.getName() + ": " + e.getLocalizedMessage()));
        }
        return false;
    }

    @Override
    public ArrayList<String> getHelp(CommandSender pSender, String pLabel) {
        ArrayList<String> tHelps = new ArrayList<>();
        if (hasCmdPermission(pSender)) {
            tHelps.add(constructCmdUsage(C("WordScriptName"), C("WordPlayer")));
            tHelps.add(this.mExector.getCmdDescPrefix() + C("HelpCmdRunScript"));
        }
        return tHelps;
    }
}
