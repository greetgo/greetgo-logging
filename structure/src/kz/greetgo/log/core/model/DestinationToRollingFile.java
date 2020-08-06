package kz.greetgo.log.core.model;

import kz.greetgo.log.core.parser.model.InstructionPart;
import kz.greetgo.log.core.parser.model.ParseError;
import kz.greetgo.log.core.parser.model.ParseErrorHandler;
import kz.greetgo.log.core.parser.model.SubInstruction;
import kz.greetgo.log.core.parser.model.TopInstruction;
import kz.greetgo.log.core.util.ByteSize;

import java.util.Optional;

import static kz.greetgo.log.core.model.Instr.DESTINATION_TO;
import static kz.greetgo.log.core.model.Instr.FILES_COUNT;
import static kz.greetgo.log.core.model.Instr.MAX_FILE_SIZE;
import static kz.greetgo.log.core.model.Instr.ROLLING_FILE;
import static kz.greetgo.log.core.model.Instr.TYPE;
import static kz.greetgo.log.core.parser.model.ParseErrorCode.LEFT_VALUE_IN__DESTINATION_TO_FILES_COUNT;
import static kz.greetgo.log.core.parser.model.ParseErrorCode.LEFT_VALUE_IN__DESTINATION_TO_FILE_SIZE;
import static kz.greetgo.log.core.parser.model.ParseErrorCode.NO_VALUE_IN__DESTINATION_TO_FILES_COUNT;
import static kz.greetgo.log.core.parser.model.ParseErrorCode.NO_VALUE_IN__DESTINATION_TO_FILE_SIZE;
import static kz.greetgo.log.core.parser.model.ParseErrorCode.UNKNOWN_SUB_INSTRUCTION_FOR_DESTINATION_TO_ROLLING_FILE;

public class DestinationToRollingFile extends DestinationTo {
  public long maxFileSizeInBytes = 100 * 1024;
  public int filesCount = 3;

  public DestinationToRollingFile(String name, TopInstruction source) {
    super(name, source);
  }

  @Override
  protected String definitionStr() {
    return "rolling_file (" + maxFileSizeInBytes + "b * " + filesCount + " files)";
  }

  protected boolean readSubInstruction(SubInstruction subInstruction, ParseErrorHandler errorHandler) {
    if (super.readSubInstruction(subInstruction, errorHandler)) {
      return true;
    }

    switch (subInstruction.first()) {
      default:
        errorHandler.happenedParseError(new ParseError(subInstruction.line,
                                                       subInstruction.partList.get(0).range,
                                                       UNKNOWN_SUB_INSTRUCTION_FOR_DESTINATION_TO_ROLLING_FILE,
                                                       "Неизвестная подинструкция `" + subInstruction.first() + '`'
                                                         + " для DestinationToRollingFile"));
        return true;

      case TYPE:
        // ignore it because it has been already used
        return true;

      case Instr.MAX_FILE_SIZE: {

        Optional<InstructionPart> fileSize = subInstruction.partList.at(1);
        if (fileSize.isEmpty()) {
          errorHandler.happenedParseError(new ParseError(subInstruction.line,
                                                         subInstruction.partList.get(0).range,
                                                         NO_VALUE_IN__DESTINATION_TO_FILE_SIZE,
                                                         "Нет значения destination_to.file_size"));
          return true;
        }

        try {
          maxFileSizeInBytes = ByteSize.parse(fileSize.get().str()).sizeInBytes;
          return true;
        } catch (Exception e) {
          errorHandler.happenedParseError(new ParseError(subInstruction.line,
                                                         subInstruction.partList.get(1).range,
                                                         LEFT_VALUE_IN__DESTINATION_TO_FILE_SIZE,
                                                         "Не верное значение destination_to.file_size: "
                                                           + e.getClass().getSimpleName() + ": " + e.getMessage()));
          return true;
        }
      }

      case Instr.FILES_COUNT: {

        Optional<InstructionPart> filesCount = subInstruction.partList.at(1);
        if (filesCount.isEmpty()) {
          errorHandler.happenedParseError(new ParseError(subInstruction.line,
                                                         subInstruction.partList.get(0).range,
                                                         NO_VALUE_IN__DESTINATION_TO_FILES_COUNT,
                                                         "Нет значения destination_to.files_count"));
          return true;
        }

        try {
          this.filesCount = Integer.parseInt(filesCount.get().str());
          return true;
        } catch (NumberFormatException e) {
          errorHandler.happenedParseError(new ParseError(subInstruction.line,
                                                         subInstruction.partList.get(1).range,
                                                         LEFT_VALUE_IN__DESTINATION_TO_FILES_COUNT,
                                                         "Не верное значения destination_to.files_count: "
                                                           + e.getMessage()));
          return true;
        }
      }
    }
  }

  @Override
  public void serializeTo(StringBuilder sb) {
    sb.append(DESTINATION_TO).append(' ').append(name).append('\n');
    sb.append("  " + TYPE + ' ' + ROLLING_FILE + '\n');
    sb.append("  " + MAX_FILE_SIZE + ' ').append(maxFileSizeInBytes).append('\n');
    sb.append("  " + FILES_COUNT + ' ').append(filesCount).append('\n');
  }

  @Override
  public String shortInfo() {
    return name + '[' + filesCount + '*' + maxFileSizeInBytes + ']';
  }
}
