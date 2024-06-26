package com.vetador.minescapebosstracker.asm.mixin;


import com.mojang.blaze3d.vertex.PoseStack;
import com.vetador.minescapebosstracker.EventHandler.BossBarHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.mojang.text2speech.Narrator.LOGGER;
import static com.vetador.minescapebosstracker.BossTracker.featuresEnabled;
import static com.vetador.minescapebosstracker.EventHandler.BossBarHandler.*;

@Mixin(EntityRenderer.class)
public abstract class getBossName<T extends Entity> {

    @Inject(method = "renderNameTag", at = @At("HEAD"))
    public void renderNameTag(T pEntity, Component pDisplayName, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci) {
        if (pEntity instanceof LivingEntity && featuresEnabled) {
            Entity entity = pEntity;
            Minecraft mc = Minecraft.getInstance();
            String entityName = pDisplayName.getString();
            String normalizedName = normalizeName(entityName);
            if (isBoss(normalizedName)) {
                double distance = mc.player.distanceTo(pEntity);
                if (distance < 60)
                {
                    int maxHealth = bossNameAndHealth.get(normalizedName);
                    int currentHealth = (int) getBossCurrentHealth(entityName);

                    if (!bossEntities.containsKey(pEntity)) {
                        bossEntities.put(pEntity, new BossBarHandler.BossInfo(normalizedName, currentHealth, maxHealth, false));
                    } else {
                        boolean inCombat = bossEntities.get(pEntity).inCombat;
                        bossEntities.replace(pEntity, new BossBarHandler.BossInfo(normalizedName, currentHealth, maxHealth, inCombat));
                    }
                }
                else
                {
                    bossEntities.remove(pEntity);
                }
            }
        }
    }
}
