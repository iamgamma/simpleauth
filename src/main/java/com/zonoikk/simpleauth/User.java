 package com.zonoikk.simpleauth;
 
 
 
 
 public class User
 {
   private final String id;
   private final String nickname;
   private String passwordHash;
   
   public User(String id, String nickname, String passwordHash) {
     this.id = id;
     this.nickname = nickname;
     this.passwordHash = passwordHash;
   }
   
   public String getId() {
     return this.id;
   }
   
   public String getNickname() {
     return this.nickname;
   }
   
   public String getPasswordHash() {
     return this.passwordHash;
   }
   
   public void setPasswordHash(String passwordHash) {
     this.passwordHash = passwordHash;
   }
 }


/* Location:              /mnt/worker/simpleauth-5.0.0.jar!/com/zonoikk/simpleauth/User.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */