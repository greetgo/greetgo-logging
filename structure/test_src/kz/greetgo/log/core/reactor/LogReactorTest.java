package kz.greetgo.log.core.reactor;

import kz.greetgo.log.core.config.ConfigFileToNativeFile;
import kz.greetgo.log.core.model.Level;
import kz.greetgo.log.core.routing.LogRouting;
import kz.greetgo.log.core.routing.LogRoutingBuilder;
import kz.greetgo.log.core.util.ByteSize;
import kz.greetgo.log.core.util.SizeUnit;
import kz.greetgo.log.test_common.TestLocker;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogReactorTest {

  @Test
  public void startReactorUp() {

    var testLocker = new TestLocker("build/LogReactorTest");

    String logConfigFileName = testLocker.dir + "/log.config.txt";

    var reactor = LogReactor
      .builder()
      .applyRouting(this::acceptLogRouting)
      .configFile(ConfigFileToNativeFile.of(Paths.get(logConfigFileName)))
      .errorFile(ConfigFileToNativeFile.of(Paths.get(logConfigFileName + ".errors.txt")))
      .initRouting(getLogRouting())
      .resetPingDelayMillis(500)
      .build();

    reactor.startup();

    testLocker.lock(500);

    reactor.shutdownAndWait();

    System.out.println("9JKBQm206Z :: By-by....");

  }

  public void acceptLogRouting(LogRouting routing) {
    //noinspection SpellCheckingInspection
    var sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    System.out.println(sdf.format(new Date()) + " gT5seAGJkJ :: Accepting log routing:");
    int i = 0;
    for (var assign : routing.assignList()) {
      System.out.println("    " + (++i) + ' ' + assign);
    }
    System.out.println();
  }

  private LogRouting getLogRouting() {
    var builder = new LogRoutingBuilder();

    //noinspection SpellCheckingInspection
    builder.layoutDefault()
           .pattern("%d{yyyy-MM-dd'T'HH:mm:ss.SSS} %32.32logger{15} Q%mdc{LID} %-5level %msg%n")
           .coloredPattern("%green(%d{yyyy-MM-dd'T'HH:mm:ss.SSS})"
                             + " %cyan(%32.32logger{15}) Q%mdc{LID} %highlight(%-5level) %msg%n");

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
    return builder.build();
  }
}
