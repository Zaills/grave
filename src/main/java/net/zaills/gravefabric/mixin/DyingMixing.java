package net.zaills.gravefabric.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.zaills.gravefabric.GraveFabric;
import net.zaills.gravefabric.block.entity.GraveBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import static net.zaills.gravefabric.GraveFabric.CONFIG;

@Mixin(PlayerEntity.class)
public abstract class DyingMixing extends LivingEntity {
	protected DyingMixing(EntityType<? extends LivingEntity> type, World world){
		super(type, world);
	}

	@Inject(method = "dropInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;dropAll()V", shift = At.Shift.BEFORE))
	private void placeGrave(CallbackInfo ci) {
		if (CONFIG.SCATTER()) {
			return;
		}

		final PlayerEntity player = (PlayerEntity) (Object) this;

		Place(this.getWorld(), this.getPos().subtract(0, 1, 0), player);
		player.getInventory().clear();
	}

	@Unique
	private void Place(World world, Vec3d pos, PlayerEntity player) {
		if (world.isClient) {
			return;
		}

		BlockState graveState = getGraveType(player);

		BlockPos gravePos = getGravePos(pos, world);

		if (gravePos == null || !world.setBlockState(gravePos, graveState)) {
			// Unable to place grave anywhere (skill issue), drop items
			player.getInventory().dropAll();
			return;
		}

		DefaultedList<ItemStack> inv = DefaultedList.of();
		inv.addAll(player.getInventory().main);
		inv.addAll(player.getInventory().armor);
		inv.addAll(player.getInventory().offHand);

		// Set block data
		GraveBlockEntity graveBlockEntity = new GraveBlockEntity(gravePos, graveState);
		graveBlockEntity.setInv(inv);
		graveBlockEntity.setOwner(player.getGameProfile());
		graveBlockEntity.setXp(player.totalExperience);
		graveBlockEntity.markDirty();

		//remove the xp
		player.totalExperience = 0;
		player.experienceLevel = 0;
		player.experienceProgress = 0;

		world.addBlockEntity(graveBlockEntity);

		System.out.println(player.getName() + "'s grave spawn at: " + gravePos.getX() + ", " + gravePos.getY() + ", " + gravePos.getZ());
		if (CONFIG.Get_grave_coord()) {
			player.sendMessage(Text.of("Grave spawn at: " + gravePos.getX() + ", " + gravePos.getY() + ", " + gravePos.getZ()), false);
		}
	}

	// Modify later to add more type of grave
	@Unique
	private static BlockState getGraveType(PlayerEntity player){
		return GraveFabric.BASE_GRAVE
				.getDefaultState()
				.with(Properties.HORIZONTAL_FACING, player.getHorizontalFacing());
	}

	@Unique
	private static BlockPos getGravePos(Vec3d pos, World world) {
		final int worldMin = world.getDimension().minY();
		final int worldMax = world.getDimension().height() + worldMin - 1;

		BlockPos originBlock = new BlockPos((int) pos.x, (int) ((pos.y < worldMin) ? worldMin : pos.y), (int) pos.z);

		Optional<BlockPos> airBlock = getBlockTop(originBlock, world, worldMax);
		if (airBlock.isPresent()){
			return airBlock.get();
		}

		for (BlockPos offsetBlock : BlockPos.iterateOutwards(originBlock, 5, 5, 5)){
			// Not within world bounds,
			if (worldMin > offsetBlock.getY() || offsetBlock.getY() > worldMax) {
				continue;
		}
			// Not within the world border
			if (!world.getWorldBorder().contains(offsetBlock)) {
				continue;
		}

			// Find a block of air in a column above the offset block
			airBlock = getBlockTop(offsetBlock, world, worldMax);
			if (airBlock.isEmpty()){
				continue;
		}

			return airBlock.get();
		}
		return null;
	}

	@Unique
	private static Optional<BlockPos> getBlockTop(BlockPos pos, World world, int worldMax) {
		BlockPos npos = pos;
		while (!world.getBlockState(npos).isAir()){
			npos = npos.up();

			if (npos.getY() >= worldMax) {
				return Optional.empty();
			}
		}
		return Optional.of(npos);
	}
}