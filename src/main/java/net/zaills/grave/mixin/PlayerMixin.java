package net.zaills.grave.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerMixin extends LivingEntity {

	protected PlayerMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	/*
	@Inject(method = "dropItem", at = @At("HEAD"), cancellable = true)
	public void dropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemStack> cir){
		PlayerEntity player = (PlayerEntity) (Object) this;

		double d = player.getEyeY() - 0.30000001192092896D;
		ItemS itemEntity = new ItemEntity(player.getWorld(), player.getX(), d, player.getZ(), stack);
		cir.setReturnValue(itemEntity);
	}
		*/
}
