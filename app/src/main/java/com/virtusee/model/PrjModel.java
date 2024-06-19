package com.virtusee.model;

public class PrjModel {
    public final String prj_proximity;
    public final int prj_radius;
    public final int prj_reverse_proximity;
    public final int prj_total_checkout;

    public PrjModel(String prj_proximity, int prj_radius, int prj_reverse_proximity, int prj_total_checkout) {
        this.prj_proximity = prj_proximity;
        this.prj_radius = prj_radius;
        this.prj_reverse_proximity = prj_reverse_proximity;
        this.prj_total_checkout = prj_total_checkout;
    }

    @Override
    public String toString() {
        return "PrjModel{" +
                "prj_proximity='" + prj_proximity + '\'' +
                ", prj_radius=" + prj_radius +
                ", prj_reverse_proximity=" + prj_reverse_proximity +
                ", prj_total_checkout=" + prj_total_checkout +
                '}';
    }
}
