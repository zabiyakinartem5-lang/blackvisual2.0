package dev.legit.client.gui;

import dev.legit.client.Config;
import dev.legit.client.feature.Category;
import dev.legit.client.feature.Feature;
import dev.legit.client.feature.Features;
import dev.legit.client.feature.setting.BoolSetting;
import dev.legit.client.feature.setting.NumSetting;
import dev.legit.client.feature.setting.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClickGui extends Screen {
	private static final int PANEL_W = 136;
	private static final int PANEL_GAP = 10;
	private static final int HEADER_H = 28;
	private static final int ROW_H = 22;
	private static final int PAD = 6;

	private final List<Hit> hits = new ArrayList<>();
	private float open;
	private long last = System.nanoTime();

	private NumSetting dragging;
	private Hit dragHit;

	private record Hit(int x, int y, int w, int h, Runnable left, Runnable right, NumSetting slider) {
		boolean contains(double mx, double my) {
			return mx >= x && mx < x + w && my >= y && my < y + h;
		}
	}

	public ClickGui() {
		super(Text.literal("Legit Client"));
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void close() {
		Config.save();
		super.close();
	}

	@Override
	public void render(DrawContext c, int mouseX, int mouseY, float delta) {
		long now = System.nanoTime();
		float dt = Math.min(0.1f, (now - last) / 1_000_000_000f);
		last = now;
		open = Math.min(1f, open + dt * 5f);
		float e = Gfx.easeOut(open);
		float k = Math.min(1f, dt * 14f);

		hits.clear();
		c.fill(0, 0, width, height, Gfx.withAlpha(0x99050510, e));

		Category[] cats = Category.values();
		int totalW = cats.length * PANEL_W + (cats.length - 1) * PANEL_GAP;
		int x0 = (width - totalW) / 2;
		int y0 = (int) (height * 0.12f + (1f - e) * 16f);

		for (int i = 0; i < cats.length; i++) {
			drawPanel(c, cats[i], x0 + i * (PANEL_W + PANEL_GAP), y0, mouseX, mouseY, e, k);
		}

		String hint = "Right Shift - menu   |   C - zoom   |   Left Alt - freelook   |   LMB toggle, RMB settings";
		c.drawText(textRenderer, hint, (width - textRenderer.getWidth(hint)) / 2, height - 16,
			Gfx.withAlpha(Gfx.TEXT_DIM, e), true);
	}

	private void drawPanel(DrawContext c, Category cat, int x, int y, int mx, int my, float e, float k) {
		List<Feature> features = new ArrayList<>();
		for (Feature f : Features.ALL) if (f.category == cat) features.add(f);

		// высота содержимого
		float contentH = 0;
		for (Feature f : features) {
			f.expandAnim += ((f.expanded ? 1f : 0f) - f.expandAnim) * k;
			contentH += ROW_H + settingsHeight(f) * f.expandAnim;
		}
		int panelH = HEADER_H + (int) contentH + 6;

		// тень + фон
		Gfx.rrect(c, x - 1, y - 1, PANEL_W + 2, panelH + 2, 8, Gfx.withAlpha(0x40000000, e));
		Gfx.rrect(c, x, y, PANEL_W, panelH, 7, Gfx.withAlpha(Gfx.panel(), e));
		Gfx.rrect(c, x, y, PANEL_W, HEADER_H, 7, Gfx.withAlpha(Gfx.header(), e));
		c.fill(x, y + HEADER_H - 7, x + PANEL_W, y + HEADER_H, Gfx.withAlpha(Gfx.header(), e));
		Gfx.gradientBar(c, x + 8, y + HEADER_H - 2, PANEL_W - 16, 2, e);
		c.drawText(textRenderer, cat.label, x + PAD + 2, y + 10, Gfx.withAlpha(Gfx.TEXT, e), true);

		float cy = y + HEADER_H + 3;
		for (Feature f : features) {
			f.anim += ((f.enabled ? 1f : 0f) - f.anim) * k;
			drawFeatureRow(c, f, x, (int) cy, mx, my, e);
			cy += ROW_H;

			float sh = settingsHeight(f) * f.expandAnim;
			if (sh > 0.5f) {
				c.enableScissor(x, (int) cy, x + PANEL_W, (int) (cy + sh));
				float sy = cy;
				boolean interactive = f.expandAnim > 0.9f && f.expanded;
				for (Setting s : f.settings) {
					drawSetting(c, s, x, (int) sy, mx, my, e, k, interactive);
					sy += s.height();
				}
				c.disableScissor();
			}
			cy += sh;
		}
	}

	private void drawFeatureRow(DrawContext c, Feature f, int x, int y, int mx, int my, float e) {
		int rx = x + PAD, rw = PANEL_W - PAD * 2, rh = ROW_H - 2;
		boolean hover = mx >= rx && mx < rx + rw && my >= y && my < y + rh;
		if (hover) Gfx.rrect(c, rx, y, rw, rh, 5, Gfx.withAlpha(Gfx.ROW_HOVER, e));

		int nameCol = Gfx.lerp(Gfx.TEXT_DIM, Gfx.TEXT, f.anim);
		c.drawText(textRenderer, f.name, rx + 6, y + 6, Gfx.withAlpha(nameCol, e), true);

		int pw = 24, ph = 12;
		int px = rx + rw - pw - 5, py = y + (rh - ph) / 2;
		Runnable expand = () -> f.expanded = !f.expanded;
		if (f.alwaysOn) {
			String sign = f.expanded ? "-" : "+";
			c.drawText(textRenderer, sign, px + pw - 6, y + 6, Gfx.withAlpha(Gfx.accentB(), e), true);
			hits.add(new Hit(rx, y, rw, rh, expand, expand, null));
			return;
		}

		// переключатель-пилюля
		int track = Gfx.lerp(Gfx.OFF, Gfx.accentA(), f.anim);
		Gfx.rrect(c, px, py, pw, ph, 6, Gfx.withAlpha(track, e));
		float kx = px + 2 + (pw - ph) * f.anim;
		Gfx.rrect(c, kx, py + 2, 8, 8, 4, Gfx.withAlpha(0xFFFFFFFF, e));

		if (!f.settings.isEmpty()) {
			String dots = f.expanded ? "-" : "+";
			c.drawText(textRenderer, dots, px - 10, y + 6, Gfx.withAlpha(Gfx.TEXT_DIM, e), false);
		}

		hits.add(new Hit(rx, y, rw, rh, f::toggle, f.settings.isEmpty() ? null : expand, null));
	}

	private void drawSetting(DrawContext c, Setting s, int x, int y, int mx, int my, float e, float k, boolean interactive) {
		int rx = x + PAD + 4, rw = PANEL_W - PAD * 2 - 8;

		if (s instanceof BoolSetting b) {
			b.anim += ((b.value ? 1f : 0f) - b.anim) * k;
			c.drawText(textRenderer, b.name, rx, y + 6, Gfx.withAlpha(Gfx.TEXT_DIM, e), true);
			int pw = 20, ph = 10;
			int px = rx + rw - pw, py = y + 5;
			Gfx.rrect(c, px, py, pw, ph, 5, Gfx.withAlpha(Gfx.lerp(Gfx.OFF, Gfx.accentB(), b.anim), e));
			Gfx.rrect(c, px + 1 + (pw - ph) * b.anim, py + 1, 8, 8, 4, Gfx.withAlpha(0xFFFFFFFF, e));
			if (interactive) {
				hits.add(new Hit(rx - 2, y, rw + 4, s.height(), () -> b.value = !b.value, null, null));
			}
		} else if (s instanceof NumSetting n) {
			c.drawText(textRenderer, n.name, rx, y + 4, Gfx.withAlpha(Gfx.TEXT_DIM, e), true);
			String v = n.display();
			c.drawText(textRenderer, v, rx + rw - textRenderer.getWidth(v), y + 4, Gfx.withAlpha(Gfx.TEXT, e), true);

			int ty = y + 19;
			Gfx.rrect(c, rx, ty, rw, 4, 2, Gfx.withAlpha(Gfx.OFF, e));
			float fw = Math.max(4f, rw * n.fraction());
			int fill = n.hue ? Gfx.hsb((float) (n.value / 360.0), 0.8f, 1f) : Gfx.accentB();
			Gfx.rrect(c, rx, ty, fw, 4, 2, Gfx.withAlpha(fill, e));
			Gfx.rrect(c, rx + fw - 4, ty - 2, 8, 8, 4, Gfx.withAlpha(0xFFFFFFFF, e));
			if (interactive) {
				hits.add(new Hit(rx - 2, y + 12, rw + 4, 18, null, null, n));
			}
		}
	}

	private int settingsHeight(Feature f) {
		int h = 0;
		for (Setting s : f.settings) h += s.height();
		return h + (f.settings.isEmpty() ? 0 : 2);
	}

	private void applySlider(NumSetting n, Hit hit, double mouseX) {
		int rx = hit.x + 2, rw = hit.w - 4;
		double t = (mouseX - rx) / rw;
		n.set(n.min + (n.max - n.min) * Math.max(0, Math.min(1, t)));
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		for (int i = hits.size() - 1; i >= 0; i--) {
			Hit h = hits.get(i);
			if (!h.contains(mx, my)) continue;
			if (button == 0) {
				if (h.slider() != null) {
					dragging = h.slider();
					dragHit = h;
					applySlider(dragging, dragHit, mx);
				} else if (h.left() != null) {
					h.left().run();
				}
			} else if (button == 1 && h.right() != null) {
				h.right().run();
			}
			return true;
		}
		return super.mouseClicked(mx, my, button);
	}

	@Override
	public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
		if (dragging != null && dragHit != null && button == 0) {
			applySlider(dragging, dragHit, mx);
			return true;
		}
		return super.mouseDragged(mx, my, button, dx, dy);
	}

	@Override
	public boolean mouseReleased(double mx, double my, int button) {
		if (dragging != null) {
			dragging = null;
			dragHit = null;
			Config.save();
		}
		return super.mouseReleased(mx, my, button);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
			close();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}
