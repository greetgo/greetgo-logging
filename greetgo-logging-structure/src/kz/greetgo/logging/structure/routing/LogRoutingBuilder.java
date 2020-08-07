package kz.greetgo.logging.structure.routing;

import kz.greetgo.logging.structure.model.Level;
import kz.greetgo.logging.structure.parser.TokenParser;
import kz.greetgo.logging.structure.parser.model.TopInstruction;
import kz.greetgo.logging.structure.util.ByteSize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static kz.greetgo.logging.structure.model.Instr.ASSIGN_TO;
import static kz.greetgo.logging.structure.model.Instr.CATEGORY;
import static kz.greetgo.logging.structure.model.Instr.COLORED_PATTERN;
import static kz.greetgo.logging.structure.model.Instr.DEFAULT;
import static kz.greetgo.logging.structure.model.Instr.DESTINATION;
import static kz.greetgo.logging.structure.model.Instr.DESTINATION_TO;
import static kz.greetgo.logging.structure.model.Instr.FILES_COUNT;
import static kz.greetgo.logging.structure.model.Instr.LAYOUT;
import static kz.greetgo.logging.structure.model.Instr.LEVEL;
import static kz.greetgo.logging.structure.model.Instr.MAX_FILE_SIZE;
import static kz.greetgo.logging.structure.model.Instr.PATTERN;
import static kz.greetgo.logging.structure.model.Instr.ROLLING_FILE;
import static kz.greetgo.logging.structure.model.Instr.ROOT;
import static kz.greetgo.logging.structure.model.Instr.STDERR;
import static kz.greetgo.logging.structure.model.Instr.STDOUT;
import static kz.greetgo.logging.structure.model.Instr.TYPE;

public class LogRoutingBuilder {

  public static class PrintDestinationTo {
    private final String name;
    private String type = null;

    private PrintDestinationTo(String name) {
      this.name = requireNonNull(name);
    }

    public PrintDestinationTo rollingFile() {
      type = ROLLING_FILE;
      return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public PrintDestinationTo stderr() {
      type = STDERR;
      return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public PrintDestinationTo stdout() {
      type = STDOUT;
      return this;
    }

    private String maxFileSize = null;

    public PrintDestinationTo maxFileSize(ByteSize size) {
      maxFileSize = size.displayStr();
      return this;
    }

    public PrintDestinationTo maxFileSize(String size) {
      maxFileSize = requireNonNull(size);
      return this;
    }

    private String filesCount = null;

    public PrintDestinationTo filesCount(int filesCount) {
      this.filesCount = "" + filesCount;
      return this;
    }

    String layoutName;

    public PrintDestinationTo layout(String layoutName) {
      this.layoutName = layoutName;
      return this;
    }

    public void appendInstructionsTo(List<TopInstruction> instructionList) {
      var topInstruction = new TopInstruction(0, true);
      instructionList.add(topInstruction);
      topInstruction.header.parse(0, DESTINATION_TO + ' ' + name);
      if (type != null) {
        topInstruction.addSubInstruction(TYPE + ' ' + type);
      }
      if (maxFileSize != null) {
        topInstruction.addSubInstruction(MAX_FILE_SIZE + ' ' + maxFileSize);
      }
      if (filesCount != null) {
        topInstruction.addSubInstruction(FILES_COUNT + ' ' + filesCount);
      }
      if (layoutName != null) {
        topInstruction.addSubInstruction(LAYOUT + ' ' + layoutName);
      }
    }
  }

  private final List<PrintDestinationTo> printDestinationToList = new ArrayList<>();

  public PrintDestinationTo destinationTo(String toName) {
    for (PrintDestinationTo x : printDestinationToList) {
      if (Objects.equals(x.name, toName)) {
        return x;
      }
    }
    {
      PrintDestinationTo x = new PrintDestinationTo(toName);
      printDestinationToList.add(x);
      return x;
    }
  }

  public static class PrintDestination {
    private final String name;
    private String destinationToName;
    private String fileNameWithSubPath;

    private PrintDestination(String name) {
      this.name = name;
    }

    private Level level = null;

    public PrintDestination level(Level level) {
      this.level = level;
      return this;
    }

    private String layoutName = null;

    public PrintDestination layout(String layoutName) {
      this.layoutName = layoutName;
      return this;
    }

    public void appendInstructionsTo(List<TopInstruction> instructionList) {
      var topInstruction = new TopInstruction(0, true);
      instructionList.add(topInstruction);
      {
        StringBuilder sb = new StringBuilder();
        sb.append(DESTINATION).append(' ');
        sb.append(requireNonNull(name));
        sb.append(' ');
        sb.append(requireNonNull(destinationToName));
        if (fileNameWithSubPath != null) {
          sb.append(' ');
          sb.append(fileNameWithSubPath);
        }
        topInstruction.header.parse(0, sb.toString());
      }

      if (layoutName != null) {
        topInstruction.addSubInstruction(LAYOUT + ' ' + layoutName);
      }

      if (level != null) {
        topInstruction.addSubInstruction(LEVEL + ' ' + level);
      }
    }

  }

  private final List<PrintDestination> printDestinationList = new ArrayList<>();

  public PrintDestination destination(String name, String destinationToName, String fileNameWithSubPath) {
    requireNonNull(destinationToName);
    PrintDestination printDestination = null;
    for (PrintDestination destination : printDestinationList) {
      if (Objects.equals(destination.name, name)) {
        printDestination = destination;
        break;
      }
    }
    if (printDestination == null) {
      printDestination = new PrintDestination(name);
      printDestinationList.add(printDestination);
    }
    printDestination.destinationToName = destinationToName;
    printDestination.fileNameWithSubPath = fileNameWithSubPath;
    return printDestination;
  }

  public PrintDestination destination(String name, String destinationToName) {
    return destination(name, destinationToName, null);
  }

  public static class PrintCategory {
    private final String name;

    private PrintCategory(String name) {
      this.name = name;
    }

    private Level level;

    public PrintCategory level(Level level) {
      this.level = level;
      return this;
    }

    private final List<String> destinationNameList = new ArrayList<>();

    public PrintCategory assignTo(String... destinationName) {
      OUT:
      for (String dName : destinationName) {
        for (String subName : destinationNameList) {
          if (Objects.equals(dName, subName)) {
            continue OUT;
          }
        }
        destinationNameList.add(dName);
      }
      return this;
    }

    @SuppressWarnings("unused")
    public PrintCategory deleteAssign(String... destinationName) {
      Set<String> names = Arrays.stream(destinationName).collect(Collectors.toSet());
      for (int i = 0; i < destinationNameList.size(); ) {
        if (names.contains(destinationNameList.get(i))) {
          destinationNameList.remove(i);
        } else {
          i++;
        }
      }
      return this;
    }

    public void appendInstructionsTo(List<TopInstruction> instructionList) {
      var topInstruction = new TopInstruction(0, true);
      instructionList.add(topInstruction);
      topInstruction.header.parse(0, name == null ? ROOT : CATEGORY + ' ' + name);

      if (level != null) {
        topInstruction.addSubInstruction(LEVEL + ' ' + level);
      }

      for (String assignTo : destinationNameList) {
        topInstruction.addSubInstruction(ASSIGN_TO + ' ' + assignTo);
      }

    }
  }

  private final List<PrintCategory> printCategoryList = new ArrayList<>();

  public PrintCategory category(String name) {
    for (PrintCategory x : printCategoryList) {
      if (Objects.equals(name, x.name)) {
        return x;
      }
    }
    {
      PrintCategory x = new PrintCategory(name);
      printCategoryList.add(x);
      return x;
    }
  }

  public PrintCategory root() {
    return category(null);
  }

  public static class PrintLayout {
    private final String name;

    public PrintLayout(String name) {
      this.name = name;
    }

    private String pattern = null;

    public PrintLayout pattern(String pattern) {
      this.pattern = pattern;
      return this;
    }

    private String coloredPattern = null;

    @SuppressWarnings("UnusedReturnValue")
    public PrintLayout coloredPattern(String coloredPattern) {
      this.coloredPattern = coloredPattern;
      return this;
    }

    public void appendInstructionsTo(List<TopInstruction> instructionList) {
      var topInstruction = new TopInstruction(0, true);
      instructionList.add(topInstruction);
      topInstruction.header.parse(0, LAYOUT + ' ' + name);
      if (pattern != null) {
        topInstruction.addSubInstruction(PATTERN + ':' + pattern);
      }
      if (coloredPattern != null) {
        topInstruction.addSubInstruction(COLORED_PATTERN + ':' + coloredPattern);
      }
    }
  }

  private final List<PrintLayout> printLayoutList = new ArrayList<>();

  public PrintLayout layout(String name) {
    for (PrintLayout x : printLayoutList) {
      if (Objects.equals(name, x.name)) {
        return x;
      }
    }
    {
      PrintLayout x = new PrintLayout(name);
      printLayoutList.add(x);
      return x;
    }
  }

  public PrintLayout layoutDefault() {
    return layout(DEFAULT);
  }

  public LogRouting build() {
    List<TopInstruction> instructionList = new ArrayList<>();

    for (PrintLayout x : printLayoutList) {
      x.appendInstructionsTo(instructionList);
    }

    for (PrintDestinationTo x : printDestinationToList) {
      x.appendInstructionsTo(instructionList);
    }

    for (PrintDestination x : printDestinationList) {
      x.appendInstructionsTo(instructionList);
    }

    for (PrintCategory x : printCategoryList) {
      x.appendInstructionsTo(instructionList);
    }

    var routingParser = new TokenParser();
    routingParser.instructionList.addAll(instructionList);
    AnalyzerResult analyzerResult = routingParser.analyze();
    analyzerResult.throwIfErrorExists();
    return analyzerResult.routing;
  }

}
