package kz.greetgo.log.zookeeper.config;

public interface ConfigEventHandler {
  void configEventHappened(String path, ConfigEventType type);
}
