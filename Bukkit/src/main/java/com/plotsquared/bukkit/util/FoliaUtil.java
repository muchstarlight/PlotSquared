/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.util;

/**
 * Utility for detecting whether the server is running Folia.
 * <p>
 * Detection is based on the presence of the {@code RegionizedServer} class,
 * as recommended by the official Folia documentation. All other APIs used
 * by this plugin (schedulers, async chunk loading) exist in the Paper API
 * and behave correctly on both Paper and Folia.
 */
public final class FoliaUtil {

    private static final boolean FOLIA = hasClass("io.papermc.paper.threadedregions.RegionizedServer");

    public static boolean isFolia() {
        return FOLIA;
    }

    private static boolean hasClass(final String className) {
        try {
            Class.forName(className);
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }

    private FoliaUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

}
