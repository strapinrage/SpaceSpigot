package org.bukkit.craftbukkit.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftEntityEquipment;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Fish;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import dev.cobblesword.nachospigot.knockback.KnockbackProfile;
import net.minecraft.server.DamageSource;
import net.minecraft.server.EntityArmorStand;
import net.minecraft.server.EntityArrow;
import net.minecraft.server.EntityEgg;
import net.minecraft.server.EntityEnderPearl;
import net.minecraft.server.EntityFireball;
import net.minecraft.server.EntityFishingHook;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityInsentient;
import net.minecraft.server.EntityLargeFireball;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.EntityPotion;
import net.minecraft.server.EntitySmallFireball;
import net.minecraft.server.EntitySnowball;
import net.minecraft.server.EntityThrownExpBottle;
import net.minecraft.server.EntityWither;
import net.minecraft.server.EntityWitherSkull;
import net.minecraft.server.GenericAttributes;
import net.minecraft.server.MobEffect;
import net.minecraft.server.MobEffectList;

public class CraftLivingEntity extends CraftEntity implements LivingEntity {
	private CraftEntityEquipment equipment;

	public CraftLivingEntity(final CraftServer server, final EntityLiving entity) {
		super(server, entity);

		if (entity instanceof EntityInsentient || entity instanceof EntityArmorStand) {
			equipment = new CraftEntityEquipment(this);
		}
	}

	@Override
	public double getHealth() {
		return Math.min(Math.max(0, getHandle().getHealth()), getMaxHealth());
	}

	@Override
	public void setHealth(double health) {
		if ((health < 0) || (health > getMaxHealth())) {
			throw new IllegalArgumentException("Health must be between 0 and " + getMaxHealth() + ", but was " + health
					+ ". (attribute base value: "
					+ this.getHandle().getAttributeInstance(GenericAttributes.maxHealth).b()
					+ (this instanceof CraftPlayer ? ", player: " + this.getName() + ')' : ')'));
		}

		if (entity instanceof EntityPlayer && health == 0) {
			((EntityPlayer) entity).die(DamageSource.GENERIC);
		}

		getHandle().setHealth((float) health);
	}

	@Override
	public double getMaxHealth() {
		return getHandle().getMaxHealth();
	}

	@Override
	public void setMaxHealth(double amount) {
		Validate.isTrue(amount > 0, "Max health must be greater than 0");

		getHandle().getAttributeInstance(GenericAttributes.maxHealth).setValue(amount);

		if (getHealth() > amount) {
			setHealth(amount);
		}
	}

	@Override
	public void resetMaxHealth() {
		setMaxHealth(getHandle().getMaxHealth());
	}

	@Override
	@Deprecated
	public Egg throwEgg() {
		return launchProjectile(Egg.class);
	}

	@Override
	@Deprecated
	public Snowball throwSnowball() {
		return launchProjectile(Snowball.class);
	}

	@Override
	public double getEyeHeight() {
		return getHandle().getHeadHeight();
	}

	@Override
	public double getEyeHeight(boolean ignoreSneaking) {
		return getEyeHeight();
	}

	private List<Block> getLineOfSight(HashSet<Byte> transparent, int maxDistance, int maxLength) {
		if (maxDistance > 120) {
			maxDistance = 120;
		}
		ArrayList<Block> blocks = new ArrayList<Block>();
		Iterator<Block> itr = new BlockIterator(this, maxDistance);
		while (itr.hasNext()) {
			Block block = itr.next();
			blocks.add(block);
			if (maxLength != 0 && blocks.size() > maxLength) {
				blocks.remove(0);
			}
			int id = block.getTypeId();
			if (transparent == null) {
				if (id != 0) {
					break;
				}
			} else if (!transparent.contains((byte) id)) {
				break;
			}
		}
		return blocks;
	}

	private List<Block> getLineOfSight(Set<Material> transparent, int maxDistance, int maxLength) {
		if (maxDistance > 120) {
			maxDistance = 120;
		}
		ArrayList<Block> blocks = new ArrayList<Block>();
		Iterator<Block> itr = new BlockIterator(this, maxDistance);
		while (itr.hasNext()) {
			Block block = itr.next();
			blocks.add(block);
			if (maxLength != 0 && blocks.size() > maxLength) {
				blocks.remove(0);
			}
			Material material = block.getType();
			if (transparent == null) {
				if (!Material.AIR.equals(material)) {
					break;
				}
			} else if (!transparent.contains(material)) {
				break;
			}
		}
		return blocks;
	}

	@Override
	public List<Block> getLineOfSight(HashSet<Byte> transparent, int maxDistance) {
		return getLineOfSight(transparent, maxDistance, 0);
	}

	@Override
	public List<Block> getLineOfSight(Set<Material> transparent, int maxDistance) {
		return getLineOfSight(transparent, maxDistance, 0);
	}

	@Override
	public Block getTargetBlock(HashSet<Byte> transparent, int maxDistance) {
		List<Block> blocks = getLineOfSight(transparent, maxDistance, 1);
		return blocks.get(0);
	}

	@Override
	public Block getTargetBlock(Set<Material> transparent, int maxDistance) {
		List<Block> blocks = getLineOfSight(transparent, maxDistance, 1);
		return blocks.get(0);
	}

	@Override
	public List<Block> getLastTwoTargetBlocks(HashSet<Byte> transparent, int maxDistance) {
		return getLineOfSight(transparent, maxDistance, 2);
	}

	@Override
	public List<Block> getLastTwoTargetBlocks(Set<Material> transparent, int maxDistance) {
		return getLineOfSight(transparent, maxDistance, 2);
	}

	@Override
	@Deprecated
	public Arrow shootArrow() {
		return launchProjectile(Arrow.class);
	}

	@Override
	public int getRemainingAir() {
		return getHandle().getAirTicks();
	}

	@Override
	public void setRemainingAir(int ticks) {
		getHandle().setAirTicks(ticks);
	}

	@Override
	public int getMaximumAir() {
		return getHandle().maxAirTicks;
	}

	@Override
	public void setMaximumAir(int ticks) {
		getHandle().maxAirTicks = ticks;
	}

	@Override
	public void damage(double amount) {
		damage(amount, null);
	}

	@Override
	public void damage(double amount, org.bukkit.entity.Entity source) {
		DamageSource reason = DamageSource.GENERIC;

		if (source instanceof HumanEntity) {
			reason = DamageSource.playerAttack(((CraftHumanEntity) source).getHandle());
		} else if (source instanceof LivingEntity) {
			reason = DamageSource.mobAttack(((CraftLivingEntity) source).getHandle());
		}

		entity.damageEntity(reason, (float) amount);
	}

	@Override
	public Location getEyeLocation() {
		Location loc = getLocation();
		loc.setY(loc.getY() + getEyeHeight());
		return loc;
	}

	@Override
	public int getMaximumNoDamageTicks() {
		return getHandle().maxNoDamageTicks;
	}

	@Override
	public void setMaximumNoDamageTicks(int ticks) {
		getHandle().maxNoDamageTicks = ticks;
	}

	@Override
	public double getLastDamage() {
		return getHandle().lastDamage;
	}

	@Override
	public void setLastDamage(double damage) {
		getHandle().lastDamage = (float) damage;
	}

	@Override
	public int getNoDamageTicks() {
		return getHandle().noDamageTicks;
	}

	@Override
	public void setNoDamageTicks(int ticks) {
		getHandle().noDamageTicks = ticks;
	}

	@Override
	public EntityLiving getHandle() {
		return (EntityLiving) entity;
	}

	public void setHandle(final EntityLiving entity) {
		super.setHandle(entity);
	}

	@Override
	public String toString() {
		return "CraftLivingEntity{" + "id=" + getEntityId() + '}';
	}

	@Override
	public Player getKiller() {
		return getHandle().killer == null ? null : (Player) getHandle().killer.getBukkitEntity();
	}

	@Override
	public boolean addPotionEffect(PotionEffect effect) {
		return addPotionEffect(effect, false);
	}

	@Override
	public boolean addPotionEffect(PotionEffect effect, boolean force) {
		if (hasPotionEffect(effect.getType())) {
			if (!force) {
				return false;
			}
			removePotionEffect(effect.getType());
		}
		getHandle().addEffect(new MobEffect(effect.getType().getId(), effect.getDuration(), effect.getAmplifier(),
				effect.isAmbient(), effect.hasParticles()));
		return true;
	}

	@Override
	public boolean addPotionEffects(Collection<PotionEffect> effects) {
		boolean success = true;
		for (PotionEffect effect : effects) {
			success &= addPotionEffect(effect);
		}
		return success;
	}

	@Override
	public boolean hasPotionEffect(PotionEffectType type) {
		return getHandle().hasEffect(MobEffectList.byId[type.getId()]);
	}

	@Override
	public void removePotionEffect(PotionEffectType type) {
		getHandle().removeEffect(type.getId());
	}

	@Override
	public Collection<PotionEffect> getActivePotionEffects() {
		List<PotionEffect> effects = new ArrayList<PotionEffect>();
		for (MobEffect raw : getHandle().effects.values()) {
			MobEffect handle = raw;
			effects.add(new PotionEffect(PotionEffectType.getById(handle.getEffectId()), handle.getDuration(),
					handle.getAmplifier(), handle.isAmbient(), handle.isShowParticles()));
		}
		return effects;
	}

	@Override
	public <T extends Projectile> T launchProjectile(Class<? extends T> projectile) {
		return launchProjectile(projectile, null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Projectile> T launchProjectile(Class<? extends T> projectile, Vector velocity) {
		net.minecraft.server.World world = ((CraftWorld) getWorld()).getHandle();
		net.minecraft.server.Entity launch = null;

		if (Snowball.class.isAssignableFrom(projectile)) {
			launch = new EntitySnowball(world, getHandle());
		} else if (Egg.class.isAssignableFrom(projectile)) {
			launch = new EntityEgg(world, getHandle());
		} else if (EnderPearl.class.isAssignableFrom(projectile)) {
			launch = new EntityEnderPearl(world, getHandle());
		} else if (Arrow.class.isAssignableFrom(projectile)) {
			launch = new EntityArrow(world, getHandle(), 1);
		} else if (ThrownPotion.class.isAssignableFrom(projectile)) {
			launch = new EntityPotion(world, getHandle(), CraftItemStack.asNMSCopy(new ItemStack(Material.POTION, 1)));
		} else if (ThrownExpBottle.class.isAssignableFrom(projectile)) {
			launch = new EntityThrownExpBottle(world, getHandle());
		} else if (Fish.class.isAssignableFrom(projectile) && getHandle() instanceof EntityHuman) {
			launch = new EntityFishingHook(world, (EntityHuman) getHandle());
		} else if (Fireball.class.isAssignableFrom(projectile)) {
			Location location = getEyeLocation();
			Vector direction = location.getDirection().multiply(10);

			if (SmallFireball.class.isAssignableFrom(projectile)) {
				launch = new EntitySmallFireball(world, getHandle(), direction.getX(), direction.getY(),
						direction.getZ());
			} else if (WitherSkull.class.isAssignableFrom(projectile)) {
				launch = new EntityWitherSkull(world, getHandle(), direction.getX(), direction.getY(),
						direction.getZ());
			} else {
				launch = new EntityLargeFireball(world, getHandle(), direction.getX(), direction.getY(),
						direction.getZ());
			}

			((EntityFireball) launch).projectileSource = this;
			launch.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(),
					location.getPitch());
		}

		Validate.notNull(launch, "Projectile not supported");

		if (velocity != null) {
			((T) launch.getBukkitEntity()).setVelocity(velocity);
		}

		world.addEntity(launch);
		return (T) launch.getBukkitEntity();
	}

	@Override
	public EntityType getType() {
		return EntityType.UNKNOWN;
	}

	@Override
	public boolean hasLineOfSight(Entity other) {
		return getHandle().hasLineOfSight(((CraftEntity) other).getHandle());
	}
	
	// SpaceSpigot start
	@Override
	public boolean hasLineOfSight(Location location) {
		return getHandle().hasLineOfSight(location.getX(), location.getY(), location.getZ());
	}
	// SpaceSpigot end

	@Override
	public boolean getRemoveWhenFarAway() {
		return getHandle() instanceof EntityInsentient && !((EntityInsentient) getHandle()).persistent;
	}

	@Override
	public void setRemoveWhenFarAway(boolean remove) {
		if (getHandle() instanceof EntityInsentient) {
			((EntityInsentient) getHandle()).persistent = !remove;
		}
	}

	@Override
	public EntityEquipment getEquipment() {
		return equipment;
	}

	@Override
	public void setCanPickupItems(boolean pickup) {
		if (getHandle() instanceof EntityInsentient) {
			((EntityInsentient) getHandle()).canPickUpLoot = pickup;
		}
	}

	@Override
	public boolean getCanPickupItems() {
		return getHandle() instanceof EntityInsentient && ((EntityInsentient) getHandle()).canPickUpLoot;
	}

	@Override
	public boolean teleport(Location location, PlayerTeleportEvent.TeleportCause cause) {
		if (getHealth() == 0) {
			return false;
		}

		return super.teleport(location, cause);
	}

	@Override
	public boolean isLeashed() {
		if (!(getHandle() instanceof EntityInsentient)) {
			return false;
		}
		return ((EntityInsentient) getHandle()).getLeashHolder() != null;
	}

	@Override
	public Entity getLeashHolder() throws IllegalStateException {
		if (!isLeashed()) {
			throw new IllegalStateException("Entity not leashed");
		}
		return ((EntityInsentient) getHandle()).getLeashHolder().getBukkitEntity();
	}

	private boolean unleash() {
		if (!isLeashed()) {
			return false;
		}
		((EntityInsentient) getHandle()).unleash(true, false);
		return true;
	}

	@Override
	public boolean setLeashHolder(Entity holder) {
		if ((getHandle() instanceof EntityWither) || !(getHandle() instanceof EntityInsentient)) {
			return false;
		}

		if (holder == null) {
			return unleash();
		}

		if (holder.isDead()) {
			return false;
		}

		unleash();
		((EntityInsentient) getHandle()).setLeashHolder(((CraftEntity) holder).getHandle(), true);
		return true;
	}

	@Override
	@Deprecated
	public int _INVALID_getLastDamage() {
		return NumberConversions.ceil(getLastDamage());
	}

	@Override
	@Deprecated
	public void _INVALID_setLastDamage(int damage) {
		setLastDamage(damage);
	}

	@Override
	@Deprecated
	public void _INVALID_damage(int amount) {
		damage(amount);
	}

	@Override
	@Deprecated
	public void _INVALID_damage(int amount, Entity source) {
		damage(amount, source);
	}

	@Override
	@Deprecated
	public int _INVALID_getHealth() {
		return NumberConversions.ceil(getHealth());
	}

	@Override
	@Deprecated
	public void _INVALID_setHealth(int health) {
		setHealth(health);
	}

	@Override
	@Deprecated
	public int _INVALID_getMaxHealth() {
		return NumberConversions.ceil(getMaxHealth());
	}

	@Override
	@Deprecated
	public void _INVALID_setMaxHealth(int health) {
		setMaxHealth(health);
	}

	// TacoSpigot start
	@Override
	public int getArrowsStuck() {
		return getHandle().getArrowsStuck();
	}

	@Override
	public void setArrowsStuck(int arrows) {
		getHandle().setArrowsStuck(arrows);
	}

	@Override
	public boolean shouldBreakLeash() {
		if (this.getHandle() instanceof EntityInsentient) {
			return ((EntityInsentient) getHandle()).shouldBreakLeash();
		}
		return true;
	}

	@Override
	public void setShouldBreakLeash(boolean shouldBreakLeash) {
		if (this.getHandle() instanceof EntityInsentient) {
			((EntityInsentient) getHandle()).setShouldBreakLeash(shouldBreakLeash);
		}
	}

	@Override
	public boolean shouldPullWhileLeashed() {
		if (this.getHandle() instanceof EntityInsentient) {
			return ((EntityInsentient) getHandle()).shouldPullWhileLeashed();
		}
		return true;
	}

	@Override
	public void setPullWhileLeashed(boolean pullWhileLeashed) {
		if (this.getHandle() instanceof EntityInsentient) {
			((EntityInsentient) getHandle()).setPullWhileLeashed(pullWhileLeashed);
		}
	}
	// TacoSpigot end

	@Override
	public KnockbackProfile getKnockbackProfile() {
		return getHandle().getKnockbackProfile();
	}

	@Override
	public void setKnockbackProfile(KnockbackProfile profile) {
		getHandle().setKnockbackProfile(profile);
	}
}
