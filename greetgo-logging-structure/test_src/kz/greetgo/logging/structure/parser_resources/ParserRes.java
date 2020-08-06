package kz.greetgo.logging.structure.parser_resources;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ParserRes {
  public String textFrom(String resourceName) {
    try {
      return new String(getClass().getResourceAsStream(resourceName).readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
