package cc.bukkitPlugin.pds.dmodel.v1_12_2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.IConfigModel;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.bukkitPlugin.pds.util.CapabilityHelper;
import cc.bukkitPlugin.pds.util.PDSNBTUtil;
import cc.commons.commentedyaml.CommentedYamlConfig;
import cc.commons.util.reflect.FieldUtil;
import cc.commons.util.reflect.MethodUtil;
import cc.commons.util.reflect.filter.MethodFilter;

public class DM_BetterQuesting extends ADM_CapabilityProvider implements IConfigModel {

    /** 用于检查是否为集合的KEY,判定此节点是否为空节点 */
    public static final HashSet<String> NBT_COL_KEY = new HashSet<String>();
    /** 已知的,可以忽视检查的节点 */
    public static final HashSet<String> NBT_IGNORE_KEY = new HashSet<String>();

    static {
        NBT_COL_KEY.add("completed");
        NBT_COL_KEY.add("completeUsers");
        NBT_COL_KEY.add("tasks");
        NBT_COL_KEY.add("userProgress");

        NBT_IGNORE_KEY.add("questID");
        NBT_IGNORE_KEY.add("index");
        NBT_IGNORE_KEY.add("taskID");
    }

    private boolean mRmoveEmptyData = true;

    //-------------------------  about sync start-----------------------
    //-------------------------  version 3.5.307 and above start-----------------------
    /** public static void sendReset(@Nonnull EntityPlayerMP,boolean,boolean) */
    private Method method_NetBulkSync_sendReset = null;
    /** public static void sendSync(@Nonnull EntityPlayerMP) */
    private Method method_NetBulkSync_sendSync = null;
    //-------------------------  version 3.5.307 and above end-----------------------
    //-------------------------  version 3.5.300 and below start-----------------------
    private Object instance_EventHandler = null;
    /** public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent) */
    private Method method_EventHandler_onPlayerJoin = null;
    //-------------------------  version 3.5.300 and below end-----------------------

    //-------------------------  about sync over-----------------------

    public static String KEY_QuestDatabase = "QuestDatabase";
    private Object instance_QuestDatabase_INSTANCE;
    /** public NBTTagList writeProgressToNBT(NBTTagList,@Nullable List<UUID>) */
    private Method method_QuestDatabase_writeProgressToNBT;
    /** public void readProgressFromNBT(NBTTagList,boolean) */
    private Method method_QuestDatabase_readProgressFromNBT;

    public static String KEY_LifeDatabase = "LifeDatabase";
    private Object instance_LifeDatabase_INSTANCE;
    /** public NBTTagCompound writeToNBT(NBTTagCompound,@Nullable List<UUID>) */
    private Method method_LifeDatabase_writeToNBT;
    /** public void readFromNBT(NBTTagCompound,boolean) { */
    private Method method_LifeDatabase_readFromNBT;

    public DM_BetterQuesting(PlayerDataSQL pPlugin) {
        super(pPlugin);

        this.addModCheckClass("betterquesting.core.BetterQuesting");
        this.addCapabilityP("betterquesting.api2.cache.CapabilityProviderQuestCache");
    }

    @Override
    public String getModelId() {
        return "BetterQuesting_v1_12_2";
    }

    @Override
    public String getDesc() {
        return "BetterQuesting";
    }

    @Override
    public byte[] getData(CPlayer pPlayer, Map<String, byte[]> pLoadedData) throws Exception {
        ByteArrayOutputStream tBAOStream = new ByteArrayOutputStream();
        DataOutputStream tDOStream = new DataOutputStream(tBAOStream);

        Object tNBT = NBTUtil.newNBTTagCompound();
        Map<String, Object> tValue = NBTUtil.getNBTTagCompoundValue(tNBT);
        ArrayList<UUID> tList = new ArrayList<>();
        tList.add(pPlayer.getUniqueId());
        Object tNBTTmp = MethodUtil.invokeMethod(method_QuestDatabase_writeProgressToNBT, instance_QuestDatabase_INSTANCE,
                NBTUtil.newNBTTagList(), tList);
        if (this.mRmoveEmptyData) checkAndRemoveEmptyNBT(tNBTTmp);
        tValue.put(KEY_QuestDatabase, tNBTTmp);
        tNBTTmp = MethodUtil.invokeMethod(method_LifeDatabase_writeToNBT, instance_LifeDatabase_INSTANCE,
                NBTUtil.newNBTTagCompound(), tList);
        if (this.mRmoveEmptyData) checkAndRemoveEmptyNBT(tNBTTmp);
        tValue.put(KEY_LifeDatabase, tNBTTmp);

        byte[] tData = PDSNBTUtil.compressNBT(tNBT);
        tDOStream.writeInt(tData.length);
        tBAOStream.write(tData);

        tDOStream.write(super.getData(pPlayer, pLoadedData));
        tDOStream.flush();
        return tBAOStream.toByteArray();
    }

    @Override
    public void restore(CPlayer pPlayer, byte[] pData) throws Exception {
        if (pData.length == 0) {

        } else {
            ByteArrayInputStream tBAIStream = new ByteArrayInputStream(pData);
            DataInputStream tDIStream = new DataInputStream(tBAIStream);
            byte[] tData = new byte[tDIStream.readInt()];
            tDIStream.read(tData);

            Object tNBT = PDSNBTUtil.decompressNBT(tData);
            Map<String, Object> tValue = NBTUtil.getNBTTagCompoundValue(tNBT);
            Object tDataTmp = tValue.get(KEY_QuestDatabase);
            if (tDataTmp != null) {
                MethodUtil.invokeMethod(method_QuestDatabase_readProgressFromNBT, instance_QuestDatabase_INSTANCE,
                        tDataTmp, true);
            }

            tDataTmp = tValue.get(KEY_LifeDatabase);
            if (tDataTmp != null) {
                MethodUtil.invokeMethod(method_LifeDatabase_readFromNBT, instance_LifeDatabase_INSTANCE,
                        tDataTmp, true);
            }

            tData = new byte[tDIStream.available()];
            tDIStream.read(tData);
            super.restore(pPlayer, tData);
        }

        Object tNMSPlayer = pPlayer.getNMSPlayer();
        if (method_NetBulkSync_sendReset != null && NMSUtil.clazz_EntityPlayerMP.isInstance(tNMSPlayer)) {
            MethodUtil.invokeStaticMethod(method_NetBulkSync_sendReset, tNMSPlayer, true, false);
            MethodUtil.invokeStaticMethod(method_NetBulkSync_sendSync, tNMSPlayer);
        } else if (method_EventHandler_onPlayerJoin != null) {
            MethodUtil.invokeMethod(method_EventHandler_onPlayerJoin, instance_EventHandler, CapabilityHelper.newLoginEvent(pPlayer));
        }
    }

    @Override
    protected boolean initCapability() throws Exception {
        Class<?> tClazz;

        try {
            // 版本3.5.307及以上
            tClazz = Class.forName("betterquesting.network.handlers.NetBulkSync");
            this.method_NetBulkSync_sendReset = MethodUtil.getMethodIgnoreParam(tClazz, "sendReset", true).oneGet();
            this.method_NetBulkSync_sendSync = MethodUtil.getDeclaredMethod(tClazz, "sendSync", NMSUtil.clazz_EntityPlayerMP);
        } catch (ClassNotFoundException exp) {
            // 版本 3.5.300 以下
            tClazz = Class.forName("betterquesting.handlers.EventHandler");
            this.instance_EventHandler = FieldUtil.getStaticFieldValue(tClazz, "INSTANCE");
            this.method_EventHandler_onPlayerJoin = MethodUtil.getDeclaredMethod(tClazz, "onPlayerJoin", CapabilityHelper.clazz_PlayerLoggedInEvent);
        }

        tClazz = Class.forName("betterquesting.questing.QuestDatabase");
        this.instance_QuestDatabase_INSTANCE = FieldUtil.getStaticDeclaredFieldValue(tClazz, "INSTANCE");
        this.method_QuestDatabase_writeProgressToNBT = MethodUtil.getDeclaredMethod(tClazz, "writeProgressToNBT",
                NBTUtil.clazz_NBTTagList, List.class);
        this.method_QuestDatabase_readProgressFromNBT = MethodUtil.getDeclaredMethod(tClazz, "readProgressFromNBT",
                NBTUtil.clazz_NBTTagList, boolean.class);

        tClazz = Class.forName("betterquesting.storage.LifeDatabase");
        this.instance_LifeDatabase_INSTANCE = FieldUtil.getStaticDeclaredFieldValue(tClazz, "INSTANCE");
        this.method_LifeDatabase_writeToNBT = MethodUtil.getDeclaredMethod(tClazz,
                MethodFilter.pn("writeToNBT", "writeProgressToNBT").setParamType(NBTUtil.clazz_NBTTagCompound, List.class)).oneGet();

        this.method_LifeDatabase_readFromNBT = MethodUtil.getDeclaredMethod(tClazz,
                MethodFilter.pn("readFromNBT", "readProgressFromNBT").setParamType(NBTUtil.clazz_NBTTagCompound, boolean.class)).oneGet();

        return super.initCapability();
    }

    /**
     * 检查并移除空的NBT值
     * <p>
     * 空NBT的定义为,在已知的集合Key对应的=值均为空 且不存在其他未知的key对应的集合值 且不存在其他未知的key对应的值
     * </p>
     * 
     * @param pNBT
     *            要检查的NBT
     * @return NBT是否为空
     */
    protected boolean checkAndRemoveEmptyNBT(Object pNBT) {
        if (NBTUtil.isNBTTagList(pNBT)) {
            List<Object> tValues = NBTUtil.getNBTTagListValue(pNBT);
            Iterator<Object> tIt = tValues.iterator();
            while (tIt.hasNext()) {
                if (checkAndRemoveEmptyNBT(tIt.next())) tIt.remove();
            }
            return tValues.isEmpty();
        } else if (NBTUtil.isNBTTagCompound(pNBT)) {
            boolean tHasEmptyCol = false, tForceNotEmpty = false;
            List<String> tKeyRemove = new ArrayList<>();
            Map<String, Object> tValues = NBTUtil.getNBTTagCompoundValue(pNBT);
            for (Map.Entry<String, Object> sEntry : tValues.entrySet()) {
                if (NBT_COL_KEY.contains(sEntry.getKey())) {
                    if (checkAndRemoveEmptyNBT(sEntry.getValue())) {
                        tHasEmptyCol = true;
                        tKeyRemove.add(sEntry.getKey());
                    } else tForceNotEmpty = true;
                } else if (NBT_IGNORE_KEY.contains(sEntry.getKey())) {
                    if (NBTUtil.isNBTTagList(sEntry.getValue()) || NBTUtil.isNBTTagCompound(sEntry.getValue())) {
                        if (!checkAndRemoveEmptyNBT(sEntry.getValue()))
                            tForceNotEmpty = true;
                    }
                } else {
                    tForceNotEmpty = true;
                }
            }

            for (String sKey : tKeyRemove)
                tValues.remove(sKey);

            return tForceNotEmpty ? false : tValues.isEmpty() || tHasEmptyCol;
        }

        return false;
    }

    @Override
    public void addDefaults(CommentedYamlConfig pConfig) {
        pConfig.addDefault("Sync." + this.getModelId() + ".RemoveEmptyData", true,
                "移除数据中的空数据",
                "由于MOD的机制,数据保存会保存很多无用的空数据,造成对数据库空间的浪费,开启此项可以清除部分空数据以尽量减小数据大小",
                "但是,MOD可能存在未知因素造成数据被错误的移除,如果你发现部分数据存在丢失,尤其是存在附属的情况下,可以禁用此项目,");
    }

    @Override
    public void setConfig(CommandSender pSender, CommentedYamlConfig pConfig) {
        this.mRmoveEmptyData = pConfig.getBoolean("Sync." + this.getModelId() + ".RemoveEmptyData", true);
    }

}
