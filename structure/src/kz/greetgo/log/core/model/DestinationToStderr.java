package kz.greetgo.log.core.model;

import kz.greetgo.log.core.parser.model.ParseErrorHandler;
import kz.greetgo.log.core.parser.model.SubInstruction;
import kz.greetgo.log.core.parser.model.TopInstruction;

import static kz.greetgo.log.core.model.Instr.DESTINATION_TO;
import static kz.greetgo.log.core.model.Instr.STDERR;
import static kz.greetgo.log.core.model.Instr.TYPE;

public class DestinationToStderr extends DestinationTo {
  public DestinationToStderr(String name, TopInstruction source) {
    super(name, source);
  }

  @Override
  protected String definitionStr() {
    return "stderr";
  }

  @Override
  public void serializeTo(StringBuilder sb) {
    sb.append(DESTINATION_TO).append(' ').append(name).append('\n');
    sb.append("  " + TYPE + ' ' + STDERR + '\n');
  }

  @Override
  public String shortInfo() {
    return name;
  }
}
