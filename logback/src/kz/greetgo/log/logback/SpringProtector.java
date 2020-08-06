package kz.greetgo.log.logback;

import ch.qos.logback.classic.LoggerContext;

import java.util.concurrent.atomic.AtomicReference;

public class SpringProtector {

  private static final String SPRING_PROTECT_CLASS = "org.springframework.boot.logging.LoggingSystem";

  private enum SpringProtectNecessity {
    NEED_TO_CHECK, NECESSARY, NOT_NECESSARY
  }

  private static final AtomicReference<SpringProtectNecessity> protectNecessity
    = new AtomicReference<>(SpringProtectNecessity.NEED_TO_CHECK);

  public static void protect(LoggerContext context) {
    switch (protectNecessity.get()) {
      case NEED_TO_CHECK:
        try {
          Class.forName(SPRING_PROTECT_CLASS);
          protectNecessity.set(SpringProtectNecessity.NECESSARY);
          // goto case NECESSARY:
        } catch (ClassNotFoundException e) {
          protectNecessity.set(SpringProtectNecessity.NOT_NECESSARY);
          return;
        }
        // No break and no return !!!
      case NECESSARY:
        if (null == context.getObject(SPRING_PROTECT_CLASS)) {
          context.putObject(SPRING_PROTECT_CLASS, new Object());
        }
        return;

      default:
        return;
    }
  }

}
