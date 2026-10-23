package mekanism.additions.client.render.entity;

import mekanism.additions.client.model.BabySkeletonModels;
import mekanism.additions.common.entity.baby.EntityBabyBogged;
import net.minecraft.client.model.monster.skeleton.BoggedModel;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SkeletonClothingLayer;
import net.minecraft.client.renderer.entity.state.BoggedRenderState;
import net.minecraft.resources.Identifier;

/// Copy of vanilla's [bogged renderer][net.minecraft.client.renderer.entity.BoggedRenderer], modified to use our scaled down baby models.
public class RenderBabyBogged extends AbstractSkeletonRenderer<EntityBabyBogged, BoggedRenderState> {

    private static final Identifier BOGGED_TEXTURE = Identifier.withDefaultNamespace("textures/entity/skeleton/bogged.png");
    private static final Identifier BOGGED_OUTER_LAYER_TEXTURE = Identifier.withDefaultNamespace("textures/entity/skeleton/bogged_overlay.png");

    public RenderBabyBogged(EntityRendererProvider.Context context) {
        super(context, BabySkeletonModels.BOGGED_ARMOR, new BoggedModel(context.bakeLayer(BabySkeletonModels.BOGGED)));
        this.addLayer(new SkeletonClothingLayer<>(this, context.getModelSet(), BabySkeletonModels.BOGGED_OUTER_LAYER, BOGGED_OUTER_LAYER_TEXTURE));
    }

    @Override
    public void extractRenderState(EntityBabyBogged entity, BoggedRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isSheared = entity.isSheared();
        RenderBabySkeleton.markScaledDown(state);
    }

    @Override
    public Identifier getTextureLocation(BoggedRenderState state) {
        return BOGGED_TEXTURE;
    }

    @Override
    public BoggedRenderState createRenderState() {
        return new BoggedRenderState();
    }
}
