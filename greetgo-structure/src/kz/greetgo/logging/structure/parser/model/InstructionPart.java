package kz.greetgo.logging.structure.parser.model;

public class InstructionPart {

  public final InstructionPartOwner owner;
  public final Range range;
  private final String content;

  public InstructionPart(InstructionPartOwner owner, Range range, char[] chars) {
    this.owner = owner;
    this.range = range;
    this.content = new String(chars, range.start, range.count());
  }

  public InstructionPart(InstructionPartOwner owner, Range range, String content) {
    this.owner = owner;
    this.range = range;
    this.content = content;
  }

  public int lineNo() {
    return owner.lineNo();
  }

  public String str() {
    return content;
  }

}
