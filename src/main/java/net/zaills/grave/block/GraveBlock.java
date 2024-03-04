package net.zaills.grave.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.zaills.grave.block.entity.GraveBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.zaills.grave.Grave.CONFIG;

public class GraveBlock extends HorizontalFacingBlock implements BlockEntityProvider, Waterloggable {

	public static final BooleanProperty WATERLOGGED;

	public GraveBlock(Settings settings){
		super(settings);
		setDefaultState(this.stateManager.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH).with(Properties.WATERLOGGED, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager){
		stateManager.add(Properties.HORIZONTAL_FACING, Properties.WATERLOGGED);
	}

	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		if (state.get(WATERLOGGED)) {
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}

		return state;
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
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!(player instanceof ServerPlayerEntity) || hand == Hand.OFF_HAND){
			return player.isSneaking() ? ActionResult.PASS : ActionResult.FAIL;
		}

		BlockEntity bE = world.getBlockEntity(pos);
		GraveBlockEntity graveBlockEntity = (GraveBlockEntity) bE;
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
	public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player){
		dropInv(world, pos);
		super.onBreak(world, pos, state, player);
	}

	public void RetrieveGraveINV(PlayerEntity pE, World world, BlockPos pos, GraveBlockEntity GbE){
		DefaultedList<ItemStack> inv = GbE.getInv();
		DefaultedList<ItemStack> check = DefaultedList.of();

		//Armor
		List<ItemStack> armor = inv.subList(36, 40);
		for (int i = 0; i < 4; i++){
			if (!armor.get(i).isEmpty()){
				if (pE.getInventory().getArmorStack(i).isEmpty())
					pE.equipStack(MobEntity.getPreferredEquipmentSlot(armor.get(i)), armor.get(i));
				else
					check.add(armor.get(i));
			}
		}

		//Offhand
		if (pE.getInventory().offHand.get(0) == ItemStack.EMPTY)
			pE.equipStack(EquipmentSlot.OFFHAND, inv.get(40));
		else
			check.add(inv.get(40));

		List<Integer> openslots = new ArrayList<>();
		for (int i = 0; i <pE.getInventory().main.size(); i++){
			if(pE.getInventory().main.get(i) == ItemStack.EMPTY)
				openslots.add(i);
		}

		check.addAll(inv.subList(0, 36));
		for (int i = 0; i < openslots.size(); i++){
			pE.getInventory().insertStack(openslots.get(i), check.get(i));

		}

		if (inv.size() > 40){
			check.addAll(inv.subList(41, inv.size()));
		}

		DefaultedList<ItemStack> dropinv = DefaultedList.of();
		dropinv.addAll(check.subList(openslots.size(), check.size()));
		ItemScatterer.spawn(world, pos, dropinv);
	}

	public void RetrieveGrave(PlayerEntity pE, World world, BlockPos pos){
		if (world.isClient) return;

		BlockEntity bE = world.getBlockEntity(pos);

		if (!(bE instanceof GraveBlockEntity GbE)) return;
		GbE.markDirty();

		if (CONFIG.Grave_Inv())
			RetrieveGraveINV(pE, world, pos, GbE);
		else
			ItemScatterer.spawn(world, pos, GbE.getInv());

		//xp
		pE.addExperience(((GraveBlockEntity) bE).getXp());

		world.removeBlock(pos, false);
	}

	public void dropInv(World world, BlockPos pos){
		if(world.isClient) return;

		BlockEntity bE = world.getBlockEntity(pos);

		if (!(bE instanceof GraveBlockEntity GbE)) return;

		GbE.markDirty();

		if (GbE.getInv() == null) return;

		ItemScatterer.spawn(world, pos, GbE.getInv());

		((GraveBlockEntity) bE).setInv(DefaultedList.copyOf(ItemStack.EMPTY));

	}

	static {
		WATERLOGGED = Properties.WATERLOGGED;
	}
}
