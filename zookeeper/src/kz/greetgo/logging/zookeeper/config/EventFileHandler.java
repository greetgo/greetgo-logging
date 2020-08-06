package kz.greetgo.logging.zookeeper.config;

public interface EventFileHandler {
  void eventHappened(ConfigEventType type);
}
