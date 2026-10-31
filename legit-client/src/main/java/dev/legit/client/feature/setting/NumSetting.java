package dev.legit.client.feature.setting;

public class NumSetting extends Setting {
	public double value;
	public final double min, max, step;
	public final String suffix;

	public NumSetting(String name, double value, double min, double max, double step, String suffix) {
		super(name);
		this.min = min;
		this.max = max;
		this.step = step;
		this.suffix = suffix;
		set(value);
	}

	public void set(double v) {
		if (step > 0) v = Math.round(v / step) * step;
		this.value = Math.max(min, Math.min(max, v));
	}

	public float fraction() {
		return (float) ((value - min) / (max - min));
	}

	public String display() {
		String s = step >= 1 ? String.valueOf((int) Math.round(value)) : String.format(java.util.Locale.ROOT, "%.1f", value);
		return s + suffix;
	}

	@Override
	public int height() {
		return 30;
	}
}
