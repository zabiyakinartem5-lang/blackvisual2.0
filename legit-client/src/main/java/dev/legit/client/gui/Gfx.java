package dev.legit.client.gui;

import net.minecraft.client.gui.DrawContext;

/** Небольшие помощники для отрисовки: скруглённые прямоугольники, градиенты, цвета. */
public final class Gfx {
	public static final int ACCENT_A = 0xFF8B5CF6; // фиолетовый
	public static final int ACCENT_B = 0xFF22D3EE; // бирюзовый
	public static final int PANEL = 0xF0121218;
	public static final int HEADER = 0xF01B1B26;
	public static final int ROW_HOVER = 0x22FFFFFF;
	public static final int OFF = 0xFF3A3A4A;
	public static final int TEXT = 0xFFF2F2F7;
	public static final int TEXT_DIM = 0xFF9A9AB0;

	private Gfx() {}

	public static void rrect(DrawContext c, float x, float y, float w, float h, float r, int color) {
		int xi = Math.round(x), yi = Math.round(y), wi = Math.round(w), hi = Math.round(h);
		int ri = (int) Math.min(r, Math.min(wi, hi) / 2f);
		for (int i = 0; i < ri; i++) {
			double dy = ri - i - 0.5;
			int inset = (int) Math.round(ri - Math.sqrt(Math.max(0, ri * ri - dy * dy)));
			c.fill(xi + inset, yi + i, xi + wi - inset, yi + i + 1, color);
			c.fill(xi + inset, yi + hi - 1 - i, xi + wi - inset, yi + hi - i, color);
		}
		c.fill(xi, yi + ri, xi + wi, yi + hi - ri, color);
	}

	/** Горизонтальный градиент accentA → accentB. */
	public static void gradientBar(DrawContext c, int x, int y, int w, int h, float alpha) {
		int seg = 3;
		for (int i = 0; i < w; i += seg) {
			float t = (float) i / Math.max(1, w - 1);
			int col = withAlpha(lerp(ACCENT_A, ACCENT_B, t), alpha);
			c.fill(x + i, y, Math.min(x + i + seg, x + w), y + h, col);
		}
	}

	public static int lerp(int a, int b, float t) {
		t = Math.max(0f, Math.min(1f, t));
		int aa = (a >>> 24) & 255, ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
		int ba = (b >>> 24) & 255, br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
		return ((int) (aa + (ba - aa) * t) << 24)
			| ((int) (ar + (br - ar) * t) << 16)
			| ((int) (ag + (bg - ag) * t) << 8)
			| (int) (ab + (bb - ab) * t);
	}

	/** Умножает альфу цвета на коэффициент (минимум 8, чтобы текст не стал непрозрачным). */
	public static int withAlpha(int color, float mul) {
		int a = (int) (((color >>> 24) & 255) * Math.max(0f, Math.min(1f, mul)));
		a = Math.max(8, a);
		return (a << 24) | (color & 0xFFFFFF);
	}

	public static float easeOut(float t) {
		float u = 1f - Math.max(0f, Math.min(1f, t));
		return 1f - u * u * u;
	}
}
