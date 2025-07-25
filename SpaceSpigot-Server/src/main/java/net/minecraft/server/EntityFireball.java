package net.minecraft.server;

import java.util.List;

import org.bukkit.craftbukkit.event.CraftEventFactory; // CraftBukkit

import net.strapinrage.spacespigot.cache.Constants;

public abstract class EntityFireball extends Entity {

	private int e = -1;
	private int f = -1;
	private int g = -1;
	private Block h;
	private boolean i;
	public EntityLiving shooter;
	private int ar;
	private int as;
	public double dirX;
	public double dirY;
	public double dirZ;
	public float bukkitYield = 1; // CraftBukkit
	public boolean isIncendiary = true; // CraftBukkit

	public EntityFireball(World world) {
		super(world);
		this.setSize(1.0F, 1.0F);
	}

	@Override
	protected void h() {
	}

	public EntityFireball(World world, double d0, double d1, double d2, double d3, double d4, double d5) {
		super(world);
		this.setSize(1.0F, 1.0F);
		this.setPositionRotation(d0, d1, d2, this.yaw, this.pitch);
		this.setPosition(d0, d1, d2);
		double d6 = MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);

		this.dirX = d3 / d6 * 0.1D;
		this.dirY = d4 / d6 * 0.1D;
		this.dirZ = d5 / d6 * 0.1D;
	}

	public EntityFireball(World world, EntityLiving entityliving, double d0, double d1, double d2) {
		super(world);
		this.shooter = entityliving;
		this.projectileSource = (org.bukkit.entity.LivingEntity) entityliving.getBukkitEntity(); // CraftBukkit
		this.setSize(1.0F, 1.0F);
		this.setPositionRotation(entityliving.locX, entityliving.locY, entityliving.locZ, entityliving.yaw,
				entityliving.pitch);
		this.setPosition(this.locX, this.locY, this.locZ);
		this.motX = this.motY = this.motZ = 0.0D;
		// CraftBukkit start - Added setDirection method
		this.setDirection(d0, d1, d2);
	}

	public void setDirection(double d0, double d1, double d2) {
		// CraftBukkit end
		d0 += this.random.nextGaussian() * 0.4D;
		d1 += this.random.nextGaussian() * 0.4D;
		d2 += this.random.nextGaussian() * 0.4D;
		double d3 = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

		this.dirX = d0 / d3 * 0.1D;
		this.dirY = d1 / d3 * 0.1D;
		this.dirZ = d2 / d3 * 0.1D;
	}

	@Override
	public void t_() {
		if (!this.world.isClientSide
				&& (this.shooter != null && this.shooter.dead || !this.world.isLoaded(new BlockPosition(this)))) {
			this.die();
		} else {
			super.t_();
			this.setOnFire(1);
			if (this.i) {
				if (this.world.getType(new BlockPosition(this.e, this.f, this.g)).getBlock() == this.h) {
					++this.ar;
					if (this.ar == 600) {
						this.die();
					}

					return;
				}

				this.i = false;
				this.motX *= this.random.nextFloat() * 0.2F;
				this.motY *= this.random.nextFloat() * 0.2F;
				this.motZ *= this.random.nextFloat() * 0.2F;
				this.ar = 0;
				this.as = 0;
			} else {
				++this.as;
			}

			Vec3D vec3d = new Vec3D(this.locX, this.locY, this.locZ);
			Vec3D vec3d1 = new Vec3D(this.locX + this.motX, this.locY + this.motY, this.locZ + this.motZ);
			MovingObjectPosition movingobjectposition = this.world.rayTrace(vec3d, vec3d1);

			vec3d = new Vec3D(this.locX, this.locY, this.locZ);
			vec3d1 = new Vec3D(this.locX + this.motX, this.locY + this.motY, this.locZ + this.motZ);
			if (movingobjectposition != null) {
				vec3d1 = new Vec3D(movingobjectposition.pos.a, movingobjectposition.pos.b, movingobjectposition.pos.c);
			}

			Entity entity = null;
			List list = this.world.getEntities(this,
					this.getBoundingBox().a(this.motX, this.motY, this.motZ).grow(1.0D, 1.0D, 1.0D));
			double d0 = 0.0D;

			for (int i = 0; i < list.size(); ++i) {
				Entity entity1 = (Entity) list.get(i);

				if (entity1.ad() && (!entity1.k(this.shooter) || this.as >= 25)) {
					float f = 0.3F;
					AxisAlignedBB axisalignedbb = entity1.getBoundingBox().grow(f, f, f);
					MovingObjectPosition movingobjectposition1 = axisalignedbb.a(vec3d, vec3d1);

					if (movingobjectposition1 != null) {
						double d1 = vec3d.distanceSquared(movingobjectposition1.pos);

						if (d1 < d0 || d0 == 0.0D) {
							entity = entity1;
							d0 = d1;
						}
					}
				}
			}

			if (entity != null) {
				movingobjectposition = new MovingObjectPosition(entity);
			}

			if (movingobjectposition != null) {
				this.a(movingobjectposition);

				// CraftBukkit start - Fire ProjectileHitEvent
				if (this.dead) {
					CraftEventFactory.callProjectileHitEvent(this);
				}
				// CraftBukkit end
			}

			this.locX += this.motX;
			this.locY += this.motY;
			this.locZ += this.motZ;
			float f1 = MathHelper.sqrt(this.motX * this.motX + this.motZ * this.motZ);

			this.yaw = (float) (MathHelper.b(this.motZ, this.motX) * 180.0D / 3.1415927410125732D) + 90.0F;

			for (this.pitch = (float) (MathHelper.b(f1, this.motY) * 180.0D / 3.1415927410125732D) - 90.0F; this.pitch
					- this.lastPitch < -180.0F; this.lastPitch -= 360.0F) {
				;
			}

			while (this.pitch - this.lastPitch >= 180.0F) {
				this.lastPitch += 360.0F;
			}

			while (this.yaw - this.lastYaw < -180.0F) {
				this.lastYaw -= 360.0F;
			}

			while (this.yaw - this.lastYaw >= 180.0F) {
				this.lastYaw += 360.0F;
			}

			this.pitch = this.lastPitch + (this.pitch - this.lastPitch) * 0.2F;
			this.yaw = this.lastYaw + (this.yaw - this.lastYaw) * 0.2F;
			float f2 = this.j();

			if (this.V()) {
				for (int j = 0; j < 4; ++j) {
					float f3 = 0.25F;

					this.world.addParticle(EnumParticle.WATER_BUBBLE, this.locX - this.motX * f3,
							this.locY - this.motY * f3, this.locZ - this.motZ * f3, this.motX, this.motY, this.motZ,
							Constants.EMPTY_ARRAY);
				}

				f2 = 0.8F;
			}

			this.motX += this.dirX;
			this.motY += this.dirY;
			this.motZ += this.dirZ;
			this.motX *= f2;
			this.motY *= f2;
			this.motZ *= f2;
			this.world.addParticle(EnumParticle.SMOKE_NORMAL, this.locX, this.locY + 0.5D, this.locZ, 0.0D, 0.0D, 0.0D,
					Constants.EMPTY_ARRAY);
			this.setPosition(this.locX, this.locY, this.locZ);
		}
	}

	protected float j() {
		return 0.95F;
	}

	protected abstract void a(MovingObjectPosition movingobjectposition);

	@Override
	public void b(NBTTagCompound nbttagcompound) {
		nbttagcompound.setShort("xTile", (short) this.e);
		nbttagcompound.setShort("yTile", (short) this.f);
		nbttagcompound.setShort("zTile", (short) this.g);
		MinecraftKey minecraftkey = Block.REGISTRY.c(this.h);

		nbttagcompound.setString("inTile", minecraftkey == null ? "" : minecraftkey.toString());
		nbttagcompound.setByte("inGround", (byte) (this.i ? 1 : 0));
		// CraftBukkit - Fix direction being mismapped to invalid variables
		nbttagcompound.set("power", this.a(new double[] { this.dirX, this.dirY, this.dirZ }));
		nbttagcompound.set("direction", this.a(new double[] { this.motX, this.motY, this.motZ }));
	}

	@Override
	public void a(NBTTagCompound nbttagcompound) {
		this.e = nbttagcompound.getShort("xTile");
		this.f = nbttagcompound.getShort("yTile");
		this.g = nbttagcompound.getShort("zTile");
		if (nbttagcompound.hasKeyOfType("inTile", 8)) {
			this.h = Block.getByName(nbttagcompound.getString("inTile"));
		} else {
			this.h = Block.getById(nbttagcompound.getByte("inTile") & 255);
		}

		this.i = nbttagcompound.getByte("inGround") == 1;
		// CraftBukkit start - direction -> power
		if (nbttagcompound.hasKeyOfType("power", 9)) {
			NBTTagList nbttaglist = nbttagcompound.getList("power", 6);

			this.dirX = nbttaglist.d(0);
			this.dirY = nbttaglist.d(1);
			this.dirZ = nbttaglist.d(2);
		} else if (nbttagcompound.hasKeyOfType("direction", 9)) {
			NBTTagList nbttaglist = nbttagcompound.getList("direction", 6);

			this.motX = nbttaglist.d(0);
			this.motY = nbttaglist.d(1);
			this.motZ = nbttaglist.d(2);
			// CraftBukkit end
		} else {
			this.die();
		}

	}

	@Override
	public boolean ad() {
		return true;
	}

	@Override
	public float ao() {
		return 1.0F;
	}

	@Override
	public boolean damageEntity(DamageSource damagesource, float f) {
		if (this.isInvulnerable(damagesource)) {
			return false;
		} else {
			this.ac();
			if (damagesource.getEntity() != null) {
				// CraftBukkit start
				if (CraftEventFactory.handleNonLivingEntityDamageEvent(this, damagesource, f)) {
					return false;
				}
				// CraftBukkit end
				Vec3D vec3d = damagesource.getEntity().ap();

				if (vec3d != null) {
					this.motX = vec3d.a;
					this.motY = vec3d.b;
					this.motZ = vec3d.c;
					this.dirX = this.motX * 0.1D;
					this.dirY = this.motY * 0.1D;
					this.dirZ = this.motZ * 0.1D;
				}

				if (damagesource.getEntity() instanceof EntityLiving) {
					this.shooter = (EntityLiving) damagesource.getEntity();
					this.projectileSource = (org.bukkit.projectiles.ProjectileSource) this.shooter.getBukkitEntity();
				}

				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public float c(float f) {
		return 1.0F;
	}
}
