package kz.greetgo.log.zookeeper.config;

public interface EventFileHandler {
  void eventHappened(ConfigEventType type);
}
