package net.zaills.grave.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.zaills.grave.block.entity.GraveBlockEntity;
import org.jetbrains.annotations.Nullable;

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

	public BlockState getPlState(ItemPlacementContext ipc){
		return this.getDefaultState().with(FACING, ipc.getPlayerFacing());
	}
}
