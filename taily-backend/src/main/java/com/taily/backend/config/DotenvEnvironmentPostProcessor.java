package com.taily.backend.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Optional local dev support: load key/value pairs from {@code taily-backend/.env} (or working dir
 * {@code .env}) into Spring properties. This keeps secrets out of git while making local setup easy.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
  private static final String PROPERTY_SOURCE_NAME = "tailyDotenv";

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
    // Do nothing if already provided by real environment/system properties.
    // We still load .env because it may provide other keys, but we don't want it to override
    // explicitly set env vars.
    Map<String, Object> dotenv = new LinkedHashMap<>();

    Path wdEnv = Path.of(".env");
    Path backendEnv = Path.of("taily-backend").resolve(".env");
    Path envFile = Files.exists(wdEnv) ? wdEnv : Files.exists(backendEnv) ? backendEnv : null;
    if (envFile == null) return;

    try {
      dotenv.putAll(parseEnvFile(envFile));
    } catch (Exception ignored) {
      // Keep graceful behavior: failure to read .env must not break the app.
      return;
    }

    // Add with low precedence so real env vars win.
    MapPropertySource ps = new MapPropertySource(PROPERTY_SOURCE_NAME, dotenv);
    if (!environment.getPropertySources().contains(PROPERTY_SOURCE_NAME)) {
      environment.getPropertySources().addLast(ps);
    }
  }

  private static Map<String, Object> parseEnvFile(Path file) throws IOException {
    Map<String, Object> out = new LinkedHashMap<>();
    try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
      String line;
      while ((line = r.readLine()) != null) {
        String t = line.trim();
        if (t.isEmpty() || t.startsWith("#")) continue;
        if (t.startsWith("export ")) t = t.substring("export ".length()).trim();
        int eq = t.indexOf('=');
        if (eq <= 0) continue;
        String key = t.substring(0, eq).trim();
        String value = t.substring(eq + 1).trim();
        value = stripOptionalQuotes(value);
        if (!key.isEmpty() && !out.containsKey(key)) {
          out.put(key, value);
        }
      }
    }
    return out;
  }

  private static String stripOptionalQuotes(String v) {
    if (v == null) return "";
    if (v.length() >= 2) {
      char a = v.charAt(0);
      char b = v.charAt(v.length() - 1);
      if ((a == '"' && b == '"') || (a == '\'' && b == '\'')) {
        return v.substring(1, v.length() - 1);
      }
    }
    return v;
  }

  @Override
  public int getOrder() {
    // Run early, but after system properties/env are already available.
    return Ordered.HIGHEST_PRECEDENCE + 20;
  }
}

