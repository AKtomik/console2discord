package io.itch.aktomik.console2Discord.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonBuilder {
    private final JsonObject json = new JsonObject();

    public JsonObject json() {
        return json;
    }

    public String parse() {
        return json.toString();
    }

    public JsonBuilder add(String key, String value) {
        json.addProperty(key, value);
        return this;
    }

    public JsonBuilder add(String key, int value) {
        json.addProperty(key, value);
        return this;
    }

    public JsonBuilder add(String key, JsonBuilder value) {
        json.add(key, value.json);
        return this;
    }

    public JsonBuilder add(String key, Iterable<JsonBuilder> values) {
        JsonArray list = new JsonArray();
        values.forEach(v -> list.add(v.json));
        json.add(key, list);
        return this;
    }
}