package com.gamma.simpleauth.client;

import com.gamma.simpleauth.network.packets.LoginFailedPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandler {
    public static void openLoginScreen(int countdownSeconds) {
        Minecraft.getInstance().setScreen(new LoginScreen(countdownSeconds));
    }

    public static void openRegisterScreen(int countdownSeconds) {
        Minecraft.getInstance().setScreen(new RegisterScreen(countdownSeconds));
    }

    public static void onLoginFailed(LoginFailedPacket.Reason reason, int countdownSeconds) {
        Minecraft mc = Minecraft.getInstance();

        String errorKey = (reason == LoginFailedPacket.Reason.RATE_LIMITED)
             ? "simpleauth.screen.login.rate_limited"
             : "simpleauth.screen.login.wrong_password";
        mc.setScreen(new LoginScreen(countdownSeconds, errorKey));
    }
}
