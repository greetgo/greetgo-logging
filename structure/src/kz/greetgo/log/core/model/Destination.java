package kz.greetgo.log.core.model;

import kz.greetgo.log.core.parser.model.InstructionPart;
import kz.greetgo.log.core.parser.model.ParseError;
import kz.greetgo.log.core.parser.model.ParseErrorCode;
import kz.greetgo.log.core.parser.model.ParseErrorHandler;
import kz.greetgo.log.core.parser.model.SubInstruction;
import kz.greetgo.log.core.parser.model.TopInstruction;
import kz.greetgo.log.core.util.ReaderUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static kz.greetgo.log.core.model.Instr.DESTINATION;
import static kz.greetgo.log.core.model.Instr.LAYOUT;
import static kz.greetgo.log.core.model.Instr.LEVEL;

/**
 * Напрваление логирования (аналог Appender)
 */
@RequiredArgsConstructor
public class Destination {

  /**
   * Имя направления логирования
   */
  public final @NonNull String name;

  /**
   * Тип направления логирования
   */
  public DestinationTo to;

  /**
   * Формат вывода сообщений
   */
  public Layout layout;

  /**
   * Источник данной модели
   */
  public final @NonNull TopInstruction source;


  /**
   * Имя типа направления логирования, по которому бедут найден объект для заполнения поля {@link #to}
   */
  public final @NonNull InstructionPart destinationToName;

  /**
   * Имя файла логирования с подпутём относительно папки логирования
   * <p>
   * Расширение отсутствует
   * <p>
   * Используется если {@link #to} instanceOf {@link DestinationToRollingFile}
   */
  public final String fileNameWithSubPath;

  /**
   * Имя формата вывода сообщений. Если не указан, то используется `default`
   */
  public InstructionPart layoutName;

  /**
   * Уровень сообщений который нужно пропускать
   */
  public Level level;

  public boolean enabled() {
    return source.enabled;
  }

  public static Optional<Destination> readFrom(TopInstruction topInstruction, ParseErrorHandler errorHandler) {

    Optional<InstructionPart> atName = topInstruction.header.at(1);
    Optional<InstructionPart> atDestinationToName = topInstruction.header.at(2);
    Optional<InstructionPart> atFileNameWithSubPath = topInstruction.header.at(3);

    if (atName.isEmpty()) {
      errorHandler.happenedParseError(new ParseError(topInstruction.line,
                                                     topInstruction.header.get(0).range,
                                                     ParseErrorCode.DESTINATION_WITHOUT_NAME,
                                                     "Инструкция destination без имени - нет первого параметра"));
      return Optional.empty();
    }

    if (atDestinationToName.isEmpty()) {
      errorHandler.happenedParseError(new ParseError(topInstruction.line,
                                                     topInstruction.header.get(1).range,
                                                     ParseErrorCode.DESTINATION_WITHOUT_DESTINATION_TO_NAME,
                                                     "Инструкция destination без имени назначения" +
                                                       " - нет второго параметра"));
      return Optional.empty();
    }

    var d = new Destination(atName.map(InstructionPart::str).orElseThrow(),
                            topInstruction,
                            atDestinationToName.orElseThrow(),
                            atFileNameWithSubPath.map(InstructionPart::str).orElse(null));

    d.readSubInstructions(topInstruction, errorHandler);

    return Optional.of(d);
  }

  private void readSubInstructions(TopInstruction topInstruction, ParseErrorHandler errorHandler) {
    for (SubInstruction subInstruction : topInstruction.subList) {
      readSubInstruction(errorHandler, subInstruction);
    }
  }

  private void readSubInstruction(ParseErrorHandler errorHandler, SubInstruction subInstruction) {
    switch (subInstruction.first()) {
      default:
        errorHandler.happenedParseError(new ParseError(subInstruction.line, subInstruction.partList.get(0).range,
                                                       ParseErrorCode.UNKNOWN_SUB_INSTRUCTION_FOR_DESTINATION,
                                                       "Неизвестная подинструкция для destination: " +
                                                         "`" + subInstruction.first() + "`"));
        return;

      case LEVEL: {
        ReaderUtil.readLevel(subInstruction, errorHandler)
                  .ifPresent(level -> this.level = level);
        return;
      }
      case LAYOUT: {
        InstructionPart atName = subInstruction.partList.at(1).orElse(null);
        if (atName == null) {
          errorHandler.happenedParseError(new ParseError(subInstruction.line, subInstruction.partList.get(0).range,
                                                         ParseErrorCode.LAYOUT_NAME_SKIPPED,
                                                         "Пропущено имя лэйаута"));
          return;
        }
        this.layoutName = atName;
        return;
      }
    }
  }

  @Override
  public String toString() {
    return "Destination{"
      + name
      + (to == null ? "<No DestinationTo>" : " " + to.shortInfo())
      + (fileNameWithSubPath == null ? "" : " " + fileNameWithSubPath)
      + (layout == null ? " <No Layout>" : " " + layout)
      + '}';
  }

  public void serializeTo(StringBuilder sb) {
    sb.append(DESTINATION + " ").append(name).append(' ').append(to.name);
    if (fileNameWithSubPath != null) {
      sb.append(' ').append(fileNameWithSubPath);
    }
    sb.append('\n');

    if (layoutName != null) {
      sb.append("  ").append(LAYOUT).append(' ').append(layoutName.str()).append('\n');
    }

    if (level != null) {
      sb.append("  ").append(LEVEL).append(' ').append(level).append('\n');
    }

  }

}
