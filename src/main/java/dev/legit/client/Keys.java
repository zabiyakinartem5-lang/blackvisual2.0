package dev.legit.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class Keys {
	private static final String CATEGORY = "category.legitclient";

	public static KeyBinding GUI;
	public static KeyBinding ZOOM;
	public static KeyBinding FREELOOK;

	private Keys() {}

	public static void init() {
		GUI = KeyBindingHelper.registerKeyBinding(
			new KeyBinding("key.legitclient.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, CATEGORY));
		ZOOM = KeyBindingHelper.registerKeyBinding(
			new KeyBinding("key.legitclient.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY));
		FREELOOK = KeyBindingHelper.registerKeyBinding(
			new KeyBinding("key.legitclient.freelook", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, CATEGORY));
	}
}
