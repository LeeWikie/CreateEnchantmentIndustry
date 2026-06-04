package plus.dragons.createenchantmentindustry.config;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import net.fabricmc.loader.api.FabricLoader;
import plus.dragons.createenchantmentindustry.CEI;

public final class CEIConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String FILE_NAME = CEI.MOD_ID + ".json";

    private static CEIClientConfig client = CEIClientConfig.defaults();
    private static CEIServerConfig server = CEIServerConfig.defaults();

    private CEIConfig() {
    }

    public static void init() {
        load(FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME));
    }

    public static void load(Path path) {
        JsonObject root = readRoot(path);
        if (root == null) {
            client = CEIClientConfig.defaults();
            server = CEIServerConfig.defaults();
        } else {
            ConfigReader reader = new ConfigReader(root);
            client = CEIClientConfig.load(reader.section("client"));
            server = CEIServerConfig.load(reader.section("server"));
            reader.flushWarnings();
        }

        if (root != null || Files.notExists(path)) {
            writeEffectiveConfig(path);
        }
    }

    public static CEIClientConfig client() {
        return client;
    }

    public static CEIServerConfig server() {
        return server;
    }

    public static CEIKineticsConfig kinetics() {
        return server.kinetics();
    }

    public static CEIStressConfig stress() {
        return server.kinetics().stress();
    }

    public static CEIFluidsConfig fluids() {
        return server.fluids();
    }

    public static CEIEnchantmentsConfig enchantments() {
        return server.enchantments();
    }

    public static CEIProcessingConfig processing() {
        return server.processing();
    }

    public static JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.add("client", client.toJson());
        root.add("server", server.toJson());
        return root;
    }

    public static void main(String[] args) {
        Path path = args.length == 0 ? Path.of(FILE_NAME) : Path.of(args[0]);
        load(path);
        System.out.println("config=" + path.toAbsolutePath());
        System.out.println("printerFluidCapacity=" + fluids().printerFluidCapacity());
        System.out.println("blazeEnchanterMaxEnchantLevel=" + enchantments().blazeEnchanterMaxEnchantLevel());
        System.out.println("mechanicalGrindstoneImpact=" + stress().mechanicalGrindstoneImpact());
    }

    private static JsonObject readRoot(Path path) {
        if (Files.notExists(path)) {
            CEI.LOGGER.info("CEI config file {} is missing; safe defaults will be used and written.", path);
            return null;
        }
        try (java.io.Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement element = JsonParser.parseReader(reader);
            if (element == null || !element.isJsonObject()) {
                CEI.LOGGER.warn("CEI config file {} must contain a JSON object; using safe defaults.", path);
                return null;
            }
            return element.getAsJsonObject();
        } catch (JsonParseException | IOException exception) {
            CEI.LOGGER.warn("Could not read CEI config file {}; using safe defaults.", path, exception);
            return null;
        }
    }

    private static void writeEffectiveConfig(Path path) {
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(toJson(), writer);
            }
        } catch (IOException exception) {
            CEI.LOGGER.warn("Could not write CEI config file {}; continuing with in-memory safe defaults.", path, exception);
        }
    }

    static final class ConfigReader {
        private final JsonObject object;
        private final String path;
        private final List<String> warnings;

        ConfigReader(JsonObject object) {
            this(object, "", new ArrayList<>());
        }

        private ConfigReader(JsonObject object, String path, List<String> warnings) {
            this.object = object;
            this.path = path;
            this.warnings = warnings;
        }

        ConfigReader section(String name) {
            JsonElement element = object.get(name);
            String childPath = childPath(name);
            if (element == null || element.isJsonNull()) {
                warnings.add(childPath + " is missing");
                return new ConfigReader(new JsonObject(), childPath, warnings);
            }
            if (!element.isJsonObject()) {
                warnings.add(childPath + " must be an object");
                return new ConfigReader(new JsonObject(), childPath, warnings);
            }
            return new ConfigReader(element.getAsJsonObject(), childPath, warnings);
        }

        boolean readBoolean(String name, boolean defaultValue) {
            JsonElement element = object.get(name);
            if (element == null || element.isJsonNull()) {
                warnings.add(childPath(name) + " is missing");
                return defaultValue;
            }
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
                warnings.add(childPath(name) + " must be a boolean");
                return defaultValue;
            }
            return element.getAsBoolean();
        }

        int readInt(String name, int defaultValue, int min, int max) {
            JsonElement element = object.get(name);
            if (element == null || element.isJsonNull()) {
                warnings.add(childPath(name) + " is missing");
                return defaultValue;
            }
            if (!element.isJsonPrimitive()) {
                warnings.add(childPath(name) + " must be an integer");
                return defaultValue;
            }
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (!primitive.isNumber()) {
                warnings.add(childPath(name) + " must be an integer");
                return defaultValue;
            }
            try {
                double numeric = primitive.getAsDouble();
                long value = primitive.getAsLong();
                if (!Double.isFinite(numeric) || numeric != value || value < min || value > max) {
                    warnings.add(childPath(name) + " must be an integer in range " + min + ".." + max);
                    return defaultValue;
                }
                return (int) value;
            } catch (NumberFormatException exception) {
                warnings.add(childPath(name) + " must be an integer");
                return defaultValue;
            }
        }

        float readFloat(String name, float defaultValue, float min, float max) {
            double value = readDoubleValue(name, defaultValue, min, max, "float");
            return (float) value;
        }

        double readDouble(String name, double defaultValue, double min, double max) {
            return readDoubleValue(name, defaultValue, min, max, "number");
        }

        private double readDoubleValue(String name, double defaultValue, double min, double max, String type) {
            JsonElement element = object.get(name);
            if (element == null || element.isJsonNull()) {
                warnings.add(childPath(name) + " is missing");
                return defaultValue;
            }
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
                warnings.add(childPath(name) + " must be a " + type);
                return defaultValue;
            }
            try {
                double value = element.getAsDouble();
                if (!Double.isFinite(value) || value < min || value > max) {
                    warnings.add(childPath(name) + " must be a " + type + " in range " + min + ".." + max);
                    return defaultValue;
                }
                return value;
            } catch (NumberFormatException exception) {
                warnings.add(childPath(name) + " must be a " + type);
                return defaultValue;
            }
        }

        void flushWarnings() {
            for (String warning : warnings) {
                CEI.LOGGER.warn("Invalid or missing CEI config value: {}; using safe default.", warning);
            }
        }

        private String childPath(String name) {
            return path.isEmpty() ? name : path + "." + name;
        }
    }
}
