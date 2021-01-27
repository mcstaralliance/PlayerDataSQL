package cc.bukkitPlugin.pds.dmodel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.Server;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.commons.util.CollUtil;
import cc.commons.util.FileUtil;
import cc.commons.util.StringUtil;
import cc.commons.util.extra.CList;
import cc.commons.util.reflect.FieldUtil;
import cc.commons.util.reflect.MethodUtil;
import cc.commons.util.reflect.filter.FieldFilter;
import cc.commons.util.reflect.filter.MethodFilter;
import cc.commons.util.tools.CacheGettor;

public class DM_MCStats extends ADataModel {

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final String MARK_STAT = "stat:";
    private static final String MARK_ADVANCE = "\nadvance:";

    private Boolean mInit = null;
    private Method method_EntityPlayerMP_getStatisticMan;
    /**
     * Map 1.12及以下<br>
     * Object2IntMaps 1.13及以上
     */
    private Field field_StatFileWriter_stats;
    /** PlayerAdvancements EntityPlayerMP.getAdvancements() 1.12+ */
    private Method method_EntityPlayerMP_getAdvancementsMan = null;
    // map
    private Field field_AdvancementData_progress = null;
    private boolean mAdvancementAlreadyDetected = false;

    private Field field_AdvancementData_file = null;

    private Function<CPlayer, String> mStatSave = null;
    private BiConsumer<CPlayer, String> mStatLoad = null;

    //1.12+
    private Function<CPlayer, String> mAdvanceSave = null;
    private BiConsumer<CPlayer, String> mAdvanceRestore = null;

    private CacheGettor<Object> mNMSServer = CacheGettor.create(() -> {
        Server tCraftServer = Bukkit.getServer();
        return MethodUtil.invokeDeclaredMethod(tCraftServer.getClass(), "getServer", tCraftServer);
    });

    private File mDataDir;

    public DM_MCStats(PlayerDataSQL pPlugin) {
        super(pPlugin);

        this.mDataDir = new File(this.mServerDir, "world" + File.separator + "stats");
    }

    @Override
    public String getModelId() {
        return "MinecraftStats";
    }

    @Override
    public String getDesc() {
        return "Minecraft成就数据";
    }

    @Override
    protected boolean initOnce() throws Exception {
        for (Method sMethod : NMSUtil.clazz_EntityPlayerMP.getDeclaredMethods()) {
            if (CollUtil.isEmpty(sMethod.getParameterTypes())) {
                if (sMethod.getReturnType().getSimpleName().toLowerCase().contains("statistic")) {
                    if (FieldUtil.isDeclaredFieldExist(NMSUtil.clazz_EntityPlayerMP, FieldFilter.t(sMethod.getReturnType())))
                        this.method_EntityPlayerMP_getStatisticMan = sMethod;
                } else if (sMethod.getReturnType().getSimpleName().toLowerCase().contains("advancement")) {
                    if (FieldUtil.isDeclaredFieldExist(NMSUtil.clazz_EntityPlayerMP, FieldFilter.t(sMethod.getReturnType())))
                        this.method_EntityPlayerMP_getAdvancementsMan = sMethod;
                }
                if (this.method_EntityPlayerMP_getAdvancementsMan != null
                        && this.method_EntityPlayerMP_getStatisticMan != null) break;
            }
        }

        if (this.method_EntityPlayerMP_getStatisticMan == null)
            return (this.mInit = false);

        Class<?> tClazz;
        if (this.method_EntityPlayerMP_getAdvancementsMan != null) {
            tClazz = this.method_EntityPlayerMP_getAdvancementsMan.getReturnType();
            this.field_AdvancementData_file = FieldUtil.getDeclaredField(tClazz, FieldFilter.t(File.class)).oneGet();
            this.field_AdvancementData_progress = FieldUtil.getDeclaredField(tClazz, FieldFilter.t(Map.class)).oneGet();
        }

        tClazz = this.method_EntityPlayerMP_getStatisticMan.getReturnType();
        if (MethodUtil.isMethodExist(tClazz, MethodFilter.rpt(Map.class, String.class))) {
            //1.7.10,1.12.2
            Method tMethod1 = MethodUtil.getDeclaredMethod(tClazz,
                    MethodFilter.rpt(String.class, Map.class)).first();
            this.mStatSave = (CPlayer pPlayer) -> {
                Object tStatMan = getStatMan(pPlayer);
                return (String)MethodUtil.invokeMethod(tMethod1,
                        tStatMan, getManStatValue(tStatMan));

            };
            Method tMethod2 = MethodUtil.getDeclaredMethod(tClazz,
                    MethodFilter.rpt(Map.class, String.class)).first();
            this.mStatLoad = (CPlayer pPlayer, String pStat) -> {
                Object tStatMan = getStatMan(pPlayer);
                Map<Object, Object> tPlayerStatValue = getManStatValue(tStatMan);
                tPlayerStatValue.clear();
                tPlayerStatValue.putAll((Map<Object, Object>)MethodUtil.invokeMethod(tMethod2,
                        tStatMan, pStat));
            };
        } else {
            //1.13+
            Method tMethod1 = MethodUtil.getDeclaredMethod(tClazz,
                    MethodFilter.rpt(String.class)).first();
            this.mStatSave = (pPlayer) -> (String)MethodUtil.invokeMethod(tMethod1, getStatMan(pPlayer));

            Field field_MinecraftServer_dataFixer = FieldUtil.getField(mNMSServer.get().getClass(),
                    (field) -> field.getType().getSimpleName().equals("DataFixer")).oneGet();
            Method tMethod2 = MethodUtil.getDeclaredMethod(tClazz,
                    MethodFilter.rpt(void.class, field_MinecraftServer_dataFixer.getType(), String.class)).first();
            Object tDataFixer = FieldUtil.getFieldValue(mNMSServer.get().getClass(),
                    (field) -> field.getType().getSimpleName().equals("DataFixer"), false, mNMSServer.get()).oneGet();
            this.mStatLoad = (CPlayer pPlayer, String pStat) -> MethodUtil.invokeMethod(tMethod2, getStatMan(pPlayer), tDataFixer, pStat);
        }

        this.field_StatFileWriter_stats = FieldUtil.getDeclaredField(tClazz.getSuperclass(),
                (field) -> Map.class.isAssignableFrom(field.getType())).first();
        return true;
    }

    @Override
    public byte[] getData(CPlayer pPlayer, Map<String, byte[]> pLoadedData) throws Exception {
        StringBuilder tSBuilder = new StringBuilder(MARK_STAT).append(this.mStatSave.apply(pPlayer)).append(MARK_ADVANCE);

        if (this.method_EntityPlayerMP_getAdvancementsMan != null) {
            if (!this.mAdvancementAlreadyDetected) this.detectAdvancementDataMethod(getPlayerAdvanceMan(pPlayer));
            if (this.mAdvanceSave != null) tSBuilder.append(this.mAdvanceSave.apply(pPlayer));
        }
        return tSBuilder.toString().getBytes(UTF_8);
    }

    @Override
    public void restore(CPlayer pPlayer, byte[] pData) throws Exception {
        Object tStatMan = this.getStatMan(pPlayer);
        String tDataStr = new String(pData, UTF_8);
        String tStat, tAdvancement;
        if (tDataStr.startsWith(MARK_STAT)) {
            int tIndex = tDataStr.indexOf(MARK_ADVANCE);
            if (tIndex == -1) tIndex = tDataStr.length();
            tStat = tDataStr.substring(MARK_STAT.length(), tIndex);
            tAdvancement = tDataStr.substring(Math.min(tIndex + MARK_ADVANCE.length(), tDataStr.length()));
        } else {
            tStat = tDataStr;
            tAdvancement = "";
        }

        this.mStatLoad.accept(pPlayer, tStat);

        if (this.method_EntityPlayerMP_getAdvancementsMan != null) {
            if (!this.mAdvancementAlreadyDetected) this.detectAdvancementDataMethod(getPlayerAdvanceMan(pPlayer));
            if (this.mAdvanceRestore != null) this.mAdvanceRestore.accept(pPlayer, tAdvancement);
        }
    }

    @Override
    public byte[] loadFileData(CPlayer pPlayer, Map<String, byte[]> pLoadedData) throws IOException {
        File tDataFile = this.getUUIDOrNameFile(pPlayer, this.mDataDir, "%name%.json");
        if (!tDataFile.isFile()) return new byte[0];

        return FileUtil.readData(tDataFile);
    }

    @Override
    public void cleanData(CPlayer pPlayer) {
        Object tStatMan = this.getStatMan(pPlayer);
        Map<Object, Object> tPlayerStatValue = this.getManStatValue(tStatMan);
        tPlayerStatValue.clear();

        if (this.method_EntityPlayerMP_getAdvancementsMan != null) {
            Object tAdvance = MethodUtil.invokeMethod(method_EntityPlayerMP_getAdvancementsMan, pPlayer.getNMSPlayer());
            Map<Object, Object> tAdvcData = (Map<Object, Object>)FieldUtil.getFieldValue(field_AdvancementData_progress, tAdvance);
            tAdvcData.clear();
        }
    }

    private Object getStatMan(CPlayer pPlayer) {
        return MethodUtil.invokeMethod(this.method_EntityPlayerMP_getStatisticMan, NMSUtil.getNMSPlayer(pPlayer.getPlayer()));
    }

    private Map<Object, Object> getManStatValue(Object pStatMan) {
        return (Map<Object, Object>)FieldUtil.getFieldValue(this.field_StatFileWriter_stats, pStatMan);
    }

    private Object getPlayerAdvanceMan(CPlayer pPlayer) {
        return MethodUtil.invokeMethod(method_EntityPlayerMP_getAdvancementsMan, pPlayer.getNMSPlayer());
    }

    /**
     * @param pObj
     *            AdvancementData实例
     */
    private void detectAdvancementDataMethod(Object pObj) {
        if (this.mAdvancementAlreadyDetected) return;
        this.mAdvancementAlreadyDetected = true;

        Class<?> tClazz = pObj.getClass();
        Object tOriginAdvc = FieldUtil.getFieldValue(field_AdvancementData_progress, pObj);
        FieldUtil.setFieldValue(field_AdvancementData_progress, pObj, new HashMap<>());
        File tOriginFile = (File)FieldUtil.getFieldValue(field_AdvancementData_file, pObj);
        CFile tTestFile = new CFile(tOriginFile.getParentFile(), "pds_test.dat");
        FieldUtil.setFieldValue(field_AdvancementData_file, pObj, tTestFile);
        int tStep = 0;
        boolean tError = false;
        Throwable tExp = null;
        try {
            String tTestContent = "\n\n{\n\n}\n\n";
            FileUtil.createNewFile(tTestFile, true);
            FileUtil.writeData(tTestFile, tTestContent.getBytes(UTF_8));
            CList<Method> tMethods = MethodUtil.getDeclaredMethod(tClazz, MethodFilter.rpt(void.class)
                    .addPossModifer(Modifier.PUBLIC)
                    .addDeniedModifer(Modifier.STATIC));

            for (Method sMethod : tMethods) {
                if (tStep == 3) break;

                tTestFile.mReadMark = false;
                try {
                    MethodUtil.invokeMethod(sMethod, pObj);
                } catch (Throwable ignore) {
                }
                boolean tReadMark = tTestFile.mReadMark;

                if (this.mAdvanceSave == null && !FileUtil.readContent(tTestFile, "UTF-8").equals(tTestContent)) {
                    Method tSave = sMethod;
                    this.mAdvanceSave = new AdvanceSave() {

                        @Override
                        protected String save(CPlayer pPlayer) throws IOException {
                            MethodUtil.invokeMethod(tSave, getPlayerAdvanceMan(pPlayer));
                            return super.save(pPlayer);
                        }
                    };

                    tStep |= 1;
                    continue;
                }
                if (this.mAdvanceRestore == null && tReadMark) {
                    Method tReload = sMethod;
                    tStep |= 2;
                    this.mAdvanceRestore = new AdvanceRestore() {

                        @Override
                        protected void restore(CPlayer pPlayer, String pData) throws IOException {
                            super.restore(pPlayer, pData);
                            MethodUtil.invokeMethod(tReload, getPlayerAdvanceMan(pPlayer));
                        }
                    };
                    continue;
                }
            }
        } catch (IOException e) {
            tError = true;
            tExp = e;
        } finally {
            FieldUtil.setFieldValue(field_AdvancementData_progress, pObj, tOriginAdvc);
            FieldUtil.setFieldValue(field_AdvancementData_file, pObj, tOriginFile);
        }

        if (!tError && this.mAdvanceRestore == null) {
            // 1.6.4
            Server tCraftServer = Bukkit.getServer();
            try {
                Object tNMSServer = MethodUtil.invokeDeclaredMethod(tCraftServer.getClass(), "getServer", tCraftServer);
                Method tGetAdvMan = MethodUtil.getMethod(tNMSServer.getClass(), MethodFilter.c().addPossModifer(Modifier.PUBLIC).noParam()
                        .addFilter((method) -> method.getReturnType().getSimpleName().toLowerCase().startsWith("advancement"))).oneGet();
                Object tAdvMan = MethodUtil.invokeMethod(tGetAdvMan, tNMSServer);
                Method tReload = MethodUtil.getMethod(pObj.getClass(),
                        MethodFilter.rpt(void.class, tGetAdvMan.getReturnType()).addPossModifer(Modifier.PUBLIC)).oneGet();

                this.mAdvanceRestore = new AdvanceRestore() {

                    @Override
                    protected void restore(CPlayer pPlayer, String pData) throws IOException {
                        super.restore(pPlayer, pData);
                        MethodUtil.invokeMethod(tReload, getPlayerAdvanceMan(pPlayer), tAdvMan);
                    }
                };
                tStep |= 2;
            } catch (Throwable exp) {
                tError = true;
                tExp = exp;
            }
        }

        if (tStep != 3) {
            String tErrorMsg = "§c成就模块部分初始化失败,成就数据无法同步";
            if (tExp != null) tErrorMsg += ": " + tExp.getLocalizedMessage();
            Log.warn("§c成就模块部分初始化失败,成就数据无法同步");
            if (tExp != null && Log.isDebug()) Log.severe(tExp);
        }
    }

    public class AdvanceSave implements Function<CPlayer, String> {

        @Override
        public final String apply(CPlayer pPlayer) {
            try {
                return this.save(pPlayer);
            } catch (IOException exp) {
                Log.severe("未能保存成就的部分数据: " + exp.getLocalizedMessage());
                if (Log.isDebug()) Log.severe(exp);
                return "";
            }
        }

        protected String save(CPlayer pPlayer) throws IOException {
            File tAdvFile = (File)FieldUtil.getFieldValue(field_AdvancementData_file, getPlayerAdvanceMan(pPlayer));
            return FileUtil.readContent(tAdvFile, UTF_8.name());
        }

    }

    public class AdvanceRestore implements BiConsumer<CPlayer, String> {

        @Override
        public final void accept(CPlayer pPlayer, String pData) {
            if (StringUtil.isBlank(pData)) pData = "{}";
            try {
                this.restore(pPlayer, pData);
                return;
            } catch (IOException exp) {
                Log.severe("未能还原成就的部分数据: " + exp.getLocalizedMessage());
                if (Log.isDebug()) Log.severe(exp);
            }
        }

        protected void restore(CPlayer pPlayer, String pData) throws IOException {
            File tAdvFile = (File)FieldUtil.getFieldValue(field_AdvancementData_file, getPlayerAdvanceMan(pPlayer));
            FileUtil.writeData(tAdvFile, pData.getBytes(UTF_8));
        }

    }

    public static class CFile extends File {

        public boolean mReadMark = false;

        public CFile(File parent, String child) {
            super(parent, child);
        }

        @Override
        public boolean isFile() {
            this.mReadMark = true;
            return super.isFile();
        }
    }

}
