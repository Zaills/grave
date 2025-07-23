package net.zaills.gravefabric.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class DyingMixing extends LivingEntity {
	protected DyingMixing(EntityType<? extends LivingEntity> type, World world){
		super(type, world);
	}

	@Inject(method = "dropInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;dropAll()V", shift = At.Shift.BEFORE))
	private void placeGrave(CallbackInfo ci) {
//		if (net.zaills.gravefabric.Grave.CONFIG.SCATTER()) {
//			return;
//		}

		final PlayerEntity player = (PlayerEntity) (Object) this;

		Place(this.getWorld(), this.getPos().subtract(0, 1, 0), player);
	}

	@Unique
	private void Place(World world, Vec3d pos, PlayerEntity player) {
		if (world.isClient) {
			return;
		}
		System.out.println("Placing grave at " + pos + " for player " + player.getName().getString());
	}
}