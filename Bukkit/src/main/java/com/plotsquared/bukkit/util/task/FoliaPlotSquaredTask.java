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
package com.plotsquared.bukkit.util.task;

import com.plotsquared.core.util.task.PlotSquaredTask;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * {@link PlotSquaredTask} implementation backed by a Paper {@link ScheduledTask}.
 * Works on both Paper (global scheduler = main thread) and Folia (regionized).
 */
public final class FoliaPlotSquaredTask implements PlotSquaredTask {

    @NonNull
    private final Runnable runnable;

    @Nullable
    private ScheduledTask scheduledTask;

    public FoliaPlotSquaredTask(final @NonNull Runnable runnable) {
        this.runnable = runnable;
    }

    /**
     * Set the backing scheduler task once it has been created
     *
     * @param scheduledTask Scheduler task
     */
    public void setScheduledTask(final @NonNull ScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
    }

    @Override
    public void runTask() {
        this.runnable.run();
    }

    @Override
    public boolean isCancelled() {
        return this.scheduledTask != null && this.scheduledTask.isCancelled();
    }

    @Override
    public void cancel() {
        if (this.scheduledTask != null) {
            this.scheduledTask.cancel();
        }
    }

}
