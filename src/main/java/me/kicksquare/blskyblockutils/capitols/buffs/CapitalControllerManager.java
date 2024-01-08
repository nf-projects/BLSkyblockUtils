package me.kicksquare.blskyblockutils.capitols.buffs;

import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import me.kicksquare.blskyblockutils.capitols.Capitol;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CapitalControllerManager {
    private final BLSkyblockUtils plugin;

    private String northAmericaCapitolController;
    private String southAmericaCapitolController;
    private String europeCapitolController;
    private String africaCapitolController;
    private String asiaCapitolController;
    private String australiaCapitolController;
    private String antarcticaCapitolController;

    public CapitalControllerManager(BLSkyblockUtils plugin) {
        this.plugin = plugin;
    }

    public void loadBuffsFromConfig() {
        northAmericaCapitolController = plugin.getMainConfig().getString("capitol_northamerica_controller");
        southAmericaCapitolController = plugin.getMainConfig().getString("capitol_southamerica_controller");
        europeCapitolController = plugin.getMainConfig().getString("capitol_europe_controller");
        africaCapitolController = plugin.getMainConfig().getString("capitol_africa_controller");
        asiaCapitolController = plugin.getMainConfig().getString("capitol_asia_controller");
        australiaCapitolController = plugin.getMainConfig().getString("capitol_australia_controller");
        antarcticaCapitolController = plugin.getMainConfig().getString("capitol_antarctica_controller");
    }

    public String getCapitolController(Capitol capitol) {
        switch (capitol) {
            case NORTH_AMERICA:
                return northAmericaCapitolController;
            case SOUTH_AMERICA:
                return southAmericaCapitolController;
            case EUROPE:
                return europeCapitolController;
            case AFRICA:
                return africaCapitolController;
            case ASIA:
                return asiaCapitolController;
            case AUSTRALIA:
                return australiaCapitolController;
            case ANTARCTICA:
                return antarcticaCapitolController;

            default:
                return null;
        }
    }

    public void setCapitolController(Capitol capitol, String controller) {
        switch (capitol) {
            case NORTH_AMERICA:
                northAmericaCapitolController = controller;
                plugin.getMainConfig().set("capitol_northamerica_controller", controller);
                break;
            case SOUTH_AMERICA:
                southAmericaCapitolController = controller;
                plugin.getMainConfig().set("capitol_southamerica_controller", controller);
                break;
            case EUROPE:
                europeCapitolController = controller;
                plugin.getMainConfig().set("capitol_europe_controller", controller);
                break;
            case AFRICA:
                africaCapitolController = controller;
                plugin.getMainConfig().set("capitol_africa_controller", controller);
                break;
            case ASIA:
                asiaCapitolController = controller;
                plugin.getMainConfig().set("capitol_asia_controller", controller);
                break;
            case AUSTRALIA:
                australiaCapitolController = controller;
                plugin.getMainConfig().set("capitol_australia_controller", controller);
                break;
            case ANTARCTICA:
                antarcticaCapitolController = controller;
                plugin.getMainConfig().set("capitol_antarctica_controller", controller);
                break;
        }
    }

    public @Nullable Capitol getCapitolFromController(String controllerNationName) {
        if (controllerNationName.equals(northAmericaCapitolController)) {
            return Capitol.NORTH_AMERICA;
        } else if (controllerNationName.equals(southAmericaCapitolController)) {
            return Capitol.SOUTH_AMERICA;
        } else if (controllerNationName.equals(europeCapitolController)) {
            return Capitol.EUROPE;
        } else if (controllerNationName.equals(africaCapitolController)) {
            return Capitol.AFRICA;
        } else if (controllerNationName.equals(asiaCapitolController)) {
            return Capitol.ASIA;
        } else if (controllerNationName.equals(australiaCapitolController)) {
            return Capitol.AUSTRALIA;
        } else if (controllerNationName.equals(antarcticaCapitolController)) {
            return Capitol.ANTARCTICA;
        } else {
            return null;
        }
    }

    public @Nullable Capitol[] getCapitolsFromController(String controllerNationName) {
        List<Capitol> capitols = new ArrayList<>();

        if (controllerNationName.equals(northAmericaCapitolController)) {
            capitols.add(Capitol.NORTH_AMERICA);
        }

        if (controllerNationName.equals(southAmericaCapitolController)) {
            capitols.add(Capitol.SOUTH_AMERICA);
        }

        if (controllerNationName.equals(europeCapitolController)) {
            capitols.add(Capitol.EUROPE);
        }

        if (controllerNationName.equals(africaCapitolController)) {
            capitols.add(Capitol.AFRICA);
        }

        if (controllerNationName.equals(asiaCapitolController)) {
            capitols.add(Capitol.ASIA);
        }

        if (controllerNationName.equals(australiaCapitolController)) {
            capitols.add(Capitol.AUSTRALIA);
        }

        if (controllerNationName.equals(antarcticaCapitolController)) {
            capitols.add(Capitol.ANTARCTICA);
        }

        if (capitols.isEmpty()) {
            return null;
        } else {
            return capitols.toArray(new Capitol[0]);
        }
    }
}
