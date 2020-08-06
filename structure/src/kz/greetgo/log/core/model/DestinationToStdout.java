package kz.greetgo.log.core.model;

import kz.greetgo.log.core.parser.model.ParseErrorHandler;
import kz.greetgo.log.core.parser.model.SubInstruction;
import kz.greetgo.log.core.parser.model.TopInstruction;

import static kz.greetgo.log.core.model.Instr.DESTINATION_TO;
import static kz.greetgo.log.core.model.Instr.STDOUT;
import static kz.greetgo.log.core.model.Instr.TYPE;

public class DestinationToStdout extends DestinationTo {
  public DestinationToStdout(String name, TopInstruction source) {
    super(name, source);
  }

  @Override
  protected String definitionStr() {
    return "stdout";
  }

  @Override
  public void serializeTo(StringBuilder sb) {
    sb.append(DESTINATION_TO).append(' ').append(name).append('\n');
    sb.append("  " + TYPE + ' ' + STDOUT + '\n');
  }

  @Override
  public String shortInfo() {
    return name;
  }
}
