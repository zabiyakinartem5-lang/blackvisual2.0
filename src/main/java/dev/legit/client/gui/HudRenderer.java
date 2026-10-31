package dev.legit.client.gui;

import dev.legit.client.feature.Features;
import dev.legit.client.feature.impl.HudFeature;
import dev.legit.client.feature.impl.KeystrokesFeature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class HudRenderer {
	private static final Deque<Long> LEFT_CLICKS = new ArrayDeque<>();
	private static final Deque<Long> RIGHT_CLICKS = new ArrayDeque<>();
	private static boolean leftDown, rightDown;

	// анимации клавиш: W A S D LMB RMB SPACE
	private static final float[] KEY_ANIM = new float[7];
	private static long lastFrame = System.nanoTime();

	private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");

	private static final EquipmentSlot[] ARMOR = {
		EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
	};

	private HudRenderer() {}

	public static void render(DrawContext c, MinecraftClient mc) {
		if (mc.player == null || mc.options.hudHidden) return;

		long now = System.nanoTime();
		float dt = Math.min(0.1f, (now - lastFrame) / 1_000_000_000f);
		lastFrame = now;

		pollClicks(mc);

		float infoBottomPx = 6;
		HudFeature h = Features.HUD;
		if (h.enabled) {
			float s = (float) h.scale.value;
			c.getMatrices().push();
			c.getMatrices().scale(s, s, 1f);
			int bottom = drawInfoPanel(c, mc, h);
			if (h.armor.value) drawArmor(c, mc, s);
			c.getMatrices().pop();
			infoBottomPx = bottom * s;
		}

		if (Features.EFFECTS.enabled) {
			float s = (float) Features.EFFECTS.scale.value;
			c.getMatrices().push();
			c.getMatrices().scale(s, s, 1f);
			drawEffects(c, mc, 6 / s, (infoBottomPx + 6) / s);
			c.getMatrices().pop();
		}

		if (Features.WATERMARK.enabled) drawWatermark(c, mc);

		if (Features.MODULE_LIST.enabled) {
			float s = (float) Features.MODULE_LIST.scale.value;
			c.getMatrices().push();
			c.getMatrices().scale(s, s, 1f);
			drawModuleList(c, mc, s);
			c.getMatrices().pop();
		}

		if (Features.KEYSTROKES.enabled) {
			float s = (float) Features.KEYSTROKES.scale.value;
			c.getMatrices().push();
			c.getMatrices().scale(s, s, 1f);
			drawKeystrokes(c, mc, Features.KEYSTROKES, s, dt);
			c.getMatrices().pop();
		}
	}

	// ------------------------------------------------------------------ info panel

	/** Возвращает нижнюю границу панели (в координатах с учётом масштаба HUD), либо 6, если ничего не нарисовано. */
	private static int drawInfoPanel(DrawContext c, MinecraftClient mc, HudFeature h) {
		List<String[]> rows = new ArrayList<>();
		if (h.fps.value) rows.add(new String[]{"FPS", String.valueOf(mc.getCurrentFps())});
		if (h.coords.value) {
			rows.add(new String[]{"XYZ", mc.player.getBlockX() + "  " + mc.player.getBlockY() + "  " + mc.player.getBlockZ()});
		}
		if (h.biome.value && mc.world != null) {
			String b = mc.world.getBiome(mc.player.getBlockPos()).getKey()
				.map(k -> k.getValue().getPath()).orElse("?").replace('_', ' ');
			rows.add(new String[]{"BIOME", b});
		}
		if (h.cps.value) rows.add(new String[]{"CPS", LEFT_CLICKS.size() + " | " + RIGHT_CLICKS.size()});
		if (h.ping.value) rows.add(new String[]{"PING", ping(mc)});
		if (h.speed.value) {
			double bps = Math.hypot(mc.player.getX() - mc.player.prevX, mc.player.getZ() - mc.player.prevZ) * 20.0;
			rows.add(new String[]{"SPEED", String.format(java.util.Locale.ROOT, "%.1f b/s", bps)});
		}
		if (h.direction.value) {
			String d = mc.player.getHorizontalFacing().asString();
			rows.add(new String[]{"DIR", Character.toUpperCase(d.charAt(0)) + d.substring(1)});
		}
		if (h.clock.value) rows.add(new String[]{"TIME", LocalTime.now().format(CLOCK)});
		if (rows.isEmpty()) return 6;

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

		int a = Gfx.accentA(), b = Gfx.accentB();
		Gfx.rrect(c, x, y, w, hgt, 5, 0xB0101018);
		c.fill(x + 2, y + 5, x + 4, y + hgt - 5, a);
		c.fill(x + 2, y + hgt / 2, x + 4, y + hgt - 5, b);

		int ty = y + pad - 1;
		for (String[] r : rows) {
			c.drawText(tr, r[0], x + pad + 3, ty, b, true);
			c.drawText(tr, r[1], x + pad + 3 + labelW + gap, ty, Gfx.TEXT, true);
			ty += rowH;
		}
		return y + hgt;
	}

	private static String ping(MinecraftClient mc) {
		if (mc.getNetworkHandler() == null) return "-";
		PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
		return e == null ? "-" : e.getLatency() + " ms";
	}

	// ------------------------------------------------------------------ watermark / module list

	private static void drawWatermark(DrawContext c, MinecraftClient mc) {
		var tr = mc.textRenderer;
		String a = "Legit";
		String b = " Client";
		String extra = Features.WATERMARK.showFps.value ? "  |  " + mc.getCurrentFps() + " fps" : "";
		int w = tr.getWidth(a) + tr.getWidth(b) + tr.getWidth(extra) + 14;
		int x = (mc.getWindow().getScaledWidth() - w) / 2;
		int y = 6;

		Gfx.rrect(c, x, y, w, 16, 5, 0xB0101018);
		Gfx.gradientBar(c, x + 6, y + 14, w - 12, 1, 1f);
		int tx = x + 7;
		c.drawText(tr, a, tx, y + 4, Gfx.accentA(), true);
		tx += tr.getWidth(a);
		c.drawText(tr, b, tx, y + 4, Gfx.TEXT, true);
		tx += tr.getWidth(b);
		if (!extra.isEmpty()) c.drawText(tr, extra, tx, y + 4, Gfx.TEXT_DIM, true);
	}

	private static void drawModuleList(DrawContext c, MinecraftClient mc, float scale) {
		var tr = mc.textRenderer;
		List<String> names = new ArrayList<>();
		for (var f : Features.ALL) {
			if (f.enabled && !f.alwaysOn && f != Features.MODULE_LIST && f != Features.WATERMARK) names.add(f.name);
		}
		if (names.isEmpty()) return;
		names.sort((p, q) -> tr.getWidth(q) - tr.getWidth(p));

		int sw = (int) (mc.getWindow().getScaledWidth() / scale);
		int rowH = 13;
		int y = 6;
		int n = names.size();
		for (int i = 0; i < n; i++) {
			String name = names.get(i);
			int w = tr.getWidth(name) + 10;
			int x = sw - w - 6;
			int col = Gfx.lerp(Gfx.accentA(), Gfx.accentB(), n == 1 ? 0f : (float) i / (n - 1));
			Gfx.rrect(c, x, y, w, rowH - 1, 3, 0xB0101018);
			c.fill(x + w - 2, y + 2, x + w, y + rowH - 3, col);
			c.drawText(tr, name, x + 4, y + 2, col, true);
			y += rowH;
		}
	}

	// ------------------------------------------------------------------ armor

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
					int col = Gfx.lerp(0xFFEF4444, 0xFF4ADE80, pct / 100f);
					c.drawText(tr, pct + "%", x + 25, iy + 5, col, true);
				}
			}
			iy += itemH;
		}
	}

	// ------------------------------------------------------------------ potion effects

	private static void drawEffects(DrawContext c, MinecraftClient mc, float x, float y) {
		var effects = mc.player.getStatusEffects();
		if (effects.isEmpty()) return;

		var tr = mc.textRenderer;
		int rowH = 15;
		float cy = y;
		for (StatusEffectInstance e : effects) {
			String name = e.getEffectType().value().getName().getString();
			if (e.getAmplifier() > 0) name += " " + (e.getAmplifier() + 1);
			String time = e.isInfinite() ? "inf" : formatTicks(e.getDuration());

			int nameW = tr.getWidth(name), timeW = tr.getWidth(time);
			int w = 8 + nameW + 10 + timeW + 6;
			boolean good = e.getEffectType().value().isBeneficial();
			int dot = good ? 0xFF4ADE80 : 0xFFEF4444;

			Gfx.rrect(c, x, cy, w, rowH - 2, 4, 0xB0101018);
			c.fill((int) x + 3, (int) cy + 3, (int) x + 5, (int) cy + rowH - 5, dot);
			c.drawText(tr, name, (int) x + 8, (int) cy + 3, Gfx.TEXT, true);
			boolean low = !e.isInfinite() && e.getDuration() < 200;
			c.drawText(tr, time, (int) x + 8 + nameW + 10, (int) cy + 3, low ? 0xFFEF4444 : Gfx.accentB(), true);
			cy += rowH;
		}
	}

	private static String formatTicks(int ticks) {
		int total = ticks / 20;
		return String.format(java.util.Locale.ROOT, "%d:%02d", total / 60, total % 60);
	}

	// ------------------------------------------------------------------ keystrokes

	private static void drawKeystrokes(DrawContext c, MinecraftClient mc, KeystrokesFeature k, float scale, float dt) {
		var o = mc.options;
		int K = 22, G = 2;
		boolean mouse = k.mouse.value, space = k.space.value;

		int totalH = K + G + K;
		if (mouse) totalH += G + K;
		if (space) totalH += G + 14;

		int sh = (int) (mc.getWindow().getScaledHeight() / scale);
		int x0 = 8;
		int y0 = sh - totalH - 8;
		int fullW = 3 * K + 2 * G;

		int y = y0;
		drawKey(c, mc, x0 + K + G, y, K, K, "W", null, 0, o.forwardKey.isPressed(), dt);
		y += K + G;
		drawKey(c, mc, x0, y, K, K, "A", null, 1, o.leftKey.isPressed(), dt);
		drawKey(c, mc, x0 + K + G, y, K, K, "S", null, 2, o.backKey.isPressed(), dt);
		drawKey(c, mc, x0 + 2 * (K + G), y, K, K, "D", null, 3, o.rightKey.isPressed(), dt);
		y += K + G;
		if (mouse) {
			int mw = (fullW - G) / 2;
			drawKey(c, mc, x0, y, mw, K, "LMB", String.valueOf(LEFT_CLICKS.size()), 4, leftDown, dt);
			drawKey(c, mc, x0 + mw + G, y, mw, K, "RMB", String.valueOf(RIGHT_CLICKS.size()), 5, rightDown, dt);
			y += K + G;
		}
		if (space) {
			drawKey(c, mc, x0, y, fullW, 14, "SPACE", null, 6, o.jumpKey.isPressed(), dt);
		}
	}

	private static void drawKey(DrawContext c, MinecraftClient mc, int x, int y, int w, int h,
								String label, String sub, int idx, boolean pressed, float dt) {
		KEY_ANIM[idx] += ((pressed ? 1f : 0f) - KEY_ANIM[idx]) * Math.min(1f, dt * 18f);
		float a = KEY_ANIM[idx];

		int bg = Gfx.lerp(0xB0101018, (0xE0 << 24) | (Gfx.accentA() & 0xFFFFFF), a);
		Gfx.rrect(c, x, y, w, h, 4, bg);
		if (a > 0.05f) {
			c.fill(x + 3, y + h - 3, x + w - 3, y + h - 2, Gfx.withAlpha(Gfx.accentB(), a));
		}

		var tr = mc.textRenderer;
		int col = Gfx.lerp(Gfx.TEXT_DIM, 0xFFFFFFFF, a);
		int lx = x + (w - tr.getWidth(label)) / 2;
		if (sub == null) {
			c.drawText(tr, label, lx, y + (h - 8) / 2, col, true);
		} else {
			c.drawText(tr, label, lx, y + 3, col, true);
			c.drawText(tr, sub, x + (w - tr.getWidth(sub)) / 2, y + 12, Gfx.lerp(Gfx.TEXT_DIM, Gfx.accentB(), a), true);
		}
	}

	// ------------------------------------------------------------------ CPS

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
}
