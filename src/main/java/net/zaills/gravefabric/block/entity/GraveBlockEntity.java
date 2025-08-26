package net.zaills.gravefabric.block.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.zaills.gravefabric.GraveFabric;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class GraveBlockEntity extends BlockEntity {
	private DefaultedList<ItemStack> savedInventory;
	private int savedExperience;
	private ProfileComponent savedOwner;

	public GraveBlockEntity(BlockPos pos, BlockState state) {
		super(GraveFabric.GRAVE_ENTITY, pos, state);

		this.savedInventory = DefaultedList.ofSize(41, ItemStack.EMPTY);
		this.savedExperience = 0;
		this.savedOwner = null;
	}

	public void setSavedInventory(DefaultedList<ItemStack> savedInventory) {
		this.savedInventory = savedInventory;
		this.markDirty();
	}
	public DefaultedList<ItemStack> getSavedInventory() {
		return this.savedInventory;
	}

	public void setSavedExperience(int savedExperience) {
		this.savedExperience = savedExperience;
		this.markDirty();
	}
	public int getSavedExperience() {
		return this.savedExperience;
	}

	public void setSavedOwner(ProfileComponent profile) {
		this.savedOwner = profile;
		this.markDirty();
	}
	public ProfileComponent getSavedOwner() {
		return this.savedOwner;
	}


	@Override
	protected void readData(ReadView view) {
		super.readData(view);

		this.savedInventory = DefaultedList.ofSize(view.getInt("ItemCount", 0), ItemStack.EMPTY);
		if (!this.savedInventory.isEmpty()) {
			Inventories.readData(view, this.savedInventory);
		}
		this.savedOwner = view.read("profile", ProfileComponent.CODEC).orElse(null);
		this.savedExperience = view.getInt("XP", 0);
	}

	@Override
	protected void readComponents(ComponentsAccess components) {
		super.readComponents(components);
	}


	@Override
	public void writeData(WriteView view) {
		super.writeData(view);

		view.putInt("ItemCount", this.savedInventory.size());
		Inventories.writeData(view, this.savedInventory);
		view.putNullable("profile", ProfileComponent.CODEC, this.savedOwner);
		view.putInt("XP", savedExperience);
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket(){
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return createNbt(registryLookup);
	}
}