 package com.gamma.simpleauth;
 
 import com.mojang.authlib.GameProfile;
 import com.mojang.brigadier.CommandDispatcher;
 import com.mojang.brigadier.arguments.ArgumentType;
 import com.mojang.brigadier.arguments.StringArgumentType;
 import com.mojang.brigadier.builder.LiteralArgumentBuilder;
 import java.util.Collection;
 import net.minecraft.commands.CommandSourceStack;
 import net.minecraft.commands.Commands;
 import net.minecraft.commands.arguments.GameProfileArgument;
 import net.minecraft.network.chat.Component;
 import net.minecraft.server.level.ServerPlayer;
 
 public class AuthCommands
 {
   public static void register(CommandDispatcher<CommandSourceStack> dispatcher, SimpleAuth mod) {
     registerRegister(dispatcher, mod);
     registerLogin(dispatcher, mod);
     registerChangePassword(dispatcher, mod);
     registerSimpleAuthGroup(dispatcher, mod);
   }
   
   private static void registerRegister(CommandDispatcher<CommandSourceStack> dispatcher, SimpleAuth mod) {
     dispatcher.register(
         (LiteralArgumentBuilder)Commands.literal("register")
         .then(Commands.argument("password", (ArgumentType)StringArgumentType.string())
           .then(Commands.argument("confirm", (ArgumentType)StringArgumentType.string())
             .executes(ctx -> {
                 ServerPlayer player = ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
                 String password = StringArgumentType.getString(ctx, "password");
                 String confirm = StringArgumentType.getString(ctx, "confirm");
                 if (!password.equals(confirm)) {
                   ((CommandSourceStack)ctx.getSource())
                           .sendFailure((Component)Component
                                   .literal(SimpleAuth.colorize(mod.getLocalizedMessage("password_mismatch",
                                           new Object[0]), "red")));
                   return 0;
                 } 
                 String username = player.getGameProfile().getName();
                 if (DatabaseManager.isUserRegistered(username)) {
                   ((CommandSourceStack)ctx.getSource())
                           .sendFailure((Component)Component
                                   .literal(SimpleAuth.colorize(mod.getLocalizedMessage("already_registered",
                                           new Object[0]), "red")));
                   return 0;
                 } 
                 if (DatabaseManager.registerUser(username, password)) {
                   mod.completeLogin(player);
                   ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal(""), false);
                   return 1;
                 } 
                 ((CommandSourceStack)ctx.getSource())
                         .sendFailure((Component)Component
                                 .literal(SimpleAuth.colorize(mod.getLocalizedMessage("registration_failed",
                                         new Object[0]), "red")));
                 return 0;
               }))));
   }

   private static void registerLogin(CommandDispatcher<CommandSourceStack> dispatcher, SimpleAuth mod) {
     dispatcher.register(
         (LiteralArgumentBuilder)Commands.literal("login")
         .then(Commands.argument("password", (ArgumentType)StringArgumentType.string())
           .executes(ctx -> {
               ServerPlayer player = ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
               String password = StringArgumentType.getString(ctx, "password");
               String username = player.getGameProfile().getName();
               if (!DatabaseManager.isUserRegistered(username)) {
                 ((CommandSourceStack)ctx.getSource())
                         .sendFailure((Component)Component
                                 .literal(SimpleAuth.colorize(mod.getLocalizedMessage("not_registered",
                                         new Object[0]), "red")));
                 return 0;
               } 
               AuthResult result = DatabaseManager.authenticateUser(username, password);
               switch (result) {
                 case SUCCESS:
                   mod.completeLogin(player);
                   ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal(""), false);
                   return 1;
                 case RATE_LIMITED:
                   ((CommandSourceStack)ctx.getSource())
                           .sendFailure((Component)Component
                                   .literal(SimpleAuth.colorize(mod.getLocalizedMessage("too_many_attempts",
                                           new Object[0]), "red")));
                   return 0;
               } 
               ((CommandSourceStack)ctx.getSource())
                       .sendFailure((Component)Component
                               .literal(SimpleAuth.colorize(mod.getLocalizedMessage("incorrect_password",
                                       new Object[0]), "red")));
               return 0;
             })));
   }
   
   private static void registerChangePassword(CommandDispatcher<CommandSourceStack> dispatcher, SimpleAuth mod) {
     dispatcher.register(
         (LiteralArgumentBuilder)Commands.literal("changepassword")
         .then(Commands.argument("oldpassword", (ArgumentType)StringArgumentType.string())
           .then(Commands.argument("newpassword", (ArgumentType)StringArgumentType.string())
             .then(Commands.argument("confirm", (ArgumentType)StringArgumentType.string())
               .executes(ctx -> {
                   ServerPlayer player = ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
                   String oldPass = StringArgumentType.getString(ctx, "oldpassword");
                   String newPass = StringArgumentType.getString(ctx, "newpassword");
                   String confirm = StringArgumentType.getString(ctx, "confirm");
                   String username = player.getGameProfile().getName();
                   if (!((Boolean)mod.getPlayerSessionStatus().getOrDefault(player.getUUID(),
                           Boolean.valueOf(false))).booleanValue()) {
                     ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component
                             .literal(SimpleAuth.colorize(mod.getLocalizedMessage("must_be_logged_in",
                                     new Object[0]), "red")));
                     return 0;
                   } 
                   if (!newPass.equals(confirm)) {
                     ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component
                             .literal(SimpleAuth.colorize(mod.getLocalizedMessage("password_mismatch",
                                     new Object[0]), "red")));
                     return 0;
                   } 
                   AuthResult result = DatabaseManager.authenticateUser(username, oldPass);
                   if (result != AuthResult.SUCCESS) {
                     ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component
                             .literal(SimpleAuth.colorize(mod.getLocalizedMessage("incorrect_old_password",
                                     new Object[0]), "red")));
                     return 0;
                   } 
                   if (DatabaseManager.updatePassword(username, newPass)) {
                     ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal(""), false);
                     return 1;
                   } 
                   ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component
                           .literal(SimpleAuth.colorize(mod.getLocalizedMessage("password_change_failed",
                                   new Object[0]), "red")));
                   return 0;
                 })))));
   }

   private static void registerSimpleAuthGroup(CommandDispatcher<CommandSourceStack> dispatcher, SimpleAuth mod) {
     dispatcher.register(
         (LiteralArgumentBuilder)Commands.literal("simpleauth")
         .then(Commands.literal("language")
           .then(Commands.argument("language", (ArgumentType)StringArgumentType.word())
             .executes(ctx -> {
                 ServerPlayer player = ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
                 if (!((Boolean)mod.getPlayerSessionStatus().getOrDefault(player.getUUID(),
                         Boolean.valueOf(false))).booleanValue()) {
                   ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component
                           .literal(SimpleAuth.colorize(mod.getLocalizedMessage("must_be_logged_in",
                                   new Object[0]), "red")));
                   return 0;
                 } 
                 String lang = StringArgumentType.getString(ctx, "language");
                 if (!lang.matches("en|es|fr|ru|ua|pl")) {
                   ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component
                           .literal(SimpleAuth.colorize(mod.getLocalizedMessage("invalid_language",
                                   new Object[0]), "red")));
                   return 0;
                 } 
                 ConfigManager.setLanguage(lang);
                 switch (lang) {
                   case "en":
                   case "es":
                   case "fr":
                   case "ru":
                   case "ua":
                   case "pl":
                 } 
                 String langName = lang;
                 ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal(""), false);
                 return 1;
               }))));
     dispatcher.register(
         (LiteralArgumentBuilder)Commands.literal("simpleauth")
         .then(Commands.literal("help")
           .executes(ctx -> {
               ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal(""), false);
               return 1;
             })));

     dispatcher.register(
         (LiteralArgumentBuilder)Commands.literal("simpleauth")
         .then(((LiteralArgumentBuilder)Commands.literal("forcelogin")
           .requires(source -> source.hasPermission(4)))
           .then(Commands.argument("player", (ArgumentType)GameProfileArgument.gameProfile())
             .executes(ctx -> {
                 Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(ctx, "player");
                 for (GameProfile profile : profiles) {
                   ServerPlayer target = ((CommandSourceStack)ctx.getSource()).getServer().getPlayerList()
                           .getPlayer(profile.getId());
                   if (target != null) {
                     mod.completeLogin(target);
                     ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal(""), true);
                   } 
                 } 
                 return 1;
             }))));
   }
 }