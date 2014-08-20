package org.openstreetmap.josm.plugins.ods.gui;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.openstreetmap.josm.plugins.ods.OdsModule;

public abstract class OdsAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private OdsModule module;
    
    public OdsAction(OdsModule module, String name, String description) {
        super(name);
        super.putValue("description", description);
        this.module = module;
    }

    public OdsAction(OdsModule module, String name, ImageIcon imageIcon) {
        super(name, imageIcon);
        this.module = module;
    }

    public OdsModule getModule() {
        return module;
    }
}
