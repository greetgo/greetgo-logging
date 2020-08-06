package kz.greetgo.logging.structure.model;

import kz.greetgo.logging.structure.parser.model.InstructionPart;
import kz.greetgo.logging.structure.parser.model.ParseError;
import kz.greetgo.logging.structure.parser.model.ParseErrorCode;
import kz.greetgo.logging.structure.parser.model.ParseErrorHandler;
import kz.greetgo.logging.structure.parser.model.SubInstruction;
import kz.greetgo.logging.structure.parser.model.TopInstruction;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static kz.greetgo.logging.structure.model.Instr.LAYOUT;
import static kz.greetgo.logging.structure.model.Instr.TYPE;
import static kz.greetgo.logging.structure.parser.model.ParseErrorCode.DESTINATION_TO_TYPE_VALUE_IS_ABSENT;
import static kz.greetgo.logging.structure.parser.model.ParseErrorCode.DESTINATION_TO_TYPE_VALUE_IS_ILLEGAL;
import static kz.greetgo.logging.structure.parser.model.ParseErrorCode.DESTINATION_TO_WITHOUT_NAME;
import static kz.greetgo.logging.structure.parser.model.ParseErrorCode.DESTINATION_TO_WITHOUT_SUB_INSTRUCTION_TYPE;

@RequiredArgsConstructor
public abstract class DestinationTo {
  public final String name;
  public final TopInstruction source;

  public InstructionPart layoutName;
  public Layout layout;

  protected final void readSubInstructions(TopInstruction topInstruction, ParseErrorHandler errorHandler) {
    for (SubInstruction subInstruction : topInstruction.subList) {
      readSubInstruction(subInstruction, errorHandler);
    }
  }

  protected boolean readSubInstruction(SubInstruction subInstruction, ParseErrorHandler errorHandler) {
    //noinspection SwitchStatementWithTooFewBranches
    switch (subInstruction.first()) {
      default:
        return false;

      case LAYOUT: {
        Optional<InstructionPart> atName = subInstruction.partList.at(1);
        if (atName.isEmpty()) {
          errorHandler.happenedParseError(new ParseError(subInstruction.line, subInstruction.partList.first().range,
                                                         ParseErrorCode.LAYOUT_NAME_SKIPPED,
                                                         "Пропущено имя лэйаута"));
          return true;
        }
        layoutName = atName.orElseThrow();
        return true;
      }
    }
  }

  protected abstract String definitionStr();

  public boolean enabled() {
    return source.enabled;
  }

  public static Optional<DestinationTo> readFrom(TopInstruction topInstruction, ParseErrorHandler errorHandler) {

    Optional<InstructionPart> name = topInstruction.header.at(1);
    if (name.isEmpty()) {
      errorHandler.happenedParseError(new ParseError(topInstruction.line, topInstruction.header.first().range,
                                                     DESTINATION_TO_WITHOUT_NAME,
                                                     "Инстукция destination_type без имени"));
      return Optional.empty();
    }

    SubInstruction type = topInstruction.byFirstWord(TYPE);
    if (type == null) {
      errorHandler.happenedParseError(new ParseError(topInstruction.line, topInstruction.header.first().range,
                                                     DESTINATION_TO_WITHOUT_SUB_INSTRUCTION_TYPE,
                                                     "В инстукции destination_to пропущена подинструкция type"));
      return Optional.empty();
    }

    Optional<InstructionPart> typeValue = type.partList.at(1);
    if (typeValue.isEmpty()) {
      errorHandler.happenedParseError(new ParseError(topInstruction.line, type.partList.get(0).range,
                                                     DESTINATION_TO_TYPE_VALUE_IS_ABSENT,
                                                     "В подинстукции destination_type.type отсутствует значение"));
      return Optional.empty();
    }

    DestinationTo destinationTo = DestinationToFactory.create(typeValue.get().str(),
                                                              name.get().str(),
                                                              topInstruction);
    if (destinationTo == null) {
      errorHandler.happenedParseError(new ParseError(topInstruction.line, type.partList.get(0).range,
                                                     DESTINATION_TO_TYPE_VALUE_IS_ILLEGAL,
                                                     "В подинстукции destination_type.type неверное значение: "
                                                       + typeValue.get().str()));
      return Optional.empty();
    }

    destinationTo.readSubInstructions(topInstruction, errorHandler);

    return Optional.of(destinationTo);
  }

  @Override
  public String toString() {
    return "DestinationTo{" + name + ' ' + definitionStr() + '}';
  }

  public abstract void serializeTo(StringBuilder sb);

  public abstract String shortInfo();
}
