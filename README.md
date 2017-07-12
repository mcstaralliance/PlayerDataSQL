#PlayerDataSQL

---


+ 本插件部分结构参照自梦梦的[PlayerSQL]( https://github.com/caoli5288/PlayerSQL/)
+ 插件特点为备份数据模块化,数据表只有一张
+ 建议只在1.7.10的mod服上使用,当前支持跨服的数据有神秘,饰品,将魂,时装,原版数据(不是只同步背包)
+ 你可以通过提交模块来丰富插件支持的跨服数据同步,只需要实现[IDataModel](https://coding.net/u/q17866380/p/PlayerDataSQL/git/blob/master/src/main/java/cc/bukkitPlugin/pds/api/IDataModel.java)接口并注册即可
+ 当前插件仍然在开发测试中,打算支持导入文件数据功能