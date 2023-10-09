package com.persistent.bionation.data;

import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class CommonName extends RealmObject {
    @PrimaryKey
    public int taxon_id;
    @Required
    public String taxon_name;
}
