package net.minecraft.server;

import org.bukkit.event.player.PlayerShearEntityEvent; // CraftBukkit

import net.strapinrage.spacespigot.cache.Constants;

public class EntityMushroomCow extends EntityCow {

	public EntityMushroomCow(World world) {
		super(world);
		this.setSize(0.9F, 1.3F);
		this.bn = Blocks.MYCELIUM;
	}

	@Override
	public boolean a(EntityHuman entityhuman) {
		ItemStack itemstack = entityhuman.inventory.getItemInHand();

		if (itemstack != null && itemstack.getItem() == Items.BOWL && this.getAge() >= 0) {
			if (itemstack.count == 1) {
				entityhuman.inventory.setItem(entityhuman.inventory.itemInHandIndex,
						new ItemStack(Items.MUSHROOM_STEW));
				return true;
			}

			if (entityhuman.inventory.pickup(new ItemStack(Items.MUSHROOM_STEW))
					&& !entityhuman.abilities.canInstantlyBuild) {
				entityhuman.inventory.splitStack(entityhuman.inventory.itemInHandIndex, 1);
				return true;
			}
		}

		if (itemstack != null && itemstack.getItem() == Items.SHEARS && this.getAge() >= 0) {
			// CraftBukkit start
			PlayerShearEntityEvent event = new PlayerShearEntityEvent(
					(org.bukkit.entity.Player) entityhuman.getBukkitEntity(), this.getBukkitEntity());
			this.world.getServer().getPluginManager().callEvent(event);

			if (event.isCancelled()) {
				return false;
			}
			// CraftBukkit end
			this.die();
			this.world.addParticle(EnumParticle.EXPLOSION_LARGE, this.locX, this.locY + this.length / 2.0F, this.locZ,
					0.0D, 0.0D, 0.0D, Constants.EMPTY_ARRAY);
			if (!this.world.isClientSide) {
				EntityCow entitycow = new EntityCow(this.world);

				entitycow.setPositionRotation(this.locX, this.locY, this.locZ, this.yaw, this.pitch);
				entitycow.setHealth(this.getHealth());
				entitycow.aI = this.aI;
				if (this.hasCustomName()) {
					entitycow.setCustomName(this.getCustomName());
				}

				this.world.addEntity(entitycow);

				for (int i = 0; i < 5; ++i) {
					this.world.addEntity(new EntityItem(this.world, this.locX, this.locY + this.length, this.locZ,
							new ItemStack(Blocks.RED_MUSHROOM)));
				}

				itemstack.damage(1, entityhuman);
				this.makeSound("mob.sheep.shear", 1.0F, 1.0F);
			}

			return true;
		} else {
			return super.a(entityhuman);
		}
	}

	public EntityMushroomCow c(EntityAgeable entityageable) {
		return new EntityMushroomCow(this.world);
	}

	@Override
	public EntityCow b(EntityAgeable entityageable) {
		return this.c(entityageable);
	}

	@Override
	public EntityAgeable createChild(EntityAgeable entityageable) {
		return this.c(entityageable);
	}
}
