 package com.gamma.simpleauth.network.packets;
 
 import com.gamma.simpleauth.client.ClientPacketHandler;
 import java.util.function.Supplier;
 import net.minecraft.network.FriendlyByteBuf;
 import net.minecraftforge.api.distmarker.Dist;
 import net.minecraftforge.fml.DistExecutor;
 import net.minecraftforge.network.NetworkEvent;
 
 public class LoginFailedPacket
 {
   private final Reason reason;
   private final int countdownSeconds;
   
   public enum Reason {
     WRONG_PASSWORD, RATE_LIMITED;
   }

   public LoginFailedPacket(Reason reason, int countdownSeconds) {
     this.reason = reason;
     this.countdownSeconds = countdownSeconds;
   }
   
   public void encode(FriendlyByteBuf buf) {
     buf.writeEnum(this.reason);
     buf.writeInt(this.countdownSeconds);
   }
   
   public static LoginFailedPacket decode(FriendlyByteBuf buf) {
     Reason reason = (Reason)buf.readEnum(Reason.class);
     int countdown = buf.readInt();
     return new LoginFailedPacket(reason, countdown);
   }
   
   public static void handle(LoginFailedPacket packet, Supplier<NetworkEvent.Context> ctx) {
     ((NetworkEvent.Context)ctx.get()).enqueueWork(() ->
             DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                     ClientPacketHandler.onLoginFailed(packet.reason, packet.countdownSeconds)));

     ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
   }
 }