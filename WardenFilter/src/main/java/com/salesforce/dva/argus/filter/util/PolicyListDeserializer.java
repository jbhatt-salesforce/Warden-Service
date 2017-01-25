package com.salesforce.dva.argus.filter.util;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.salesforce.dva.warden.dto.Policy;
import com.salesforce.dva.warden.dto.Policy.TriggerType;
import com.salesforce.dva.warden.dto.SuspensionLevel;
import com.salesforce.dva.warden.dto.Policy.Aggregator;

/**
 * policy defs deserializer
 */
public class PolicyListDeserializer extends JsonDeserializer<ListMultimap> {

	@Override
	public ListMultimap<String, Policy> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

		ListMultimap<String, Policy> policyDef = ArrayListMultimap.create();

		JsonNode arraynode = jp.getCodec().readTree(jp);
		Iterator<JsonNode> nodes = arraynode.elements();

		while (nodes.hasNext()) {
			JsonNode node = nodes.next();
			PolicyConfig policyConfig = _deserializePolicyConfig(node);
			String key = _deserializeKey(node);
			if (policyConfig != null && key != null) {

				policyDef.put(key, policyConfig);
			}
		}

		return policyDef;
	}

	private static PolicyConfig _deserializePolicyConfig(JsonNode node) throws IOException {
		ObjectMapper mapper = new ObjectMapper();

		String url = node.get("url").asText();
		String verb = node.get("verb").asText();
		String service = node.get("service").asText();
		String name = node.get("name").asText();
		List<String> owners = mapper.readValue(node.get("owners").traverse(), new TypeReference<ArrayList<String>>() {
		});
		List<String> users = mapper.readValue(node.get("users").traverse(), new TypeReference<ArrayList<String>>() {
		});
		String subSystem = node.get("subSystem").asText();
		TriggerType triggerType = TriggerType.fromString(node.get("triggerType").asText());
		Aggregator aggregator = Aggregator.fromString(node.get("aggregator").asText());
		List<Double> threshold = mapper.readValue(node.get("threshold").traverse(),
				new TypeReference<ArrayList<Double>>() {
				});
		String timeUnit = node.get("timeUnit").asText();
		Double defaultValue = node.get("defaultValue").asDouble();
		String cronEntry = node.get("cronEntry").asText();
		List<SuspensionLevel> levels = mapper.readValue(node.get("levels").traverse(),
				new TypeReference<ArrayList<SuspensionLevel>>() {
				});

		PolicyConfig policyConfig = new PolicyConfig();

		policyConfig.setService(service);
		policyConfig.setName(name);
		policyConfig.setOwners(owners);
		policyConfig.setUsers(users);
		policyConfig.setSubSystem(subSystem);
		policyConfig.setTriggerType(triggerType);
		policyConfig.setAggregator(aggregator);
		policyConfig.setThresholds(threshold);
		policyConfig.setTimeUnit(timeUnit);
		policyConfig.setDefaultValue(defaultValue);
		policyConfig.setCronEntry(cronEntry);
		policyConfig.setUrl(url);
		policyConfig.setVerb(verb);
		policyConfig.setSuspensionLevels(levels);

		return policyConfig;
	}

	private static String _deserializeKey(JsonNode node) throws IOException {
		String url = node.get("url").asText();
		String verb = node.get("verb").asText();
		String format = "{0}:{1}";

		return MessageFormat.format(format, Arrays.asList(url, verb));
	}
}
