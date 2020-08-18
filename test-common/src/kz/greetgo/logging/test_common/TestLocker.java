package kz.greetgo.logging.test_common;

import java.io.File;
import java.io.IOException;

public class TestLocker {
  public final String dir;
  private final File workingFile;
  private final File delItToStop;

  public TestLocker(String dir) {
    this.dir = dir;
    String lockFileName = dir + "/__lock__";
    File createLockFileName = new File(lockFileName + "1_removeItWith1");
    workingFile = new File(lockFileName);
    delItToStop = new File(dir + "/__deleteItToStop__");

    createLockFileName.getParentFile().mkdirs();
    delItToStop.getParentFile().mkdirs();
    try {
      delItToStop.createNewFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    if (!workingFile.exists()) {
      try {
        createLockFileName.createNewFile();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public boolean isWorking() {
    return workingFile.exists() && delItToStop.exists();
  }

  public void lock(long millis) {
    while (isWorking()) {
      try {
        //noinspection BusyWait
        Thread.sleep(millis);
      } catch (InterruptedException e) {
        return;
      }
    }
  }

  public void newButton(String lockName, Runnable run) {
    var file = new File(dir + "/__" + lockName + "__");
    file.getParentFile().mkdirs();
    try {
      file.createNewFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    new Thread(() -> {

      while (isWorking()) {

        if (!file.exists()) {
          try {
            file.createNewFile();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          run.run();
        }

        try {
          //noinspection BusyWait
          Thread.sleep(400);
        } catch (InterruptedException e) {
          return;
        }
      }

    }).start();
  }
}
