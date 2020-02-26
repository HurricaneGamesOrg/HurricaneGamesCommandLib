package org.hurricanegames.commandlib.configurations;

import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;

public abstract class SimpleConfiguration extends BaseConfiguration {

	protected void load() {
		load(YamlConfiguration.loadConfiguration(getStorageFile()));
	}

	public void save() {
		YamlConfiguration config = new YamlConfiguration();
		save(config);
		ConfigurationUtils.safeSave(config, getStorageFile());
	}

	public void reload() {
		load();
		save();
	}

	protected abstract File getStorageFile();

}
