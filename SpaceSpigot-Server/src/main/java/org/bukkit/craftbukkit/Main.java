package org.bukkit.craftbukkit;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.fusesource.jansi.AnsiConsole;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraft.server.DedicatedServer;
import net.minecraft.server.DispenserRegistry;
import net.minecraft.server.MinecraftServer;

public class Main {
	public static boolean useJline = true;
	public static boolean useConsole = true;

	public static void main(String[] args) {
		System.setProperty("log4j2.formatMsgNoLookups", "true");
		try {
			if (!SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_17)) {
				System.err.println("It seems like you are not using Java 17!");
				System.out.println("The use of Java 17 is strongly recommended.");
			}
		} catch (Exception ignored) {
			System.err.println("Failed to get Java version! Continuing either way..");
		}
		OptionParser parser = new OptionParser() {
			{
				acceptsAll(asList("?", "help"), "Show the help");

				acceptsAll(asList("c", "config"), "Properties file to use").withRequiredArg().ofType(File.class)
						.defaultsTo(new File("server.properties")).describedAs("Properties file");

				acceptsAll(asList("P", "plugins"), "Plugin directory to use").withRequiredArg().ofType(File.class)
						.defaultsTo(new File("plugins")).describedAs("Plugin directory");

				acceptsAll(asList("h", "host", "server-ip"), "Host to listen on").withRequiredArg().ofType(String.class)
						.describedAs("Hostname or IP");

				acceptsAll(asList("W", "world-dir", "universe", "world-container"), "World container").withRequiredArg()
						.ofType(File.class).describedAs("Directory containing worlds");

				acceptsAll(asList("w", "world", "level-name"), "World name").withRequiredArg().ofType(String.class)
						.describedAs("World name");

				acceptsAll(asList("p", "port", "server-port"), "Port to listen on").withRequiredArg()
						.ofType(Integer.class).describedAs("Port");

				acceptsAll(asList("o", "online-mode"), "Whether to use online authentication").withRequiredArg()
						.ofType(Boolean.class).describedAs("Authentication");

				acceptsAll(asList("s", "size", "max-players"), "Maximum amount of players").withRequiredArg()
						.ofType(Integer.class).describedAs("Server size");

				acceptsAll(asList("d", "date-format"), "Format of the date to display in the console (for log entries)")
						.withRequiredArg().ofType(SimpleDateFormat.class).describedAs("Log date format");

				acceptsAll(asList("log-pattern"), "Specfies the log filename pattern").withRequiredArg()
						.ofType(String.class).defaultsTo("server.log").describedAs("Log filename");

				acceptsAll(asList("log-limit"), "Limits the maximum size of the log file (0 = unlimited)")
						.withRequiredArg().ofType(Integer.class).defaultsTo(0).describedAs("Max log size");

				acceptsAll(asList("log-count"), "Specified how many log files to cycle through").withRequiredArg()
						.ofType(Integer.class).defaultsTo(1).describedAs("Log count");

				acceptsAll(asList("log-append"), "Whether to append to the log file").withRequiredArg()
						.ofType(Boolean.class).defaultsTo(true).describedAs("Log append");

				acceptsAll(asList("log-strip-color"), "Strips color codes from log file");

				acceptsAll(asList("b", "bukkit-settings"), "File for bukkit settings").withRequiredArg()
						.ofType(File.class).defaultsTo(new File("bukkit.yml")).describedAs("Yml file");

				acceptsAll(asList("C", "commands-settings"), "File for command settings").withRequiredArg()
						.ofType(File.class).defaultsTo(new File("commands.yml")).describedAs("Yml file");

				acceptsAll(asList("nojline"), "Disables jline and emulates the vanilla console");

				acceptsAll(asList("noconsole"), "Disables the console");

				acceptsAll(asList("v", "version"), "Show the CraftBukkit Version");

				acceptsAll(asList("demo"), "Demo mode");

				// Spigot Start
				acceptsAll(asList("S", "spigot-settings"), "File for spigot settings").withRequiredArg()
						.ofType(File.class).defaultsTo(new File("spigot.yml")).describedAs("Yml file");
				// Spigot End

				// PaperSpigot Start
				acceptsAll(asList("paper", "paper-settings"), "File for paper settings").withRequiredArg()
						.ofType(File.class).defaultsTo(new File("paper.yml")).describedAs("Yml file");
				// PaperSpigot End

				// TacoSpigot start
				acceptsAll(asList("taco", "taco-settings"), "File for tacospigot settings").withRequiredArg()
						.ofType(File.class).defaultsTo(new File("taco.yml")).describedAs("Yml file");
				// TacoSpigot end
				// NachoSpigot start
				acceptsAll(asList("nacho", "nacho-settings"), "File for nachospigot settings").withRequiredArg()
						.ofType(File.class).defaultsTo(new File("nacho.yml")).describedAs("Yml file");
				acceptsAll(asList("knockback", "knockback-settings"), "File for nachospigot knockback settings")
						.withRequiredArg().ofType(File.class).defaultsTo(new File("knockback.yml"))
						.describedAs("Yml file");
				// NachoSpigot end
				// spacespigot start - config
				acceptsAll(asList("spacespigot", "spacespigot-settings"), "File for spacespigot settings").withRequiredArg()
				.ofType(File.class).defaultsTo(new File("spacespigot.yml")).describedAs("Yml file");
				// spacespigot end
			}
		};

		OptionSet options = null;

		try {
			options = parser.parse(args);
		} catch (joptsimple.OptionException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage());
		}

		if ((options == null) || (options.has("?"))) {
			try {
				parser.printHelpOn(System.out);
			} catch (IOException ex) {
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			}
		} else if (options.has("v")) {
			System.out.println(CraftServer.class.getPackage().getImplementationVersion());
		} else {
			// Do you love Java using + and ! as string based identifiers? I sure do!
			String path = new File(".").getAbsolutePath();
			if (path.contains("!") || path.contains("+")) {
				System.err.println(
						"Cannot run server in a directory with ! or + in the pathname. Please rename the affected folders and try again.");
				return;
			}

			try {
				// This trick bypasses Maven Shade's clever rewriting of our getProperty call
				// when using String literals
				String jline_UnsupportedTerminal = new String(new char[] { 'j', 'l', 'i', 'n', 'e', '.', 'U', 'n', 's',
						'u', 'p', 'p', 'o', 'r', 't', 'e', 'd', 'T', 'e', 'r', 'm', 'i', 'n', 'a', 'l' });
				String jline_terminal = new String(
						new char[] { 'j', 'l', 'i', 'n', 'e', '.', 't', 'e', 'r', 'm', 'i', 'n', 'a', 'l' });

				useJline = !(jline_UnsupportedTerminal).equals(System.getProperty(jline_terminal));

				if (options.has("nojline")) {
					System.setProperty("user.language", "en");
					useJline = false;
				}

				if (useJline) {
					System.setProperty("library.jansi.version", "NachoSpigot");
					AnsiConsole.systemInstall();
				} else {
					// This ensures the terminal literal will always match the jline implementation
					System.setProperty(jline.TerminalFactory.JLINE_TERMINAL, jline.UnsupportedTerminal.class.getName());
				}

				if (options.has("noconsole")) {
					useConsole = false;
				}

				// Spigot Start
				int maxPermGen = 0; // In kb
				for (String s : java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments()) {
					if (s.startsWith("-XX:MaxPermSize")) {
						maxPermGen = Integer.parseInt(s.replaceAll("[^\\d]", ""));
						maxPermGen <<= 10 * ("kmg".indexOf(Character.toLowerCase(s.charAt(s.length() - 1))));
					}
				}
				if (Float.parseFloat(System.getProperty("java.class.version")) < 52 && maxPermGen < (128 << 10)) // 128mb
				{
					System.out.println(
							"Warning, your max perm gen size is not set or less than 128mb. It is recommended you restart Java with the following argument: -XX:MaxPermSize=128M");
					System.out.println(
							"Please see http://www.spigotmc.org/wiki/changing-permgen-size/ for more details and more in-depth instructions.");
				}
				// Spigot End
				net.techcable.tacospigot.TacoSpigotConfig.init((File) options.valueOf("taco-settings")); // TacoSpigot -
																											// load
																											// config
																											// before we
																											// load
																											// libraries
																											// to allow
																											// access
																											// while
																											// loading
				System.out.println("Loading libraries, please wait...");

				DispenserRegistry.c();
				OptionSet finalOptions = options;

				DedicatedServer server = MinecraftServer.spin(thread -> {
					DedicatedServer dedicatedserver = new DedicatedServer(finalOptions, thread);

					if (finalOptions.has("port")) {
						int port = (Integer) finalOptions.valueOf("port");
						if (port > 0) {
							dedicatedserver.setPort(port);
						}
					}

					if (finalOptions.has("universe")) {
						dedicatedserver.universe = (File) finalOptions.valueOf("universe");
					}

					if (finalOptions.has("world")) {
						dedicatedserver.setWorld((String) finalOptions.valueOf("world"));
					}
					return dedicatedserver;
				});
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	private static List<String> asList(String... params) {
		return Arrays.asList(params);
	}
}
