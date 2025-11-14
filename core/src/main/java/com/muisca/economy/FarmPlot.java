package com.muisca.economy;

import com.badlogic.gdx.math.Vector2;

public class FarmPlot {

    public enum State {
        FALLOW,
        GROWING,
        READY
    }

    private final int id;
    private final Vector2 position = new Vector2();
    private String cropId;
    private String plannedCropId;
    private float growth;
    private boolean reserved;
    private State state = State.FALLOW;

    public FarmPlot(int id, float x, float y) {
        this.id = id;
        this.position.set(x, y);
    }

    public int getId() {
        return id;
    }

    public Vector2 getPosition() {
        return position;
    }

    public String getCropId() {
        return cropId;
    }

    public void setCropId(String cropId) {
        this.cropId = cropId;
    }

    public String getPlannedCropId() {
        return plannedCropId;
    }

    public void setPlannedCropId(String plannedCropId) {
        this.plannedCropId = plannedCropId;
    }

    public float getGrowth() {
        return growth;
    }

    public void setGrowth(float growth) {
        this.growth = growth;
    }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
