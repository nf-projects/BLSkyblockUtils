package me.kicksquare.blskyblockutils.capitols;

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

    public String getRegionName() {
        return regionName;
    }

    public String getBeaconRegionName() {
        return regionName + "_beacon";
    }


}
