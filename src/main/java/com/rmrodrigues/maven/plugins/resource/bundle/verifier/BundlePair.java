package com.rmrodrigues.maven.plugins.resource.bundle.verifier;

import java.util.Properties;

/**
 * The Class BundlePair.
 */
public class BundlePair {
    
    /** The file path. */
    private String filePath;
    
    /** The properties. */
    private Properties properties;

    /**
     * Instantiates a new bundle pair.
     *
     * @param filePath the file path
     * @param properties the properties
     */
    public BundlePair(String filePath, Properties properties) {
        super();
        this.filePath = filePath;
        this.properties = properties;
    }

    /**
     * Gets the file path.
     *
     * @return the file path
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Gets the properties.
     *
     * @return the properties
     */
    public Properties getProperties() {
        return properties;
    }

    

}
