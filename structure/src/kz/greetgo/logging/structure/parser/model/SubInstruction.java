package kz.greetgo.logging.structure.parser.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SubInstruction implements InstructionPartOwner {
  public final int line;
  public final InstructionPartList partList = new InstructionPartList(this);

  public String first() {
    return partList.first().str();
  }

  @Override
  public int lineNo() {
    return line;
  }

  public SubInstruction addPart(String line) {
    partList.parse(0, line);
    return this;
  }
}
