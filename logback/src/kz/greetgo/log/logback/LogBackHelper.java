package kz.greetgo.log.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.LevelFilter;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.util.FileSize;
import kz.greetgo.log.core.model.Category;
import kz.greetgo.log.core.model.Destination;
import kz.greetgo.log.core.model.DestinationTo;
import kz.greetgo.log.core.model.DestinationToRollingFile;
import kz.greetgo.log.core.model.DestinationToStderr;
import kz.greetgo.log.core.model.DestinationToStdout;
import kz.greetgo.log.core.model.Layout;
import kz.greetgo.log.core.routing.LogRouting;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class LogBackHelper {

  private LoggerContext context;
  public Path logFileRoot;
  public boolean colored;

  public void apply(LogRouting routing) {

    context = (LoggerContext) LoggerFactory.getILoggerFactory();
    context.reset();
    SpringProtector.protect(context);

    Map<String, Appender<ILoggingEvent>> appenderMap = new HashMap<>();

    for (Destination destination : routing.destinationList) {
      appenderMap.put(destination.name, createAppender(destination));
    }

    for (Category category : routing.categoryList) {

      Logger logger = context.getLogger(Optional.of(category).map(x -> x.name).orElse(Logger.ROOT_LOGGER_NAME));
      if (category.level != null) {
        logger.setLevel(convertLevel(category.level));
      }
      for (Destination destination : category.assignToList) {
        Appender<ILoggingEvent> appender = appenderMap.get(destination.name);
        if (appender != null) {
          logger.addAppender(appender);
        }
      }

    }

  }

  private static Level convertLevel(kz.greetgo.log.core.model.Level level) {
    switch (level) {
      //@formatter:off
      case     FATAL   :
      case     ERROR   :    return Level.ERROR  ;
      case     WARN    :    return Level.WARN   ;
      case     INFO    :    return Level.INFO   ;
      case     DEBUG   :    return Level.DEBUG  ;
      case     TRACE   :    return Level.TRACE  ;
      case     OFF     :    return Level.OFF    ;
      default          :    throw new IllegalArgumentException("BSveBuPpAh :: Unknown value " + level);
      //@formatter:on
    }
  }

  private Appender<ILoggingEvent> createAppender(Destination destination) {

    var destinationTo = requireNonNull(destination.to);

    Appender<ILoggingEvent> appender = createAppenderTo(destination, destinationTo);

    if (destination.level != null) {
      ThresholdFilter filter = new ThresholdFilter();
      filter.setLevel(destination.level.name());
      filter.start();
      appender.addFilter(filter);
    }

    return appender;

  }

  private @NotNull Appender<ILoggingEvent> createAppenderTo(Destination destination, DestinationTo destinationTo) {

    if (destinationTo instanceof DestinationToRollingFile) {
      return createAppender__rollingFile(destination, (DestinationToRollingFile) destinationTo);
    }

    if (destinationTo instanceof DestinationToStdout) {
      return createAppender__stdout(destination);
    }

    if (destinationTo instanceof DestinationToStderr) {
      return createAppender__stderr(destination);
    }

    throw new RuntimeException("Dku6Z5I17m :: Cannot create appender for " + destinationTo.getClass());
  }

  private Appender<ILoggingEvent> createAppender__stderr(Destination destination) {

    ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
    consoleAppender.setContext(context);
    consoleAppender.setName(destination.name);
    consoleAppender.setEncoder(createConsoleEncoder());
    consoleAppender.setTarget("System.err");
    consoleAppender.setWithJansi(true);
    consoleAppender.start();

    return consoleAppender;
  }

  private Appender<ILoggingEvent> createAppender__stdout(Destination destination) {

    ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
    consoleAppender.setContext(context);
    consoleAppender.setName(destination.name);
    consoleAppender.setEncoder(createConsoleEncoder());
    consoleAppender.setTarget("System.out");
    consoleAppender.setWithJansi(true);
    consoleAppender.start();

    return consoleAppender;
  }

  private Appender<ILoggingEvent> createAppender__rollingFile(Destination destination,
                                                              DestinationToRollingFile destinationTo) {

    String filePrefix = logFileRoot.resolve(destination.fileNameWithSubPath).toString();
    String name = destination.name;

    PatternLayoutEncoder encoder = createEncoder(requireNonNull(destination.layout));

    var appender = new RollingFileAppender<ILoggingEvent>();
    appender.setContext(context);
    appender.setName(name.toUpperCase());
    appender.setEncoder(encoder);
    appender.setFile(filePrefix + ".log");

    var rolling = new FixedWindowRollingPolicy();
    rolling.setContext(context);
    rolling.setFileNamePattern(filePrefix + ".%i.log");
    rolling.setMinIndex(1);
    rolling.setMaxIndex(destinationTo.filesCount);
    rolling.setParent(appender);
    rolling.start();

    var triggeringPolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
    triggeringPolicy.setContext(context);
    triggeringPolicy.setMaxFileSize(new FileSize(destinationTo.maxFileSizeInBytes));
    triggeringPolicy.start();

    appender.setTriggeringPolicy(triggeringPolicy);
    appender.setRollingPolicy(rolling);
    appender.start();

    return appender;
  }

  private @NotNull PatternLayoutEncoder createConsoleEncoder() {
    var mainEncoder = new PatternLayoutEncoder();
    mainEncoder.setContext(context);
    if (colored) {
      //noinspection SpellCheckingInspection
      mainEncoder.setPattern("%green(%d{yyyy-MM-dd'T'HH:mm:ss.SSS})"
                               + " %cyan(%32.32logger{15}) %highlight(%-5level) %msg%n");
    } else {
      //noinspection SpellCheckingInspection
      mainEncoder.setPattern("%d{yyyy-MM-dd'T'HH:mm:ss.SSS} %32.32logger{15} %-5level %msg%n");
    }
    mainEncoder.start();
    return mainEncoder;
  }

  private @NotNull PatternLayoutEncoder createEncoder(Layout layout) {
    var encoder = new PatternLayoutEncoder();
    encoder.setContext(context);
    encoder.setPattern(layout.getPattern(colored));
    encoder.start();
    return encoder;
  }
}
