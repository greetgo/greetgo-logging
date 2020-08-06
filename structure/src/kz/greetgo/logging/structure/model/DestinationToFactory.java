package kz.greetgo.logging.structure.model;

import kz.greetgo.logging.structure.parser.model.TopInstruction;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class DestinationToFactory {
  private static final Map<String, BiFunction<String, TopInstruction, ? extends DestinationTo>> map;

  static {
    map = new HashMap<>();
    map.put(Instr.ROLLING_FILE, DestinationToRollingFile::new);
    map.put(Instr.STDOUT, DestinationToStdout::new);
    map.put(Instr.STDERR, DestinationToStderr::new);
  }

  public static DestinationTo create(String type, String name, TopInstruction source) {
    var fab = map.get(type);
    return fab == null ? null : fab.apply(name, source);
  }
}
