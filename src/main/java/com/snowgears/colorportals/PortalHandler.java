package com.snowgears.colorportals;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * This class handles all of the basic functions in the managing of Portals.
 * - Managing HashMap of Portals
 * - Adding Portals
 * - Removing Portals
 * - Saving data file
 * - Loading data file
 */
public class PortalHandler {

    public ColorPortals plugin;

    private Map<Location, Portal> allPortals = new HashMap<>();

    public PortalHandler(ColorPortals instance) {
        plugin = instance;
    }

    public Portal getPortal(Location loc) {
        return allPortals.get(loc);
    }

    public void registerPortal(Portal portal) {
        allPortals.put(portal.getSignLocation(), portal);

        List<Portal> portalFamily = this.getPortalFamily(portal);
        if (portalFamily.size() == 1) {
            portal.setLinkedPortal(null);
            return;
        }
        Portal lastPortal = portalFamily.get(portalFamily.size() - 2);
        lastPortal.setLinkedPortal(portal);
        portal.setLinkedPortal(portalFamily.get(0));
    }

    //this method should only be called from the portal class when removing portals
    public void deregisterPortal(Portal portal) {
        if (allPortals.containsKey(portal.getSignLocation())) {
            allPortals.remove(portal.getSignLocation());
        }
    }

    public Collection<Portal> getAllPortals() {
        return allPortals.values();
    }

    public Portal getPortalByFrameLocation(Location location) {
        Location loc;
        for (int x = -1; x < 2; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = -1; z < 2; z++) {
                    loc = location.clone().add(x, y, z);
                    if (getPortal(loc) != null) {
                        Portal portal = getPortal(loc);
                        if (portal.getOccupiedLocations().contains(location))
                            return portal;
                    }
                }
            }
        }
        return null;
    }

    //TODO bug here. Will always prioritize finding north sign first and would return null if multiple signs on portal
    public Portal getPortalByKeyBlock(Block portalKeyBlock) {
        if (portalKeyBlock.getRelative(BlockFace.NORTH).getType() == Material.WALL_SIGN) {
            return plugin.getPortalHandler().getPortal(portalKeyBlock.getRelative(BlockFace.NORTH).getLocation());
        } else if (portalKeyBlock.getRelative(BlockFace.EAST).getType() == Material.WALL_SIGN) {
            return plugin.getPortalHandler().getPortal(portalKeyBlock.getRelative(BlockFace.EAST).getLocation());
        } else if (portalKeyBlock.getRelative(BlockFace.SOUTH).getType() == Material.WALL_SIGN) {
            return plugin.getPortalHandler().getPortal(portalKeyBlock.getRelative(BlockFace.SOUTH).getLocation());
        } else if (portalKeyBlock.getRelative(BlockFace.WEST).getType() == Material.WALL_SIGN) {
            return plugin.getPortalHandler().getPortal(portalKeyBlock.getRelative(BlockFace.WEST).getLocation());
        }
        return null;
    }

    public int getNumberOfPortals() {
        return allPortals.size();
    }

    /**
     * Finds all portals with the same color and channel as the portal provided
     * Return:
     * - List of all portals in the family (matching color and channel)
     */
    public List<Portal> getPortalFamily(Portal portal) {
        List<Portal> portalFamily = new ArrayList<>();
        for (Portal checkedPortal : plugin.getPortalHandler().getAllPortals()) {
            if (checkedPortal.getChannel() == portal.getChannel() && checkedPortal.getColor().equals(portal.getColor())) {
                portalFamily.add(checkedPortal);
            }
        }
        Collections.sort(portalFamily);
        return portalFamily;
    }

    /**
     * Finds all portals with the same color and channel as the ones provided
     * Return:
     * - List of all portals in the family (matching color and channel)
     */
    public List<Portal> getPortalFamily(Integer channel, DyeColor color) {
        List<Portal> portalFamily = new ArrayList<>();
        for (Portal checkedPortal : plugin.getPortalHandler().getAllPortals()) {
            if (checkedPortal.getChannel() == channel && checkedPortal.getColor().equals(color)) {
                portalFamily.add(checkedPortal);
            }
        }
        Collections.sort(portalFamily);
        return portalFamily;
    }

    private List<Portal> orderedPortalList() {
        List<Portal> list = new ArrayList<>(allPortals.values());
        Collections.sort(list);
        return list;
    }

    public void savePortals() {
        Path fileDirectory = plugin.getDataFolder().toPath().resolve("Data");
        if (Files.notExists(fileDirectory)) {
            try {
                Files.createDirectory(fileDirectory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Path portalFile = fileDirectory.resolve("portals.yml");
        if (Files.notExists(portalFile)) { // file doesn't exist
            try {
                Files.createFile(portalFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else { //does exist, clear it for future saving
            try {
                Files.newBufferedWriter(portalFile, StandardCharsets.UTF_8).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(portalFile.toFile());
        List<Portal> portalList = orderedPortalList();

        for (Portal portal : portalList) {
            config.set("portals." + portal.getColor().toString() + "." + portal.getChannel() + "-" + portal.getNode() + ".name", portal.getName());
            config.set("portals." + portal.getColor().toString() + "." + portal.getChannel() + "-" + portal.getNode() + ".location", locationToString(portal.getSignLocation()));
            config.set("portals." + portal.getColor().toString() + "." + portal.getChannel() + "-" + portal.getNode() + ".creator", portal.getCreator().toString());
        }

        try {
            config.save(portalFile.toFile());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void loadPortals() {
        Path fileDirectory = plugin.getDataFolder().toPath().resolve("Data");
        if (Files.notExists(fileDirectory)) {
            return;
        }
        Path portalFile = fileDirectory.resolve("portals.yml");
        if (Files.notExists(portalFile)) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(portalFile.toFile());
        loadPortalsFromConfig(config);
    }

    private void loadPortalsFromConfig(YamlConfiguration config) {
        if (config.getConfigurationSection("portals") == null) {
            return;
        }
        Set<String> allPortalColors = config.getConfigurationSection("portals").getKeys(false);

        List<Portal> portalFamily = new ArrayList<>();

        //  for (String portalColor : allPortalColors) {
        for (Iterator<String> colorIterator = allPortalColors.iterator(); colorIterator.hasNext(); ) {
            String portalColor = colorIterator.next();
            Set<String> allPortalChannels = config.getConfigurationSection("portals." + portalColor).getKeys(false);
            portalFamily.clear();

            int previousChannel = 0;
            if (allPortalChannels.iterator().hasNext()) {
                String stringChannel = allPortalChannels.iterator().next();
                String[] split = stringChannel.split("-");
                previousChannel = Integer.parseInt(split[0]);
            }

            //  for (String portalChannel : allPortalChannels) {
            for (Iterator<String> channelIterator = allPortalChannels.iterator(); channelIterator.hasNext(); ) {
                String portalChannel = channelIterator.next();

                Location signLocation = locationFromString(config.getString("portals." + portalColor + "." + portalChannel + ".location"));
                Block signBlock = signLocation.getBlock();

                if (signBlock.getType() == Material.WALL_SIGN) {
                    DyeColor color = DyeColor.valueOf(portalColor);

                    String[] split = portalChannel.split("-");
                    int channel = Integer.parseInt(split[0]);
                    int node = Integer.parseInt(split[1]);

                    String name = config.getString("portals." + portalColor + "." + portalChannel + ".name");

                    String creatorString = config.getString("portals." + portalColor + "." + portalChannel + ".creator");
                    UUID creator = UUID.fromString(creatorString);

                    Portal portal = new Portal(creator, name, color, channel, node, signLocation);

                    //previous portal was the last portal in the family (same color, different channel)
                    //portal working with now is the first portal of the new family
                    if (previousChannel != channel) {
                        previousChannel = channel;
                        //if family only has one portal, set link to null
                        if (portalFamily.size() == 1) {
                            portalFamily.get(0).setLinkedPortal(null);
                        }
                        //if family has more than 1 portal, link the last portal to the first portal
                        else {
                            portalFamily.get(portalFamily.size() - 1).setLinkedPortal(portalFamily.get(0));
                        }

                        //register portalFamily before resetting for the next family
                        for (Portal p : portalFamily) {
                            this.registerPortal(p);
                        }

                        //reset for next family
                        portalFamily.clear();
                        portalFamily.add(portal);
                    }
                    //portal working with now is still a member of the current family
                    else {
                        portalFamily.add(portal);

                        if (portalFamily.size() > 1) {
                            portalFamily.get(portalFamily.size() - 2).setLinkedPortal(portalFamily.get(portalFamily.size() - 1));
                        }

                        //if there are no more channels and no more colors to go through
                        if (!(channelIterator.hasNext() && colorIterator.hasNext())) {
                            //load the last portalFamily currently in memory to the class level
                            for (Portal p : portalFamily) {
                                this.registerPortal(p);
                            }
                        }
                    }
                }
            }
        }
    }

    private String locationToString(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    private Location locationFromString(String loc) {
        String[] parts = loc.split(",");
        return new Location(plugin.getServer().getWorld(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
    }

}
