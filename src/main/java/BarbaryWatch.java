package com.barbarysoftware.watchservice;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.barbarysoftware.watchservice.StandardWatchEventKind.*;


public class BarbaryWatch {
  private final static WatchService watcher = WatchService.newWatchService();

  private final static WatchableFile file = new WatchableFile(new File("/Users/Jan/Google Drive/App/midget/data/settings"));

  public static Thread start(com.barbarysoftware.watchservice.WatchRun func) throws IOException {
    Thread consumer = new Thread(createRunnable(watcher, func));
    file.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    consumer.start();
    return consumer;
  }

  public static void stop(Thread consumer) throws IOException {
    consumer.interrupt();
    watcher.close();
  }

  private static Runnable createRunnable(final WatchService watcher, com.barbarysoftware.watchservice.WatchRun func) {
    return () -> {
      for (; ; ) {

        // wait for key to be signaled
        WatchKey key;
        try {
          key = watcher.take();
        } catch (InterruptedException x) {
          return;
        }
        for (WatchEvent<?> event : key.pollEvents()) {
          WatchEvent.Kind<?> kind = event.kind();

          if (kind == OVERFLOW) {
            continue;
          }
          func.run();

        }

        // Reset the key -- this step is critical to receive further watch events.

        boolean valid = key.reset();
        if (!valid) {
          break;
        }

      }
    };
  }
}
