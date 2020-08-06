package kz.greetgo.logging.zookeeper.core;

import kz.greetgo.logging.structure.config.ConfigFile;
import kz.greetgo.logging.zookeeper.config.EventConfigFileFromStorage;
import kz.greetgo.logging.zookeeper.config.EventConfigStorageZooKeeper;
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
