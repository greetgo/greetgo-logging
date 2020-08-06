package kz.greetgo.logging.structure.model;

import kz.greetgo.logging.structure.parser.model.TopInstruction;

import static kz.greetgo.logging.structure.model.Instr.DESTINATION_TO;
import static kz.greetgo.logging.structure.model.Instr.STDOUT;
import static kz.greetgo.logging.structure.model.Instr.TYPE;

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
