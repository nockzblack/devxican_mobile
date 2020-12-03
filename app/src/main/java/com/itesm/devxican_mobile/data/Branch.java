package com.itesm.devxican_mobile.data;

import com.google.firebase.firestore.DocumentReference;
import java.util.ArrayList;
import java.util.List;

public class Branch {

    public List<DocumentReference> posts,
            followers,
            admins;
    public String name,
                  desc,
                  backURL;

    public Branch() {}

    public Branch(String name, String desc, String backURL, List<DocumentReference> posts, List<DocumentReference> followers, List<DocumentReference> admins) {

        this.name = name;
        this.desc = desc;
        this.backURL = backURL;
        this.posts = posts;
        this.followers = followers;
        this.admins = admins;

    }

    public Branch(String name, String desc, String backURL) {
       this(name, desc, backURL, new ArrayList<DocumentReference>(), new ArrayList<DocumentReference>(), new ArrayList<DocumentReference>());
    }

}
