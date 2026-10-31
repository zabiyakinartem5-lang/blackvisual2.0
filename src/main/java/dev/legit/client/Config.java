package dev.legit.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.legit.client.feature.Feature;
import dev.legit.client.feature.Features;
import dev.legit.client.feature.setting.BoolSetting;
import dev.legit.client.feature.setting.NumSetting;
import dev.legit.client.feature.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Config {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("legitclient.json");

	private Config() {}

	public static void save() {
		JsonObject root = new JsonObject();
		for (Feature f : Features.ALL) {
			JsonObject fo = new JsonObject();
			fo.addProperty("enabled", f.enabled);
			JsonObject so = new JsonObject();
			for (Setting s : f.settings) {
				if (s instanceof BoolSetting b) so.addProperty(s.name, b.value);
				else if (s instanceof NumSetting n) so.addProperty(s.name, n.value);
			}
			fo.add("settings", so);
			root.add(f.name, fo);
		}
		try {
			Files.writeString(FILE, GSON.toJson(root), StandardCharsets.UTF_8);
		} catch (IOException e) {
			LegitClient.LOG.warn("Не удалось сохранить конфиг", e);
		}
	}

	public static void load() {
		if (!Files.exists(FILE)) return;
		try {
			JsonObject root = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), JsonObject.class);
			if (root == null) return;
			for (Feature f : Features.ALL) {
				if (!root.has(f.name)) continue;
				JsonObject fo = root.getAsJsonObject(f.name);
				if (fo.has("enabled")) {
					boolean en = fo.get("enabled").getAsBoolean();
					f.enabled = en;
					f.anim = en ? 1f : 0f;
				}
				if (!fo.has("settings")) continue;
				JsonObject so = fo.getAsJsonObject("settings");
				for (Setting s : f.settings) {
					if (!so.has(s.name)) continue;
					if (s instanceof BoolSetting b) {
						b.value = so.get(s.name).getAsBoolean();
						b.anim = b.value ? 1f : 0f;
					} else if (s instanceof NumSetting n) {
						n.set(so.get(s.name).getAsDouble());
					}
				}
			}
		} catch (Exception e) {
			LegitClient.LOG.warn("Не удалось прочитать конфиг, используются значения по умолчанию", e);
		}
	}
}
