pushd libs/Java-Commons-Util
mvn package
mvn install
popd

pushd libs/Java-Commons-CommentedYaml
mvn package
mvn install
popd

pushd libs/Bukkit-Commons-PluginBase
mvn package
mvn install
popd

pushd libs/Bukkit-Commons-NMSUtil
mvn package
mvn install
popd