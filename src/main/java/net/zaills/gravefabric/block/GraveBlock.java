package net.zaills.gravefabric.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.zaills.gravefabric.block.entity.GraveBlockEntity;
import org.jetbrains.annotations.Nullable;

import static net.zaills.gravefabric.GraveFabric.CONFIG;
import static net.zaills.gravefabric.GraveFabric.GRAVE_ITEM;

public class GraveBlock extends HorizontalFacingBlock implements BlockEntityProvider, Waterloggable {
	public static final BooleanProperty WATERLOGGED;
	public static final MapCodec<GraveBlock> CODEC = createCodec(GraveBlock::new);

	public GraveBlock(Settings settings) {
		super(settings);
		setDefaultState(this.stateManager.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH).with(Properties.WATERLOGGED, false));
	}

	@Nullable
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockPos blockPos = ctx.getBlockPos();
		FluidState fluidState = ctx.getWorld().getFluidState(blockPos);
		return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing()).with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager){
		stateManager.add(Properties.HORIZONTAL_FACING, Properties.WATERLOGGED);
	}

	@Override
	protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
		return CODEC;
	}

	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new GraveBlockEntity(pos, state);
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!(player instanceof ServerPlayerEntity)){
			return player.isSneaking() ? ActionResult.PASS : ActionResult.FAIL;
		}

		BlockEntity blockEntity = world.getBlockEntity(pos);
		GraveBlockEntity graveBlockEntity = (GraveBlockEntity) blockEntity;
		assert graveBlockEntity != null;
		if (graveBlockEntity.getOwner() == null)
			return ActionResult.FAIL;

		if (graveBlockEntity.getOwner().getId().equals(player.getGameProfile().getId())) {
			if (player.isSneaking()) {
				player.sendMessage(Text.of(graveBlockEntity.getOwner().getName() + "'s Grave"), true);
				return ActionResult.PASS;
			} else {
				RetrieveGrave(player, world, pos);
			}

		} else {
			player.sendMessage(Text.of(graveBlockEntity.getOwner().getName() + "'s Grave"), true);
		}

		return player.isSneaking() ? ActionResult.PASS : ActionResult.SUCCESS;
	}

	@Override
	public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player){
		dropInv(world, pos, player);
		super.onBreak(world, pos, state, player);
		return state;
	}


	private void RetrieveGrave(PlayerEntity playerEntity, World world, BlockPos pos){
		if (world.isClient) return;

		BlockEntity blockEntity = world.getBlockEntity(pos);

		if (!(blockEntity instanceof GraveBlockEntity graveBlockEntity)) return;
		graveBlockEntity.markDirty();

		if (CONFIG.Grave_Inv())
			RetrieveGraveINV(playerEntity, world, pos, graveBlockEntity);
		else
			ItemScatterer.spawn(world, pos, graveBlockEntity.getInv());

		//xp
		playerEntity.addExperience(((GraveBlockEntity) blockEntity).getXp());

		world.removeBlock(pos, false);
	}

	public void RetrieveGraveINV(PlayerEntity playerEntity, World world, BlockPos pos, GraveBlockEntity graveBlockEntity){
		DefaultedList<ItemStack> inv = graveBlockEntity.getInv();
		DefaultedList<ItemStack> dropInv = DefaultedList.of();

		for (ItemStack itemStack : inv){
			EquipmentSlot slot = playerEntity.getPreferredEquipmentSlot(itemStack);
			if (slot != EquipmentSlot.MAINHAND && playerEntity.canEquip(itemStack, slot)){
				playerEntity.equipStack(slot, itemStack);
			}
			else if (playerEntity.getInventory().getEmptySlot() != -1) {
				playerEntity.getInventory().insertStack(itemStack);
			}
			else {
				if (playerEntity.getInventory().getStack(40).isEmpty()){
					playerEntity.getInventory().insertStack(40, itemStack);
				}
				else{
					dropInv.add(itemStack);
				}
			}

		}

		ItemScatterer.spawn(world, pos, dropInv);
	}

	public void dropInv(World world, BlockPos pos, PlayerEntity player){
		if(world.isClient) return;

		BlockEntity blockEntity = world.getBlockEntity(pos);

		if (!(blockEntity instanceof GraveBlockEntity graveBlockEntity)) return;

		graveBlockEntity.markDirty();

		if (graveBlockEntity.getOwner() == null){
			if (player.isCreative()) return;
			DefaultedList<ItemStack> inv = DefaultedList.ofSize(1, GRAVE_ITEM.asItem().getDefaultStack());
			ItemScatterer.spawn(world, pos, inv);
			return;
		}
		if (graveBlockEntity.getInv() == null) return;

		ItemScatterer.spawn(world, pos, graveBlockEntity.getInv());

		((GraveBlockEntity) blockEntity).setInv(DefaultedList.copyOf(ItemStack.EMPTY));
	}


	static {
		WATERLOGGED = Properties.WATERLOGGED;
	}
}
