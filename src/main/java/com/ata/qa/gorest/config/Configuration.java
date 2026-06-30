package com.ata.qa.gorest.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralised, read-only configuration for the suite.
 *
 * <p>Every value is resolved with the same precedence, which keeps the suite friendly both
 * locally and in CI:</p>
 * <ol>
 *   <li>JVM system property  &nbsp;(e.g. {@code -DGOREST_API_TOKEN=...})</li>
 *   <li>Environment variable &nbsp;(e.g. {@code export GOREST_API_TOKEN=...}) - typical for CI secrets</li>
 *   <li>Local {@code .env} file at the project root - typical for local development</li>
 * </ol>
 *
 * <p>The API token is never hard-coded and the {@code .env} file is git-ignored, so credentials
 * never reach the repository.</p>
 */
public final class Configuration {

    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    private static final String DEFAULT_BASE_URI = "https://gorest.co.in";
    private static final String DEFAULT_BASE_PATH = "/public/v2";

    private static volatile Configuration instance;

    private final Dotenv dotenv;
    private final String baseUri;
    private final String basePath;
    private final String token;

    private Configuration() {
        // ignoreIfMissing -> the suite still runs in CI where there is no .env file on disk.
        this.dotenv = Dotenv.configure().ignoreIfMissing().load();
        this.baseUri = resolveOrDefault("GOREST_BASE_URI", DEFAULT_BASE_URI);
        this.basePath = resolveOrDefault("GOREST_BASE_PATH", DEFAULT_BASE_PATH);
        this.token = resolve("GOREST_API_TOKEN");

        log.info("Configuration loaded -> baseUri='{}', basePath='{}', tokenPresent={}",
                baseUri, basePath, hasToken());
    }

    public static Configuration getInstance() {
        if (instance == null) {
            synchronized (Configuration.class) {
                if (instance == null) {
                    instance = new Configuration();
                }
            }
        }
        return instance;
    }

    private String resolveOrDefault(String key, String defaultValue) {
        String value = resolve(key);
        return value != null ? value : defaultValue;
    }

    /** Returns the first non-blank value found across system property, env var, then .env. */
    private String resolve(String key) {
        String sysProp = System.getProperty(key);
        if (isPresent(sysProp)) {
            return sysProp.trim();
        }
        String envVar = System.getenv(key);
        if (isPresent(envVar)) {
            return envVar.trim();
        }
        String dotEnv = dotenv.get(key);
        if (isPresent(dotEnv)) {
            return dotEnv.trim();
        }
        return null;
    }

    private boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    public String getBaseUri() {
        return baseUri;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getToken() {
        return token;
    }

    /** True when an API token is available for authenticated (POST/PUT/DELETE) operations. */
    public boolean hasToken() {
        return isPresent(token);
    }
}
