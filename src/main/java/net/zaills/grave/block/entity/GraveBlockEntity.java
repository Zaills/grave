package net.zaills.grave.block.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.zaills.grave.Grave;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.enchantment.EnchantmentHelper.createNbt;

public class GraveBlockEntity extends BlockEntity {
	private DefaultedList<ItemStack> inv;
	private	int xp;
	private GameProfile Owner;
	private BlockPos location;
	private int type;

	public GraveBlockEntity(BlockPos pos, BlockState state) {
		super(Grave.GRAVE_ENTITY, pos, state);

		this.inv = DefaultedList.ofSize(41, ItemStack.EMPTY);
		this.xp = 0;
		this.Owner = null;
		this.location = BlockPos.ORIGIN;
		this.type = 0;
	}

	public void setInv(DefaultedList<ItemStack> inv){
		this.inv = inv;
		this.markDirty();
	}

	public void setXp(int xp){
		this.xp = xp;
		this.markDirty();
	}

	public void setOwner(GameProfile gameProfile){
		this.Owner = gameProfile;
		this.markDirty();
	}

	public void setLocation(BlockPos pos){
		this.location = pos;
		this.markDirty();
	}

	public void setType(int type){
		this.type = type;
		this.markDirty();
	}

	public DefaultedList<ItemStack> getInv(){
		return this.inv;
	}

	public int getXp(){
		return this.xp;
	}

	public GameProfile getOwner(){
		return this.Owner;
	}

	public BlockPos getLocation(){
		return this.location;
	}

	public int gettype(){
		return this.type;
	}
	@Override
	public void	readNbt(NbtCompound tag){
		super.readNbt(tag);

		this.inv = DefaultedList.ofSize(tag.getInt("ItemCount"), ItemStack.EMPTY);
		Inventories.readNbt(tag.getCompound("Items"), this.inv);
		this.xp = tag.getInt("XP");

		if (tag.contains("Owner")) {
			this.Owner = NbtHelper.toGameProfile(tag.getCompound("Owner"));
		}
		int[] array = tag.getIntArray("Location");
		this.location = new BlockPos(array[0], array[1], array[2]);
	}

	@Override
	public void	writeNbt(NbtCompound tag){
		super.writeNbt(tag);

		tag.putInt("ItemCount", this.inv.size());
		tag.put("Items", Inventories.writeNbt(new NbtCompound(), this.inv, true));
		tag.putInt("XP", xp);

		if (Owner != null){
			tag.put("Owner", NbtHelper.writeGameProfile(new NbtCompound(), Owner));
		}
		tag.putIntArray("Location", new int[]{this.location.getX(), this.location.getY(), this.location.getZ()});
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket(){
		return BlockEntityUpdateS2CPacket.of(this);
	}

}
