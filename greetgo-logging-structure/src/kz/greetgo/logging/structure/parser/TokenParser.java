package kz.greetgo.logging.structure.parser;

import kz.greetgo.logging.structure.model.Destination;
import kz.greetgo.logging.structure.model.DestinationTo;
import kz.greetgo.logging.structure.model.Instr;
import kz.greetgo.logging.structure.model.Category;
import kz.greetgo.logging.structure.model.Layout;
import kz.greetgo.logging.structure.parser.model.InstructionPart;
import kz.greetgo.logging.structure.parser.model.ParseError;
import kz.greetgo.logging.structure.parser.model.ParseErrorCode;
import kz.greetgo.logging.structure.parser.model.ParseErrorHandler;
import kz.greetgo.logging.structure.parser.model.Range;
import kz.greetgo.logging.structure.parser.model.SubInstruction;
import kz.greetgo.logging.structure.parser.model.TopInstruction;
import kz.greetgo.logging.structure.routing.AnalyzerResult;
import kz.greetgo.logging.structure.routing.LogRouting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static kz.greetgo.logging.structure.model.Instr.DEFAULT;
import static kz.greetgo.logging.structure.parser.model.ParseErrorCode.SUB_INSTRUCTION_WITHOUT_PARENT;
import static kz.greetgo.logging.structure.parser.model.ParseErrorCode.UNKNOWN_TOP_INSTRUCTION;

public class TokenParser implements ParseErrorHandler {

  public final List<TopInstruction> instructionList = new ArrayList<>();

  final List<ParseError> errorList = new ArrayList<>();

  public void parse(String text) {
    int lineNo = 0;
    TopInstruction current = null;
    for (String line : text.split("\n")) {
      lineNo++;

      if (line.trim().startsWith("#")) {
        continue;
      }

      if (line.trim().length() == 0) {
        continue;
      }

      int offset = 0;
      boolean enabled = !line.startsWith("-");
      if (!enabled) {
        offset = 1;
      }

      if (!Character.isWhitespace(line.charAt(0))) {
        current = new TopInstruction(lineNo, enabled);
        instructionList.add(current);
        current.header.parse(offset, line);
        continue;
      }

      if (current == null) {

        errorList.add(new ParseError(lineNo,
                                     Range.offsetCount(0, line.length()),
                                     SUB_INSTRUCTION_WITHOUT_PARENT, "Подинструкция без родителя"));
        continue;
      }

      SubInstruction subInstruction = new SubInstruction(lineNo);
      current.addSubInstruction(subInstruction);
      subInstruction.partList.parse(offset, line);
    }
  }

  @Override
  public void happenedParseError(ParseError parseError) {
    errorList.add(parseError);
  }

  private List<Layout> layoutList;
  private List<Destination> destinationList;
  private List<DestinationTo> destinationToList;
  private List<Category> categoryList;

  private void createObjects() {
    layoutList = new ArrayList<>();
    destinationList = new ArrayList<>();
    destinationToList = new ArrayList<>();
    categoryList = new ArrayList<>();
    populate();
  }

  private void populate() {
    for (TopInstruction topInstruction : instructionList) {
      performTopInstruction(topInstruction);
    }

    populateAssignList();

    checkDuplicatedLayoutNames();
    checkDuplicatedDestinationToNames();
    checkDuplicatedDestinationNames();
    checkDuplicatedCategoryNames();

    destinationToFillFieldLayout();

    fillFieldDestinationTo();
    fillFieldLayout();
  }

  private void checkDuplicatedLayoutNames() {
    Map<String, Layout> map = new HashMap<>();

    for (Layout layout : layoutList) {
      if (!layout.enabled()) {
        continue;
      }

      Layout withSameName = map.get(layout.name);
      if (withSameName == null) {
        map.put(layout.name, layout);
        continue;
      }

      errorList.add(new ParseError(layout.source.line, layout.source.header.get(0).range,
                                   ParseErrorCode.DUPLICATE_LAYOUT_NAME,
                                   "Дубликат layout с именем `" + layout.name + "`"
                                     + " - предыдущий находиться в строке № " + withSameName.source.line));
    }
  }

  private void checkDuplicatedDestinationToNames() {

    Map<String, DestinationTo> map = new HashMap<>();

    for (DestinationTo destinationTo : destinationToList) {
      if (!destinationTo.source.enabled) {
        continue;
      }

      DestinationTo withSameName = map.get(destinationTo.name);
      if (withSameName == null) {
        map.put(destinationTo.name, destinationTo);
        continue;
      }

      errorList.add(new ParseError(destinationTo.source.line, destinationTo.source.header.get(0).range,
                                   ParseErrorCode.DUPLICATE_DESTINATION_TO_NAME,
                                   "Дубликат destination_to с именем `" + destinationTo.name + "`"
                                     + " - предыдущий находиться в строке № " + withSameName.source.line));
    }

  }

  private void checkDuplicatedDestinationNames() {

    Map<String, Destination> map = new HashMap<>();

    for (Destination destination : destinationList) {
      if (!destination.source.enabled) {
        continue;
      }

      Destination withSameName = map.get(destination.name);
      if (withSameName == null) {
        map.put(destination.name, destination);
        continue;
      }

      errorList.add(new ParseError(destination.source.line, destination.source.header.get(0).range,
                                   ParseErrorCode.DUPLICATE_DESTINATION_NAME,
                                   "Дубликат destination с именем `" + destination.name + "`"
                                     + " - предыдущий находиться в строке № " + withSameName.source.line));

    }

  }

  private void checkDuplicatedCategoryNames() {

    Map<String, Category> map = new HashMap<>();

    for (Category category : categoryList) {
      if (!category.source.enabled) {
        continue;
      }

      Category withSameName = map.get(category.name);
      if (withSameName == null) {
        map.put(category.name, category);
        continue;
      }

      errorList.add(new ParseError(category.source.line, category.source.header.get(0).range,
                                   ParseErrorCode.DUPLICATE_CATEGORY_NAME,
                                   "Дубликат category с именем `" + category.name + "`"
                                     + " - предыдущий находиться в строке № " + withSameName.source.line));
    }

  }

  private void destinationToFillFieldLayout() {
    Map<String, Layout> map = layoutList
      .stream()
      .filter(Layout::enabled)
      .collect(toMap(x -> x.name, x -> x, (a, b) -> a));

    for (DestinationTo destinationTo : destinationToList) {
      if (!destinationTo.enabled()) {
        continue;
      }

      InstructionPart layoutName = destinationTo.layoutName;
      if (layoutName == null) {
        continue;
      }

      destinationTo.layout = map.get(layoutName.str());

      if (destinationTo.layout == null) {
        errorList.add(new ParseError(layoutName.lineNo(), layoutName.range,
                                     ParseErrorCode.NOT_FOUND_LAYOUT_WITH_NAME,
                                     "Не найден layout с именем `" + layoutName.str() + "`"));
      }
    }
  }

  private void fillFieldLayout() {
    Map<String, Layout> map = layoutList
      .stream()
      .filter(Layout::enabled)
      .collect(toMap(x -> x.name, x -> x, (a, b) -> a));

    for (Destination destination : destinationList) {
      if (!destination.enabled()) {
        continue;
      }

      InstructionPart layoutName = destination.layoutName;
      if (layoutName != null) {

        Layout layout = map.get(layoutName.str());
        if (layout != null) {
          destination.layout = layout;
          continue;
        }

        errorList.add(new ParseError(layoutName.lineNo(), layoutName.range,
                                     ParseErrorCode.NOT_FOUND_LAYOUT_WITH_NAME,
                                     "Не найден layout с именем `" + layoutName.str() + "`"));
        continue;
      }

      DestinationTo destinationTo = destination.to;
      if (destinationTo == null) {
        continue;
      }

      {
        Layout layout = destinationTo.layout;
        if (layout != null) {
          destination.layout = layout;
          continue;
        }
      }

      {
        Layout layout = map.get(DEFAULT);
        if (layout != null) {
          destination.layout = layout;
          continue;
        }

        errorList.add(new ParseError(destination.source.line, destination.source.header.first().range,
                                     ParseErrorCode.DEFAULT_LAYOUT_NOT_FOUND,
                                     "Не найден layout по-умолчанию, т.е. с именем `" + DEFAULT + "`"));
      }

    }
  }

  private void fillFieldDestinationTo() {

    Map<String, DestinationTo> map = destinationToList
      .stream()
      .filter(DestinationTo::enabled)
      .collect(toMap(x -> x.name, x -> x, (a, b) -> a));

    for (Destination destination : destinationList) {
      if (!destination.enabled()) {
        continue;
      }

      DestinationTo found = map.get(destination.destinationToName.str());
      if (found != null) {
        destination.to = found;
        continue;
      }

      int lineNo = destination.destinationToName.lineNo();
      Range range = destination.destinationToName.range;

      errorList.add(new ParseError(lineNo, range,
                                   ParseErrorCode.NOT_FOUND_DESTINATION_TO_WITH_NAME,
                                   "Не найдена DestinationTo по имени `" + destination.destinationToName.str() + "`"));

    }
  }

  private void populateAssignList() {

    for (Category category : categoryList) {
      for (InstructionPart assignToName : category.assignToNameList) {
        appendAssignmentFrom(category, assignToName);
      }
    }

  }

  private void performTopInstruction(TopInstruction topInstruction) {
    switch (topInstruction.header.first().str()) {
      default:
        errorList.add(new ParseError(topInstruction.line, topInstruction.header.first().range,
                                     UNKNOWN_TOP_INSTRUCTION,
                                     "Неизвестная корневая инструкция: " + topInstruction.header.first().str()));
        return;

      case Instr.LAYOUT:
        Layout.readFrom(topInstruction, this)
              .ifPresent(layoutList::add);
        return;


      case Instr.DESTINATION_TO:
        DestinationTo.readFrom(topInstruction, this)
                     .ifPresent(destinationToList::add);
        return;

      case Instr.DESTINATION:
        Destination.readFrom(topInstruction, this)
                   .ifPresent(destinationList::add);
        return;

      case Instr.CATEGORY:
      case Instr.ROOT:
        Category.readFrom(topInstruction, this)
                .ifPresent(categoryList::add);
        return;
    }
  }

  private void appendAssignmentFrom(Category category, InstructionPart assignToName) {
    if (!category.enabled()) {
      return;
    }

    Destination foundDestination = null;

    for (Destination destination : destinationList) {
      if (!destination.source.enabled) {
        continue;
      }
      if (assignToName.str().equals(destination.name)) {
        foundDestination = destination;
        break;
      }
    }

    if (foundDestination == null) {
      errorList.add(new ParseError(assignToName.lineNo(), assignToName.range,
                                   ParseErrorCode.NOT_FOUND_DESTINATION_WITH_NAME,
                                   "Не найдено назначение с именем: `" + assignToName.str() + "`"));
      return;
    }

    category.assignToList.add(foundDestination);

  }

  public AnalyzerResult analyze() {
    createObjects();
    LogRouting ret = new LogRouting();
    ret.layoutList = layoutList;
    ret.destinationList = destinationList;
    ret.destinationToList = destinationToList;
    ret.categoryList = categoryList;
    return new AnalyzerResult(ret, errorList);
  }

}
