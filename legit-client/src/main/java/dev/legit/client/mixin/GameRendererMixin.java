package dev.legit.client.mixin;

import dev.legit.client.feature.Features;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
	private void legit$zoom(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
		float factor = Features.ZOOM.currentFactor();
		if (factor > 1.0001f) {
			cir.setReturnValue(cir.getReturnValueF() / factor);
		}
	}
}
