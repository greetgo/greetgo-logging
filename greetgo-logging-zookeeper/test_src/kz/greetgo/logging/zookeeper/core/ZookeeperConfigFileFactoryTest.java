package kz.greetgo.logging.zookeeper.core;

import kz.greetgo.logging.structure.config.ConfigFile;
import kz.greetgo.logging.structure.model.Level;
import kz.greetgo.logging.structure.reactor.LogReactor;
import kz.greetgo.logging.structure.routing.LogRouting;
import kz.greetgo.logging.structure.routing.LogRoutingBuilder;
import kz.greetgo.logging.structure.util.ByteSize;
import kz.greetgo.logging.structure.util.SizeUnit;
import kz.greetgo.logging.test_common.Connects;
import kz.greetgo.logging.test_common.TestLocker;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ZookeeperConfigFileFactoryTest {

  @Test
  public void runReactor() {

    ZookeeperConnectParams connectParams = new ZookeeperConnectParams()
      .connectStr(Connects::toZookeeper)
      .connectTimeoutMillis(10_000);

    var configFileFactory = new ZookeeperConfigFileFactory(connectParams,
                                                           "/tests/ZookeeperConfigFileFactoryTest/01");

    ConfigFile logConfig = configFileFactory.getOrCreate("log-config.txt");
    ConfigFile logConfigError = configFileFactory.getOrCreate("log-config.txt.errors.txt");

    var routingBuilder = getLogRoutingBuilder();

    var testLocker = new TestLocker("build/ZookeeperConfigFileFactoryTest");

    var reactor = LogReactor
      .builder()
      .applyRouting(this::acceptLogRouting)
      .configFile(logConfig)
      .errorFile(logConfigError)
      .routingBuilder(routingBuilder)
      .build();

    System.out.println("nGeH2InCD3 :: starting reactor...");
    reactor.startup();
    System.out.println("ijsJw4p0s9 :: started OK");

    System.out.println("w6R0HU816g :: start to lock");
    testLocker.lock(500);
    System.out.println("AKhm13emrP :: Unlocked");

    System.out.println("8rMcRkE108 :: shutdownAndWait...");
    reactor.shutdownAndWait();
    System.out.println("7zC6F57OsJ :: it is shut down");

    System.out.println("Lc4njD29sY :: configFileFactory closing...");
    configFileFactory.close();
    System.out.println("UiJiBpDMC9 :: configFileFactory closed OK");

  }

  public void acceptLogRouting(LogRouting routing) {
    //noinspection SpellCheckingInspection
    var sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    System.out.println(sdf.format(new Date()) + " DBgD66QQw6 :: Accepting log routing:");
    int i = 0;
    for (var assign : routing.assignList()) {
      System.out.println("    " + (++i) + ' ' + assign);
    }
    System.out.println();
  }

  private LogRoutingBuilder getLogRoutingBuilder() {
    var builder = new LogRoutingBuilder();

    //noinspection SpellCheckingInspection
    builder.layoutDefault()
           .pattern("%d{yyyy-MM-dd'T'HH:mm:ss.SSS} %32.32logger{15} %-5level %msg%n")
           .coloredPattern("%green(%d{yyyy-MM-dd'T'HH:mm:ss.SSS})" +
                             " %cyan(%32.32logger{15}) %highlight(%-5level) %msg%n");

    builder.layout("cool")
           .pattern("cool");

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
    return builder;
  }
}
