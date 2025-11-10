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
                      ReputationTracker reputation, DecisionState decisionState,
                      CombatSnapshot combatSnapshot) {
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

        if (combatSnapshot != null) {
            for (SaveData.SaveColonist colonist : combatSnapshot.colonists) {
                data.colonists.add(copyColonist(colonist));
            }
            for (SaveData.SaveEnemy enemy : combatSnapshot.enemies) {
                data.enemies.add(copyEnemy(enemy));
            }
        }

        saveFile.writeString(json.prettyPrint(data), false, "UTF-8");
    }

    public CombatSnapshot read(Inventory inventory, StructureManager structures, JobBoard jobBoard,
                               ReputationTracker reputation, DecisionState decisionState) {
        if (!saveFile.exists()) {
            return null;
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

        CombatSnapshot snapshot = new CombatSnapshot();
        for (SaveData.SaveColonist colonist : data.colonists) {
            snapshot.colonists.add(copyColonist(colonist));
        }
        for (SaveData.SaveEnemy enemy : data.enemies) {
            snapshot.enemies.add(copyEnemy(enemy));
        }
        return snapshot;
    }

    private SaveData.SaveColonist copyColonist(SaveData.SaveColonist source) {
        SaveData.SaveColonist copy = new SaveData.SaveColonist();
        copy.name = source.name;
        copy.x = source.x;
        copy.y = source.y;
        copy.health = source.health;
        copy.stamina = source.stamina;
        copy.focus = source.focus;
        copy.defeated = source.defeated;
        copy.globalCooldown = source.globalCooldown;
        copy.talents.addAll(source.talents);
        for (SaveData.SaveSpellSlot slot : source.spells) {
            copy.spells.add(copySpellSlot(slot));
        }
        return copy;
    }

    private SaveData.SaveEnemy copyEnemy(SaveData.SaveEnemy source) {
        SaveData.SaveEnemy copy = new SaveData.SaveEnemy();
        copy.archetypeId = source.archetypeId;
        copy.x = source.x;
        copy.y = source.y;
        copy.health = source.health;
        copy.stamina = source.stamina;
        copy.focus = source.focus;
        for (SaveData.SaveSpellSlot slot : source.spells) {
            copy.spells.add(copySpellSlot(slot));
        }
        return copy;
    }

    private SaveData.SaveSpellSlot copySpellSlot(SaveData.SaveSpellSlot source) {
        SaveData.SaveSpellSlot copy = new SaveData.SaveSpellSlot();
        copy.spellId = source.spellId;
        copy.cooldown = source.cooldown;
        return copy;
    }

    public static class CombatSnapshot {
        public final Array<SaveData.SaveColonist> colonists = new Array<>();
        public final Array<SaveData.SaveEnemy> enemies = new Array<>();
    }
}
