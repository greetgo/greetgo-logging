package kz.greetgo.logging.structure.util;

import kz.greetgo.logging.structure.model.Level;
import kz.greetgo.logging.structure.parser.model.InstructionPart;
import kz.greetgo.logging.structure.parser.model.ParseError;
import kz.greetgo.logging.structure.parser.model.ParseErrorCode;
import kz.greetgo.logging.structure.parser.model.ParseErrorHandler;
import kz.greetgo.logging.structure.parser.model.SubInstruction;

import java.util.Optional;

public class ReaderUtil {
  public static Optional<Level> readLevel(SubInstruction subInstruction, ParseErrorHandler errorHandler) {
    Optional<InstructionPart> atValue = subInstruction.partList.at(1);

    if (atValue.isEmpty()) {
      errorHandler.happenedParseError(new ParseError(subInstruction.line, subInstruction.partList.get(0).range,
                                                     ParseErrorCode.SUB_INSTRUCTION_LEVEL_WITHOUT_VALUE,
                                                     "Подинструкция level без значения"));
      return Optional.empty();
    }

    try {
      var level = Level.valueOf(atValue.orElseThrow().str());
      return Optional.of(level);
    } catch (IllegalArgumentException e) {
      errorHandler.happenedParseError(new ParseError(subInstruction.line, subInstruction.partList.get(0).range,
                                                     ParseErrorCode.SUB_INSTRUCTION_LEVEL_WITH_UNKNOWN_VALUE,
                                                     "Подинструкция level с неизвестным значением: " +
                                                       "`" + atValue.orElseThrow().str() + "`"));
      return Optional.empty();
    }
  }
}
