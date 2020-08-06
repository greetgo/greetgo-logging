package kz.greetgo.log.core.model;

import kz.greetgo.log.core.parser.model.TopInstruction;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static kz.greetgo.log.core.model.Instr.ROLLING_FILE;
import static kz.greetgo.log.core.model.Instr.STDERR;
import static kz.greetgo.log.core.model.Instr.STDOUT;

public class DestinationToFactory {
  private static final Map<String, BiFunction<String, TopInstruction, ? extends DestinationTo>> map;

  static {
    map = new HashMap<>();
    map.put(ROLLING_FILE, DestinationToRollingFile::new);
    map.put(STDOUT, DestinationToStdout::new);
    map.put(STDERR, DestinationToStderr::new);
  }

  public static DestinationTo create(String type, String name, TopInstruction source) {
    var fab = map.get(type);
    return fab == null ? null : fab.apply(name, source);
  }
}
