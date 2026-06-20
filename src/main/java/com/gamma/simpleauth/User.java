 package com.gamma.simpleauth;

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
