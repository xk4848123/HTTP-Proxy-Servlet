package com.zary.sniffer.config;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;

public class ConfigLoader {
    public static void loadConfigMonitorChanges(String configFile) throws IOException {
        Yaml yaml = new Yaml();
        try (FileInputStream fileInputStream = new FileInputStream(configFile)) {
            Config config = yaml.loadAs(fileInputStream, Config.class);
            ConfigCache.setConfig(config);
        }
        Thread watchThread = new Thread(() -> {
            try {
                watchForChanges(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        watchThread.start();
    }

    public static void loadConfig(String configFile) throws IOException {
        Yaml yaml = new Yaml();
        try (FileInputStream fileInputStream = new FileInputStream(configFile)) {
            Config config = yaml.loadAs(fileInputStream, Config.class);
            ConfigCache.setConfig(config);
        }

    }

    private static void watchForChanges(String configFile) throws IOException {
        Path path = Paths.get(configFile).getParent();
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            while (true) {
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        loadConfig(configFile);
                    }
                }

                boolean reset = key.reset();
                if (!reset) {
                    break;
                }
            }
        }
    }
}
