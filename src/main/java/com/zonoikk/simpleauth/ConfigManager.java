package com.zonoikk.simpleauth;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConfigManager {
    private static Map<String, Map<String, String>> messages = new HashMap<>();
    private static final String CONFIG_DIR = "config/simpleauth";
    private static final String CONFIG_FILE = "config/simpleauth/simpleauth_config.json";
    private static String language = "en";
    private static int authCountdown = 60;
    private static int invulnerabilityTimer = 10;
    private static boolean cancelInvulnerabilityOnWalk = false;
    private static boolean kickOnWrongPassword = false;
    private static final double CURRENT_CONFIG_VERSION = 5.0;

    public static void init() {
        File dir = new File(CONFIG_DIR);
        if (!dir.exists()) dir.mkdirs();
        
        File file = new File(CONFIG_FILE);
        if (!file.exists()) {
            generateDefaultConfig();
        }
        loadConfigFile();
    }

    public static String getTranslation(String key, Object... args) {
        Map<String, String> langMap = messages.getOrDefault(language, messages.get("en"));
        if (langMap == null) return key;
        String message = langMap.getOrDefault(key, key);
        return (args.length > 0) ? String.format(message, args) : message;
    }

    public static String getLanguage() {
        return language;
    }

    public static void setLanguage(String lang) {
        if (!messages.containsKey(lang)) {
            throw new IllegalArgumentException("Unsupported language: " + lang);
        }
        language = lang;
        save();
    }

    private static void save() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject config = new JsonObject();
            config.addProperty("language", language);
            config.addProperty("authCountdown", authCountdown);
            config.addProperty("invulnerability", invulnerabilityTimer);
            config.addProperty("cancelInvulnerabilityOnWalk", cancelInvulnerabilityOnWalk);
            config.addProperty("kickOnWrongPassword", kickOnWrongPassword);
            config.addProperty("configVersion", CURRENT_CONFIG_VERSION);
            config.add("messages", gson.toJsonTree(messages));
            gson.toJson(config, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config", e);
        }
    }

    private static void generateDefaultConfig() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject config = new JsonObject();
            config.addProperty("language", "en");
            config.addProperty("authCountdown", 60);
            config.addProperty("invulnerability", 10);
            config.addProperty("cancelInvulnerabilityOnWalk", false);
            config.addProperty("kickOnWrongPassword", false);
            config.addProperty("configVersion", CURRENT_CONFIG_VERSION);
            
            JsonObject msgs = new JsonObject();
            // English defaults (could add more)
            JsonObject en = new JsonObject();
            en.addProperty("welcome_register", "Welcome! Use /register <password> <password> to register.");
            en.addProperty("welcome_back", "Welcome back! Use /login <password> to log in.");
            msgs.add("en", en);
            
            config.add("messages", msgs);
            gson.toJson(config, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate default config", e);
        }
    }

    private static void loadConfigFile() {
        try (Reader reader = new InputStreamReader(new FileInputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
            Gson gson = new Gson();
            JsonObject config = JsonParser.parseReader(reader).getAsJsonObject();
            language = config.has("language") ? config.get("language").getAsString() : "en";
            authCountdown = config.has("authCountdown") ? config.get("authCountdown").getAsInt() : 60;
            invulnerabilityTimer = config.has("invulnerability") ? config.get("invulnerability").getAsInt() : 10;
            cancelInvulnerabilityOnWalk = config.has("cancelInvulnerabilityOnWalk") ? config.get("cancelInvulnerabilityOnWalk").getAsBoolean() : false;
            kickOnWrongPassword = config.has("kickOnWrongPassword") ? config.get("kickOnWrongPassword").getAsBoolean() : false;

            Type type = new TypeToken<Map<String, Map<String, String>>>() {}.getType();
            messages = gson.fromJson(config.get("messages"), type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }

    public static int getAuthCountdown() { return authCountdown; }
    public static boolean isCancelInvulnerabilityOnWalk() { return cancelInvulnerabilityOnWalk; }
    public static boolean isKickOnWrongPassword() { return kickOnWrongPassword; }
    public static int getInvulnerabilityTimer() { return invulnerabilityTimer; }
    
    public static JsonObject loadLanguageFile(String lang) {
        Map<String, String> langMap = messages.getOrDefault(lang, messages.get("en"));
        JsonObject json = new JsonObject();
        if (langMap != null) langMap.forEach(json::addProperty);
        return json;
    }
}
