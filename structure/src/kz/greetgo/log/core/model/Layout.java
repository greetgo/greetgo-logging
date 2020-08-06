package kz.greetgo.log.core.model;

import kz.greetgo.log.core.parser.model.InstructionPart;
import kz.greetgo.log.core.parser.model.ParseError;
import kz.greetgo.log.core.parser.model.ParseErrorCode;
import kz.greetgo.log.core.parser.model.ParseErrorHandler;
import kz.greetgo.log.core.parser.model.SubInstruction;
import kz.greetgo.log.core.parser.model.TopInstruction;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static kz.greetgo.log.core.model.Instr.COLORED_PATTERN;
import static kz.greetgo.log.core.model.Instr.DEFAULT;
import static kz.greetgo.log.core.model.Instr.LAYOUT;
import static kz.greetgo.log.core.model.Instr.PATTERN;

/**
 * Способ оттображения событий логов в текст
 */
@RequiredArgsConstructor
public class Layout {
  /**
   * Имя оттображения
   */
  public final @NonNull String name;

  /**
   * Источник данной модели
   */
  public final @NonNull TopInstruction source;

  /**
   * Формула оттображения для нормального логирования
   */
  public final @NonNull InstructionPart pattern;

  /**
   * Формула оттображения для цветного логирования. Если не определена, то используется {@link #pattern}.
   */
  public final InstructionPart coloredPattern;

  @Override
  public String toString() {
    return "Layout{" + name
      + ":`" + cutLen(10, pattern.str()) + "`"
      + (coloredPattern == null ? "" : "/colored:`" + cutLen(11, coloredPattern.str()) + "`")
      + '}';
  }

  private static String cutLen(int len, String str) {
    if (str == null) {
      return null;
    }
    if (str.length() <= len) {
      return str;
    }
    return str.substring(0, len - 1) + "…";
  }

  public void serializeTo(StringBuilder sb) {
    sb.append(LAYOUT).append(' ').append(name).append('\n');
    sb.append("  ").append(PATTERN).append(" : ").append(pattern.str()).append("\n");
    if (coloredPattern != null) {
      sb.append("  ").append(COLORED_PATTERN).append(" : ").append(coloredPattern.str()).append("\n");
    }
  }

  public boolean enabled() {
    return source.enabled;
  }

  public static Optional<Layout> readFrom(TopInstruction topInstruction, ParseErrorHandler errorHandler) {
    return new LayoutBuilder(topInstruction, errorHandler).build();
  }

  @RequiredArgsConstructor
  private static class LayoutBuilder {
    final TopInstruction topInstruction;
    final ParseErrorHandler errorHandler;

    String name = null;
    InstructionPart pattern = null;
    InstructionPart coloredPattern = null;

    public Optional<Layout> build() {
      name = topInstruction.header.at(1).map(InstructionPart::str).orElse(DEFAULT);

      for (SubInstruction subInstruction : topInstruction.subList) {
        readSubInstruction(subInstruction);
      }

      if (pattern == null) {
        errorHandler.happenedParseError(new ParseError(topInstruction.line, topInstruction.header.first().range,
                                                       ParseErrorCode.LAYOUT_WITHOUT_PATTERN,
                                                       "У layout отсутствует pattern"));
        return Optional.empty();
      }

      return Optional.of(new Layout(name, topInstruction, pattern, coloredPattern));
    }

    private void readSubInstruction(SubInstruction subInstruction) {
      switch (subInstruction.first()) {
        default:
          errorHandler.happenedParseError(new ParseError(subInstruction.line, subInstruction.partList.first().range,
                                                         ParseErrorCode.UNKNOWN_SUB_INSTRUCTION_FOR_LAYOUT,
                                                         "Неизвестная подинструкция для layout: "
                                                           + "`" + subInstruction.first() + "`"));
          return;

        case Instr.PATTERN: {
          Optional<InstructionPart> atValue = subInstruction.partList.at(1);
          if (atValue.isEmpty()) {
            errorHandler.happenedParseError(new ParseError(subInstruction.line, subInstruction.partList.first().range,
                                                           ParseErrorCode.SUB_INSTRUCTION_PATTERN_WITHOUT_VALUE,
                                                           "Подинструкция " + PATTERN + " без параметра"));
            return;
          }
          pattern = atValue.orElseThrow();
          return;
        }

        case Instr.COLORED_PATTERN: {
          Optional<InstructionPart> atValue = subInstruction.partList.at(1);
          if (atValue.isEmpty()) {
            errorHandler.happenedParseError(new ParseError(subInstruction.line, subInstruction.partList.first().range,
                                                           ParseErrorCode.SUB_INSTRUCTION_COLORED_PATTERN_WITHOUT_VALUE,
                                                           "Подинструкция " + COLORED_PATTERN + " без параметра"));
            return;
          }
          coloredPattern = atValue.orElseThrow();
          return;
        }
      }
    }
  }

  public String getPattern(boolean colored) {
    return (!colored || coloredPattern == null ? pattern : coloredPattern).str();
  }
}
