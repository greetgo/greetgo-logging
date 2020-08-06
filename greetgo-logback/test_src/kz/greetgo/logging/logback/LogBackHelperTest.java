package kz.greetgo.logging.logback;

import kz.greetgo.logging.structure.model.Level;
import kz.greetgo.logging.structure.routing.LogRouting;
import kz.greetgo.logging.structure.routing.LogRoutingBuilder;
import kz.greetgo.util.RND;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class LogBackHelperTest {

  private @NotNull LogRouting createRouting(boolean boTraceEnabled) {
    var routing = new LogRoutingBuilder();

    //noinspection SpellCheckingInspection
    routing.layoutDefault()
           .pattern(" %d{yyyy-MM-dd'T'HH:mm:ss.SSS} %32.32logger{15} Q%mdc{LID} %-5level %msg%n");

    routing.destinationTo("to_file")
           .rollingFile()
           .maxFileSize("1k")
           .filesCount(3);

    routing.destination("smallTrace", "to_file", "traces/small")
           .level(Level.TRACE);
    routing.destination("boTrace", "to_file", "traces/bo")
           .level(Level.TRACE);

    routing.category("SMALL_TRACE")
           .level(Level.TRACE)
           .assignTo("smallTrace");
    routing.category("BO_TRACE")
           .level(boTraceEnabled ? Level.TRACE : Level.OFF)
           .assignTo("boTrace");

    return routing.build();
  }

  @Test
  public void apply__simpleLogTraces() {

    LogRouting routing = createRouting(true);

    var logBackHelper = new LogBackHelper();
    logBackHelper.logFileRoot = Paths.get("build/LogBackHelperTest/01");
    logBackHelper.colored = true;

    //
    //
    logBackHelper.apply(routing);
    //
    //

    Logger loggerSmallTrace = LoggerFactory.getLogger("SMALL_TRACE");
    Logger loggerBoTrace = LoggerFactory.getLogger("BO_TRACE");

    loggerSmallTrace.trace("message 1");
    loggerSmallTrace.trace("message 2");
    loggerBoTrace.trace("hello 1");
    loggerBoTrace.trace("hello 2");

  }

  @Test
  public void apply__logTrace__thenOffOne__logAgain__logbackMustBeUpdated() {

    Path logFileRoot = Paths.get("build/LogBackHelperTest/02");

    {
      LogRouting routing = createRouting(true);

      var logBackHelper = new LogBackHelper();
      logBackHelper.logFileRoot = logFileRoot;
      logBackHelper.colored = true;

      //
      //
      logBackHelper.apply(routing);
      //
      //
    }

    Logger loggerSmallTrace = LoggerFactory.getLogger("SMALL_TRACE");
    Logger loggerBoTrace = LoggerFactory.getLogger("BO_TRACE");

    loggerSmallTrace.trace("message 1");
    loggerBoTrace.trace("hello 1");

    {
      LogRouting routing = createRouting(false);

      var logBackHelper = new LogBackHelper();
      logBackHelper.logFileRoot = logFileRoot;
      logBackHelper.colored = true;

      //
      //
      logBackHelper.apply(routing);
      //
      //
    }

    loggerSmallTrace.trace("message 2");
    loggerBoTrace.trace("hello 2");

  }

  @Test
  public void apply__manyTraces() {

    LogRouting routing = createRouting(true);

    var logBackHelper = new LogBackHelper();
    logBackHelper.logFileRoot = Paths.get("build/LogBackHelperTest/03");
    logBackHelper.colored = true;

    //
    //
    logBackHelper.apply(routing);
    //
    //

    String pre = RND.str(10);

    Logger loggerSmallTrace = LoggerFactory.getLogger("SMALL_TRACE");

    for (int i = 0; i < 100; i++) {
      loggerSmallTrace.trace(pre + " message " + i);
    }
  }

}
