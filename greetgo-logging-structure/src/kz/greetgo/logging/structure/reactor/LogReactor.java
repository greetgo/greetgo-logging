package kz.greetgo.logging.structure.reactor;

import kz.greetgo.logging.structure.config.ConfigFile;
import kz.greetgo.logging.structure.parser.TokenParser;
import kz.greetgo.logging.structure.routing.AnalyzerResult;
import kz.greetgo.logging.structure.routing.LogRouting;
import kz.greetgo.logging.structure.routing.LogRoutingBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class LogReactor {

  private final @NonNull ConfigFile configFile;
  private final ConfigFile errorFile;
  private final LogRoutingBuilder routingBuilder;
  private final @NonNull Consumer<LogRouting> applyRouting;
  private final long resetPingDelayMillis;

  private boolean firstUpdate = true;

  private Date savedConfigFileLastModifiedAt = null;

  private void setErrorContent(String errorContent) {
    ConfigFile errorFile = this.errorFile;
    if (errorFile != null) {
      errorFile.write(errorContent);
    }
  }

  private int routingBuilderChangeCount = 0;

  private void update() {

    ConfigFile configFile = this.configFile;

    if (firstUpdate) {
      firstUpdate = false;

      LogRoutingBuilder routingBuilder = this.routingBuilder;
      routingBuilderChangeCount = routingBuilder == null ? 0 : routingBuilder.changeCount();

      LogRouting fileRouting = new LogRouting();
      String content = configFile.read();
      if (content != null) {
        savedConfigFileLastModifiedAt = configFile.lastModifiedAt();
        var parser = new TokenParser();
        parser.parse(content);
        AnalyzerResult analyzerResult = parser.analyze();
        String errorContent = analyzerResult.errorContent();
        setErrorContent(errorContent);
        if (errorContent == null) {
          fileRouting = analyzerResult.routing;
        } else {

          LogRouting initRouting = routingBuilder == null ? null : routingBuilder.build();
          if (initRouting != null) {
            if (!initRouting.isEmpty()) {
              applyRouting.accept(initRouting);
            }
          }
          return;
        }
      }

      LogRouting routing = routingBuilder == null ? null : routingBuilder.build();
      LogRouting delta = routing == null ? new LogRouting() : routing.copy().delete(fileRouting);

      if (delta.isEmpty()) {
        if (content == null) {
          StringBuilder sb = new StringBuilder();
          appendHelpConfigFile(sb);
          configFile.write(sb.toString());
          savedConfigFileLastModifiedAt = configFile.lastModifiedAt();
        }
      } else {
        writeDelta(content, delta);
      }

      fileRouting.appendFrom(delta);

      applyRouting.accept(fileRouting);

      return;
    }//end of: if (firstUpdate)

    var fcc = fileContentAndChangeFlag();
    var rbc = routingBuilderAndChangeFlag();

    //ccf.content == null - это значит что файла нет
    if (fcc.content == null) {
      if (rbc.changed) {
        applyRouting.accept(rbc.routingBuilder.build());
      }
      return;
    }

    if (!rbc.changed && !fcc.changed) {
      return;
    }

    var parser = new TokenParser();
    parser.parse(fcc.content);
    var analyzerResult = parser.analyze();
    String errorContent = analyzerResult.errorContent();
    setErrorContent(errorContent);
    if (errorContent != null) {
      return;
    }

    if (!rbc.changed) {
      applyRouting.accept(analyzerResult.routing);
      return;
    }

    LogRouting delta = rbc.routingBuilder.build()
                                         .delete(analyzerResult.routing);

    if (delta.isEmpty()) {
      if (fcc.changed) {
        applyRouting.accept(analyzerResult.routing);
      }
      return;
    }

    writeDelta(fcc.content, delta);
    analyzerResult.routing.appendFrom(delta);
    applyRouting.accept(analyzerResult.routing);
  }

  private void writeDelta(String currentContent, LogRouting delta) {
    StringBuilder sb = new StringBuilder();
    if (currentContent != null) {
      sb.append(currentContent);
    }
    if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '\n') {
      sb.append('\n');
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //noinspection IfStatementWithIdenticalBranches
    if (currentContent == null) {
      sb.append("## \n");
      sb.append("## Created at ").append(sdf.format(new Date())).append('\n');
      sb.append("## \n");
    } else {
      sb.append("\n");
      sb.append("## \n");
      sb.append("## Updated at ").append(sdf.format(new Date())).append('\n');
      sb.append("## \n");
    }

    sb.append(delta.serialize());

    configFile.write(sb.toString());
    savedConfigFileLastModifiedAt = configFile.lastModifiedAt();
  }

  @RequiredArgsConstructor
  private static class ContentAndChangeFlag {
    final boolean changed;
    final String content;
  }

  @RequiredArgsConstructor
  private static class RoutingBuilderAndChangeFlag {
    final boolean changed;
    final LogRoutingBuilder routingBuilder;
  }

  private @NonNull RoutingBuilderAndChangeFlag routingBuilderAndChangeFlag() {
    LogRoutingBuilder routingBuilder = this.routingBuilder;

    if (routingBuilder == null) {
      return new RoutingBuilderAndChangeFlag(false, null);
    }

    int count = routingBuilder.changeCount();
    if (routingBuilderChangeCount != count) {
      routingBuilderChangeCount = count;
      return new RoutingBuilderAndChangeFlag(true, routingBuilder);
    }

    return new RoutingBuilderAndChangeFlag(false, routingBuilder);
  }

  private @NonNull ContentAndChangeFlag fileContentAndChangeFlag() {
    Date lastModifiedAt = configFile.lastModifiedAt();
    if (lastModifiedAt == null) {
      return new ContentAndChangeFlag(false, null);
    }
    String content = configFile.read();
    if (content == null) {
      return new ContentAndChangeFlag(false, null);
    }
    if (savedConfigFileLastModifiedAt == null) {
      savedConfigFileLastModifiedAt = lastModifiedAt;
      return new ContentAndChangeFlag(false, content);
    }

    if (lastModifiedAt.equals(savedConfigFileLastModifiedAt)) {
      return new ContentAndChangeFlag(false, content);
    }

    savedConfigFileLastModifiedAt = lastModifiedAt;

    return new ContentAndChangeFlag(true, content);
  }

  private void appendHelpConfigFile(StringBuilder sb) {
    sb.append("#\n");
    sb.append("#  Log configuration\n");
    sb.append("#\n");
    sb.append("\n");
    sb.append("layout\n");
    //noinspection SpellCheckingInspection
    sb.append("  pattern : %d{yyyy-MM-dd'T'HH:mm:ss.SSS} %32.32logger{15} Q%mdc{LID} %-5level %msg%n\n");
    //noinspection SpellCheckingInspection
    sb.append("  colored_pattern : %green(%d{yyyy-MM-dd'T'HH:mm:ss.SSS}) %cyan(%32.32logger{15})" +
                " Q%mdc{LID} %highlight(%-5level) %msg%n\n");
    sb.append("\n");
    sb.append("#destination_to to_file\n");
    sb.append("#  type rolling_file\n");
    sb.append("#  maxFileSize 1.1Mi\n");
    sb.append("#  filesCount 10\n");
    sb.append("#\n");
    sb.append("#destination_to to_stdout\n");
    sb.append("#  type stdout\n");
    sb.append("#\n");
    sb.append("#destination_to to_stderr\n");
    sb.append("#  type stderr\n");
    sb.append("#\n");
    sb.append("#destination server to_file server/main\n");
    sb.append("#  level ERROR\n");
    sb.append("#\n");
    sb.append("#destination CONSOLE to_stdout\n");
    sb.append("#  level INFO\n");
    sb.append("#\n");
    sb.append("#destination CONSOLE_ERR to_stderr\n");
    sb.append("#  level ERROR\n");
    sb.append("#\n");
    sb.append("#category TRACES\n");
    sb.append("#  level TRACE\n");
    sb.append("#  assign_to CONSOLE\n");
    sb.append("#\n");
    sb.append("#category kz.greetgo.project\n");
    sb.append("#  level INFO\n");
    sb.append("#  assign_to server\n");
    sb.append("#\n");
    sb.append("#root\n");
    sb.append("#  level ERROR\n");
    sb.append("#  assign_to server\n");
    sb.append("#  assign_to CONSOLE_ERR\n");
    sb.append("#\n");
  }

  //<editor-fold desc="Builder">
  public static class LogReactorBuilder {
    private @NonNull ConfigFile configFile;
    private ConfigFile errorFile;
    private LogRoutingBuilder routingBuilder;
    private @NonNull Consumer<LogRouting> applyRouting;
    private long resetPingDelayMillis = 500;

    private LogReactorBuilder() {}

    public LogReactorBuilder configFile(@NonNull ConfigFile configFile) {
      this.configFile = configFile;
      return this;
    }

    public LogReactorBuilder resetPingDelayMillis(long resetPingDelayMillis) {
      this.resetPingDelayMillis = resetPingDelayMillis;
      return this;
    }

    public LogReactorBuilder errorFile(ConfigFile errorFile) {
      this.errorFile = errorFile;
      return this;
    }

    public LogReactorBuilder routingBuilder(@NonNull LogRoutingBuilder routingBuilder) {
      this.routingBuilder = routingBuilder;
      return this;
    }

    public LogReactorBuilder applyRouting(@NonNull Consumer<LogRouting> applyRouting) {
      this.applyRouting = applyRouting;
      return this;
    }

    public LogReactor build() {
      return new LogReactor(configFile, errorFile, routingBuilder, applyRouting, resetPingDelayMillis);
    }

    public String toString() {
      return "LogReactor-Builder("
        + "configFile=" + this.configFile
        + ", errorFile=" + this.errorFile
        + ", routingBuilder=" + this.routingBuilder
        + ", applyRouting=" + this.applyRouting
        + ")";
    }
  }

  private LogReactor(@NonNull ConfigFile configFile, ConfigFile errorFile, LogRoutingBuilder routingBuilder,
                     @NonNull Consumer<LogRouting> applyRouting, long resetPingDelayMillis) {
    this.configFile = configFile;
    this.errorFile = errorFile;
    this.routingBuilder = routingBuilder;
    this.applyRouting = applyRouting;
    this.resetPingDelayMillis = resetPingDelayMillis;
  }

  public static LogReactorBuilder builder() {
    return new LogReactorBuilder();
  }

  private final AtomicBoolean working = new AtomicBoolean(true);

  private final AtomicReference<Thread> thread = new AtomicReference<>(null);
  //</editor-fold>

  public void startup() {
    {
      var t = thread.get();
      if (t != null) {
        return;
      }
    }

    synchronized (working) {
      {
        var t = thread.get();
        if (t != null) {
          return;
        }
      }

      update();

      {
        Thread t = new Thread(() -> {
          while (working.get()) {
            try {
              //noinspection BusyWait
              Thread.sleep(resetPingDelayMillis);
            } catch (InterruptedException e) {
              return;
            }
            update();
          }
        });
        thread.set(t);
        t.start();
      }
    }
  }

  public void shutdown() {
    working.set(false);
  }

  public void shutdownAndWait() {
    shutdown();
    Thread t = this.thread.get();
    if (t != null) {
      try {
        t.join();
      } catch (InterruptedException e) {
        return;
      }
    }
  }

}
