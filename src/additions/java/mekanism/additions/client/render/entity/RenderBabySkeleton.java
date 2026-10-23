package mekanism.additions.client.render.entity;

import mekanism.additions.client.model.BabySkeletonModels;
import mekanism.additions.common.entity.baby.EntityBabySkeleton;
import mekanism.additions.common.entity.baby.EntityBabyStray;
import mekanism.additions.common.entity.baby.EntityBabyWitherSkeleton;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SkeletonClothingLayer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;

/// Copy of vanilla's skeleton renderers, modified to use our scaled down baby models.
public class RenderBabySkeleton<T extends AbstractSkeleton> extends AbstractSkeletonRenderer<T, SkeletonRenderState> {

    private static final Identifier SKELETON_TEXTURE = Identifier.withDefaultNamespace("textures/entity/skeleton/skeleton.png");
    private static final Identifier STRAY_TEXTURE = Identifier.withDefaultNamespace("textures/entity/skeleton/stray.png");
    private static final Identifier STRAY_CLOTHES_TEXTURE = Identifier.withDefaultNamespace("textures/entity/skeleton/stray_overlay.png");
    private static final Identifier WITHER_SKELETON_TEXTURE = Identifier.withDefaultNamespace("textures/entity/skeleton/wither_skeleton.png");

    public static RenderBabySkeleton<EntityBabySkeleton> skeleton(EntityRendererProvider.Context context) {
        return new RenderBabySkeleton<>(context, BabySkeletonModels.SKELETON, BabySkeletonModels.SKELETON_ARMOR, SKELETON_TEXTURE);
    }

    public static RenderBabySkeleton<EntityBabyStray> stray(EntityRendererProvider.Context context) {
        RenderBabySkeleton<EntityBabyStray> renderer = new RenderBabySkeleton<>(context, BabySkeletonModels.STRAY, BabySkeletonModels.STRAY_ARMOR, STRAY_TEXTURE);
        renderer.addLayer(new SkeletonClothingLayer<>(renderer, context.getModelSet(), BabySkeletonModels.STRAY_OUTER_LAYER, STRAY_CLOTHES_TEXTURE));
        return renderer;
    }

    public static RenderBabySkeleton<EntityBabyWitherSkeleton> witherSkeleton(EntityRendererProvider.Context context) {
        return new RenderBabySkeleton<>(context, BabySkeletonModels.WITHER_SKELETON, BabySkeletonModels.WITHER_SKELETON_ARMOR, WITHER_SKELETON_TEXTURE);
    }

    private final Identifier texture;

    private RenderBabySkeleton(EntityRendererProvider.Context context, ModelLayerLocation body, ArmorModelSet<ModelLayerLocation> armor, Identifier texture) {
        super(context, body, armor);
        this.texture = texture;
    }

    @Override
    public void extractRenderState(T entity, SkeletonRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        markScaledDown(state);
    }

    /// Our models are scaled down versions of the adult models (like the small armor stand), rather than vanilla's baby models which have different proportions
    /// and texture layouts. Mark the state as not being a baby so that layers (such as armor) render using the adult textures and models, which our models are
    /// shaped like.
    static void markScaledDown(SkeletonRenderState state) {
        state.isBaby = false;
    }

    @Override
    public Identifier getTextureLocation(SkeletonRenderState state) {
        return texture;
    }

    @Override
    public SkeletonRenderState createRenderState() {
        return new SkeletonRenderState();
    }
}
