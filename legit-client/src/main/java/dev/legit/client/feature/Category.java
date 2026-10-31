package dev.legit.client.feature;

public enum Category {
	VISUAL("Visual"),
	MOVEMENT("Movement"),
	HUD("HUD");

	public final String label;

	Category(String label) {
		this.label = label;
	}
}
