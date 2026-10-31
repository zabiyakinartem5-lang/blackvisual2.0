package dev.legit.client.feature;

import dev.legit.client.feature.impl.*;

import java.util.List;

public final class Features {
	public static final ZoomFeature ZOOM = new ZoomFeature();
	public static final FullbrightFeature FULLBRIGHT = new FullbrightFeature();
	public static final FreelookFeature FREELOOK = new FreelookFeature();
	public static final ToggleSprintFeature TOGGLE_SPRINT = new ToggleSprintFeature();
	public static final ToggleSneakFeature TOGGLE_SNEAK = new ToggleSneakFeature();
	public static final HudFeature HUD = new HudFeature();

	public static final List<Feature> ALL = List.of(ZOOM, FULLBRIGHT, FREELOOK, TOGGLE_SPRINT, TOGGLE_SNEAK, HUD);

	private Features() {}
}
