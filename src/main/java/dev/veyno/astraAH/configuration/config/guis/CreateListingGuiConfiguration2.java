package dev.veyno.astraAH.configuration.config.guis;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.configuration.Configurable;

public class CreateListingGuiConfiguration2 extends Configurable {

    private final String line1;
    private final String line2;
    private final String line3;

    public CreateListingGuiConfiguration2(String path, AstraAH plugin) {
        super(path, plugin);

        this.line1 = getString("lines.1", "");
        this.line2 = getString("lines.2", "");
        this.line3 = getString("lines.3", "");

    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public String getLine3() {
        return line3;
    }
}
