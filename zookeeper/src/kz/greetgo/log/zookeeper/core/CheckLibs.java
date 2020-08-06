package kz.greetgo.log.zookeeper.core;

public class CheckLibs {

  private static final String ORG_APACHE_ZOOKEEPER_ZOOKEEPER = "" +
    "\tcompileOnly('org.apache.zookeeper:zookeeper:3.4.13') {\n" +
    "\t  exclude group: 'org.slf4j', module: 'slf4j-log4j12'\n" +
    "\t  exclude group: 'log4j', module: 'log4j'\n" +
    "\t}";

  private static final String ORG_APACHE_CURATOR_CURATOR_X_ASYNC = "" +
    "\tcompileOnly(\"org.apache.curator:curator-x-async:4.0.1\") {\n" +
    "\t  exclude group: \"org.apache.zookeeper\", module: \"zookeeper\"\n" +
    "\t}";

  public static void checkLibs() {
    checkLib("org.apache.zookeeper.ZooKeeper", ORG_APACHE_ZOOKEEPER_ZOOKEEPER);
    checkLib("org.apache.curator.framework.CuratorFramework", ORG_APACHE_CURATOR_CURATOR_X_ASYNC);
  }

  private static void checkLib(String className, String inclusion) {
    try {
      Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("You must include library.\n" +
                                   "Using gradle it can be done adding:\n" + inclusion, e);
    }
  }
}
