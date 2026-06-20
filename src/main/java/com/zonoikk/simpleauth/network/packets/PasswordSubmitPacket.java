 package com.zonoikk.simpleauth.network.packets;
 
 import com.zonoikk.simpleauth.AuthResult;
 import com.zonoikk.simpleauth.ConfigManager;
 import com.zonoikk.simpleauth.DatabaseManager;
 import com.zonoikk.simpleauth.SimpleAuth;
 import com.zonoikk.simpleauth.network.ModPacketHandler;
 import java.util.function.Supplier;
 import net.minecraft.network.FriendlyByteBuf;
 import net.minecraft.network.chat.Component;
 import net.minecraft.server.level.ServerPlayer;
 import net.minecraftforge.fml.ModList;
 import net.minecraftforge.network.NetworkEvent;
 
 public class PasswordSubmitPacket
 {
   private final Mode mode;
   private final String password;
   private final String confirm;
   
   public enum Mode {
     LOGIN, REGISTER;
   }
 
 
 
   
   public PasswordSubmitPacket(Mode mode, String password, String confirm) {
     this.mode = mode;
     this.password = password;
     this.confirm = confirm;
   }
   
   public void encode(FriendlyByteBuf buf) {
     buf.writeEnum(this.mode);
     buf.writeUtf(this.password, 256);
     buf.writeUtf(this.confirm, 256);
   }
   
   public static PasswordSubmitPacket decode(FriendlyByteBuf buf) {
     Mode mode = (Mode)buf.readEnum(Mode.class);
     String password = buf.readUtf(256);
     String confirm = buf.readUtf(256);
     return new PasswordSubmitPacket(mode, password, confirm);
   }
   
   public static void handle(PasswordSubmitPacket packet, Supplier<NetworkEvent.Context> ctx) {
     ServerPlayer player = ((NetworkEvent.Context)ctx.get()).getSender();
     if (player == null) {
       ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
       
       return;
     } 
     ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> {
           SimpleAuth mod = ModList.get().getModObjectById("simpleauth").filter(obj -> obj instanceof SimpleAuth).map(obj -> (SimpleAuth) obj).orElse(null);
           if (mod == null) {
             return;
           }
           if (((Boolean)mod.getPlayerSessionStatus().getOrDefault(player.getUUID(), Boolean.valueOf(false))).booleanValue()) {
             return;
           }
           String username = player.getGameProfile().getName();
           if (packet.mode == Mode.REGISTER) {
             if (!packet.password.equals(packet.confirm)) {
               player.sendSystemMessage((Component)Component.literal(SimpleAuth.colorize(mod.getLocalizedMessage("password_mismatch", new Object[0]), "red")));
               return;
             } 
             if (DatabaseManager.isUserRegistered(username)) {
               player.sendSystemMessage((Component)Component.literal(SimpleAuth.colorize(mod.getLocalizedMessage("already_registered", new Object[0]), "red")));
               return;
             } 
             if (DatabaseManager.registerUser(username, packet.password)) {
               mod.completeLogin(player);
               player.sendSystemMessage((Component)Component.literal(SimpleAuth.colorize(mod.getLocalizedMessage("registration_successful", new Object[0]), "green")));
             } else {
               player.sendSystemMessage((Component)Component.literal(SimpleAuth.colorize(mod.getLocalizedMessage("registration_failed", new Object[0]), "red")));
             } 
           } else {
             if (!DatabaseManager.isUserRegistered(username)) {
               player.sendSystemMessage((Component)Component.literal(SimpleAuth.colorize(mod.getLocalizedMessage("not_registered", new Object[0]), "red")));
               return;
             } 
             AuthResult result = DatabaseManager.authenticateUser(username, packet.password);
             switch (result) {
               case SUCCESS:
                 mod.completeLogin(player);
                 player.sendSystemMessage((Component)Component.literal(SimpleAuth.colorize(mod.getLocalizedMessage("login_successful", new Object[0]), "green")));
                 return;
 
 
 
 
 
 
 
 
 
               
               case RATE_LIMITED:
                 player.connection.disconnect((Component)Component.literal(SimpleAuth.colorize(mod.getLocalizedMessage("too_many_attempts", new Object[0]), "red")));
                 return;
             } 
 
 
 
 
 
 
 
 
 
             
             if (ConfigManager.isKickOnWrongPassword()) {
               player.connection.disconnect((Component)Component.literal(SimpleAuth.colorize(mod.getLocalizedMessage("kicked_wrong_password", new Object[0]), "red")));
             } else {
               ModPacketHandler.sendToPlayer(player, new LoginFailedPacket(LoginFailedPacket.Reason.WRONG_PASSWORD, mod.getRemainingCountdown(player)));
             } 
           } 
         });
     ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
   }
 }


/* Location:              /mnt/worker/simpleauth-5.0.0.jar!/com/zonoikk/simpleauth/network/packets/PasswordSubmitPacket.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */