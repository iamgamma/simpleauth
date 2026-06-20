 package com.zonoikk.simpleauth.network.packets;
 
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
     ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().setScreen(null)));
 
 
 
     
     ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
   }
 }


/* Location:              /mnt/worker/simpleauth-5.0.0.jar!/com/zonoikk/simpleauth/network/packets/LoginSuccessPacket.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */