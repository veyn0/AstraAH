package dev.veyno.astraAH.ah.configuration.config.guis;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.Configurable;

public class CreateListingGuiConfiguration2 extends Configurable {

    private String line1;
    private String line2;
    private String line3;

    public CreateListingGuiConfiguration2(String path, AstraAH plugin) {
        super(path, plugin);

        this.line1 = plugin.getConfig().getString(path+".lines.1");
        this.line2 = plugin.getConfig().getString(path+".lines.2");
        this.line3 = plugin.getConfig().getString(path+".lines.3");

    }




}
