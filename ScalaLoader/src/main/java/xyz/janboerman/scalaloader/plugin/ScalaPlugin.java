package xyz.janboerman.scalaloader.plugin;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ScalaPlugin implements Plugin, Comparable<Plugin> {

    private final ScalaPluginDescription description;
    private PluginDescriptionFile lazyDescription;

    private Server server;
    private ScalaPluginLoader pluginLoader;
    private File dataFolder;
    private File file;
    private ScalaPluginClassLoader classLoader;
    private boolean naggable;
    private PluginLogger logger;

    //TODO lazily assign config stuff. do I want to inject the config file similarly to the plugin.yml?
    private File configFile;
    private FileConfiguration config;

    private boolean enabled;

    protected ScalaPlugin(ScalaPluginDescription pluginDescription) {
        this.description = pluginDescription;
    }

    //intentionally package protected
    final void init(ScalaPluginLoader pluginLoader, Server server, File dataFolder, File file, ScalaPluginClassLoader classLoader) {
        //at this point, the description is set already :)
        this.server = server;
        this.pluginLoader = pluginLoader;
        this.file = file;
        this.dataFolder = dataFolder;
        this.classLoader = classLoader;
        this.naggable = true;
        this.logger = new PluginLogger(this);
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    //TODO protected PluginCommand getCommand(String name) ??

    protected ScalaPluginClassLoader getClassLoader() {
        return classLoader;
    }

    public final String getScalaVersion() {
        return getClassLoader().getScalaVersion();
    }

    public String getName() {
        return description.getName();
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    ScalaPluginDescription getScalaDescription() {
        return description;
    }

    @Override
    public PluginDescriptionFile getDescription() {
        return lazyDescription == null ? lazyDescription = description.toPluginDescriptionFile() : lazyDescription;
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    @Override
    public InputStream getResource(String filename) {
        Objects.requireNonNull(filename, "Filename cannot be null");

        URL url = getClassLoader().getResource(filename);
        if (url == null) return null;

        try {
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void saveConfig() {
        try {
            getConfig().save(configFile);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not save config to " + configFile, e);
        }
    }

    @Override
    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            saveResource("config.yml", false);
        }
    }

    @Override
    public void saveResource(String resourcePath, boolean replace) {
        //copy from the jar into this plugin's directory
    }

    @Override
    public void reloadConfig() {

    }

    @Override
    public ScalaPluginLoader getPluginLoader() {
        return pluginLoader;
    }

    @Override
    public Server getServer() {
        return server;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
    }

    @Override
    public boolean isNaggable() {
        return naggable;
    }

    @Override
    public void setNaggable(boolean canNag) {
        this.naggable = canNag;
    }


    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return null;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    @Override
    public int compareTo(Plugin other) {
        if (other instanceof JavaPlugin) return 1; //java plugins are smaller.

        return getName().compareTo(other.getName());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (!(other instanceof Plugin)) return false;
        Plugin that = (Plugin) other;
        return Objects.equals(this.getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }
}
