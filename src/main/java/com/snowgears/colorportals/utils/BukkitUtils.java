package com.snowgears.colorportals.utils;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Wool;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic utilities I use for a lot of my plugins that I put into one place.
 * Feel free to use this when creating any of your own plugins.
 * <p>
 * Created by SnowGears (Tanner Embry)
 */
public class BukkitUtils {

    //takes two locations, returns a blockface and a number

    /**
     * Takes two locations and calculates all cardinal distances
     * Return:
     * - HashMap<BlockFace, Integer>: cardinal direction, distance in that direction
     */
    public static Map<BlockFace, Integer> getCardinalDistances(Location startLocation, Location endLocation) {
        Map<BlockFace, Integer> cardinalDistances = new HashMap<>();
        int northSouth = startLocation.getBlockZ() - endLocation.getBlockZ();
        if (northSouth >= 0)
            cardinalDistances.put(BlockFace.NORTH, Math.abs(northSouth));
        else
            cardinalDistances.put(BlockFace.SOUTH, Math.abs(northSouth));
        int eastWest = startLocation.getBlockX() - endLocation.getBlockX();
        if (eastWest <= 0)
            cardinalDistances.put(BlockFace.EAST, Math.abs(eastWest));
        else
            cardinalDistances.put(BlockFace.WEST, Math.abs(eastWest));
        int upDown = startLocation.getBlockY() - endLocation.getBlockY();
        if (upDown <= 0)
            cardinalDistances.put(BlockFace.UP, Math.abs(upDown));
        else
            cardinalDistances.put(BlockFace.DOWN, Math.abs(upDown));
        return cardinalDistances;
    }

    /**
     * Converts a BlockFace direction to a byte
     * Return:
     * - byte: the basic data of the BlockFace direction provided
     */
    public byte determineDataOfDirection(BlockFace bf) {
        if (bf.equals(BlockFace.NORTH))
            return 2;
        if (bf.equals(BlockFace.SOUTH))
            return 5;
        if (bf.equals(BlockFace.WEST))
            return 3;
        return ((byte) (!bf.equals(BlockFace.EAST) ? 0 : 4));
    }

    /**
     * Converts a BlockFace direction to a yaw (float) value
     * Return:
     * - float: the yaw value of the BlockFace direction provided
     */
    public float faceToYaw(BlockFace bf) {
        if (bf.equals(BlockFace.NORTH))
            return 0F;
        else if (bf.equals(BlockFace.EAST))
            return 90F;
        else if (bf.equals(BlockFace.SOUTH))
            return 180F;
        else if (bf.equals(BlockFace.WEST))
            return 270F;
        return 0F;
    }

    /**
     * Checks if a String is an Integer
     * Return:
     * - true: String is an Integer
     * - false: String is not an Integer
     */
    public boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Gets the color from a (wool) block
     * Return:
     * - DyeColor: the color of the (wool) block
     */
    public DyeColor getWoolColor(Block block) {
        if (block.getType() != Material.WOOL)
            return null;

        Wool wool = (Wool) block.getState().getData();
        return wool.getColor();
    }

}


