package dev.veyno.astraAH.econ;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.data.dto.Listing;
import dev.veyno.astraAH.util.IDLocks;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TransactionController {

    private final Object lock = new Object();
    private final AstraAH plugin;

    public TransactionController(AstraAH plugin) {
        this.plugin = plugin;
    }

    public void finalizePurchase(Listing l, Player p){
        synchronized (IDLocks.getLock(l.getListingId())){
            synchronized (IDLocks.getLock(p.getUniqueId())){
                synchronized (lock){

                    EconomyProvider econ = plugin.getEconomy();

                    //TODO: check for empty inventory, add storage for purchased listings per player, where they can collect them within the next X days.

                    if(!econ.withdraw(p.getUniqueId(), l.getPrice())){
                        p.sendMessage(Component.text("[PLACEHOLDER] insufficient Balance"));
                        return;
                    }

                    econ.add(l.getSellerId(), l.getPrice());

                    p.getInventory().addItem(l.getContent());

                    p.sendMessage(Component.text("[PLACEHOLDER] purchased Listing."));

                }
            }
        }
    }


}
