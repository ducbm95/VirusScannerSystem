/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ducbm.commonutils;

import java.io.File;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author ducbm
 */
public class AppConfiguration {
    
    private static Configuration configuration;
    
    public static Configuration getConfigInstance() {
        if (configuration == null) {
            Configurations configurations = new Configurations();
            try {
                configuration = configurations.properties(new File(Constants.CONFIGURATION_FILE));
            } catch (ConfigurationException ex) {
                LogManager.getLogger().error(ex);
            }
        }
        return configuration;
    }
    
}
