package com.virtusee.model;

public class ComboModel {
    public String[] val;
    public int[] target;

    public ComboModel(String selval) {
        if(selval.equals("")) return;

        String nval = selval.replace("\r\n", "\n");
        nval = nval.replace("\r", "\n");
        String[] iformList = nval.split("\n");
        val = new String[iformList.length];
        target = new int[iformList.length];

        for (int i = 0, n = iformList.length; i < n; i++) {
            String[] fff = iformList[i].split("#####");
            val[i] = fff[0];
            target[i] = (fff.length==1 || fff[1].isEmpty() || fff[1].equals("")) ? 0 : Integer.parseInt(fff[1]);
        }

    }

    public int GetTarget(String v){
        int t = 0;
        for (int i = 0, n = this.val.length; i < n; i++) {
            if(this.val[i].equals(v)) {
                t = this.target[i];
                break;
            }
        }
        return t;
    }
}