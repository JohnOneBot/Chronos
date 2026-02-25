package me.alpha432.oyvey.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.impl.render.RenderBlockOutlineEvent;
import me.alpha432.oyvey.features.modules.visuals.VisualsModule;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.alpha432.oyvey.util.traits.Util.EVENT_BUS;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "renderBlockOutline", at = @At("HEAD"), cancellable = true)
    public void renderBlockOutline(MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, boolean bl, LevelRenderState levelRenderState, CallbackInfo ci) {
        if (EVENT_BUS.post(new RenderBlockOutlineEvent())) {
            ci.cancel();
        }
    }


    @ModifyVariable(method = "renderLevel", at = @At("HEAD"), argsOnly = true)
    private Vector4f modifyFogColor(Vector4f fogColor) {
        if (fogColor == null || OyVey.moduleManager == null) return fogColor;

        VisualsModule visuals = OyVey.moduleManager.getModuleByClass(VisualsModule.class);
        if (visuals == null || !visuals.shouldOverrideFog()) return fogColor;

        java.awt.Color color = visuals.getFogColor();
        return new Vector4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, fogColor.w());
    }
}
