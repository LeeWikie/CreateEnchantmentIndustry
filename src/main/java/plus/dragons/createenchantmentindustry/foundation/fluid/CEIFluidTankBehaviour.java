package plus.dragons.createenchantmentindustry.foundation.fluid;

import java.util.List;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class CEIFluidTankBehaviour extends BlockEntityBehaviour<SmartBlockEntity> {
    public static final BehaviourType<CEIFluidTankBehaviour> TYPE = new BehaviourType<>("cei_fluid_tanks");

    private final List<CEIConfigurableFluidTank> tanks;
    private final Storage<FluidVariant> storage;

    public CEIFluidTankBehaviour(SmartBlockEntity blockEntity, List<CEIConfigurableFluidTank> tanks) {
        super(blockEntity);
        this.tanks = List.copyOf(tanks);
        this.storage = new CombinedStorage<>(this.tanks);
    }

    public static CEIFluidTankBehaviour singleLiquidExperience(SmartBlockEntity blockEntity, long capacity) {
        return new CEIFluidTankBehaviour(blockEntity, List.of(CEIConfigurableFluidTank.liquidExperience(capacity, blockEntity::setChanged)));
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public List<CEIConfigurableFluidTank> getHandlers() {
        return tanks;
    }

    public CEIConfigurableFluidTank getPrimaryHandler() {
        if (tanks.isEmpty()) {
            throw new IllegalStateException("CEI fluid tank behaviour has no handlers");
        }
        return tanks.get(0);
    }

    public Storage<FluidVariant> getStorage() {
        return storage;
    }

    public void writeNbt(CompoundTag tag) {
        tag.putInt("TankCount", tanks.size());
        for (int index = 0; index < tanks.size(); index++) {
            tag.put("Tank" + index, tanks.get(index).writeNbt());
        }
    }

    public void readNbt(CompoundTag tag) {
        int count = Math.min(tag.getIntOr("TankCount", tanks.size()), tanks.size());
        for (int index = 0; index < count; index++) {
            String key = "Tank" + index;
            if (tag.contains(key)) {
                tanks.get(index).readNbt(tag.getCompoundOrEmpty(key));
            }
        }
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        view.putInt("TankCount", tanks.size());
        for (int index = 0; index < tanks.size(); index++) {
            tanks.get(index).write(view.child("Tank" + index));
        }
    }

    @Override
    public void read(ValueInput view, boolean clientPacket) {
        int count = Math.min(view.getIntOr("TankCount", tanks.size()), tanks.size());
        for (int index = 0; index < count; index++) {
            tanks.get(index).read(view.childOrEmpty("Tank" + index));
        }
    }
}
