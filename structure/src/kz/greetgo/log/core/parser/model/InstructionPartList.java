package kz.greetgo.log.core.parser.model;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class InstructionPartList implements Iterable<InstructionPart> {

  private final InstructionPartOwner owner;
  private final List<InstructionPart> list = new ArrayList<>();

  public void parse(int offset, String line) {

    char[] chars = line.toCharArray();
    int charsLength = chars.length;

    InstructionPart last = null;

    for (int i = offset; i < charsLength; i++) {
      if (chars[i] == ':') {
        charsLength = i;
        i++;
        while (i < chars.length && Character.isWhitespace(chars[i])) {
          i++;
        }
        int start = i;
        for (i = chars.length - 1; i > start && Character.isWhitespace(chars[i]); i--) ;
        int end = i + 1;
        last = new InstructionPart(owner, Range.of(start, end), chars);
        break;
      }
    }

    int start = offset;

    while (start < charsLength) {
      while (start < charsLength && Character.isWhitespace(chars[start])) {
        start++;
      }

      int end = start;
      while (end < charsLength && !Character.isWhitespace(chars[end])) {
        end++;
      }

      if (end > start) {
        list.add(new InstructionPart(owner, Range.of(start, end), chars));
      }

      start = end;
    }

    if (last != null) {
      list.add(last);
    }

  }

  public InstructionPart get(int i) {
    return list.get(i);
  }

  public InstructionPart first() {
    return get(0);
  }

  public Optional<InstructionPart> at(int i) {
    if (i < 0) {
      throw new IllegalArgumentException("i = " + i);
    }
    if (i >= list.size()) {
      return Optional.empty();
    }
    return Optional.ofNullable(list.get(i));
  }

  public int size() {
    return list.size();
  }

  @Override
  public @NotNull Iterator<InstructionPart> iterator() {
    return list.iterator();
  }
}
