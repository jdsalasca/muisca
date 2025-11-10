package com.muisca.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.muisca.inventory.Inventory;
import com.muisca.jobs.JobBoard;
import com.muisca.jobs.JobBoard.HarvestSite;
import com.muisca.reputation.ReputationTracker;
import com.muisca.structures.StructureInstance;
import com.muisca.structures.StructureManager;
import com.muisca.structures.StructureBlueprint;
import com.muisca.decisions.DecisionState;

public class SaveManager {

    private final Json json = new Json();
    private final FileHandle saveFile;

    public SaveManager(String slot) {
        FileHandle dir = Gdx.files.local("saves");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        this.saveFile = dir.child(slot + ".json");
    }

    public void write(Inventory inventory, StructureManager structures, JobBoard jobBoard,
                      ReputationTracker reputation, DecisionState decisionState) {
        SaveData data = new SaveData();
        ObjectIntMap<String> invSnapshot = inventory.snapshot();
        for (ObjectIntMap.Entry<String> entry : invSnapshot.entries()) {
            SaveData.SaveItem item = new SaveData.SaveItem();
            item.id = entry.key;
            item.amount = entry.value;
            data.inventory.add(item);
        }

        for (StructureInstance instance : structures.getInstances()) {
            SaveData.SaveStructure structure = new SaveData.SaveStructure();
            structure.blueprintId = instance.getBlueprint().getId();
            structure.x = instance.getBounds().x + instance.getBounds().width / 2f;
            structure.y = instance.getBounds().y + instance.getBounds().height / 2f;
            data.structures.add(structure);
        }

        Array<HarvestSite> sites = jobBoard.getSites();
        for (HarvestSite site : sites) {
            SaveData.SaveSite saveSite = new SaveData.SaveSite();
            saveSite.x = site.position.x;
            saveSite.y = site.position.y;
            saveSite.harvested = site.harvested;
            data.harvestSites.add(saveSite);
        }

        ObjectFloatMap<String> repSnapshot = reputation.snapshot();
        for (ObjectFloatMap.Entry<String> entry : repSnapshot.entries()) {
            SaveData.SaveReputation rep = new SaveData.SaveReputation();
            rep.faction = entry.key;
            rep.value = entry.value;
            data.reputation.add(rep);
        }

        ObjectMap<String, Boolean> flagSnapshot = decisionState.snapshot();
        for (ObjectMap.Entry<String, Boolean> entry : flagSnapshot.entries()) {
            SaveData.SaveFlag flag = new SaveData.SaveFlag();
            flag.id = entry.key;
            flag.value = entry.value;
            data.flags.add(flag);
        }

        saveFile.writeString(json.prettyPrint(data), false, "UTF-8");
    }

    public void read(Inventory inventory, StructureManager structures, JobBoard jobBoard,
                     ReputationTracker reputation, DecisionState decisionState) {
        if (!saveFile.exists()) {
            return;
        }
        SaveData data = json.fromJson(SaveData.class, saveFile);

        ObjectIntMap<String> inv = new ObjectIntMap<>();
        for (SaveData.SaveItem item : data.inventory) {
            inv.put(item.id, item.amount);
        }
        inventory.setFromSnapshot(inv);

        structures.getInstances().clear();
        for (SaveData.SaveStructure structure : data.structures) {
            structures.addRestored(structure.blueprintId, new Vector2(structure.x, structure.y));
        }

        if (data.harvestSites.size > 0) {
            Array<HarvestSite> restoredSites = new Array<>();
            for (SaveData.SaveSite site : data.harvestSites) {
                HarvestSite newSite = new HarvestSite();
                newSite.position.set(site.x, site.y);
                newSite.harvested = site.harvested;
                restoredSites.add(newSite);
            }
            jobBoard.replaceSites(restoredSites);
        }

        ObjectFloatMap<String> rep = new ObjectFloatMap<>();
        for (SaveData.SaveReputation entry : data.reputation) {
            rep.put(entry.faction, entry.value);
        }
        reputation.restore(rep);

        ObjectMap<String, Boolean> flags = new ObjectMap<>();
        for (SaveData.SaveFlag flag : data.flags) {
            flags.put(flag.id, flag.value);
        }
        decisionState.restore(flags);
    }
}
