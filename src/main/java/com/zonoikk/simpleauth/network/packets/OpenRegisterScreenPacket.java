 package com.zonoikk.simpleauth.network.packets;
 
 import com.zonoikk.simpleauth.client.ClientPacketHandler;
 import java.util.function.Supplier;
 import net.minecraft.network.FriendlyByteBuf;
 import net.minecraftforge.api.distmarker.Dist;
 import net.minecraftforge.fml.DistExecutor;
 import net.minecraftforge.network.NetworkEvent;
 
 
 
 
 
 public class OpenRegisterScreenPacket
 {
   private final int countdownSeconds;
   
   public OpenRegisterScreenPacket(int countdownSeconds) {
     this.countdownSeconds = countdownSeconds;
   }
   
   public void encode(FriendlyByteBuf buf) {
     buf.writeInt(this.countdownSeconds);
   }
   
   public static OpenRegisterScreenPacket decode(FriendlyByteBuf buf) {
     return new OpenRegisterScreenPacket(buf.readInt());
   }
   
   public static void handle(OpenRegisterScreenPacket packet, Supplier<NetworkEvent.Context> ctx) {
     ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.openRegisterScreen(packet.getCountdownSeconds())));
 
 
 
     
     ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
   }
   
   public int getCountdownSeconds() {
     return this.countdownSeconds;
   }
 }


/* Location:              /mnt/worker/simpleauth-5.0.0.jar!/com/zonoikk/simpleauth/network/packets/OpenRegisterScreenPacket.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */