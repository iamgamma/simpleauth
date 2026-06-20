 package com.zonoikk.simpleauth;
 import com.mojang.logging.LogUtils;
 import com.zonoikk.simpleauth.network.ModPacketHandler;
 import com.zonoikk.simpleauth.network.packets.LoginSuccessPacket;
 import com.zonoikk.simpleauth.network.packets.OpenLoginScreenPacket;
 import com.zonoikk.simpleauth.network.packets.OpenRegisterScreenPacket;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 import net.minecraft.commands.CommandSourceStack;
 import net.minecraft.network.chat.Component;
 import net.minecraft.server.level.ServerBossEvent;
 import net.minecraft.server.level.ServerPlayer;
 import net.minecraft.world.BossEvent;
 import net.minecraft.world.effect.MobEffectInstance;
 import net.minecraft.world.effect.MobEffects;
 import net.minecraft.world.entity.Entity;
 import net.minecraft.world.entity.player.Player;
 import net.minecraft.world.level.GameType;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.CommandEvent;
 import net.minecraftforge.event.RegisterCommandsEvent;
 import net.minecraftforge.event.ServerChatEvent;
 import net.minecraftforge.event.TickEvent;
 import net.minecraftforge.event.entity.player.PlayerEvent;
 import net.minecraftforge.event.server.ServerStartingEvent;
 import net.minecraftforge.eventbus.api.SubscribeEvent;
 import net.minecraftforge.fml.common.Mod;
 import org.slf4j.Logger;
 
 @Mod("simpleauth")
 public class SimpleAuth {
   public static final String MODID = "simpleauth";
   private static final Logger LOGGER = LogUtils.getLogger();
   
   private final Map<UUID, Boolean> playerSessionStatus = new HashMap<>();
   private final InvulnerabilityHandler invulnerabilityHandler = new InvulnerabilityHandler();
   private final Map<UUID, Integer> loginCountdowns = new HashMap<>();
   private final Map<UUID, ServerBossEvent> authBossBars = new HashMap<>();
   private final Map<UUID, double[]> playerInitialPositions = (Map)new HashMap<>();
   
   public SimpleAuth() {
     MinecraftForge.EVENT_BUS.register(this);
     ConfigManager.init();
     DatabaseManager.initializeDatabase();
     ModPacketHandler.register();
     MinecraftForge.EVENT_BUS.register(this.invulnerabilityHandler);
     MinecraftForge.EVENT_BUS.register(new GlobalAuthListener());
   }
   
   public static String colorize(String message, String color) {
     switch (color.toLowerCase()) { case "red": case "redbold": case "green": case "greenbold": case "gold": case "special":  }  return 
 
 
 
 
 
       
       message;
   }
 
 
 
 
 
   
   public Map<UUID, Boolean> getPlayerSessionStatus() {
     return this.playerSessionStatus;
   }
   
   public Map<UUID, ServerBossEvent> getAuthBossBars() {
     return this.authBossBars;
   }
   
   public int getRemainingCountdown(ServerPlayer player) {
     return ((Integer)this.loginCountdowns.getOrDefault(player.getUUID(), Integer.valueOf(0))).intValue();
   }
 
 
 
 
   
   public void completeLogin(ServerPlayer player) {
     UUID id = player.getUUID();
     this.playerSessionStatus.put(id, Boolean.valueOf(true));
     removeEffects(player);
     startInvulnerabilityTimer(player);
     ServerBossEvent bossBar = this.authBossBars.remove(id);
     if (bossBar != null) bossBar.removePlayer(player);
     
     if (ModPacketHandler.clientHasMod(player)) {
       ModPacketHandler.sendToPlayer(player, new LoginSuccessPacket());
     }
   }
 
 
 
 
   
   @SubscribeEvent
   public void onServerStarting(ServerStartingEvent event) {
     LOGGER.info("SimpleAuth is ready.");
   }
   @SubscribeEvent
   public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
     ServerPlayer player;
     Player player1 = event.getEntity(); if (player1 instanceof ServerPlayer) { player = (ServerPlayer)player1; }
     else { return; }
      UUID playerId = player.getUUID();
     this.playerSessionStatus.put(playerId, Boolean.valueOf(false));
     this.playerInitialPositions.put(playerId, new double[] { player.getX(), player.getY(), player.getZ() });
     
     applyFreezeAndBlindEffect(player);
     this.loginCountdowns.put(playerId, Integer.valueOf(ConfigManager.getAuthCountdown()));
 
     
     ServerBossEvent bossBar = new ServerBossEvent((Component)Component.literal(colorize(getLocalizedMessage("login_bossbar", new Object[] { Integer.valueOf(ConfigManager.getAuthCountdown()) }), "gold")), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
 
 
     
     bossBar.setProgress(1.0F);
     bossBar.addPlayer(player);
     this.authBossBars.put(playerId, bossBar);
     
     String username = player.getGameProfile().getName();
     boolean isRegistered = DatabaseManager.isUserRegistered(username);
     
     if (ModPacketHandler.clientHasMod(player)) {
       
       if (isRegistered) {
         ModPacketHandler.sendToPlayer(player, new OpenLoginScreenPacket(ConfigManager.getAuthCountdown()));
       } else {
         ModPacketHandler.sendToPlayer(player, new OpenRegisterScreenPacket(ConfigManager.getAuthCountdown()));
       }
     
     }
     else if (isRegistered) {
       player.sendSystemMessage((Component)Component.literal(colorize(getLocalizedMessage("welcome_back", new Object[0]), "green")));
     } else {
       player.sendSystemMessage((Component)Component.literal(colorize(getLocalizedMessage("welcome_register", new Object[0]), "gold")));
     } 
   }
   
   @SubscribeEvent
   public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
     ServerPlayer player;
     Player player1 = event.getEntity(); if (player1 instanceof ServerPlayer) { player = (ServerPlayer)player1; } else { return; }
      UUID id = player.getUUID();
     this.playerSessionStatus.remove(id);
     this.loginCountdowns.remove(id);
     this.playerInitialPositions.remove(id);
     ServerBossEvent bossBar = this.authBossBars.remove(id);
     if (bossBar != null) bossBar.removePlayer(player); 
   }
   
   @SubscribeEvent
   public void onRegisterCommands(RegisterCommandsEvent event) {
     AuthCommands.register(event.getDispatcher(), this);
   }
 
 
 
   
   @SubscribeEvent
   public void onCommandEvent(CommandEvent event) {
     ServerPlayer player;
     Entity entity = ((CommandSourceStack)event.getParseResults().getContext().getSource()).getEntity(); if (entity instanceof ServerPlayer) { player = (ServerPlayer)entity; }
     else { return; }
      UUID id = player.getUUID();
     if (((Boolean)this.playerSessionStatus.getOrDefault(id, Boolean.valueOf(false))).booleanValue())
       return; 
     String raw = event.getParseResults().getReader().getString().toLowerCase().trim();
     if (!raw.startsWith("login") && !raw.startsWith("register")) {
       event.setCanceled(true);
     }
   }
   
   @SubscribeEvent
   public void onChat(ServerChatEvent event) {
     ServerPlayer player = event.getPlayer();
     if (!((Boolean)this.playerSessionStatus.getOrDefault(player.getUUID(), Boolean.valueOf(false))).booleanValue()) {
       event.setCanceled(true);
     }
   }
 
 
 
 
   
   void applyFreezeAndBlindEffect(ServerPlayer player) {
     player.setGameMode(GameType.SPECTATOR);
     player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 2147483647, 255, false, false));
     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2147483647, 0, false, false));
   }
   
   void removeEffects(ServerPlayer player) {
     player.removeEffect(MobEffects.BLINDNESS);
     player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
     player.setGameMode(GameType.SURVIVAL);
     this.playerInitialPositions.remove(player.getUUID());
   }
   
   private void startInvulnerabilityTimer(ServerPlayer player) {
     this.invulnerabilityHandler.startTimer(player);
   }
   
   public String getLocalizedMessage(String key, Object... args) {
     String message = ConfigManager.getTranslation(key, new Object[0]);
     return (args.length > 0) ? String.format(message, args) : message;
   }
 
 
 
   
   public class GlobalAuthListener
   {
     @SubscribeEvent
     public void onPlayerTick(TickEvent.PlayerTickEvent event) {
       ServerPlayer player;
       if (event.phase != TickEvent.Phase.END)
         return;  Player player1 = event.player; if (player1 instanceof ServerPlayer) { player = (ServerPlayer)player1; }
       else { return; }
        UUID id = player.getUUID();
       if (((Boolean)SimpleAuth.this.playerSessionStatus.getOrDefault(id, Boolean.valueOf(false))).booleanValue()) {
         return;
       }
       double[] pos = SimpleAuth.this.playerInitialPositions.get(id);
       if (pos != null) {
         double dx = Math.abs(player.getX() - pos[0]);
         double dy = Math.abs(player.getY() - pos[1]);
         double dz = Math.abs(player.getZ() - pos[2]);
         if (dx > 0.1D || dy > 0.1D || dz > 0.1D) {
           player.teleportTo(pos[0], pos[1], pos[2]);
         }
       } 
 
       
       if (player.level().getGameTime() % 20L != 0L)
         return; 
       int timeLeft = ((Integer)SimpleAuth.this.loginCountdowns.getOrDefault(id, Integer.valueOf(0))).intValue();
       
       if (timeLeft > 0) {
         SimpleAuth.this.loginCountdowns.put(id, Integer.valueOf(timeLeft - 1));
         
         ServerBossEvent bossBar = SimpleAuth.this.authBossBars.get(id);
         if (bossBar != null) {
           float progress = timeLeft / ConfigManager.getAuthCountdown();
           bossBar.setProgress(Math.max(0.0F, Math.min(1.0F, progress)));
           bossBar.setName((Component)Component.literal(
                 SimpleAuth.colorize(SimpleAuth.this.getLocalizedMessage("login_bossbar", new Object[] { Integer.valueOf(timeLeft) }), "gold")));
         } 
       } else {
         
         player.connection.disconnect((Component)Component.literal(
               SimpleAuth.colorize(SimpleAuth.this.getLocalizedMessage("login_timeout", new Object[0]), "red")));
         
         SimpleAuth.this.playerInitialPositions.remove(id);
         ServerBossEvent bossBar = SimpleAuth.this.authBossBars.remove(id);
         if (bossBar != null) bossBar.removePlayer(player); 
       } 
     }
   }
 }


/* Location:              /mnt/worker/simpleauth-5.0.0.jar!/com/zonoikk/simpleauth/SimpleAuth.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */