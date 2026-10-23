package mekanism.client.model;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import mekanism.common.Mekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.obj.ObjGeometry;
import net.neoforged.neoforge.client.model.obj.ObjLoader;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import net.neoforged.neoforge.client.model.standalone.UnbakedStandaloneModel;

public class BaseModelCache {

    private final Map<Identifier, MekanismModelData> modelMap = new Object2ObjectOpenHashMap<>();

    private final String modid;

    protected BaseModelCache(String modid) {
        this.modid = modid;
    }

    private Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(modid, path);
    }

    public void onBake(ModelEvent.BakingCompleted evt) {
        for (MekanismModelData m : modelMap.values()) {
            m.reload(evt);
        }
    }

    public void setup(ModelEvent.RegisterStandalone event) {
        for (MekanismModelData mekanismModelData : modelMap.values()) {
            mekanismModelData.setup(event);
        }
    }

    protected OBJModelData registerOBJ(String path) {
        return registerOBJ(rl(path));
    }

    protected OBJModelData registerOBJ(Identifier rl) {
        return register(rl, OBJModelData::new);
    }

    /// Registers an OBJ model that only the part names are needed of, rather than the actual quads.
    protected OBJModelData registerOBJPartNames(String path) {
        return register(rl(path), rl -> new OBJModelData(rl, false));
    }

    protected BlockStateModelPartHelper registerJSON(String path) {
        return registerJSON(rl(path));
    }

    protected BlockStateModelPartHelper registerJSON(Identifier rl) {
        return register(rl, BlockStateModelPartHelper::new);
    }

    protected <DATA extends MekanismModelData> DATA register(Identifier rl, Function<Identifier, DATA> creator) {
        DATA data = creator.apply(rl);
        modelMap.put(rl, data);
        return data;
    }

    public static class MekanismModelData implements ModelDebugName {

        //protected IUnbakedGeometry<?> model;

        protected final Identifier rl;
        //protected final StandaloneModelKey<T> mrl;
        //private final Map<IGeometryBakingContext, BakedModel> bakedMap = new Object2ObjectOpenHashMap<>();

        protected MekanismModelData(Identifier rl) {
            this.rl = rl;
            //this.mrl = new StandaloneModelKey<>(this);//ModelResourceLocation.standalone(rl);
        }

        @Override
        public String debugName() {
            return rl.toString();
        }

        protected void reload(ModelEvent.BakingCompleted evt) {
            //bakedMap.clear();
        }

        protected void setup(ModelEvent.RegisterStandalone event) {
        }

        /*public BakedModel bake(IGeometryBakingContext config) {
            BakedModel bakedModel = bakedMap.get(config);
            if (bakedModel == null) {
                ModelBaker baker = Minecraft.getInstance().getModelManager().getModelBakery().new ModelBakerImpl(
                      (modelLoc, material) -> material.sprite(),
                      mrl
                );
                bakedModel = model.bake(config, baker, Material::sprite, BlockModelRotation.X0_Y0, ItemOverrides.EMPTY);
                bakedMap.put(config, bakedModel);
            }
            return bakedModel;
        }

        public IUnbakedGeometry<?> getModel() {
            return model;
        }*/
    }

    public static class OBJModelData extends MekanismModelData {

        private final StandaloneModelKey<Map<String, List<BakedQuad>>> key;
        private final boolean bakeQuads;
        //Quads for each root part of the model, baked individually so that which parts get rendered can be picked at render time
        private Map<String, List<BakedQuad>> partQuads = Collections.emptyMap();

        protected OBJModelData(Identifier rl) {
            this(rl, true);
        }

        /// @param bakeQuads `false` if only the names of the parts are needed and the quads themselves don't have to be baked
        protected OBJModelData(Identifier rl, boolean bakeQuads) {
            super(rl);
            this.key = new StandaloneModelKey<>(this);
            this.bakeQuads = bakeQuads;
        }

        @Override
        protected void reload(ModelEvent.BakingCompleted evt) {
            super.reload(evt);
            Map<String, List<BakedQuad>> baked = evt.getBakingResult().standaloneModels().get(key);
            partQuads = baked == null ? Collections.emptyMap() : baked;
        }

        @Override
        protected void setup(ModelEvent.RegisterStandalone event) {
            event.register(key, new UnbakedStandaloneModel<>() {
                @Override
                public Map<String, List<BakedQuad>> bake(ModelBaker baker, ModelDebugName name) {
                    return bakeParts(baker, name);
                }

                @Override
                public void resolveDependencies(Resolver resolver) {
                    //OBJ models don't depend on any other models
                }
            });
        }

        private Map<String, List<BakedQuad>> bakeParts(ModelBaker baker, ModelDebugName name) {
            ObjGeometry geometry = ObjLoader.INSTANCE.loadGeometry(new ObjGeometry.Settings(rl, true, useDiffuseLighting(), true, true, null));
            TextureSlots textureSlots = bakeQuads ? getTextureSlots(name) : TextureSlots.EMPTY;
            Map<String, List<BakedQuad>> parts = new Object2ObjectOpenHashMap<>();
            for (ObjGeometry.ModelObject part : geometry.getRootParts()) {
                if (bakeQuads) {
                    QuadCollection.Builder builder = new QuadCollection.Builder();
                    try {
                        part.addQuads(builder, textureSlots, baker, BlockModelRotation.IDENTITY, name, ContextMap.EMPTY);
                        parts.put(part.name(), builder.build().getAll());
                    } catch (RuntimeException e) {
                        //Parts with a material that has no texture (for example "none") can't be baked by the OBJ loader
                        Mekanism.logger.debug("Skipping part '{}' of model {} as it failed to bake", part.name(), rl, e);
                        parts.put(part.name(), Collections.emptyList());
                    }
                } else {
                    parts.put(part.name(), Collections.emptyList());
                }
            }
            return Collections.unmodifiableMap(parts);
        }

        /// The OBJ materials reference textures directly by their sprite id, so add a slot for each of them that resolves to that sprite.
        private TextureSlots getTextureSlots(ModelDebugName name) {
            TextureSlots.Data.Builder builder = new TextureSlots.Data.Builder();
            Identifier mtl = rl.withPath(path -> path.substring(0, path.lastIndexOf('.')) + ".mtl");
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(mtl);
            if (resource.isPresent()) {
                try (BufferedReader reader = resource.get().openAsReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.startsWith("map_Kd ")) {
                            String texture = line.substring("map_Kd ".length()).trim();
                            if (!texture.startsWith("#")) {
                                builder.addTexture(texture, new Material(Identifier.parse(texture)));
                            }
                        }
                    }
                } catch (IOException e) {
                    Mekanism.logger.error("Failed to read material library {}", mtl, e);
                }
            }
            return new TextureSlots.Resolver().addLast(builder.build()).resolve(name);
        }

        public Set<String> getRootComponentNames() {
            return partQuads.keySet();
        }

        public List<BakedQuad> getQuads(String part) {
            return partQuads.getOrDefault(part, Collections.emptyList());
        }

        protected boolean useDiffuseLighting() {
            return true;
        }
    }

    public static class BlockStateModelPartHelper extends MekanismModelData {

        //this is a list due to SubmitNodeCollector wanting a list
        private List<BlockStateModelPart> bakedModel = Collections.emptyList();
        private final StandaloneModelKey<BlockStateModelPart> key;

        private BlockStateModelPartHelper(Identifier rl) {
            super(rl);
            key = new StandaloneModelKey<>(this);
        }

        @Override
        protected void reload(ModelEvent.BakingCompleted evt) {
            super.reload(evt);
            BlockStateModelPart modelPart = evt.getBakingResult().standaloneModels().get(key);
            bakedModel = modelPart == null ? Collections.emptyList() : List.of(modelPart);
        }

        @Override
        protected void setup(ModelEvent.RegisterStandalone event) {
            event.register(key, SimpleUnbakedStandaloneModel.simpleModelWrapper(rl));
        }

        public List<BlockStateModelPart> getBakedModel() {
            return bakedModel;
        }
    }
}
