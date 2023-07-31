package io;

import com.fasterxml.jackson.databind.ObjectMapper;
import general.Configuration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static @NotNull Configuration readConfig(@NotNull File target) throws IOException {
        return ConfigManager.objectMapper.readValue(target, Configuration.class);
    }

    public static void writeConfig(@NotNull File target, @NotNull Configuration configuration) throws IOException {
        ConfigManager.objectMapper.writerWithDefaultPrettyPrinter().writeValue(target, configuration);
    }
}
