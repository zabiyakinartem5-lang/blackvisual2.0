package dev.legit.client.feature;

import dev.legit.client.feature.impl.*;

import java.util.List;

public final class Features {
	// Visual
	public static final ZoomFeature ZOOM = new ZoomFeature();
	public static final FullbrightFeature FULLBRIGHT = new FullbrightFeature();
	public static final FreelookFeature FREELOOK = new FreelookFeature();
	public static final AspectRatioFeature ASPECT = new AspectRatioFeature();
	public static final MotionBlurFeature MOTION_BLUR = new MotionBlurFeature();
	public static final FovFeature FOV = new FovFeature();
	public static final TimeChangerFeature TIME_CHANGER = new TimeChangerFeature();

	// Movement
	public static final ToggleSprintFeature TOGGLE_SPRINT = new ToggleSprintFeature();
	public static final ToggleSneakFeature TOGGLE_SNEAK = new ToggleSneakFeature();

	// HUD
	public static final HudFeature HUD = new HudFeature();
	public static final KeystrokesFeature KEYSTROKES = new KeystrokesFeature();
	public static final EffectsFeature EFFECTS = new EffectsFeature();
	public static final ModuleListFeature MODULE_LIST = new ModuleListFeature();
	public static final WatermarkFeature WATERMARK = new WatermarkFeature();

	// Client
	public static final ThemeFeature THEME = new ThemeFeature();

	public static final List<Feature> ALL = List.of(
		ZOOM, FULLBRIGHT, FREELOOK, ASPECT, MOTION_BLUR, FOV, TIME_CHANGER,
		TOGGLE_SPRINT, TOGGLE_SNEAK,
		HUD, KEYSTROKES, EFFECTS, MODULE_LIST, WATERMARK,
		THEME
	);

	private Features() {}
}
