package kz.greetgo.log.core.parser.model;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class TopInstruction implements InstructionPartOwner {
  public final int line;
  public final boolean enabled;
  public final InstructionPartList header = new InstructionPartList(this);
  public final List<SubInstruction> subList = new ArrayList<>();

  public void addSubInstruction(SubInstruction subInstruction) {
    subList.add(subInstruction);
  }

  private Map<String, SubInstruction> _subMap = null;

  public SubInstruction byFirstWord(String firstWord) {
    if (_subMap == null) {
      Map<String, SubInstruction> subMap = new HashMap<>();
      for (SubInstruction subInstruction : subList) {
        subMap.put(subInstruction.first(), subInstruction);
      }
      _subMap = subMap;
    }
    return _subMap.get(firstWord);
  }

  @Override
  public int lineNo() {
    return line;
  }

  public TopInstruction addSubInstruction(String line) {
    subList.add(new SubInstruction(0).addPart(line));
    return this;
  }
}
