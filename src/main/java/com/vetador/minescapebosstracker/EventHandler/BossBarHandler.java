package com.vetador.minescapebosstracker.EventHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mojang.text2speech.Narrator.LOGGER;
import static com.vetador.minescapebosstracker.BossTracker.MODID;
import static com.vetador.minescapebosstracker.BossTracker.featuresEnabled;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class BossBarHandler {



    public static class BossInfo {
        public String bossName;
        public float currentHealth;
        public float maxHealth;
        public boolean inCombat;

        public BossInfo(String bossName, float currentHealth, float maxHealth, boolean inCombat) {
            this.bossName = bossName;
            this.currentHealth = currentHealth;
            this.maxHealth = maxHealth;
            this.inCombat = inCombat;
        }
    }

    public static final Map<Entity, BossInfo> bossEntities = new HashMap<>();

    public static final Map<String, Integer> bossNameAndHealth = new HashMap<>();

    static {
        bossNameAndHealth.put("Dungeon keeper", 11251);
        bossNameAndHealth.put("Koschei the deathless", 101010);
        bossNameAndHealth.put("Delrith", 1000);
        bossNameAndHealth.put("Obor", 3000);
        bossNameAndHealth.put("Silver Guardian", 3000);
        bossNameAndHealth.put("Chaos Elemental", 22000);
        bossNameAndHealth.put("Dharok the Wretched", 3500);
        bossNameAndHealth.put("Karil the Tainted", 3500);
        bossNameAndHealth.put("Guthan the Infested", 3500);
        bossNameAndHealth.put("Ahrim the Blighted", 3500);
        bossNameAndHealth.put("Verac the Defiled", 3500);
        bossNameAndHealth.put("Torag the Corrupted", 3500);
        bossNameAndHealth.put("Dad", 4000);
        bossNameAndHealth.put("Scorpia", 30000);
        bossNameAndHealth.put("Venenatis", 28000);
        bossNameAndHealth.put("Bryophyta", 5500);
        bossNameAndHealth.put("Evil Chicken", 8000);
        bossNameAndHealth.put("Skip's pet", 20240);
        bossNameAndHealth.put("Kraken", 6500);
        bossNameAndHealth.put("Kalphite queen", 10000);
        bossNameAndHealth.put("Dagannoth Supreme", 8000);
        bossNameAndHealth.put("Dagannoth Prime", 8000);
        bossNameAndHealth.put("Dagannoth Rex", 8000);
        bossNameAndHealth.put("The creature", 10000);
        bossNameAndHealth.put("King Black Dragon", 15000);
        bossNameAndHealth.put("Kree'arra", 20000);
        bossNameAndHealth.put("Commander Zilyana", 20000);
        bossNameAndHealth.put("General Graardor", 20000);
        bossNameAndHealth.put("K'ril Tsutsaroth", 20000);
        bossNameAndHealth.put("TzTok Jad", 25000);
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Entity entity = event.getEntity();
        if (bossEntities.containsKey(entity) && featuresEnabled) {
            String normalizedName = bossEntities.get(entity).bossName;
            float currentHealth = bossEntities.get(entity).currentHealth;
            float maxHealth = bossEntities.get(entity).maxHealth;
            bossEntities.replace(entity, new BossBarHandler.BossInfo(normalizedName, currentHealth, maxHealth, true));
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        bossEntities.remove(entity);
    }

    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        bossEntities.remove(entity);
    }



    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        int index = 0;
        if (!bossEntities.isEmpty() && featuresEnabled) {
            for (Map.Entry<Entity, BossInfo> entry : bossEntities.entrySet()) {
                BossInfo bossInfo = entry.getValue();
                if (bossInfo.currentHealth > 0 && bossInfo.inCombat) {
                    onRenderCustomBossBar(event.getGuiGraphics(), bossInfo.bossName, bossInfo.currentHealth, bossInfo.maxHealth, index);
                    index++;
                }
            }
        }
    }

    public static boolean isBoss(String input) {
        return bossNameAndHealth.containsKey(input);
    }

    public static String normalizeName(String name) {
        return name.replaceAll("[^a-zA-Z\\s']", "").replaceAll("\\s+$", "").trim();
    }

    public static float getBossCurrentHealth(String name)
    {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return 0;
    }

    private static void onRenderCustomBossBar(GuiGraphics guiGraphics, String bossName, float currentHealth, float maxHealth, int index) {
        int i = guiGraphics.guiWidth();
        int j = 16 + (20 * index);
        int k = i / 2 - 91;
        int l = getBarColor(currentHealth, maxHealth);
        drawBar(guiGraphics, bossName, k, j, l, currentHealth, maxHealth, index);
    }

    private static void drawBar(GuiGraphics guiGraphics, String bossName, int pX, int pY, int color, float currentHealth, float maxHealth, int index) {

        int i = (int) ((currentHealth / maxHealth) * 183.0F);
        if (i > 0) {
            Font font = Minecraft.getInstance().font;
            float scale = 0.5f;
            String healthText = (int) currentHealth + " / " + (int) maxHealth + " (" + String.format("%.1f%%", (currentHealth / maxHealth) * 100) + ")";
            int bossNameWidth = font.width(bossName);
            int healthTextWidth = font.width(healthText);
            int bossNameX = (int) ((pX + (183 / 2)) / scale) - (bossNameWidth / 2);
            int healthTextX = (int) ((pX + (183 / 2)) / scale) - (healthTextWidth / 2);
            guiGraphics.pose().pushPose();
            guiGraphics.fill(RenderType.guiOverlay(), pX - 1, pY + 1, pX + 183, pY - 14, 0xFF1c1c1c);
            guiGraphics.fill(RenderType.guiOverlay(), pX, pY, pX + i - 1, pY - 6, color | 0xFF000000);
            guiGraphics.pose().scale(scale, scale, 1);
            guiGraphics.drawString(font, bossName, bossNameX, (pY - 12) / scale, 0xFFff8200, true);
            guiGraphics.drawString(font, healthText, healthTextX, (pY - 5) / scale, 0xFFFFFFFF, false);
            guiGraphics.pose().popPose();
        }
    }

    public static int getBarColor(float currentHealth, float maxHealth) {
        float f = Math.max(0.0f, currentHealth / maxHealth);
        float hue = Math.max(0, f / 2.0f - 0.12f);
        int barColor = Mth.hsvToRgb(hue, 1.0f, 0.6f);
        return barColor;
    }
}
