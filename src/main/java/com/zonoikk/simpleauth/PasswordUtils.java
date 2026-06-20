 package com.zonoikk.simpleauth;
 
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.security.spec.InvalidKeySpecException;
 import java.util.Base64;
 import javax.crypto.SecretKeyFactory;
 import javax.crypto.spec.PBEKeySpec;
 
 public class PasswordUtils
 {
   private static final int ITERATIONS = 65536;
   private static final int KEY_LENGTH = 256;
   private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
   private static final SecureRandom SECURE_RANDOM = new SecureRandom();
 
   
   public static String hashPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
     byte[] salt = generateSalt();
     PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
     SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
     byte[] hash = factory.generateSecret(spec).getEncoded();
     return Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(hash);
   }
 
   
   public static boolean verifyPassword(String password, String storedHash) throws NoSuchAlgorithmException, InvalidKeySpecException {
     String[] parts = storedHash.split("\\$");
     byte[] salt = Base64.getDecoder().decode(parts[0]);
     byte[] hash = Base64.getDecoder().decode(parts[1]);
     
     PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
     SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
     byte[] testHash = factory.generateSecret(spec).getEncoded();
     
     return slowEquals(hash, testHash);
   }
 
   
   private static byte[] generateSalt() {
     byte[] salt = new byte[16];
     SECURE_RANDOM.nextBytes(salt);
     return salt;
   }
 
   
   private static boolean slowEquals(byte[] a, byte[] b) {
     int diff = a.length ^ b.length;
     for (int i = 0; i < a.length && i < b.length; i++) {
       diff |= a[i] ^ b[i];
     }
     return (diff == 0);
   }
 }


/* Location:              /mnt/worker/simpleauth-5.0.0.jar!/com/zonoikk/simpleauth/PasswordUtils.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */