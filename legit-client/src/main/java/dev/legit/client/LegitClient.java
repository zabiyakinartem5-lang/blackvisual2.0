package dev.legit.client;

import dev.legit.client.feature.Feature;
import dev.legit.client.feature.Features;
import dev.legit.client.gui.ClickGui;
import dev.legit.client.gui.HudRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegitClient implements ClientModInitializer {
	public static final Logger LOG = LoggerFactory.getLogger("legitclient");

	@Override
	public void onInitializeClient() {
		Keys.init();
		Config.load();

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (Keys.GUI.wasPressed()) {
				if (mc.currentScreen == null) mc.setScreen(new ClickGui());
			}
			for (Feature f : Features.ALL) f.onTick(mc);
		});

		HudRenderCallback.EVENT.register((context, tickCounter) -> {
			Features.ZOOM.update();
			HudRenderer.render(context, MinecraftClient.getInstance());
		});

		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> Config.save());

		LOG.info("Legit Client загружен. Меню: Right Shift.");
	}
}
