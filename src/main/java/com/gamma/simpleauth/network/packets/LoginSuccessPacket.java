 package com.gamma.simpleauth.network.packets;
 
 import java.util.function.Supplier;
 import net.minecraft.client.Minecraft;
 import net.minecraft.network.FriendlyByteBuf;
 import net.minecraftforge.api.distmarker.Dist;
 import net.minecraftforge.fml.DistExecutor;
 import net.minecraftforge.network.NetworkEvent;

 public class LoginSuccessPacket
 {
   public void encode(FriendlyByteBuf buf) {}
   
   public static LoginSuccessPacket decode(FriendlyByteBuf buf) {
     return new LoginSuccessPacket();
   }
   
   public static void handle(LoginSuccessPacket packet, Supplier<NetworkEvent.Context> ctx) {
     ((NetworkEvent.Context)ctx.get()).enqueueWork(() ->
             DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                     Minecraft.getInstance().setScreen(null)));
     
     ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
   }
 }