package kz.greetgo.logging.zookeeper.config;

public interface ConfigEventHandler {
  void configEventHappened(String path, ConfigEventType type);
}
