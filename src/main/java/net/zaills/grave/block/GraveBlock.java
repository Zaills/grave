package net.zaills.grave.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.zaills.grave.block.entity.GraveBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GraveBlock extends HorizontalFacingBlock implements BlockEntityProvider {
	public GraveBlock(Settings settings){
		super(settings);
		setDefaultState(this.stateManager.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager){
		stateManager.add(Properties.HORIZONTAL_FACING);
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new GraveBlockEntity(pos, state);
	}


	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit){
		RetrieveGrave(player, world, pos);

		return super.onUse(state, world, pos, player, hand, hit);
	}

	@Override
	public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player){
		dropInv(world, pos);
		super.onBreak(world, pos, state, player);
	}

	public void RetrieveGrave(PlayerEntity pE, World world, BlockPos pos){
		if (world.isClient) return;

		BlockEntity bE = world.getBlockEntity(pos);

		if (!(bE instanceof GraveBlockEntity GbE)) return;

		GbE.markDirty();
		if (GbE.getInv() == null) return;
		if (GbE.getOwner() == null) return;
		if (!pE.getGameProfile().equals(GbE.getOwner())){
			return;
		}

		DefaultedList<ItemStack> inv = GbE.getInv();
		DefaultedList<ItemStack> graveinv = DefaultedList.of();

		graveinv.addAll(pE.getInventory().main);
		graveinv.addAll(pE.getInventory().armor);
		graveinv.addAll(pE.getInventory().offHand);

		pE.getInventory().clear();

		List<ItemStack> armor = inv.subList(36, 40);
		for (ItemStack iS : armor){
			EquipmentSlot eS = MobEntity.getPreferredEquipmentSlot(iS);
			pE.equipStack(eS, iS);
		}

		pE.equipStack(EquipmentSlot.OFFHAND, inv.get(40));
		List<ItemStack> pI = inv.subList(0, 36);

		//check if place inv
		for (int i = 0; i < pI.size(); i++){
			pE.getInventory().insertStack(i, pI.get(i));
		}

		DefaultedList<ItemStack> extra = DefaultedList.of();
		List<Integer> openSlots = new ArrayList<>();

		//check if armor is already equip
		for (int i = 0; i < pE.getInventory().armor.size(); i++){
			if(pE.getInventory().armor.get(i) == ItemStack.EMPTY)
				openSlots.add(i);
		}

		//then equip it
		for (int i = 0; i<4; i++){
			if (openSlots.contains(i)){
				pE.equipStack(EquipmentSlot.fromTypeIndex(EquipmentSlot.Type.ARMOR, i), graveinv.subList(36, 40).get(i));
			}
			else
				extra.add(graveinv.subList(36, 40).get(i));
		}

		//equip OFFHAND
		if (pE.getInventory().offHand.get(0) == ItemStack.EMPTY)
			pE.equipStack(EquipmentSlot.OFFHAND, graveinv.get(40));
		else
			extra.add(graveinv.get(40));

		extra.addAll(graveinv.subList(0, 36));

		//check if there is place in main inv
		openSlots.clear();
		for (int i = 0; i < pE.getInventory().main.size(); i++){
			if(pE.getInventory().main.get(i) == ItemStack.EMPTY)
				openSlots.add(i);
		}

		//then take it
		for (int i = 0; i < openSlots.size(); i++){
			pE.getInventory().insertStack(openSlots.get(i), extra.get(i));
		}

		//get all others item
		DefaultedList<ItemStack> drop = DefaultedList.of();
		drop.addAll(extra.subList(openSlots.size(), extra.size()));

		//then drop it
		ItemScatterer.spawn(world, pos, drop);

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
}
