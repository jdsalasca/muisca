package com.muisca.jobs;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.muisca.world.WorldMap;

public class JobBoard {

    public static class HarvestSite {
        public final Vector2 position = new Vector2();
        public boolean reserved = false;
        public boolean harvested = false;
    }

    private final Array<HarvestSite> sites = new Array<>();
    private final Vector2 temp = new Vector2();

    public JobBoard(WorldMap map, int tileSize, int count) {
        float centerX = map.getWidth() * tileSize / 2f;
        float centerY = map.getHeight() * tileSize / 2f;
        for (int i = 0; i < count; i++) {
            HarvestSite site = new HarvestSite();
            float radius = MathUtils.random(80f, 260f);
            float angle = MathUtils.random(0f, MathUtils.PI2);
            site.position.set(centerX + MathUtils.cos(angle) * radius,
                    centerY + MathUtils.sin(angle) * radius);
            sites.add(site);
        }
    }

    public int reserveSite() {
        for (int i = 0; i < sites.size; i++) {
            HarvestSite site = sites.get(i);
            if (!site.harvested && !site.reserved) {
                site.reserved = true;
                return i;
            }
        }
        return -1;
    }

    public void completeJob(int jobId) {
        if (jobId < 0 || jobId >= sites.size) {
            return;
        }
        HarvestSite site = sites.get(jobId);
        site.harvested = true;
        site.reserved = false;
    }

    public void releaseJob(int jobId) {
        if (jobId < 0 || jobId >= sites.size) {
            return;
        }
        HarvestSite site = sites.get(jobId);
        if (!site.harvested) {
            site.reserved = false;
        }
    }

    public Vector2 getSitePosition(int jobId, Vector2 out) {
        if (jobId < 0 || jobId >= sites.size) {
            return out.setZero();
        }
        return out.set(sites.get(jobId).position);
    }

    public int getRemainingSites() {
        int count = 0;
        for (HarvestSite site : sites) {
            if (!site.harvested) {
                count++;
            }
        }
        return count;
    }

    public int getActiveReservations() {
        int count = 0;
        for (HarvestSite site : sites) {
            if (site.reserved && !site.harvested) {
                count++;
            }
        }
        return count;
    }

    public Array<HarvestSite> getSites() {
        return sites;
    }

    public void replaceSites(Array<HarvestSite> newSites) {
        sites.clear();
        for (HarvestSite site : newSites) {
            HarvestSite copy = new HarvestSite();
            copy.position.set(site.position);
            copy.harvested = site.harvested;
            sites.add(copy);
        }
    }
}
