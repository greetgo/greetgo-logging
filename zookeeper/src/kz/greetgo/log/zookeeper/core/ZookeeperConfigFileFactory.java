package kz.greetgo.log.zookeeper.core;

import kz.greetgo.log.core.config.ConfigFile;
import kz.greetgo.log.zookeeper.config.EventConfigFileFromStorage;
import kz.greetgo.log.zookeeper.config.EventConfigStorageZooKeeper;
import lombok.NonNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class ZookeeperConfigFileFactory implements AutoCloseable {

  static {
    CheckLibs.checkLibs();
  }

  private final EventConfigStorageZooKeeper storage;

  public ZookeeperConfigFileFactory(String rootPath,
                                    @NonNull Supplier<String> zookeeperServers,
                                    @NonNull IntSupplier sessionTimeout) {
    storage = new EventConfigStorageZooKeeper(rootPath, zookeeperServers, sessionTimeout);
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
