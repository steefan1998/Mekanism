package mekanism.additions.client.model;

import mekanism.additions.common.MekanismAdditions;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.monster.skeleton.BoggedModel;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/// Vanilla doesn't have baby skeletons, and as of 26.1 the scale of a baby is part of its model rather than applied by the renderer. So we make scaled down
/// versions of the skeleton layers, in the same way vanilla does for the small armor stand.
public class BabySkeletonModels {

    private static final MeshTransformer WITHER_SKELETON_SCALE = MeshTransformer.scaling(1.2F);

    public static final ModelLayerLocation SKELETON = layer("baby_skeleton", "main");
    public static final ArmorModelSet<ModelLayerLocation> SKELETON_ARMOR = armorLayers("baby_skeleton");
    public static final ModelLayerLocation STRAY = layer("baby_stray", "main");
    public static final ArmorModelSet<ModelLayerLocation> STRAY_ARMOR = armorLayers("baby_stray");
    public static final ModelLayerLocation STRAY_OUTER_LAYER = layer("baby_stray", "outer");
    public static final ModelLayerLocation WITHER_SKELETON = layer("baby_wither_skeleton", "main");
    public static final ArmorModelSet<ModelLayerLocation> WITHER_SKELETON_ARMOR = armorLayers("baby_wither_skeleton");
    public static final ModelLayerLocation BOGGED = layer("baby_bogged", "main");
    public static final ArmorModelSet<ModelLayerLocation> BOGGED_ARMOR = armorLayers("baby_bogged");
    public static final ModelLayerLocation BOGGED_OUTER_LAYER = layer("baby_bogged", "outer");

    private BabySkeletonModels() {
    }

    private static ModelLayerLocation layer(String name, String layer) {
        return new ModelLayerLocation(MekanismAdditions.rl(name), layer);
    }

    private static ArmorModelSet<ModelLayerLocation> armorLayers(String name) {
        return new ArmorModelSet<>(layer(name, "helmet"), layer(name, "chestplate"), layer(name, "leggings"), layer(name, "boots"));
    }

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        LayerDefinition skeleton = SkeletonModel.createBodyLayer().apply(HumanoidModel.BABY_TRANSFORMER);
        ArmorModelSet<LayerDefinition> armor = HumanoidModel.createArmorMeshSet(LayerDefinitions.INNER_ARMOR_DEFORMATION, LayerDefinitions.OUTER_ARMOR_DEFORMATION)
              .map(mesh -> LayerDefinition.create(mesh, 64, 32).apply(HumanoidModel.BABY_TRANSFORMER));

        event.registerLayerDefinition(SKELETON, () -> skeleton);
        registerArmor(event, SKELETON_ARMOR, armor);

        event.registerLayerDefinition(STRAY, () -> skeleton);
        registerArmor(event, STRAY_ARMOR, armor);
        event.registerLayerDefinition(STRAY_OUTER_LAYER, () -> outerLayer(0.25F));

        event.registerLayerDefinition(WITHER_SKELETON, () -> skeleton.apply(WITHER_SKELETON_SCALE));
        registerArmor(event, WITHER_SKELETON_ARMOR, armor.map(layer -> layer.apply(WITHER_SKELETON_SCALE)));

        event.registerLayerDefinition(BOGGED, () -> BoggedModel.createBodyLayer().apply(HumanoidModel.BABY_TRANSFORMER));
        registerArmor(event, BOGGED_ARMOR, armor);
        event.registerLayerDefinition(BOGGED_OUTER_LAYER, () -> outerLayer(0.2F));
    }

    private static LayerDefinition outerLayer(float deformation) {
        return LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(deformation), 0.0F), 64, 32).apply(HumanoidModel.BABY_TRANSFORMER);
    }

    private static void registerArmor(EntityRenderersEvent.RegisterLayerDefinitions event, ArmorModelSet<ModelLayerLocation> locations,
          ArmorModelSet<LayerDefinition> definitions) {
        event.registerLayerDefinition(locations.head(), definitions::head);
        event.registerLayerDefinition(locations.chest(), definitions::chest);
        event.registerLayerDefinition(locations.legs(), definitions::legs);
        event.registerLayerDefinition(locations.feet(), definitions::feet);
    }
}
