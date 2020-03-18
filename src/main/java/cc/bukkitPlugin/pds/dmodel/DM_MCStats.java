package cc.bukkitPlugin.pds.dmodel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

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

public class DM_MCStats extends ADataModel {

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final String MARK_STAT = "stat:";
    private static final String MARK_ADVANCE = "\nadvance:";

    private static MCVersion mMCV = null;

    private Boolean mInit = null;
    private Method method_EntityPlayerMP_getStatisticMan;
    /**
     * static Map loadStatistic(String) 1.12及以下<br>
     * void parseLocal(DataFixer, String) 1.13及以上
     */
    private Method method_StatisticsFile_loadStatistic;
    /**
     * String saveStatistic(Map) 1.12及以下<br>
     * String saveStatistic() 1.13及以上
     */
    private Method method_StatisticsFile_saveStatistic;
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
    // set
    //private Field field_AdvancementData_dirty = null;
    private Field field_AdvancementData_file = null;
    private Method method_AdvancementData_save = null;
    private Method method_AdvancementData_reload = null;

    //1.13+
    private Field field__ServerStatisticManager_server = null;
    private Field field__MinecraftServer_dataFixer = null;
    private Object mDataFixer = null;

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
            mMCV = MCVersion.v1_12_2;
            tClazz = this.method_EntityPlayerMP_getAdvancementsMan.getReturnType();
            this.field_AdvancementData_file = FieldUtil.getDeclaredField(tClazz, FieldFilter.t(File.class)).oneGet();
            this.field_AdvancementData_progress = FieldUtil.getDeclaredField(tClazz, FieldFilter.t(Map.class)).oneGet();
        } else {
            mMCV = MCVersion.v1_7_10;
        }

        tClazz = this.method_EntityPlayerMP_getStatisticMan.getReturnType();
        if (MethodUtil.isMethodExist(tClazz, MethodFilter.rpt(Map.class, String.class))) {
            //1.7.10,1.12.2
            this.method_StatisticsFile_loadStatistic = MethodUtil.getDeclaredMethod(tClazz,
                    MethodFilter.rpt(Map.class, String.class)).first();
            this.method_StatisticsFile_saveStatistic = MethodUtil.getDeclaredMethod(tClazz,
                    MethodFilter.rpt(String.class, Map.class)).first();
        } else {
            //1.13+
            mMCV = MCVersion.v1_13_ABOVE;
            this.field__ServerStatisticManager_server = FieldUtil.getDeclaredField(tClazz,
                    (field) -> field.getType().getSimpleName().equals("MinecraftServer")).oneGet();
            this.field__MinecraftServer_dataFixer = FieldUtil.getDeclaredField(this.field__ServerStatisticManager_server.getType(),
                    (field) -> field.getType().getSimpleName().equals("DataFixer")).oneGet();

            this.method_StatisticsFile_loadStatistic = MethodUtil.getDeclaredMethod(tClazz,
                    MethodFilter.rpt(void.class, this.field__MinecraftServer_dataFixer.getType(), String.class)).first();
            this.method_StatisticsFile_saveStatistic = MethodUtil.getDeclaredMethod(tClazz,
                    MethodFilter.rpt(String.class)).first();
        }

        this.field_StatFileWriter_stats = FieldUtil.getDeclaredField(tClazz.getSuperclass(),
                (field) -> Map.class.isAssignableFrom(field.getType())).first();
        return true;
    }

    @Override
    public byte[] getData(CPlayer pPlayer, Map<String, byte[]> pLoadedData) throws Exception {
        StringBuilder tSBuilder = new StringBuilder(MARK_STAT);
        Object tStatMan = this.getStatMan(pPlayer);
        String tJson;
        if (mMCV.ordinal() <= MCVersion.v1_12_2.ordinal()) {
            tJson = (String)MethodUtil.invokeMethod(this.method_StatisticsFile_saveStatistic,
                    tStatMan, this.getManStatValue(tStatMan));
        } else {
            tJson = (String)MethodUtil.invokeMethod(this.method_StatisticsFile_saveStatistic, tStatMan);
        }
        tSBuilder.append(tJson);
        tSBuilder.append(MARK_ADVANCE);

        if (this.method_EntityPlayerMP_getAdvancementsMan != null) {
            Object tAdvance = MethodUtil.invokeMethod(method_EntityPlayerMP_getAdvancementsMan, pPlayer.getNMSPlayer());
            if (!this.mAdvancementAlreadyDetected) this.detectAdvancementDataMethod(tAdvance);

            if (this.method_AdvancementData_save != null) {
                try {
                    MethodUtil.invokeMethod(method_AdvancementData_save, tAdvance);
                    File tAdvFile = (File)FieldUtil.getFieldValue(field_AdvancementData_file, tAdvance);
                    tSBuilder.append(FileUtil.readContent(tAdvFile, UTF_8.name()));
                } catch (IOException exp) {
                    Log.severe("未能保存成就的部分数据: " + exp.getLocalizedMessage());
                }
            }
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

        if (mMCV.ordinal() <= MCVersion.v1_12_2.ordinal()) {
            Map<Object, Object> tPlayerStatValue = this.getManStatValue(tStatMan);
            tPlayerStatValue.clear();
            tPlayerStatValue.putAll((Map<Object, Object>)MethodUtil.invokeMethod(this.method_StatisticsFile_loadStatistic,
                    tStatMan, tStat));
        } else {
            if (this.mDataFixer == null) {
                this.mDataFixer = FieldUtil.getFieldValue(field__MinecraftServer_dataFixer,
                        FieldUtil.getFieldValue(field__ServerStatisticManager_server, tStatMan));
            }

            MethodUtil.invokeMethod(method_StatisticsFile_loadStatistic, tStatMan, this.mDataFixer, tStat);
        }

        if (this.method_EntityPlayerMP_getAdvancementsMan != null) {
            Object tAdvance = MethodUtil.invokeMethod(method_EntityPlayerMP_getAdvancementsMan, pPlayer.getNMSPlayer());
            if (!this.mAdvancementAlreadyDetected) this.detectAdvancementDataMethod(tAdvance);

            if (this.method_AdvancementData_reload != null) {
                if (StringUtil.isBlank(tAdvancement)) tAdvancement = "{}";
                try {
                    File tAdvFile = (File)FieldUtil.getFieldValue(field_AdvancementData_file, tAdvance);
                    FileUtil.writeData(tAdvFile, tAdvancement.getBytes(UTF_8));
                    MethodUtil.invokeMethod(method_AdvancementData_reload, tAdvance);
                } catch (IOException exp) {
                    Log.severe("未能还原成就的部分数据: " + exp.getLocalizedMessage());
                }
            }
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
                } catch (NullPointerException ignore) {
                }
                boolean tReadMark = tTestFile.mReadMark;

                if (this.method_AdvancementData_save == null && !FileUtil.readContent(tTestFile, "UTF-8").equals(tTestContent)) {
                    this.method_AdvancementData_save = sMethod;
                    tStep |= 1;
                    continue;
                }
                if (this.method_AdvancementData_reload == null && tReadMark) {
                    this.method_AdvancementData_reload = sMethod;
                    tStep |= 2;
                    continue;
                }
            }
        } catch (IOException e) {
            Log.warn("§c成就模块部分初始化失败,成就数据无法同步: " + e.getLocalizedMessage());
        } finally {
            FieldUtil.setFieldValue(field_AdvancementData_progress, pObj, tOriginAdvc);
            FieldUtil.setFieldValue(field_AdvancementData_file, pObj, tOriginFile);
        }

        if (tStep != 3) Log.warn("§c成就模块部分初始化失败,成就数据无法同步");
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

    public static enum MCVersion {
        v1_7_10,
        v1_12_2,
        v1_13_ABOVE;
    }

}
