package configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.hurricanegames.commandlib.configurations.BaseConfiguration;
import org.hurricanegames.commandlib.configurations.ConfigurationUtils.SimpleListConfigurationField;
import org.hurricanegames.commandlib.configurations.ConfigurationUtils.SimpleMapConfigurationField;
import org.hurricanegames.commandlib.configurations.ConfigurationUtils.SimpleSetConfigurationField;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConfigurationTest {

	protected static class TestConfiguration extends BaseConfiguration {

		@ConfigurationFieldDefinition
		public String string = "test_string";

		@ConfigurationFieldDefinition
		public Integer integer = 1337;

		@ConfigurationFieldDefinition(fieldType = SimpleListConfigurationField.class)
		public List<String> list = Arrays.asList("string1", "string2");

		@ConfigurationFieldDefinition(fieldType = SimpleSetConfigurationField.class)
		public Set<Number> set = new HashSet<>(Arrays.asList(Integer.valueOf(1337), Integer.valueOf(1338)));

		@ConfigurationFieldDefinition(fieldType = SimpleMapConfigurationField.class)
		public Map<String, String> map = new HashMap<>();
		{
			map.put("key1", "value1");
			map.put("key2", "value2");
		}

		@Override
		public void load(ConfigurationSection section) {
			super.load(section);
		}

		@Override
		public void save(ConfigurationSection section) {
			super.save(section);
		}

	}

	@Test
	public void test() {
		TestConfiguration testconfiguration = new TestConfiguration();
		MemoryConfiguration memoryconfiguration = new MemoryConfiguration();
		testconfiguration.save(memoryconfiguration);

		Assertions.assertEquals(testconfiguration.string, memoryconfiguration.getString("string"));
		Assertions.assertEquals(testconfiguration.integer.intValue(), memoryconfiguration.getInt("integer"));
		Assertions.assertEquals(testconfiguration.list, memoryconfiguration.getStringList("list"));
		Assertions.assertEquals(testconfiguration.set, new HashSet<>(memoryconfiguration.getList("set")));

		{
			Map<String, String> map = new HashMap<>();
			ConfigurationSection mapSection = memoryconfiguration.getConfigurationSection("map");
			for (String key : mapSection.getKeys(false)) {
				map.put(key, mapSection.getString(key));
			}
			Assertions.assertEquals(testconfiguration.map, map);
		}
	}

}
