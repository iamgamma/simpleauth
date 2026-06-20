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
 public class RegisterScreen
   extends Screen
 {
   private static final ResourceLocation BANNER = new ResourceLocation("simpleauth", "textures/gui/banner.png");
   private static final int BOX_WIDTH = 200;
   private static final int BOX_HEIGHT = 20;
   private static final int PANEL_W = 260;
   private static final int PANEL_H = 240; // Giảm nhẹ chiều cao khung
   private int countdownSeconds;
   private int tickCounter = 0;
   private EditBox passwordBox;
   private EditBox confirmBox;
   private String errorMessage = "";
   
   public RegisterScreen(int countdownSeconds) {
     super((Component)Component.translatable("simpleauth.screen.register.title"));
     this.countdownSeconds = countdownSeconds;
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
 
     // Dời vị trí các ô nhập lên (giảm offsets khoảng 10-15 đơn vị)
     this.passwordBox = new EditBox(this.font, cx - 100, py + 110, 200, 20, (Component)Component.translatable("simpleauth.screen.register.password"));
     this.passwordBox.setMaxLength(128);
     this.passwordBox.setFormatter((text, cursor) -> {
           StringBuilder masked = new StringBuilder();
           for (int i = 0; i < text.length(); i++)
             masked.append('•'); 
           return FormattedCharSequence.forward(masked.toString(), Style.EMPTY);
         });
     this.passwordBox.setHint((Component)Component.translatable("simpleauth.screen.register.password_hint"));
     addWidget(this.passwordBox);
 
     this.confirmBox = new EditBox(this.font, cx - 100, py + 155, 200, 20, (Component)Component.translatable("simpleauth.screen.register.confirm"));
     this.confirmBox.setMaxLength(128);
     this.confirmBox.setFormatter((text, cursor) -> {
           StringBuilder masked = new StringBuilder();
           for (int i = 0; i < text.length(); i++)
             masked.append('•'); 
           return FormattedCharSequence.forward(masked.toString(), Style.EMPTY);
         });
     this.confirmBox.setHint((Component)Component.translatable("simpleauth.screen.register.confirm_hint"));
     addWidget(this.confirmBox);
     setInitialFocus(this.passwordBox);
 
     addRenderableWidget(Button.builder(
           (Component)Component.translatable("simpleauth.screen.register.confirm"), btn -> submitRegister())
         .bounds(cx - 100, py + 185, 200, 20).build());
   }
   
   private void submitRegister() {
     String password = this.passwordBox.getValue();
     String confirm = this.confirmBox.getValue();
     
     if (password.isEmpty()) {
       this.errorMessage = "Password is required";
       return;
     } 
     if (!password.equals(confirm)) {
       this.errorMessage = "Confirm password not math";
       this.passwordBox.setValue("");
       this.confirmBox.setValue("");
       return;
     } 
     this.errorMessage = "";
     ModPacketHandler.sendToServer(new PasswordSubmitPacket(PasswordSubmitPacket.Mode.REGISTER, password, confirm));
     onClose();
   }
 
 
   
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
       if (keyCode == 258) {
           if (this.passwordBox.isFocused()) {
               this.setFocused(this.confirmBox);
           } else if (this.confirmBox.isFocused()) {
               this.setFocused(this.passwordBox);
           }
           return true;
       }
 
     if (keyCode == 257 || keyCode == 335) {
       submitRegister();
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
 
     // Dời title lên
     graphics.drawCenteredString(this.font, 
         (Component)Component.translatable("simpleauth.screen.register.title"), cx, py + 85, -22016);
 
     // Dời nhãn lên
     graphics.drawString(this.font, 
         (Component)Component.translatable("simpleauth.screen.register.password"), cx - 100, py + 98, -3355444);
     graphics.drawString(this.font, 
         (Component)Component.translatable("simpleauth.screen.register.confirm"), cx - 100, py + 143, -3355444);
 
     if (!this.errorMessage.isEmpty()) {
       graphics.drawCenteredString(this.font, this.errorMessage, cx, py + 213, -43691);
     }
 
     if (this.countdownSeconds > 0) {
       graphics.drawCenteredString(this.font, 
           (Component)Component.translatable("simpleauth.screen.login.time_left", new Object[] { Integer.valueOf(this.countdownSeconds) }), cx, py + PANEL_H - 14, -43691);
     }
 
     this.passwordBox.render(graphics, mouseX, mouseY, partialTick);
     this.confirmBox.render(graphics, mouseX, mouseY, partialTick);
     super.render(graphics, mouseX, mouseY, partialTick);
   }
 
   public boolean shouldCloseOnEsc() { return false; }
   public boolean isPauseScreen() { return false; }
 }
