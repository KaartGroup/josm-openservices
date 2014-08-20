package org.openstreetmap.josm.plugins.ods;

import com.google.inject.AbstractModule;

/**
 * Guice Module to configure the ODS Modules
 * 
 * @author Gertjan Idema <mail@gertjanidema.nl>
 *
 */
public class OdsModuleConfig extends AbstractModule {
    private OdsModulePlugin plugin;
    
    public OdsModuleConfig(OdsModulePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(OdsModulePlugin.class).toInstance(plugin);
    }
}
