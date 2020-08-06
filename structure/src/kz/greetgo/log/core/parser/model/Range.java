package kz.greetgo.log.core.parser.model;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor
public class Range {
  public final int start, end;

  public static Range of(int start, int end) {
    return new Range(start, end);
  }

  public static Range offsetCount(int offset, int count) {
    return of(offset, offset + count);
  }

  public int count() {
    return end - start;
  }

  public String toString() {
    return "Range(" + start + ':' + count() + ")";
  }
}
