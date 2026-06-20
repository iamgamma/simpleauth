 package com.zonoikk.simpleauth;
 import com.google.gson.*;
 import com.mojang.logging.LogUtils;
 import java.io.*;
 import java.nio.charset.StandardCharsets;
 import java.util.Map;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 import org.slf4j.Logger;
 
 public class DatabaseManager {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String CONFIG_DIR = "config/simpleauth";
   private static final String DB_FILE = "config/simpleauth/simpleauth_users.json";
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
 
   
   private static final Map<String, User> userCache = new ConcurrentHashMap<>();
 
   
   private static final int MAX_FAILED_ATTEMPTS = 5;
 
   
   private static final long LOCKOUT_DURATION_MS = 60000L;
   
   private static final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
   private static final Map<String, Long> lockoutExpiry = new ConcurrentHashMap<>();
 
 
 
 
   
   public static void initializeDatabase() {
     File configDir = new File("config/simpleauth");
     if (!configDir.exists()) configDir.mkdirs();
     
     File file = new File("config/simpleauth/simpleauth_users.json");
     if (!file.exists()) {
       try { FileWriter writer = new FileWriter(file); 
         try { GSON.toJson((JsonElement)new JsonArray(), writer);
           writer.close(); } catch (Throwable throwable) { try { writer.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }  throw throwable; }  } catch (IOException e)
       { LOGGER.error("Critical Error: Could not create the JSON database file.", e);
         return; }
     
     }
     loadCacheFromDisk();
   }
 
 
 
 
   
   public static boolean registerUser(String nickname, String password) {
     if (isUserRegistered(nickname)) return false; 
     try {
       String hash = PasswordUtils.hashPassword(password);
       User user = new User(UUID.randomUUID().toString(), nickname, hash);
       userCache.put(nickname.toLowerCase(), user);
       persistCacheToDisk();
       return true;
     } catch (Exception e) {
       LOGGER.error("Error: The user '{}' could not be registered.", nickname, e);
       return false;
     } 
   }
   
   public static AuthResult authenticateUser(String nickname, String password) {
     String key = nickname.toLowerCase();
 
     
     Long expiry = lockoutExpiry.get(key);
     if (expiry != null) {
       if (System.currentTimeMillis() < expiry.longValue()) {
         return AuthResult.RATE_LIMITED;
       }
       
       lockoutExpiry.remove(key);
       failedAttempts.remove(key);
     } 
 
     
     User user = userCache.get(key);
     if (user == null) return AuthResult.WRONG_PASSWORD;
     
     try {
       if (PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
         failedAttempts.remove(key);
         lockoutExpiry.remove(key);
         return AuthResult.SUCCESS;
       } 
       int attempts = ((Integer)failedAttempts.merge(key, Integer.valueOf(1), Integer::sum)).intValue();
       if (attempts >= 5) {
         lockoutExpiry.put(key, Long.valueOf(System.currentTimeMillis() + 60000L));
         failedAttempts.remove(key);
         LOGGER.warn("Player '{}' has been temporarily locked out after {} failed attempts.", nickname, Integer.valueOf(5));
       } 
       return AuthResult.WRONG_PASSWORD;
     }
     catch (Exception e) {
       LOGGER.error("Error: Authentication failed for '{}'.", nickname, e);
       return AuthResult.WRONG_PASSWORD;
     } 
   }
   
   public static boolean isUserRegistered(String nickname) {
     return userCache.containsKey(nickname.toLowerCase());
   }
   
   public static boolean updatePassword(String nickname, String newPassword) {
     User user = userCache.get(nickname.toLowerCase());
     if (user == null) return false; 
     try {
       user.setPasswordHash(PasswordUtils.hashPassword(newPassword));
       persistCacheToDisk();
       return true;
     } catch (Exception e) {
       LOGGER.error("Error: Could not update password for '{}'.", nickname, e);
       return false;
     } 
   }
   
   public static boolean unregisterUser(String nickname) {
     if (userCache.remove(nickname.toLowerCase()) != null) {
       persistCacheToDisk();
       return true;
     } 
     return false;
   }
 
 
 
   
   private static void loadCacheFromDisk() {
     
     try { Reader reader = new InputStreamReader(new FileInputStream("config/simpleauth/simpleauth_users.json"), StandardCharsets.UTF_8); 
       try { JsonElement root = JsonParser.parseReader(reader);
         if (!root.isJsonArray())
         
         { 
 
 
 
 
 
 
           
           reader.close(); return; }  userCache.clear(); for (JsonElement element : root.getAsJsonArray()) { JsonObject obj = element.getAsJsonObject(); String id = obj.get("id").getAsString(); String nickname = obj.get("nickname").getAsString(); String hash = obj.get("password").getAsString(); userCache.put(nickname.toLowerCase(), new User(id, nickname, hash)); }  LOGGER.info("SimpleAuth: loaded {} user(s) into cache.", Integer.valueOf(userCache.size())); reader.close(); } catch (Throwable throwable) { try { reader.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }  throw throwable; }  } catch (IOException e)
     { LOGGER.error("Error: Could not load user database from disk.", e); }
   
   }
   
   private static void persistCacheToDisk() {
     JsonArray array = new JsonArray();
     for (User user : userCache.values()) {
       JsonObject obj = new JsonObject();
       obj.addProperty("id", user.getId());
       obj.addProperty("nickname", user.getNickname());
       obj.addProperty("password", user.getPasswordHash());
       array.add((JsonElement)obj);
     }  
     try { Writer writer = new OutputStreamWriter(new FileOutputStream("config/simpleauth/simpleauth_users.json"), StandardCharsets.UTF_8); 
       try { GSON.toJson((JsonElement)array, writer);
         writer.close(); } catch (Throwable throwable) { try { writer.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }  throw throwable; }  } catch (IOException e)
     { LOGGER.error("Error: Could not persist user database to disk.", e); }
   
   }
 }


/* Location:              /mnt/worker/simpleauth-5.0.0.jar!/com/zonoikk/simpleauth/DatabaseManager.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */