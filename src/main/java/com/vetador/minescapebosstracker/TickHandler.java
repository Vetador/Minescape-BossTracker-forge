package com.vetador.minescapebosstracker;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.vetador.minescapebosstracker.ModToggle.toggleKeyBinding;
import static com.mojang.text2speech.Narrator.LOGGER;

@Mod.EventBusSubscriber(modid = BossTracker.MODID, value = Dist.CLIENT)
public class TickHandler {

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event)
    {
        if (toggleKeyBinding == null) {
            return;
        }
        if (event.phase == TickEvent.Phase.END)
        {
            while (toggleKeyBinding.consumeClick()) {
                toggleEnabled();
            }
        }
    }

    static void toggleEnabled() {
        BossTracker.featuresEnabled = !BossTracker.featuresEnabled;
        LOGGER.info("BossTracker is now: " + (BossTracker.featuresEnabled ? "enabled" : "disabled"));
    }

}
