package net.strapinrage.spacespigot.knockback;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.sugarcanemc.sugarcane.util.yaml.YamlCommenter;

import com.google.common.base.Throwables;

import dev.cobblesword.nachospigot.knockback.KnockbackProfile;
import net.strapinrage.spacespigot.SpaceSpigot;

public class KnockbackConfig {
	private static final Logger LOGGER = LogManager.getLogger(KnockbackConfig.class);
	private static File CONFIG_FILE;
	protected static final YamlCommenter c = new YamlCommenter();
	private static final String HEADER = "This is the knockback configuration file for SpaceSpigot.\n"
			+ "For configuration info see this: https://github.com/strapinrage/SpaceSpigot/wiki/Knockback-Configuration";
	static YamlConfiguration config;

	private static volatile KnockbackProfile currentKb;
	private static volatile Set<KnockbackProfile> kbProfiles = new HashSet<>();

	public static void init(File configFile) {
		CONFIG_FILE = configFile;
		config = new YamlConfiguration();
		try {
			SpaceSpigot.LOGGER.info("Loading SpaceSpigot knockback config from " + configFile.getName());
			config.load(CONFIG_FILE);
		} catch (IOException ignored) {
		} catch (InvalidConfigurationException ex) {
			LOGGER.log(Level.ERROR, "Could not load knockback.yml, please correct your syntax errors", ex);
			throw Throwables.propagate(ex);
		}
		config.options().copyDefaults(true);
		c.setHeader(HEADER);

		Set<String> keys = getKeys("knockback.profiles");
		
		if (!keys.contains("vanilla")) {
			final KnockbackProfile vanillaProfile = new CraftKnockbackProfile("vanilla");
			vanillaProfile.save(true);
		}
		
		if (!keys.contains("spacecraftpractice")) {
			final KnockbackProfile spacecraftpracticeProfile = new CraftKnockbackProfile("spacecraftpractice");

			spacecraftpracticeProfile.setRodHorizontal(0.7);
			spacecraftpracticeProfile.setRodVertical(0.5);
			spacecraftpracticeProfile.setSnowballHorizontal(0.7);
			spacecraftpracticeProfile.setSnowballHorizontal(0.5);
			spacecraftpracticeProfile.setEggHorizontal(0.7);
			spacecraftpracticeProfile.setEggVertical(0.5);
			spacecraftpracticeProfile.setArrowHorizontal(0.5);

			spacecraftpracticeProfile.setFrictionHorizontal(1.85);
			spacecraftpracticeProfile.setFrictionVertical(2.0);

			spacecraftpracticeProfile.setHorizontal(0.1);
			spacecraftpracticeProfile.setVertical(0.3);

			spacecraftpracticeProfile.setExtraHorizontal(0.75);
			spacecraftpracticeProfile.setExtraVertical(0.1);

			spacecraftpracticeProfile.setWTapExtraHorizontal(0.76);
			spacecraftpracticeProfile.setWTapExtraVertical(0.1);

			spacecraftpracticeProfile.setAddHorizontal(0.04);

			spacecraftpracticeProfile.save(true);
		}
		
		if (!keys.contains("hypixel")) {
			final KnockbackProfile hypixelProfile = new CraftKnockbackProfile("hypixel");
			
			hypixelProfile.setVertical(0.36);
			hypixelProfile.setVerticalMax(0.43075);
			
			hypixelProfile.save(true);
		}
		
		if (!keys.contains("kohi")) {
			final KnockbackProfile defaultProfile = new CraftKnockbackProfile("kohi"); 

			defaultProfile.setHorizontal(0.35);
			defaultProfile.setRodHorizontal(0.35);
			defaultProfile.setArrowHorizontal(0.35);
			defaultProfile.setPearlHorizontal(0.35);
			defaultProfile.setSnowballHorizontal(0.35);
			defaultProfile.setEggHorizontal(0.35);
			defaultProfile.setExtraHorizontal(0.425);
			defaultProfile.setWTapExtraHorizontal(0.425);

			defaultProfile.setVertical(0.35);
			defaultProfile.setRodVertical(0.35);
			defaultProfile.setArrowVertical(0.35);
			defaultProfile.setPearlVertical(0.35);
			defaultProfile.setSnowballVertical(0.35);
			defaultProfile.setEggVertical(0.35);
			defaultProfile.setExtraVertical(0.085);
			defaultProfile.setWTapExtraVertical(0.085);

			defaultProfile.save(true);
		}

		keys = getKeys("knockback.profiles");
		
		for (String key : keys) {
			final String path = "knockback.profiles." + key;
			CraftKnockbackProfile profile = (CraftKnockbackProfile) getKbProfileByName(key);
			if (profile == null) {
				profile = new CraftKnockbackProfile(key);
				kbProfiles.add(profile);
			}
			profile.setStopSprint(getBoolean(path + ".stop-sprint", true));
			profile.setFrictionHorizontal(getDouble(path + ".friction-horizontal", 2.0D));
			profile.setFrictionVertical(getDouble(path + ".friction-vertical", 2.0D));
			profile.setHorizontal(getDouble(path + ".horizontal", 0.4D));
			profile.setVertical(getDouble(path + ".vertical", 0.4D));
			profile.setVerticalMax(getDouble(path + ".vertical-max", 0.4D));
			profile.setVerticalMin(getDouble(path + ".vertical-min", -1.0D));
			profile.setExtraHorizontal(getDouble(path + ".extra-horizontal", 0.5D));
			profile.setExtraVertical(getDouble(path + ".extra-vertical", 0.1D));

			profile.setWTapExtraHorizontal(getDouble(path + ".wtap-extra-horizontal", 0.5));
			profile.setWTapExtraVertical(getDouble(path + ".wtap-extra-vertical", 0.1));
			
			profile.setAddHorizontal(getDouble(path + ".add-horizontal", 0));
			profile.setAddVertical(getDouble(path + ".add-vertical", 0));

			profile.setRodHorizontal(getDouble(path + ".projectiles.rod.horizontal", 0.4D));
			profile.setRodVertical(getDouble(path + ".projectiles.rod.vertical", 0.4D));
			profile.setArrowHorizontal(getDouble(path + ".projectiles.arrow.horizontal", 0.4D));
			profile.setArrowVertical(getDouble(path + ".projectiles.arrow.vertical", 0.4D));
			profile.setPearlHorizontal(getDouble(path + ".projectiles.pearl.horizontal", 0.4D));
			profile.setPearlVertical(getDouble(path + ".projectiles.pearl.vertical", 0.4D));
			profile.setSnowballHorizontal(getDouble(path + ".projectiles.snowball.horizontal", 0.4D));
			profile.setSnowballVertical(getDouble(path + ".projectiles.snowball.vertical", 0.4D));
			profile.setEggHorizontal(getDouble(path + ".projectiles.egg.horizontal", 0.4D));
			profile.setEggVertical(getDouble(path + ".projectiles.egg.vertical", 0.4D));
		}
		currentKb = getKbProfileByName(getString("knockback.current", "kohi"));
		if (currentKb == null) {
			SpaceSpigot.LOGGER.warn("Knockback profile selected was not found, using profile 'kohi' for now!");
			currentKb = getKbProfileByName("kohi");
			
			SpaceSpigot.LOGGER.info("Setting default knockback as 'kohi'...");
			set("knockback.current", "kohi");
		}
		save();
	}

	public static KnockbackProfile getCurrentKb() {
		if (currentKb == null) {
			setCurrentKb(getKbProfileByName("vanilla"));
		}
		return currentKb;
	}

	public static void setCurrentKb(KnockbackProfile kb) {
		currentKb = kb;
	}

	public static KnockbackProfile getKbProfileByName(String name) {
		for (KnockbackProfile profile : kbProfiles) {
			if (profile.getName().equalsIgnoreCase(name)) {
				return profile;
			}
		}
		return null;
	}

	public static Set<KnockbackProfile> getKbProfiles() {
		return kbProfiles;
	}

	public static void save() {
		try {
			config.save(CONFIG_FILE);
		} catch (IOException ex) {
			LOGGER.log(Level.ERROR, "Could not save " + CONFIG_FILE, ex);
		}
	}

	public static void set(String path, Object val) {
		config.set(path, val);

		save();
	}

	public static Set<String> getKeys(String path) {
		if (!config.isConfigurationSection(path)) {
			config.createSection(path);
			return new HashSet<>();
		}

		return config.getConfigurationSection(path).getKeys(false);
	}

	private static boolean getBoolean(String path, boolean def) {
		config.addDefault(path, def);
		return config.getBoolean(path, config.getBoolean(path));
	}

	private static double getDouble(String path, double def) {
		config.addDefault(path, def);
		return config.getDouble(path, config.getDouble(path));
	}

	private static float getFloat(String path, float def) {
		config.addDefault(path, def);
		return config.getFloat(path, config.getFloat(path));
	}

	private static int getInt(String path, int def) {
		config.addDefault(path, def);
		return config.getInt(path, config.getInt(path));
	}

	private static <T> List getList(String path, T def) {
		config.addDefault(path, def);
		return config.getList(path, config.getList(path));
	}

	private static String getString(String path, String def) {
		config.addDefault(path, def);
		return config.getString(path, config.getString(path));
	}
}
