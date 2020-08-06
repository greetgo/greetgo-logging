package kz.greetgo.logging.structure.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

public class ConfigFileToNativeFile implements ConfigFile {

  private final Path pathToFile;

  private ConfigFileToNativeFile(Path pathToFile) {
    this.pathToFile = pathToFile;
  }

  public static ConfigFileToNativeFile of(Path pathToFile) {
    return new ConfigFileToNativeFile(pathToFile);
  }

  @Override
  public String read() {
    if (!Files.exists(pathToFile)) {
      return null;
    }
    try {
      return Files.readString(pathToFile);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void write(String content) {
    if (content == null) {
      if (Files.exists(pathToFile)) {
        try {
          Files.deleteIfExists(pathToFile);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      return;
    }
    pathToFile.toFile().getParentFile().mkdirs();
    try {
      Files.writeString(pathToFile, content);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Date lastModifiedAt() {
    if (!Files.exists(pathToFile)) {
      return null;
    }

    try {
      return Date.from(Files
                         .getLastModifiedTime(pathToFile)
                         .toInstant());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }
}
