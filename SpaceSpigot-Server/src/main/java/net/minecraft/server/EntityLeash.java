package net.minecraft.server;

import java.util.Iterator;
import java.util.List;

import org.bukkit.craftbukkit.event.CraftEventFactory; // CraftBukkit

public class EntityLeash extends EntityHanging {

	public EntityLeash(World world) {
		super(world);
	}

	public EntityLeash(World world, BlockPosition blockposition) {
		super(world, blockposition);
		this.setPosition(blockposition.getX() + 0.5D, blockposition.getY() + 0.5D, blockposition.getZ() + 0.5D);
		float f = 0.125F;
		float f1 = 0.1875F;
		float f2 = 0.25F;

		this.a(new AxisAlignedBB(this.locX - 0.1875D, this.locY - 0.25D + 0.125D, this.locZ - 0.1875D,
				this.locX + 0.1875D, this.locY + 0.25D + 0.125D, this.locZ + 0.1875D));
	}

	@Override
	protected void h() {
		super.h();
	}

	@Override
	public void setDirection(EnumDirection enumdirection) {
	}

	@Override
	public int l() {
		return 9;
	}

	@Override
	public int m() {
		return 9;
	}

	@Override
	public float getHeadHeight() {
		return -0.0625F;
	}

	@Override
	public void b(Entity entity) {
	}

	@Override
	public boolean d(NBTTagCompound nbttagcompound) {
		return false;
	}

	@Override
	public void b(NBTTagCompound nbttagcompound) {
	}

	@Override
	public void a(NBTTagCompound nbttagcompound) {
	}

	@Override
	public boolean e(EntityHuman entityhuman) {
		ItemStack itemstack = entityhuman.bA();
		boolean flag = false;
		double d0;
		List list;
		Iterator iterator;
		EntityInsentient entityinsentient;

		if (itemstack != null && itemstack.getItem() == Items.LEAD && !this.world.isClientSide) {
			d0 = 7.0D;
			list = this.world.a(EntityInsentient.class, new AxisAlignedBB(this.locX - d0, this.locY - d0,
					this.locZ - d0, this.locX + d0, this.locY + d0, this.locZ + d0));
			iterator = list.iterator();

			while (iterator.hasNext()) {
				entityinsentient = (EntityInsentient) iterator.next();
				if (entityinsentient.cc() && entityinsentient.getLeashHolder() == entityhuman) {
					// CraftBukkit start
					if (CraftEventFactory.callPlayerLeashEntityEvent(entityinsentient, this, entityhuman)
							.isCancelled()) {
						((EntityPlayer) entityhuman).playerConnection.sendPacket(
								new PacketPlayOutAttachEntity(1, entityinsentient, entityinsentient.getLeashHolder()));
						continue;
					}
					// CraftBukkit end
					entityinsentient.setLeashHolder(this, true);
					flag = true;
				}
			}
		}

		if (!this.world.isClientSide && !flag) {
			// CraftBukkit start - Move below
			// this.die();
			boolean die = true;
			// CraftBukkit end
			
			// SpaceSpigot - remove dead code
			// if (true || entityhuman.abilities.canInstantlyBuild) { // CraftBukkit -
			// Process for non-creative as well
			d0 = 7.0D;
			list = this.world.a(EntityInsentient.class, new AxisAlignedBB(this.locX - d0, this.locY - d0,
					this.locZ - d0, this.locX + d0, this.locY + d0, this.locZ + d0));
			iterator = list.iterator();

			while (iterator.hasNext()) {
				entityinsentient = (EntityInsentient) iterator.next();
				if (entityinsentient.cc() && entityinsentient.getLeashHolder() == this) {
					// CraftBukkit start
					if (CraftEventFactory.callPlayerUnleashEntityEvent(entityinsentient, entityhuman).isCancelled()) {
						die = false;
						continue;
					}
					entityinsentient.unleash(true, !entityhuman.abilities.canInstantlyBuild); // false -> survival
																								// mode boolean
					// CraftBukkit end
				}
			}

			// CraftBukkit start
			if (die) {
				this.die();
			}
			// CraftBukkit end
		}

		return true;
	}

	@Override
	public boolean survives() {
		return this.world.getType(this.blockPosition).getBlock() instanceof BlockFence;
	}

	public static EntityLeash a(World world, BlockPosition blockposition) {
		EntityLeash entityleash = new EntityLeash(world, blockposition);

		entityleash.attachedToPlayer = true;
		world.addEntity(entityleash);
		return entityleash;
	}

	public static EntityLeash b(World world, BlockPosition blockposition) {
		int i = blockposition.getX();
		int j = blockposition.getY();
		int k = blockposition.getZ();
		List list = world.a(EntityLeash.class,
				new AxisAlignedBB(i - 1.0D, j - 1.0D, k - 1.0D, i + 1.0D, j + 1.0D, k + 1.0D));
		Iterator iterator = list.iterator();

		EntityLeash entityleash;

		do {
			if (!iterator.hasNext()) {
				return null;
			}

			entityleash = (EntityLeash) iterator.next();
		} while (!entityleash.getBlockPosition().equals(blockposition));

		return entityleash;
	}
}
