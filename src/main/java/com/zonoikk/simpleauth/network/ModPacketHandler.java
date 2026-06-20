 package com.zonoikk.simpleauth.network;
 
 import com.zonoikk.simpleauth.network.packets.LoginFailedPacket;
 import com.zonoikk.simpleauth.network.packets.LoginSuccessPacket;
 import com.zonoikk.simpleauth.network.packets.OpenLoginScreenPacket;
 import com.zonoikk.simpleauth.network.packets.OpenRegisterScreenPacket;
 import com.zonoikk.simpleauth.network.packets.PasswordSubmitPacket;
 import net.minecraft.resources.ResourceLocation;
 import net.minecraft.server.level.ServerPlayer;
 import net.minecraftforge.network.NetworkRegistry;
 import net.minecraftforge.network.PacketDistributor;
 import net.minecraftforge.network.simple.SimpleChannel;
 
 public class ModPacketHandler {
   private static final String PROTOCOL_VERSION = "1";
   public static final SimpleChannel CHANNEL;
   
   static {
     CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation("simpleauth", "main"), () -> "1", v -> 
 
 
         
         (v.equals("1") || v.equals(NetworkRegistry.ABSENT)), v -> 
         
         (v.equals("1") || v.equals(NetworkRegistry.ABSENT)));
   }
   
   private static int nextId = 0;
 
   
   public static void register() {
     CHANNEL.messageBuilder(OpenLoginScreenPacket.class, nextId++)
       .encoder(OpenLoginScreenPacket::encode)
       .decoder(OpenLoginScreenPacket::decode)
       .consumerMainThread(OpenLoginScreenPacket::handle)
       .add();
 
     
     CHANNEL.messageBuilder(OpenRegisterScreenPacket.class, nextId++)
       .encoder(OpenRegisterScreenPacket::encode)
       .decoder(OpenRegisterScreenPacket::decode)
       .consumerMainThread(OpenRegisterScreenPacket::handle)
       .add();
 
     
     CHANNEL.messageBuilder(PasswordSubmitPacket.class, nextId++)
       .encoder(PasswordSubmitPacket::encode)
       .decoder(PasswordSubmitPacket::decode)
       .consumerMainThread(PasswordSubmitPacket::handle)
       .add();
 
     
     CHANNEL.messageBuilder(LoginFailedPacket.class, nextId++)
       .encoder(LoginFailedPacket::encode)
       .decoder(LoginFailedPacket::decode)
       .consumerMainThread(LoginFailedPacket::handle)
       .add();
 
     
     CHANNEL.messageBuilder(LoginSuccessPacket.class, nextId++)
       .encoder(LoginSuccessPacket::encode)
       .decoder(LoginSuccessPacket::decode)
       .consumerMainThread(LoginSuccessPacket::handle)
       .add();
   }
 
   
   public static void sendToPlayer(ServerPlayer player, Object packet) {
     CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
   }
 
   
   public static void sendToServer(Object packet) {
     CHANNEL.sendToServer(packet);
   }
 
 
 
 
   
   public static boolean clientHasMod(ServerPlayer player) {
     return CHANNEL.isRemotePresent(player.connection.connection);
   }
 }


/* Location:              /mnt/worker/simpleauth-5.0.0.jar!/com/zonoikk/simpleauth/network/ModPacketHandler.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */