package kz.greetgo.logging.structure.model;

import kz.greetgo.logging.structure.parser.model.TopInstruction;

import static kz.greetgo.logging.structure.model.Instr.DESTINATION_TO;
import static kz.greetgo.logging.structure.model.Instr.STDERR;
import static kz.greetgo.logging.structure.model.Instr.TYPE;

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
