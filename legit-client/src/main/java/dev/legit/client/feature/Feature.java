package dev.legit.client.feature;

import dev.legit.client.feature.setting.Setting;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public abstract class Feature {
	public final String name;
	public final String description;
	public final Category category;
	public final List<Setting> settings = new ArrayList<>();

	public boolean enabled;

	// состояние анимаций для GUI
	public boolean expanded;
	public float anim;
	public float expandAnim;

	protected Feature(String name, String description, Category category) {
		this.name = name;
		this.description = description;
		this.category = category;
	}

	protected <S extends Setting> S add(S setting) {
		settings.add(setting);
		return setting;
	}

	public void toggle() {
		setEnabled(!enabled);
	}

	public void setEnabled(boolean value) {
		if (value == enabled) return;
		enabled = value;
		if (value) onEnable(); else onDisable();
	}

	public void onEnable() {}

	public void onDisable() {}

	public void onTick(MinecraftClient mc) {}
}
