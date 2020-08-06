package kz.greetgo.logging.zookeeper.core;

import kz.greetgo.logging.structure.config.ConfigFile;
import kz.greetgo.logging.zookeeper.config.EventConfigFile;

import java.util.Date;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ConfigFileBridge implements ConfigFile, AutoCloseable {

  private final EventConfigFile eventConfigFile;

  public ConfigFileBridge(EventConfigFile eventConfigFile) {
    this.eventConfigFile = eventConfigFile;
  }

  @Override
  public String read() {
    byte[] content = eventConfigFile.readContent();
    if (content == null) {
      return null;
    }
    return new String(content, UTF_8);
  }

  @Override
  public void write(String content) {
    eventConfigFile.writeContent(content == null ? null : content.getBytes(UTF_8));
  }

  @Override
  public Date lastModifiedAt() {
    return eventConfigFile.lastModifiedAt().orElse(null);
  }

  @Override
  public void close() {
    eventConfigFile.close();
  }
}
