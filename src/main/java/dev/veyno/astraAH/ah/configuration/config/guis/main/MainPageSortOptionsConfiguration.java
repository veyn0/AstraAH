package dev.veyno.astraAH.ah.configuration.config.guis.main;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.Configurable;
import net.kyori.adventure.text.Component;

public class MainPageSortOptionsConfiguration extends Configurable {

    private Component nameAZEnabled;
    private Component nameZAEnabled;
    private Component priceHLEnabled;
    private Component priceLHEnabled;
    private Component pricePerPieceHLEnabled;
    private Component pricePerPieceLHEnabled;

    private Component nameAZDisabled;
    private Component nameZADisabled;
    private Component priceHLDisabled;
    private Component priceLHDisabled;
    private Component pricePerPieceHLDisabled;
    private Component pricePerPieceLHDisabled;

    public MainPageSortOptionsConfiguration(String path, AstraAH plugin) {
        super(path, plugin);

        this.nameAZEnabled = getMessage("enabled.name_a_z");
        this.nameZAEnabled = getMessage("enabled.name_z_a");
        this.priceHLEnabled = getMessage("enabled.price_h_l");
        this.priceLHEnabled = getMessage("enabled.price_l_h");
        this.pricePerPieceHLEnabled = getMessage("enabled.price_per_piece_h_l");
        this.pricePerPieceLHEnabled = getMessage("enabled.price_per_piece_l_h");

        this.nameAZDisabled = getMessage("disabled.name_a_z");
        this.nameZADisabled = getMessage("disabled.name_z_a");
        this.priceHLDisabled = getMessage("disabled.price_h_l");
        this.priceLHDisabled = getMessage("disabled.price_l_h");
        this.pricePerPieceHLDisabled = getMessage("disabled.price_per_piece_h_l");
        this.pricePerPieceLHDisabled = getMessage("disabled.price_per_piece_l_h");
    }

    public Component getNameAZEnabled() {
        return nameAZEnabled;
    }

    public Component getNameZAEnabled() {
        return nameZAEnabled;
    }

    public Component getPriceHLEnabled() {
        return priceHLEnabled;
    }

    public Component getPriceLHEnabled() {
        return priceLHEnabled;
    }

    public Component getPricePerPieceHLEnabled() {
        return pricePerPieceHLEnabled;
    }

    public Component getPricePerPieceLHEnabled() {
        return pricePerPieceLHEnabled;
    }

    public Component getNameAZDisabled() {
        return nameAZDisabled;
    }

    public Component getNameZADisabled() {
        return nameZADisabled;
    }

    public Component getPriceHLDisabled() {
        return priceHLDisabled;
    }

    public Component getPriceLHDisabled() {
        return priceLHDisabled;
    }

    public Component getPricePerPieceHLDisabled() {
        return pricePerPieceHLDisabled;
    }

    public Component getPricePerPieceLHDisabled() {
        return pricePerPieceLHDisabled;
    }
}