package com.virtusee.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by winata on 06/04/2018.
 */

public class AnswerGroupModel {
    private int data;

    private List<AnswerGroupModel> children = new ArrayList<>();

    private AnswerGroupModel parent = null;

    public AnswerGroupModel(int data) {
        this.data = data;
    }

    public AnswerGroupModel addChild(AnswerGroupModel child) {
        child.setParent(this);
        this.children.add(child);
        return child;
    }

    public void addChildren(List<AnswerGroupModel> children) {
        for (AnswerGroupModel child: children) {
            child.setParent(this);
            this.children.add(child);
        }
    }

    public List<AnswerGroupModel> getChildren() {
        return children;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    private void setParent(AnswerGroupModel parent) {
        this.parent = parent;
    }

    public AnswerGroupModel getParent() {
        return parent;
    }
}
