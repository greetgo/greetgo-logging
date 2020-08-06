package kz.greetgo.logging.structure.model;

public interface Instr {

  //Top Instructions
  String LAYOUT = "layout";
  String DESTINATION_TO = "destination_to";
  String DESTINATION = "destination";
  String CATEGORY = "category";
  String ROOT = "root";

  //Sub Instructions
  String LEVEL = "level";
  String TYPE = "type";
  String ROLLING_FILE = "rolling_file";
  String STDOUT = "stdout";
  String STDERR = "stderr";
  String MAX_FILE_SIZE = "max_file_size";
  String FILES_COUNT = "files_count";
  String ASSIGN_TO = "assign_to";
  String PATTERN = "pattern";
  String COLORED_PATTERN = "colored_pattern";

  // etc
  String DEFAULT = "default";

}
