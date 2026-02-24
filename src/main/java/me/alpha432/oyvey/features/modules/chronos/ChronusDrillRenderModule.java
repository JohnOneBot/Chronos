package me.alpha432.oyvey.features.modules.chronos;

import me.alpha432.oyvey.event.impl.render.Render3DEvent;
import me.alpha432.oyvey.event.system.Subscribe;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import me.alpha432.oyvey.util.render.RenderUtil;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public class ChronusDrillRenderModule extends Module {
    private final Setting<Color> sideColor = color("SideColor", 168, 85, 247, 40);
    private final Setting<Color> lineColor = color("LineColor", 236, 72, 153, 200);
    private final Setting<Float> lineWidth = num("LineWidth", 1.5f, 0.5f, 5.0f);
    private final Setting<Float> cubeSize = num("CubeSize", 1.0f, 0.1f, 1.0f);

    public ChronusDrillRenderModule() {
        super("ChronusDrillRender", "Space-themed 3x3 drill plane render.", Category.CHRONUS_FEATURES);
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if (nullCheck() || !(mc.hitResult instanceof BlockHitResult hit)) return;

        BlockPos center = hit.getBlockPos();
        Direction face = remapVerticalFace(hit.getDirection());

        BlockState state = mc.level.getBlockState(center);
        if (state.isAir() || !state.getFluidState().isEmpty()) return;

        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                BlockPos pos = switch (face.getAxis()) {
                    case X -> center.offset(0, a, b);
                    case Y -> center.offset(a, 0, b);
                    case Z -> center.offset(a, b, 0);
                };

                BlockState planeState = mc.level.getBlockState(pos);
                if (planeState.isAir() || !planeState.getFluidState().isEmpty()) continue;

                float inset = (1.0f - cubeSize.getValue()) / 2.0f;
                Vec3 min = new Vec3(pos.getX() + inset, pos.getY() + inset, pos.getZ() + inset);
                Vec3 max = new Vec3(pos.getX() + 1 - inset, pos.getY() + 1 - inset, pos.getZ() + 1 - inset);

                RenderUtil.drawBoxFilled(event.getMatrix(), new net.minecraft.world.phys.AABB(min, max), sideColor.getValue());
                RenderUtil.drawBox(event.getMatrix(), new net.minecraft.world.phys.AABB(min, max), lineColor.getValue(), lineWidth.getValue());
            }
        }
    }

    private Direction remapVerticalFace(Direction face) {
        if (face.getAxis() != Direction.Axis.Y) return face;

        Camera camera = mc.gameRenderer.getMainCamera();
        if (camera == null) return Direction.SOUTH;

        Direction horizontal = Direction.fromYRot(camera.getYRot());
        return horizontal.getAxis() == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
    }
}
