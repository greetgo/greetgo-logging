package kz.greetgo.log.zookeeper.core;

import kz.greetgo.log.core.config.ConfigFile;
import kz.greetgo.log.zookeeper.config.EventConfigFileFromStorage;
import kz.greetgo.log.zookeeper.config.EventConfigStorageZooKeeper;
import lombok.NonNull;

import java.util.concurrent.ConcurrentHashMap;

public final class ZookeeperConfigFileFactory implements AutoCloseable {

  static {
    CheckLibs.checkLibs();
  }

  private final EventConfigStorageZooKeeper storage;

  public ZookeeperConfigFileFactory(@NonNull ZookeeperConnectParams connectParams,
                                    String rootPath) {
    storage = new EventConfigStorageZooKeeper(rootPath, connectParams);
  }

  private final ConcurrentHashMap<String, ConfigFile> pathToConfigFileMap = new ConcurrentHashMap<>();

  public ConfigFile getOrCreate(String path) {
    return pathToConfigFileMap.computeIfAbsent(path, p -> new ConfigFileBridge(
      new EventConfigFileFromStorage(path, storage)));
  }

  @Override
  public void close() {
    storage.close();
  }
}
