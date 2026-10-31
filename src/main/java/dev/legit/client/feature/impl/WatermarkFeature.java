package dev.legit.client.feature.impl;

import dev.legit.client.feature.Category;
import dev.legit.client.feature.Feature;
import dev.legit.client.feature.setting.BoolSetting;

public class WatermarkFeature extends Feature {
	public final BoolSetting showFps = add(new BoolSetting("Show FPS", true));

	public WatermarkFeature() {
		super("Watermark", "Client name at the top of the screen", Category.HUD);
	}
}
