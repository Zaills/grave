package net.zaills.grave.mixin;

import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.world.World;
import net.zaills.grave.Grave;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class DyingMixing extends LivingEntity {
	@Shadow
	@Final
	private PlayerInventory inventory;

	protected DyingMixing(EntityType<? extends LivingEntity> type, World world){
		super(type, world);
	}

	@Inject(method = "dropInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;dropInventory()V", shift = At.Shift.BEFORE))
	private void placeGrave(CallbackInfo ci) {
		if (!Grave.CONFIG.SCATTER()){
			Grave.Place(this.world, this.getPos(), this.inventory.player);
			TrinketsApi.getTrinketComponent(this.inventory.player).get().getInventory().clear();
			this.inventory.clear();
		}
	}

}

