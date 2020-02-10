package org.hurricanegames.commandlib.configurations;

import java.io.File;

public abstract class SimpleConfiguration extends BaseConfiguration {

	@SuppressWarnings("unchecked")
	protected void load() {
		ConfigurationUtils.load(this, getStorageFile(), fields);
	}

	@SuppressWarnings("unchecked")
	public void save() {
		ConfigurationUtils.save(this, getStorageFile(), fields);
	}

	public void reload() {
		load();
		save();
	}

	protected abstract File getStorageFile();

}
