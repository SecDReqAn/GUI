package io.local;

import com.fasterxml.jackson.databind.ObjectMapper;
import general.entities.Configuration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Class providing read / write operations to / from a specified save-file.
 */
public class ConfigManager {
    /**
     * Class {@link Logger}.
     */
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);

    /**
     * The {@link ObjectMapper} that is used for (de)serialization.
     */
    private static final @NotNull ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Tries to deserialize a {@link Configuration} instance from the contents found within the specified {@link File}.
     *
     * @param target The {@link File} whose contents should be deserialized.
     * @return The {@link Configuration} that resulted from deserializing the contents of the specified {@link File}.
     * @throws IOException If a low-level I/O problem occurs while reading.
     */
    public static @NotNull Configuration readConfig(@NotNull File target) throws IOException {
        LOGGER.info("Reading configuration from file \"" + target.getAbsolutePath() + "\"");
        return ConfigManager.objectMapper.readValue(target, Configuration.class);
    }

    /**
     * Tries to serialize the specified {@link Configuration} instance and write it to the specified {@link File}.
     *
     * <p><b>Note</b>: If the specified file already exists, it is overwritten without regard to its previous
     * content.</p>
     *
     * @param target        The {@link File} to which the serialized {@link Configuration} should be written.
     * @param configuration The {@link Configuration} whose serialization should be written to the specified
     *                      {@link File}.
     * @throws IOException If a low-level I/O problem occurs while writing.
     */
    public static void writeConfig(@NotNull File target, @NotNull Configuration configuration) throws IOException {
        LOGGER.info("Writing configuration to file \"" + target.getAbsolutePath() + "\"");
        ConfigManager.objectMapper.writerWithDefaultPrettyPrinter().writeValue(target, configuration);
    }
}
