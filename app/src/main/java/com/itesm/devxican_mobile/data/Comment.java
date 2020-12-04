package com.itesm.devxican_mobile.data;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import java.util.ArrayList;
import java.util.List;

public class Comment {

    public Boolean show;
    public Timestamp time;
    public String body;
    public DocumentReference author, post;
    public List<DocumentReference> likes,
                                   dislikes;
    public Comment() {}

    public Comment(DocumentReference author, String body, DocumentReference post, ArrayList<DocumentReference> likes, ArrayList<DocumentReference> dislikes) {

        this.author = author;
        this.body = body;
        this.post = post;
        this.likes = likes;
        this.dislikes = dislikes;

        this.show = true;
        this.time = Timestamp.now();

    }

    public Comment(DocumentReference author, String body, DocumentReference post) {
        this(author, body, post, new ArrayList<DocumentReference>(), new ArrayList<DocumentReference>());
    }
}
