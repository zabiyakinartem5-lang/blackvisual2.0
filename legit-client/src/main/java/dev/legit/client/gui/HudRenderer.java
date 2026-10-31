package dev.legit.client.gui;

import dev.legit.client.feature.Features;
import dev.legit.client.feature.impl.HudFeature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class HudRenderer {
	private static final Deque<Long> LEFT_CLICKS = new ArrayDeque<>();
	private static final Deque<Long> RIGHT_CLICKS = new ArrayDeque<>();
	private static boolean leftDown, rightDown;

	private static final EquipmentSlot[] ARMOR = {
		EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
	};

	private HudRenderer() {}

	public static void render(DrawContext c, MinecraftClient mc) {
		HudFeature h = Features.HUD;
		if (!h.enabled || mc.player == null || mc.options.hudHidden) return;

		pollClicks(mc);

		float s = (float) h.scale.value;
		c.getMatrices().push();
		c.getMatrices().scale(s, s, 1f);

		drawInfoPanel(c, mc, h);
		if (h.armor.value) drawArmor(c, mc, s);

		c.getMatrices().pop();
	}

	private static void drawInfoPanel(DrawContext c, MinecraftClient mc, HudFeature h) {
		List<String[]> rows = new ArrayList<>();
		if (h.fps.value) rows.add(new String[]{"FPS", String.valueOf(mc.getCurrentFps())});
		if (h.coords.value) {
			rows.add(new String[]{"XYZ", mc.player.getBlockX() + "  " + mc.player.getBlockY() + "  " + mc.player.getBlockZ()});
		}
		if (h.cps.value) rows.add(new String[]{"CPS", cps(LEFT_CLICKS) + " | " + cps(RIGHT_CLICKS)});
		if (rows.isEmpty()) return;

		var tr = mc.textRenderer;
		int labelW = 0, valueW = 0;
		for (String[] r : rows) {
			labelW = Math.max(labelW, tr.getWidth(r[0]));
			valueW = Math.max(valueW, tr.getWidth(r[1]));
		}
		int pad = 6, rowH = 12, gap = 8;
		int w = pad + labelW + gap + valueW + pad + 3;
		int hgt = pad + rows.size() * rowH - 2 + pad - 2;
		int x = 6, y = 6;

		Gfx.rrect(c, x, y, w, hgt, 5, 0xB0101018);
		// акцентная полоса слева
		c.fill(x + 2, y + 5, x + 4, y + hgt - 5, Gfx.ACCENT_A);
		c.fill(x + 2, y + hgt / 2, x + 4, y + hgt - 5, Gfx.ACCENT_B);

		int ty = y + pad - 1;
		for (String[] r : rows) {
			c.drawText(tr, r[0], x + pad + 3, ty, Gfx.ACCENT_B, true);
			c.drawText(tr, r[1], x + pad + 3 + labelW + gap, ty, Gfx.TEXT, true);
			ty += rowH;
		}
	}

	private static void drawArmor(DrawContext c, MinecraftClient mc, float scale) {
		var tr = mc.textRenderer;
		int itemH = 20;
		int panelW = 52, panelH = ARMOR.length * itemH + 8;
		int sw = (int) (mc.getWindow().getScaledWidth() / scale);
		int sh = (int) (mc.getWindow().getScaledHeight() / scale);
		int x = sw - panelW - 6;
		int y = sh / 2 - panelH / 2;

		boolean any = false;
		for (EquipmentSlot slot : ARMOR) if (!mc.player.getEquippedStack(slot).isEmpty()) any = true;
		if (!any) return;

		Gfx.rrect(c, x, y, panelW, panelH, 5, 0xB0101018);
		int iy = y + 4;
		for (EquipmentSlot slot : ARMOR) {
			ItemStack st = mc.player.getEquippedStack(slot);
			if (!st.isEmpty()) {
				c.drawItem(st, x + 5, iy + 1);
				if (st.isDamageable()) {
					int left = st.getMaxDamage() - st.getDamage();
					int pct = Math.round(left * 100f / st.getMaxDamage());
					float f = pct / 100f;
					int col = Gfx.lerp(0xFFEF4444, 0xFF4ADE80, f);
					c.drawText(tr, pct + "%", x + 25, iy + 5, col, true);
				}
			}
			iy += itemH;
		}
	}

	private static void pollClicks(MinecraftClient mc) {
		long handle = mc.getWindow().getHandle();
		long now = System.currentTimeMillis();
		boolean inWorld = mc.currentScreen == null;

		boolean l = inWorld && GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		boolean r = inWorld && GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
		if (l && !leftDown) LEFT_CLICKS.addLast(now);
		if (r && !rightDown) RIGHT_CLICKS.addLast(now);
		leftDown = l;
		rightDown = r;

		trim(LEFT_CLICKS, now);
		trim(RIGHT_CLICKS, now);
	}

	private static void trim(Deque<Long> q, long now) {
		while (!q.isEmpty() && now - q.peekFirst() > 1000) q.pollFirst();
	}

	private static int cps(Deque<Long> q) {
		return q.size();
	}
}
