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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.plotsquared.bukkit.BukkitPlatform;
import com.plotsquared.core.util.task.PlotSquaredTask;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * {@link TaskManager} implementation based on the Paper scheduler API
 * ({@link GlobalRegionScheduler} / {@link AsyncScheduler}).
 * <p>
 * These schedulers are part of the Paper API: on Paper the global region
 * scheduler runs tasks on the main server thread, while on Folia it runs
 * on the global region thread. This makes the implementation compatible
 * with both platforms without runtime platform checks.
 */
@Singleton
public class FoliaTaskManager extends TaskManager {

    private final BukkitPlatform bukkitMain;
    private final TaskTime.TimeConverter timeConverter;
    private final GlobalRegionScheduler globalRegionScheduler;
    private final AsyncScheduler asyncScheduler;

    @Inject
    public FoliaTaskManager(
            final @NonNull BukkitPlatform bukkitMain,
            final TaskTime.@NonNull TimeConverter timeConverter
    ) {
        this.bukkitMain = bukkitMain;
        this.timeConverter = timeConverter;
        this.globalRegionScheduler = Bukkit.getGlobalRegionScheduler();
        this.asyncScheduler = Bukkit.getAsyncScheduler();
    }

    @Override
    public PlotSquaredTask taskRepeat(
            final @NonNull Runnable runnable,
            final @NonNull TaskTime taskTime
    ) {
        final long ticks = this.timeConverter.toTicks(taskTime);
        final FoliaPlotSquaredTask task = new FoliaPlotSquaredTask(runnable);
        final ScheduledTask scheduledTask = this.globalRegionScheduler.runAtFixedRate(
                this.bukkitMain,
                scheduledTaskInner -> task.runTask(),
                ticks,
                ticks
        );
        task.setScheduledTask(scheduledTask);
        return task;
    }

    @Override
    public PlotSquaredTask taskRepeatAsync(
            final @NonNull Runnable runnable,
            final @NonNull TaskTime taskTime
    ) {
        final long delayMs = this.timeConverter.toMs(taskTime);
        final FoliaPlotSquaredTask task = new FoliaPlotSquaredTask(runnable);
        final ScheduledTask scheduledTask = this.asyncScheduler.runAtFixedRate(
                this.bukkitMain,
                scheduledTaskInner -> task.runTask(),
                50L,
                Math.max(50L, delayMs),
                TimeUnit.MILLISECONDS
        );
        task.setScheduledTask(scheduledTask);
        return task;
    }

    @Override
    public void taskAsync(final @NonNull Runnable runnable) {
        if (this.bukkitMain.isEnabled()) {
            this.asyncScheduler.runNow(this.bukkitMain, scheduledTask -> runnable.run());
        } else {
            runnable.run();
        }
    }

    @Override
    public <T> T sync(final @NonNull Callable<T> function, final int timeout) throws Exception {
        if (Bukkit.isPrimaryThread()) {
            return function.call();
        }
        return this.callMethodSync(function).get(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public <T> Future<T> callMethodSync(final @NonNull Callable<T> method) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        this.globalRegionScheduler.run(this.bukkitMain, scheduledTask -> {
            try {
                future.complete(method.call());
            } catch (final Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public void task(final @NonNull Runnable runnable) {
        this.globalRegionScheduler.run(this.bukkitMain, scheduledTask -> runnable.run());
    }

    @Override
    public void taskLater(
            final @NonNull Runnable runnable,
            final @NonNull TaskTime taskTime
    ) {
        final long ticks = this.timeConverter.toTicks(taskTime);
        this.globalRegionScheduler.runDelayed(
                this.bukkitMain,
                scheduledTask -> runnable.run(),
                ticks
        );
    }

    @Override
    public void taskLaterAsync(
            final @NonNull Runnable runnable,
            final @NonNull TaskTime taskTime
    ) {
        final long delayMs = this.timeConverter.toMs(taskTime);
        this.asyncScheduler.runDelayed(
                this.bukkitMain,
                scheduledTask -> runnable.run(),
                delayMs,
                TimeUnit.MILLISECONDS
        );
    }

}
