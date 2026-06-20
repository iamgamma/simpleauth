 package com.zonoikk.simpleauth.client;
 
 import com.zonoikk.simpleauth.network.ModPacketHandler;
 import com.zonoikk.simpleauth.network.packets.PasswordSubmitPacket;
 import net.minecraft.client.gui.GuiGraphics;
 import net.minecraft.client.gui.components.Button;
 import net.minecraft.client.gui.components.EditBox;
 import net.minecraft.client.gui.components.events.GuiEventListener;
 import net.minecraft.client.gui.screens.Screen;
 import net.minecraft.network.chat.Component;
 import net.minecraft.network.chat.Style;
 import net.minecraft.resources.ResourceLocation;
 import net.minecraft.util.FormattedCharSequence;
 import net.minecraftforge.api.distmarker.Dist;
 import net.minecraftforge.api.distmarker.OnlyIn;
 
 
 
 @OnlyIn(Dist.CLIENT)
 public class LoginScreen
   extends Screen
 {
   private static final ResourceLocation BANNER = new ResourceLocation("simpleauth", "textures/gui/banner.png");
   private static final int BOX_WIDTH = 200;
   private static final int BOX_HEIGHT = 20;
   private static final int PANEL_W = 260;
   private static final int PANEL_H = 210; // Giảm nhẹ chiều cao khung cho gọn
   private int countdownSeconds;
   private int tickCounter = 0;
   private final String errorKey;
   private boolean waiting = false;
   private EditBox passwordBox;
   
   public LoginScreen(int countdownSeconds) {
     this(countdownSeconds, (String)null);
   }
   
   public LoginScreen(int countdownSeconds, String errorKey) {
     super((Component)Component.translatable("simpleauth.screen.login.title"));
     this.countdownSeconds = countdownSeconds;
     this.errorKey = errorKey;
   }
 
   
   public void tick() {
     super.tick();
     if (this.countdownSeconds > 0) {
       this.tickCounter++;
       if (this.tickCounter >= 20) {
         this.tickCounter = 0;
         this.countdownSeconds--;
       } 
     } 
   }
 
   
   protected void init() {
     int cx = this.width / 2;
     int cy = this.height / 2;
     int py = cy - (PANEL_H / 2);
 
     // Dời vị trí ô nhập mật khẩu lên (từ +125 thành +115)
     this.passwordBox = new EditBox(this.font, cx - 100, py + 115, 200, 20, (Component)Component.translatable("simpleauth.screen.login.password"));
     this.passwordBox.setMaxLength(128);
     
     this.passwordBox.setFormatter((text, cursor) -> {
           StringBuilder masked = new StringBuilder();
           for (int i = 0; i < text.length(); i++)
             masked.append('•'); 
           return FormattedCharSequence.forward(masked.toString(), Style.EMPTY);
         });
     this.passwordBox.setHint((Component)Component.translatable("simpleauth.screen.login.password_hint"));
     addWidget(this.passwordBox);
     setInitialFocus(this.passwordBox);
 
     // Dời nút bấm lên (từ +155 thành +145)
     addRenderableWidget(Button.builder(
           (Component)Component.translatable("simpleauth.screen.login.confirm"), btn -> submitPassword())
         .bounds(cx - 100, py + 145, 200, 20).build());
   }
   
   private void submitPassword() {
     if (this.waiting)
       return;  String password = this.passwordBox.getValue();
     if (password.isEmpty())
       return; 
     this.waiting = true;
     this.passwordBox.setValue("");
     ModPacketHandler.sendToServer(new PasswordSubmitPacket(PasswordSubmitPacket.Mode.LOGIN, password, ""));
   }
 
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
     if (keyCode == 257 || keyCode == 335) {
       submitPassword();
       return true;
     } 
     return super.keyPressed(keyCode, scanCode, modifiers);
   }
 
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
     renderBackground(graphics);
     
     int cx = this.width / 2;
     int cy = this.height / 2;
     int px = cx - (PANEL_W / 2);
     int py = cy - (PANEL_H / 2);
 
     graphics.fill(px, py, px + PANEL_W, py + PANEL_H, -1157627904);
     graphics.renderOutline(px, py, PANEL_W, PANEL_H, -11184811);
 
     int bannerW = 160;
     int bannerH = 80;
     graphics.blit(BANNER, cx - (bannerW / 2), py + 5, 0.0F, 0.0F, bannerW, bannerH, bannerW, bannerH);
 
     // Dời title lên (từ +95 thành +85)
     graphics.drawCenteredString(this.font, 
         (Component)Component.translatable("simpleauth.screen.login.title"), cx, py + 85, -22016);
 
     // Dời nhãn Password lên (từ +113 thành +103)
     graphics.drawString(this.font, 
         (Component)Component.translatable("simpleauth.screen.login.password"), cx - 100, py + 103, -3355444);
 
     if (this.errorKey != null) {
       graphics.drawCenteredString(this.font, 
           (Component)Component.translatable(this.errorKey), cx, py + 172, -43691);
     }
 
     if (this.countdownSeconds > 0) {
       graphics.drawCenteredString(this.font, 
           (Component)Component.translatable("simpleauth.screen.login.time_left", new Object[] { Integer.valueOf(this.countdownSeconds) }), cx, py + PANEL_H - 14, -43691);
     }
 
     if (this.waiting) {
       graphics.drawCenteredString(this.font, 
           (Component)Component.translatable("simpleauth.screen.login.waiting"), cx, py + 175, -5592321);
     }
 
     this.passwordBox.render(graphics, mouseX, mouseY, partialTick);
     super.render(graphics, mouseX, mouseY, partialTick);
   }
 
   public boolean shouldCloseOnEsc() { return false; }
   public boolean isPauseScreen() { return false; }
 }
