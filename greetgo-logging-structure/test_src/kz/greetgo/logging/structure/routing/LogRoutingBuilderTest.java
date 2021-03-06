package kz.greetgo.logging.structure.routing;

import kz.greetgo.logging.structure.model.CategoryDestinationAssignment;
import kz.greetgo.logging.structure.model.Destination;
import kz.greetgo.logging.structure.model.DestinationTo;
import kz.greetgo.logging.structure.model.DestinationToRollingFile;
import kz.greetgo.logging.structure.model.DestinationToStderr;
import kz.greetgo.logging.structure.model.DestinationToStdout;
import kz.greetgo.logging.structure.model.Instr;
import kz.greetgo.logging.structure.model.Layout;
import kz.greetgo.logging.structure.model.Level;
import kz.greetgo.logging.structure.model.Category;
import kz.greetgo.logging.structure.util.ByteSize;
import kz.greetgo.logging.structure.util.SizeUnit;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LogRoutingBuilderTest {

  @Test
  public void build() {

    var builder = new LogRoutingBuilder();

    builder.layoutDefault()
           .pattern("WOW")
           .coloredPattern("Colored WOW");

    builder.layout("server")
           .pattern("Server Pattern")
           .coloredPattern("Colored Server Pattern");

    builder.destinationTo("to_file")
           .rollingFile()
           .filesCount(10)
           .maxFileSize(ByteSize.of(10, SizeUnit.MiB))
    ;

    builder.destinationTo("to_big_file")
           .rollingFile()
           .maxFileSize(ByteSize.of(100, SizeUnit.MiB))
           .filesCount(10)
    ;

    builder.destinationTo("to_small_file")
           .rollingFile()
           .maxFileSize("1Mi")
           .filesCount(10)
    ;

    builder.destinationTo("to_small_file")
           .rollingFile()
           .maxFileSize("100k");

    builder.destinationTo("to_stdout")
           .stdout()
    ;
    builder.destinationTo("to_stderr")
           .stderr()
    ;

    builder.destination("smallTraces", "to_big_file", "traces/small")
           .level(Level.TRACE)
    ;
    builder.destination("wow", "to_file", "wow-log")
           .level(Level.ERROR)
    ;
    builder.destination("CONSOLE", "to_stdout")
           .level(Level.DEBUG)
    ;

    builder.category("kz.greetgo.test.wow")
           .level(Level.ERROR)
           .assignTo("wow")
           .assignTo("CONSOLE")
    ;

    builder.root()
           .level(Level.ERROR)
           .assignTo("wow")
           .assignTo("CONSOLE")
    ;

    //
    //
    LogRouting logRouting = builder.build();
    //
    //

    {
      {
        System.out.println("Layout list:");
        int i = 0;
        for (Layout layout : logRouting.layoutList) {
          System.out.println("  " + (++i) + " " + layout);
        }
        System.out.println();
      }
      {
        System.out.println("DestinationTo list:");
        int i = 0;
        for (DestinationTo destinationTo : logRouting.destinationToList) {
          System.out.println("  " + (++i) + " " + destinationTo);
        }
        System.out.println();
      }
      {
        System.out.println("Destination list:");
        int i = 0;
        for (Destination destination : logRouting.destinationList) {
          System.out.println("  " + (++i) + " " + destination);
        }
        System.out.println();
      }
      {
        System.out.println("Category list:");
        int i = 0;
        for (Category category : logRouting.categoryList) {
          System.out.println("  " + (++i) + " " + category);
        }
        System.out.println();
      }
      {
        System.out.println("Assign list:");
        int i = 0;
        for (CategoryDestinationAssignment assign : logRouting.assignList()) {
          System.out.println("  " + (++i) + " " + assign);
        }
        System.out.println();
      }
    }

    Assertions.assertThat(logRouting.layoutList.get(0).name).isEqualTo(Instr.DEFAULT);
    Assertions.assertThat(logRouting.layoutList.get(0).pattern.str()).isEqualTo("WOW");
    Assertions.assertThat(logRouting.layoutList.get(0).coloredPattern.str()).isEqualTo("Colored WOW");

    Assertions.assertThat(logRouting.layoutList.get(1).name).isEqualTo("server");
    Assertions.assertThat(logRouting.layoutList.get(1).pattern.str()).isEqualTo("Server Pattern");
    Assertions.assertThat(logRouting.layoutList.get(1).coloredPattern.str()).isEqualTo("Colored Server Pattern");

    Assertions.assertThat(logRouting.destinationToList.get(0).name).isEqualTo("to_file");
    Assertions.assertThat(logRouting.destinationToList.get(1).name).isEqualTo("to_big_file");
    Assertions.assertThat(logRouting.destinationToList.get(2).name).isEqualTo("to_small_file");
    Assertions.assertThat(logRouting.destinationToList.get(3).name).isEqualTo("to_stdout");
    Assertions.assertThat(logRouting.destinationToList.get(4).name).isEqualTo("to_stderr");

    Assertions.assertThat(logRouting.destinationToList.get(0)).isInstanceOf(DestinationToRollingFile.class);
    Assertions.assertThat(logRouting.destinationToList.get(1)).isInstanceOf(DestinationToRollingFile.class);
    Assertions.assertThat(logRouting.destinationToList.get(2)).isInstanceOf(DestinationToRollingFile.class);
    Assertions.assertThat(logRouting.destinationToList.get(3)).isInstanceOf(DestinationToStdout.class);
    Assertions.assertThat(logRouting.destinationToList.get(4)).isInstanceOf(DestinationToStderr.class);

    assertThat(((DestinationToRollingFile) logRouting.destinationToList.get(0)).maxFileSizeInBytes)
      .isEqualTo(10485760L);
    assertThat(((DestinationToRollingFile) logRouting.destinationToList.get(1)).maxFileSizeInBytes)
      .isEqualTo(104857600L);
    assertThat(((DestinationToRollingFile) logRouting.destinationToList.get(2)).maxFileSizeInBytes)
      .isEqualTo(100000L);

    Assertions.assertThat(logRouting.destinationList.get(0).name).isEqualTo("smallTraces");
    Assertions.assertThat(logRouting.destinationList.get(1).name).isEqualTo("wow");
    Assertions.assertThat(logRouting.destinationList.get(2).name).isEqualTo("CONSOLE");

    Assertions.assertThat(logRouting.categoryList.get(0).name).isEqualTo("kz.greetgo.test.wow");
    Assertions.assertThat(logRouting.categoryList.get(1).name).isNull();

    Assertions.assertThat(logRouting.assignList().get(0).category.name).isEqualTo("kz.greetgo.test.wow");
    Assertions.assertThat(logRouting.assignList().get(1).category.name).isEqualTo("kz.greetgo.test.wow");
    Assertions.assertThat(logRouting.assignList().get(2).category.name).isNull();
    Assertions.assertThat(logRouting.assignList().get(3).category.name).isNull();

    Assertions.assertThat(logRouting.assignList().get(0).destination.name).isEqualTo("wow");
    Assertions.assertThat(logRouting.assignList().get(1).destination.name).isEqualTo("CONSOLE");
    Assertions.assertThat(logRouting.assignList().get(2).destination.name).isEqualTo("wow");
    Assertions.assertThat(logRouting.assignList().get(3).destination.name).isEqualTo("CONSOLE");
  }
}
