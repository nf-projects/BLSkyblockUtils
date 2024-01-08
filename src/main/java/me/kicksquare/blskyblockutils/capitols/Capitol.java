package me.kicksquare.blskyblockutils.capitols;

import java.util.Arrays;
import java.util.List;

public enum Capitol {
    NORTH_AMERICA("capitol_northamerica"),
    SOUTH_AMERICA("capitol_southamerica"),
    EUROPE("capitol_europe"),
    AFRICA("capitol_africa"),
    ASIA("capitol_asia"),
    AUSTRALIA("capitol_australia"),
    ANTARCTICA("capitol_antarctica");

    private final String regionName;

    Capitol(String regionName) {
        this.regionName = regionName;
    }

    public static Capitol get(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        // Replace underscores and convert to uppercase for uniformity
        String processedName = name.replace("_", "").toUpperCase();

        // Check against enum names
        for (Capitol capitol : Capitol.values()) {
            if (capitol.name().replace("_", "").equalsIgnoreCase(processedName)) {
                return capitol;
            }

            // Check against region names
            if (capitol.getRegionName().replace("_", "").equalsIgnoreCase(processedName)) {
                return capitol;
            }
        }

        // Return null if no match is found
        return null;
    }


    public String getRegionName() {
        return regionName;
    }

    public String getBeaconRegionName(int num) {
        return regionName + "_beacon_" + num;
    }

    public static List<Capitol> getAllCapitols() {
        return Arrays.asList(
                NORTH_AMERICA,
                SOUTH_AMERICA,
                EUROPE,
                AFRICA,
                ASIA,
                AUSTRALIA,
                ANTARCTICA
        );
    }

    public static List<String> getAllBeaconRegionNames() {
        // gets all beacon region names of all capitols
        return Arrays.asList(
                NORTH_AMERICA.getBeaconRegionName(1),
                NORTH_AMERICA.getBeaconRegionName(2),
                NORTH_AMERICA.getBeaconRegionName(3),
                NORTH_AMERICA.getBeaconRegionName(4),
                SOUTH_AMERICA.getBeaconRegionName(1),
                SOUTH_AMERICA.getBeaconRegionName(2),
                SOUTH_AMERICA.getBeaconRegionName(3),
                SOUTH_AMERICA.getBeaconRegionName(4),
                EUROPE.getBeaconRegionName(1),
                EUROPE.getBeaconRegionName(2),
                EUROPE.getBeaconRegionName(3),
                EUROPE.getBeaconRegionName(4),
                AFRICA.getBeaconRegionName(1),
                AFRICA.getBeaconRegionName(2),
                AFRICA.getBeaconRegionName(3),
                AFRICA.getBeaconRegionName(4),
                ASIA.getBeaconRegionName(1),
                ASIA.getBeaconRegionName(2),
                ASIA.getBeaconRegionName(3),
                ASIA.getBeaconRegionName(4),
                AUSTRALIA.getBeaconRegionName(1),
                AUSTRALIA.getBeaconRegionName(2),
                AUSTRALIA.getBeaconRegionName(3),
                AUSTRALIA.getBeaconRegionName(4),
                ANTARCTICA.getBeaconRegionName(1),
                ANTARCTICA.getBeaconRegionName(2),
                ANTARCTICA.getBeaconRegionName(3),
                ANTARCTICA.getBeaconRegionName(4)
        );
    }
}
