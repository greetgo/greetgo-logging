package kz.greetgo.log.core.parser;

import kz.greetgo.log.core.model.Destination;
import kz.greetgo.log.core.model.DestinationToRollingFile;
import kz.greetgo.log.core.model.DestinationToStdout;
import kz.greetgo.log.core.model.Level;
import kz.greetgo.log.core.parser.model.ParseError;
import kz.greetgo.log.core.parser.model.ParseErrorCode;
import kz.greetgo.log.core.parser.model.Range;
import kz.greetgo.log.core.parser.model.SubInstruction;
import kz.greetgo.log.core.parser.model.TopInstruction;
import kz.greetgo.log.core.parser_resources.ParserRes;
import kz.greetgo.log.core.routing.AnalyzerResult;
import kz.greetgo.log.core.util.ByteSize;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static kz.greetgo.log.core.model.Instr.DEFAULT;
import static kz.greetgo.log.core.parser.model.ParseErrorCode.SUB_INSTRUCTION_WITHOUT_PARENT;
import static org.assertj.core.api.Assertions.assertThat;

public class TokenParserTest {
  private final ParserRes parserRes = new ParserRes();

  @Test
  public void parse__normal() {

    String text = new ParserRes().textFrom("test-config-01.txt");

    var parser = new TokenParser();

    //
    //
    parser.parse(text);
    //
    //

    assertThat(parser.errorList).isEmpty();

    {
      TopInstruction topInstruction = parser.instructionList.get(0);
      assertThat(topInstruction.line).isEqualTo(2);
      assertThat(topInstruction.enabled).isTrue();
      assertThat(topInstruction.header.get(0).str()).isEqualTo("destination_to");
      assertThat(topInstruction.header.get(0).range).isEqualTo(Range.offsetCount(0, 14));
      assertThat(topInstruction.header.get(1).str()).isEqualTo("to_file");
      assertThat(topInstruction.header.get(1).range).isEqualTo(Range.offsetCount(15, 7));

      {
        SubInstruction subInstruction = topInstruction.subList.get(0);
        assertThat(subInstruction.line).isEqualTo(3);
        assertThat(subInstruction.partList.get(0).str()).isEqualTo("type");
        assertThat(subInstruction.partList.get(0).range).isEqualTo(Range.offsetCount(2, 4));
        assertThat(subInstruction.partList.get(1).str()).isEqualTo("rolling_file");
        assertThat(subInstruction.partList.get(1).range).isEqualTo(Range.offsetCount(7, 12));
      }
      {
        SubInstruction subInstruction = topInstruction.subList.get(1);
        assertThat(subInstruction.line).isEqualTo(4);
        assertThat(subInstruction.partList.get(0).str()).isEqualTo("max_file_size");
        assertThat(subInstruction.partList.get(1).str()).isEqualTo("1.31M");
      }
    }

    {
      TopInstruction topInstruction = parser.instructionList.get(1);
      assertThat(topInstruction.line).isEqualTo(7);
      assertThat(topInstruction.enabled).isFalse();
      assertThat(topInstruction.header.get(0).str()).isEqualTo("destination_to");
      assertThat(topInstruction.header.get(1).str()).isEqualTo("to_file");
    }

    {
      TopInstruction topInstruction = parser.instructionList.get(6);
      assertThat(topInstruction.line).isEqualTo(29);
      assertThat(topInstruction.enabled).isTrue();
      assertThat(topInstruction.header.get(0).str()).isEqualTo("destination");
      assertThat(topInstruction.header.get(1).str()).isEqualTo("smallTraces");
      assertThat(topInstruction.header.get(2).str()).isEqualTo("to_big_file");
      assertThat(topInstruction.header.get(2).range).isEqualTo(Range.offsetCount(24, 11));
      assertThat(topInstruction.header.get(3).str()).isEqualTo("traces/small");
    }

    {
      TopInstruction topInstruction = parser.instructionList.get(10);
      assertThat(topInstruction.line).isEqualTo(44);
      assertThat(topInstruction.enabled).isTrue();
      assertThat(topInstruction.header.get(0).str()).isEqualTo("category");
      assertThat(topInstruction.header.get(1).str()).isEqualTo("kz.greetgo.test.wow");

      {
        SubInstruction subInstruction = topInstruction.subList.get(0);
        assertThat(subInstruction.line).isEqualTo(45);
        assertThat(subInstruction.partList.get(0).str()).isEqualTo("level");
        assertThat(subInstruction.partList.get(0).range).isEqualTo(Range.offsetCount(2, 5));
        assertThat(subInstruction.partList.get(1).str()).isEqualTo("INFO");
        assertThat(subInstruction.partList.get(1).range).isEqualTo(Range.offsetCount(8, 4));
      }
      {
        SubInstruction subInstruction = topInstruction.subList.get(1);
        assertThat(subInstruction.line).isEqualTo(46);
        assertThat(subInstruction.partList.get(0).str()).isEqualTo("assign_to");
        assertThat(subInstruction.partList.get(1).str()).isEqualTo("wow");
      }
    }

  }

  @Test
  public void parse__normal__comma() {

    String text = "\n" +
      "layout    asd\n" +
      "  pattern:Hello World Status\n" +
      "  colored_pattern   asd    dsa  :   The last parameter       \n" +
      "\n" +
      "destination_to to_file\n" +
      "  type rolling_file\n" +
      "  max_file_size 1.31M\n" +
      "  files_count 17\n";

    var parser = new TokenParser();

    //
    //
    parser.parse(text);
    //
    //

    assertThat(parser.errorList).isEmpty();

    TopInstruction theFirst = parser.instructionList.get(0);

    assertThat(theFirst.header.get(0).str()).isEqualTo("layout");
    assertThat(theFirst.header.get(1).str()).isEqualTo("asd");

    SubInstruction sub0 = theFirst.subList.get(0);

    assertThat(sub0.partList).hasSize(2);
    assertThat(sub0.partList.get(0).str()).isEqualTo("pattern");
    assertThat(sub0.partList.get(1).str()).isEqualTo("Hello World Status");

    SubInstruction sub1 = theFirst.subList.get(1);

    assertThat(sub1.partList).hasSize(4);
    assertThat(sub1.partList.get(0).str()).isEqualTo("colored_pattern");
    assertThat(sub1.partList.get(1).str()).isEqualTo("asd");
    assertThat(sub1.partList.get(2).str()).isEqualTo("dsa");
    assertThat(sub1.partList.get(3).str()).isEqualTo("The last parameter");

  }

  @Test
  public void parse__parseError() {
    String text = parserRes.textFrom("test-config-02-parseError.txt");

    var parser = new TokenParser();

    //
    //
    parser.parse(text);
    //
    //


    assertThat(parser.errorList).hasSize(2);
    assertThat(parser.errorList.get(0).code).isEqualTo(SUB_INSTRUCTION_WITHOUT_PARENT);
    assertThat(parser.errorList.get(0).lineNo).isEqualTo(2);
    assertThat(parser.errorList.get(1).code).isEqualTo(SUB_INSTRUCTION_WITHOUT_PARENT);
    assertThat(parser.errorList.get(1).lineNo).isEqualTo(3);
  }

  @Test
  public void analyze__ok() {

    String text = parserRes.textFrom("test-config-01.txt");

    var parser = new TokenParser();
    parser.parse(text);

    //
    //
    AnalyzerResult analyzerResult = parser.analyze();
    //
    //

    var list = analyzerResult.routing.assignList();


    for (ParseError parseError : analyzerResult.errorList) {
      System.out.println("mk9YRJUnyN :: " + parseError);
    }

    assertThat(analyzerResult.errorList).isEmpty();

    {
      var assignment = list.find("BO_TRACE", "boTraces");
      assertThat(assignment.category.name).isEqualTo("BO_TRACE");
      assertThat(assignment.category.level).isEqualTo(Level.TRACE);
      assertThat(assignment.destination.to.name).isEqualTo("to_big_file");
      assertThat(assignment.destination.to).isInstanceOf(DestinationToRollingFile.class);
      assertThat(((DestinationToRollingFile) assignment.destination.to).maxFileSizeInBytes)
        .isEqualTo(ByteSize.parse("1.71Gi").sizeInBytes);
      assertThat(((DestinationToRollingFile) assignment.destination.to).filesCount).isEqualTo(13);
      assertThat(assignment.destination.name).isEqualTo("boTraces");
      assertThat(assignment.destination.level).isEqualTo(Level.TRACE);
      assertThat(assignment.destination.fileNameWithSubPath).isEqualTo("traces/bo");
    }
    {
      var assignment = list.find("kz.greetgo.test.super", "wow");
      assertThat(assignment.category.name).isEqualTo("kz.greetgo.test.super");
      assertThat(assignment.category.level).isEqualTo(Level.ERROR);
      assertThat(assignment.destination.to.name).isEqualTo("to_file");
      assertThat(assignment.destination.to).isInstanceOf(DestinationToRollingFile.class);
      assertThat(((DestinationToRollingFile) assignment.destination.to).maxFileSizeInBytes)
        .isEqualTo(ByteSize.parse("1.31M").sizeInBytes);
      assertThat(((DestinationToRollingFile) assignment.destination.to).filesCount).isEqualTo(17);
      assertThat(assignment.destination.name).isEqualTo("wow");
      assertThat(assignment.destination.level).isEqualTo(Level.ERROR);
      assertThat(assignment.destination.fileNameWithSubPath).isEqualTo("wow-log");
    }
    {
      var assignment = list.find("kz.greetgo.test.super", "CONSOLE");
      assertThat(assignment.category.name).isEqualTo("kz.greetgo.test.super");
      assertThat(assignment.category.level).isEqualTo(Level.ERROR);
      assertThat(assignment.destination.to.name).isEqualTo("to_stdout");
      assertThat(assignment.destination.to).isInstanceOf(DestinationToStdout.class);
      assertThat(assignment.destination.name).isEqualTo("CONSOLE");
      assertThat(assignment.destination.level).isEqualTo(Level.DEBUG);
    }

  }

  @Test
  public void analyze__error() {
    String text = parserRes.textFrom("test-config-03.txt");

    var parser = new TokenParser();
    parser.parse(text);

    //
    //
    parser.analyze();
    //
    //

    for (ParseError parseError : parser.errorList) {
      System.out.println("TYBxZPBhPB :: " + parseError);
    }

    assertThat(parser.errorList).hasSize(1);
    assertThat(parser.errorList.get(0).code).isEqualTo(ParseErrorCode.DUPLICATE_DESTINATION_NAME);
  }

  @Test
  public void analyze__error__noDefaultLayout() {

    String text = parserRes.textFrom("test-config-04-error__noDefaultLayout.txt");

    var parser = new TokenParser();
    parser.parse(text);

    //
    //
    AnalyzerResult result = parser.analyze();
    //
    //

    for (ParseError parseError : result.errorList) {
      System.out.println("vRN04CxOYs :: " + parseError);
    }

    assertThat(parser.errorList).hasSize(1);
    assertThat(parser.errorList.get(0).code).isEqualTo(ParseErrorCode.DEFAULT_LAYOUT_NOT_FOUND);

  }

  @Test
  public void analyze__ok__defaultLayout() {
    String text = parserRes.textFrom("test-config-05-ok__defaultLayout.txt");

    var parser = new TokenParser();
    parser.parse(text);

    //
    //
    AnalyzerResult result = parser.analyze();
    //
    //

    for (ParseError parseError : result.errorList) {
      System.out.println("kBWOLwXdi9 :: " + parseError);
    }

    assertThat(result.errorList).isEmpty();
    assertThat(result.routing.layoutList).hasSize(1);
    assertThat(result.routing.layoutList.get(0).name).isEqualTo(DEFAULT);
    assertThat(result.routing.layoutList.get(0).pattern.str()).isEqualTo("Hello World");
    assertThat(result.routing.layoutList.get(0).coloredPattern.str()).isEqualTo("Hello Colored World");
  }

  @Test
  public void analyze__ok__namedLayout() {
    String text = parserRes.textFrom("test-config-06-ok__namedLayout.txt");

    var parser = new TokenParser();
    parser.parse(text);

    //
    //
    AnalyzerResult result = parser.analyze();
    //
    //

    for (ParseError parseError : result.errorList) {
      System.out.println("RkWQjWNu7P :: " + parseError);
    }

    assertThat(result.errorList).isEmpty();
    assertThat(result.routing.layoutList).hasSize(1);
    assertThat(result.routing.layoutList.get(0).name).isEqualTo("testLayout");
    assertThat(result.routing.layoutList.get(0).pattern.str()).isEqualTo("Hello World");
    assertThat(result.routing.layoutList.get(0).coloredPattern.str()).isEqualTo("Hello Colored World");
    assertThat(result.routing.destinationList).hasSize(1);
    assertThat(result.routing.destinationList.get(0).layout).isNotNull();
    assertThat(result.routing.destinationList.get(0).layout.name).isEqualTo("testLayout");
  }

  @Test
  public void analyze__ok__namedLayout2() {
    String text = parserRes.textFrom("test-config-07-ok__namedLayout.txt");

    var parser = new TokenParser();
    parser.parse(text);

    //
    //
    AnalyzerResult result = parser.analyze();
    //
    //

    for (ParseError parseError : result.errorList) {
      System.out.println("RkWQjWNu7P :: " + parseError);
    }

    assertThat(result.errorList).isEmpty();
    assertThat(result.routing.destinationList).hasSize(1);
    assertThat(result.routing.destinationList.get(0).layout).isNotNull();
    assertThat(result.routing.destinationList.get(0).layout.name).isEqualTo("testLayout2");
    assertThat(result.routing.destinationList.get(0).layout.pattern.str()).isEqualTo("Hello World");
    assertThat(result.routing.destinationList.get(0).layout.coloredPattern.str()).isEqualTo("Hello Colored World");
  }

  @DataProvider
  Object[][] analyze__ok__duplicatedLayoutName_DP() {
    return new Object[][]{
      {"test-config-08-err_duplicatedLayoutName.txt", "testLayout2"},
      {"test-config-09-err_duplicatedLayoutDefaultName.txt", DEFAULT},
    };
  }

  @Test(dataProvider = "analyze__ok__duplicatedLayoutName_DP")
  public void analyze__ok__duplicatedLayoutName(String fileName, String layoutName) {
    String text = parserRes.textFrom(fileName);

    var parser = new TokenParser();
    parser.parse(text);

    //
    //
    AnalyzerResult result = parser.analyze();
    //
    //

    for (ParseError parseError : result.errorList) {
      System.out.println("y1fueY78en :: " + parseError);
    }

    assertThat(result.errorList).hasSize(1);
    assertThat(result.errorList.get(0).code).isEqualTo(ParseErrorCode.DUPLICATE_LAYOUT_NAME);
    assertThat(result.errorList.get(0).getMessage()).contains("`" + layoutName + "`");
  }

  @Test
  public void analyze__destinationTo_rollingFile_with_layout() {

    String text = "\n" +
      "layout    asd\n" +
      "  pattern: mvs1TTaL05 AeSzyCNRIc\n" +
      "\n" +
      "destination_to to_file\n" +
      "  type rolling_file\n" +
      "  max_file_size 1.31M\n" +
      "  files_count 17\n" +
      "  layout asd\n" +
      "\n" +
      "destination asd to_file asd\n" +
      "\n" +
      "category asd\n" +
      "  assign_to asd\n" +
      "\n";

    var parser = new TokenParser();
    parser.parse(text);

    //
    //
    AnalyzerResult result = parser.analyze();
    //
    //

    assertThat(result.errorList).isEmpty();

    Destination destination = result.routing.destinationList.get(0);

    assertThat(destination.layout.name).isEqualTo("asd");
    assertThat(destination.layout.pattern.str()).isEqualTo("mvs1TTaL05 AeSzyCNRIc");

  }
}
