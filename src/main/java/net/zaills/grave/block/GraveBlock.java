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
import net.zaills.grave.Grave;
import net.zaills.grave.block.entity.GraveBlockEntity;
import net.zaills.grave.compatibility.TrinketsCompat;
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
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit){
		if (!(player instanceof ServerPlayerEntity) || hand == Hand.OFF_HAND){
			return player.isSneaking() ? ActionResult.PASS : ActionResult.FAIL;
		}

		BlockEntity bE = world.getBlockEntity(pos);
		if (bE instanceof GraveBlockEntity graveBlockEntity && graveBlockEntity.getOwner().getId().equals(player.getGameProfile().getId())) {
			if (player.isSneaking()){
				player.sendMessage(Text.of(player.getEntityName() + "'s Grave"), true);
			return ActionResult.PASS;
		}
			else
				RetrieveGrave(player, world, pos);
		}
		else
			player.sendMessage(Text.of(player.getEntityName() + "'s Grave"), true);
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

		for (TrinketsCompat trinkets : Grave.trinketsMod){
			check.addAll(trinkets.getInv(pE));
		}

		DefaultedList<ItemStack> dropinv = DefaultedList.of();
		dropinv.addAll(check.subList(openslots.size(), check.size()));
		ItemScatterer.spawn(world, pos, dropinv);
	}

	public void RetrieveGraveGRV(PlayerEntity pE, World world, BlockPos pos, GraveBlockEntity GbE){
		DefaultedList<ItemStack> inv = GbE.getInv();
		DefaultedList<ItemStack> replace_inv = DefaultedList.of();

		replace_inv.addAll(pE.getInventory().main);
		replace_inv.addAll(pE.getInventory().armor);
		replace_inv.addAll(pE.getInventory().offHand);

		for (TrinketsCompat trinkets : Grave.trinketsMod){
			replace_inv.addAll(trinkets.getInv(pE));
		}
		pE.getInventory().clear();

		List<ItemStack> armor = inv.subList(36, 40);
		for (ItemStack iS : armor){
			EquipmentSlot eS = MobEntity.getPreferredEquipmentSlot(iS);
			pE.equipStack(eS, iS);
		}
		pE.equipStack(EquipmentSlot.OFFHAND, inv.get(40));

		List<ItemStack> pI = inv.subList(0, 36);
		for (int i = 0; i < pI.size(); i++){
			pE.getInventory().insertStack(i, pI.get(i));
		}

		DefaultedList<ItemStack> extra = DefaultedList.of();
		List<Integer> openSlots = new ArrayList<>();

		for (int i = 0; i < pE.getInventory().armor.size(); i++){
			if(pE.getInventory().armor.get(i) == ItemStack.EMPTY)
				openSlots.add(i);
		}

		for (int i = 0; i<4; i++){
			if (openSlots.contains(i)){
				pE.equipStack(EquipmentSlot.fromTypeIndex(EquipmentSlot.Type.ARMOR, i), replace_inv.subList(36, 40).get(i));
			}
			else
				extra.add(replace_inv.subList(36, 40).get(i));
		}

		if (pE.getInventory().offHand.get(0) == ItemStack.EMPTY)
			pE.equipStack(EquipmentSlot.OFFHAND, replace_inv.get(40));
		else
			extra.add(replace_inv.get(40));

		extra.addAll(replace_inv.subList(0, 36));
		if (replace_inv.size() > 41)
			extra.addAll(replace_inv.subList(41, replace_inv.size()));

		openSlots.clear();
		for (int i = 0; i < pE.getInventory().main.size(); i++){
			if(pE.getInventory().main.get(i) == ItemStack.EMPTY)
				openSlots.add(i);
		}

		for (int i = 0; i < openSlots.size(); i++){
			pE.getInventory().insertStack(openSlots.get(i), extra.get(i));
		}

		DefaultedList<ItemStack> drop = DefaultedList.of();
		drop.addAll(extra.subList(openSlots.size(), extra.size()));

		int offset = 41;
		for (TrinketsCompat trinketsCompat : Grave.trinketsMod){
			trinketsCompat.setInv(inv.subList(offset, offset + trinketsCompat.getInvSize(pE)), pE);
			offset += trinketsCompat.getInvSize(pE);
		}

		ItemScatterer.spawn(world, pos, drop);
	}
	public void RetrieveGrave(PlayerEntity pE, World world, BlockPos pos){
		if (world.isClient) return;

		BlockEntity bE = world.getBlockEntity(pos);

		if (!(bE instanceof GraveBlockEntity GbE)) return;
		GbE.markDirty();

		if (CONFIG.Priorities_Inv())
			RetrieveGraveINV(pE, world, pos, GbE);
		else
			RetrieveGraveGRV(pE, world, pos, GbE);

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
