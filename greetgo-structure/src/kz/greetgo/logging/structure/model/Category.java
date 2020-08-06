package kz.greetgo.logging.structure.model;

import kz.greetgo.logging.structure.parser.model.InstructionPart;
import kz.greetgo.logging.structure.parser.model.ParseError;
import kz.greetgo.logging.structure.parser.model.ParseErrorCode;
import kz.greetgo.logging.structure.parser.model.ParseErrorHandler;
import kz.greetgo.logging.structure.parser.model.SubInstruction;
import kz.greetgo.logging.structure.parser.model.TopInstruction;
import kz.greetgo.logging.structure.util.ReaderUtil;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static kz.greetgo.logging.structure.model.Instr.ASSIGN_TO;
import static kz.greetgo.logging.structure.model.Instr.CATEGORY;
import static kz.greetgo.logging.structure.model.Instr.LEVEL;
import static kz.greetgo.logging.structure.model.Instr.ROOT;

/**
 * Категория журналов
 * <p>
 * Она объединяет журналы по имени, т.е. она содержит в себе все журналы, имя которых начинается с {@link #name}
 */
@RequiredArgsConstructor
public class Category {
  /**
   * Имя категории - с него должны начинаться имена логеров, которые принадлежат данной категории
   * <p>
   * Если это поле null, то это - root
   */
  public final String name;

  /**
   * Исходная структура, из которой прирасился данный объект
   */
  public final TopInstruction source;

  /**
   * Уровень фильтрации жирнала - пропускать нужно только те записи, что крепче или равен этому уровню
   */
  public Level level;

  /**
   * Список имён assign_to инструкций
   */
  public final List<InstructionPart> assignToNameList = new ArrayList<>();

  /**
   * Заполненный по полю {@link #assignToNameList} список назначений логирования
   */
  public final List<Destination> assignToList = new ArrayList<>();

  public boolean enabled() {
    return source.enabled;
  }

  public static Optional<Category> readFrom(TopInstruction topInstruction, ParseErrorHandler errorHandler) {

    String first = topInstruction.header.first().str();

    String name = null;

    if (CATEGORY.equals(first)) {

      Optional<InstructionPart> atName = topInstruction.header.at(1);

      if (atName.isEmpty()) {
        errorHandler.happenedParseError(new ParseError(topInstruction.line, topInstruction.header.get(0).range,
                                                       ParseErrorCode.CATEGORY_WITHOUT_NAME,
                                                       "Инструкция category без имени - отсутствует параметр"));
        return Optional.empty();
      }

      name = atName.map(InstructionPart::str).orElseThrow();

    }
    {
      Category ret = new Category(name, topInstruction);
      ret.readSubInstructions(topInstruction, errorHandler);
      return Optional.of(ret);
    }
  }

  private void readSubInstructions(TopInstruction topInstruction, ParseErrorHandler errorHandler) {
    for (SubInstruction subInstruction : topInstruction.subList) {
      readSubInstruction(subInstruction, errorHandler);
    }
  }

  private void readSubInstruction(SubInstruction subInstruction, ParseErrorHandler errorHandler) {
    switch (subInstruction.first()) {
      default:
        errorHandler.happenedParseError(new ParseError(subInstruction.line, subInstruction.partList.get(0).range,
                                                       ParseErrorCode.UNKNOWN_SUB_INSTRUCTION_FOR_CATEGORY,
                                                       "Неизвестная подинструкция для " + getClass().getSimpleName()));
        return;

      case "assign_to": {
        int size = subInstruction.partList.size();
        for (int i = 1; i < size; i++) {
          assignToNameList.add(subInstruction.partList.get(i));
        }
        return;
      }

      case "level": {
        ReaderUtil.readLevel(subInstruction, errorHandler)
                  .ifPresent(level -> this.level = level);
        return;
      }
    }
  }

  @Override
  public String toString() {
    return "Category{" + (name == null ? "<ROOT>" : name) + '}';
  }

  public void serializeTo(StringBuilder sb) {
    if (name == null) {
      sb.append(ROOT).append('\n');
    } else {
      sb.append(CATEGORY).append(' ').append(name).append('\n');
    }

    if (level != null) {
      sb.append("  ").append(LEVEL).append(' ').append(level).append('\n');
    }
    for (Destination destination : assignToList) {
      sb.append("  ").append(ASSIGN_TO).append(' ').append(destination.name).append('\n');
    }
  }

}
