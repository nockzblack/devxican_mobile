package com.itesm.devxican_mobile.data;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

public class Post {

    public Boolean show;
    public Timestamp time;
    public String title, body;
    public List<String> photoURL;
    public DocumentReference author, branch;
    public List<DocumentReference> comments,
                                   likes,
                                   dislikes;
    ;



    public Post(String author, String title, String content) {
        //public no-arg constructor needed
    }

    public Post(String title, String body, DocumentReference author, DocumentReference branch, List<String> photoURL,
                List<DocumentReference> comments, List<DocumentReference> likes, List<DocumentReference> dislikes) {

        this.title = title;
        this.author = author;
        this.body = body;
        this.branch = branch;
        this.photoURL = photoURL;
        this.comments = comments;
        this.likes = likes;
        this.dislikes = dislikes;

        this.show = true;
        this.time = Timestamp.now();

    }

    // New post
    public Post(String title, String body, DocumentReference author, DocumentReference branch) {
        this(title, body, author, branch, new ArrayList<String>(), new ArrayList<DocumentReference>(), new ArrayList<DocumentReference>(), new ArrayList<DocumentReference>());
    }

}
