package dev.legit.client.feature.impl;

import dev.legit.client.feature.Category;
import dev.legit.client.feature.Feature;
import dev.legit.client.feature.setting.BoolSetting;
import dev.legit.client.feature.setting.NumSetting;

public class HudFeature extends Feature {
	public final BoolSetting fps = add(new BoolSetting("FPS", true));
	public final BoolSetting coords = add(new BoolSetting("Coordinates", true));
	public final BoolSetting cps = add(new BoolSetting("CPS", true));
	public final BoolSetting armor = add(new BoolSetting("Armor", true));
	public final NumSetting scale = add(new NumSetting("Scale", 1.0, 0.7, 1.6, 0.1, "x"));

	public HudFeature() {
		super("HUD", "FPS, coordinates, CPS and armor overlay", Category.HUD);
		this.enabled = true;
		this.anim = 1f;
	}
}
