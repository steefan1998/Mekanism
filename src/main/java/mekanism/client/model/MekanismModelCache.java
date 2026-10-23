package mekanism.client.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import mekanism.api.gear.IModuleHelper;
import mekanism.client.render.armor.MekaSuitArmorRenderer.ModuleOBJModelData;
import mekanism.client.render.transmitter.RenderTransmitterBase;
import mekanism.common.Mekanism;
import mekanism.common.tile.qio.TileEntityQIODriveArray.DriveStatus;
import mekanism.common.util.EnumUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent.BakingCompleted;

public class MekanismModelCache extends BaseModelCache {

    public static final MekanismModelCache INSTANCE = new MekanismModelCache();
    private final Set<Runnable> callbacks = new HashSet<>();

    public final OBJModelData MEKASUIT = registerOBJ("models/entity/mekasuit.obj");
    public final OBJModelData MEKATOOL_LEFT_HAND = registerOBJPartNames("models/entity/mekatool_left.obj");
    public final OBJModelData MEKATOOL_RIGHT_HAND = registerOBJPartNames("models/entity/mekatool_right.obj");
    private final Set<ModuleOBJModelData> mekaSuitModules = new HashSet<>();
    public final Set<ModuleOBJModelData> MEKASUIT_MODULES = Collections.unmodifiableSet(mekaSuitModules);

    //TODO - 26.1 models: Bake the quads again once transmitter contents rendering is ported (the mtl for it has no texture so it is resolved when rendering)
    public final OBJModelData TRANSMITTER_CONTENTS = register(RenderTransmitterBase.MODEL_LOCATION, rl -> new OBJModelData(rl, false) {
        @Override
        protected boolean useDiffuseLighting() {
            return false;
        }
    });
    public final BlockStateModelPartHelper LIQUIFIER_BLADE = registerJSON("block/liquifier_blade");
    public final BlockStateModelPartHelper VIBRATOR_SHAFT = registerJSON("block/vibrator_shaft");
    public final BlockStateModelPartHelper PIGMENT_MIXER_SHAFT = registerJSON("block/pigment_mixer_shaft");
    public final BlockStateModelPartHelper[] QIO_DRIVES = new BlockStateModelPartHelper[EnumUtils.DRIVE_STATUSES.length];

    private MekanismModelCache() {
        super(Mekanism.MODID);
        for (DriveStatus status : EnumUtils.DRIVE_STATUSES) {
            Identifier model = status.getModel();
            if (model != null) {
                QIO_DRIVES[status.ordinal()] = registerJSON(model);
            }
        }
    }

    @Override
    public void onBake(BakingCompleted evt) {
        super.onBake(evt);
        callbacks.forEach(Runnable::run);
    }

    public void reloadCallback(Runnable callback) {
        callbacks.add(callback);
    }

    /// Call via [IModuleHelper#addMekaSuitModuleModels(Identifier)].
    public ModuleOBJModelData registerMekaSuitModuleModel(Identifier rl) {
        ModuleOBJModelData data = register(rl, ModuleOBJModelData::new);
        mekaSuitModules.add(data);
        return data;
    }
}
