package com.itesm.devxican_mobile.data.tools;

import android.util.Log;

import androidx.annotation.NonNull;

import com.itesm.devxican_mobile.data.Comment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.itesm.devxican_mobile.data.Post;
import com.itesm.devxican_mobile.data.User;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Random;

public class DataGenerator {
    public Random random;
    FirebaseFirestore db;

    final String[] art = {"El", "La", "Lo"};
    final String[] noun = {"programa", "crash", "booteo", "pc", "lap", "mochila", "mouse", "HDD", "SDD", "red neuronal", "RTX", "stack", "stack overflow"};
    final String[] verb = {"crasheo", "encendio", "apago", "murio", "congelo", "reinicio", "descargo", "subir", "implmentar", "tardo"};
    final String[] adj = {"verde", "azul", "actualizado", "facil", "imposible"};
    final String[] adv = {"rapido", "lento", "eficientemente", "increiblemente"};



    public DataGenerator()  {
        this.random = new Random(System.currentTimeMillis());
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();
    }

    public DocumentReference genRandomBranch() {

        final List<DocumentReference> branches = new ArrayList<>();

        db.collection("branches")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                branches.add(document.getReference());
                            }
                        } else {
                            Log.w("WARN", "Error getting documents.", task.getException());
                        }
                    }
                });

        return branches.get(this.random.nextInt(branches.size()));
    }

    public void generatePosts(int num_post, int max_com, boolean sameBranch) {

        String title = genRandomTitle(), content = genRandomContent();
        Post post;
        DocumentReference branch = genRandomBranch();
        for (int i = 0; i <  num_post; i++) {

            post = new Post(title, content, genRandomAuthor(), sameBranch ? genRandomBranch() : branch);

            // generate and upload random comments
            db.collection("posts").add(post)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(final DocumentReference documentReference) {
                            // generate n comments
                            ArrayList<DocumentReference> comments = new ArrayList<>(genRandomComments(max_com, documentReference));
                            db.collection("posts")
                                    .document(documentReference.getId())
                                    .update("comments", comments)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("INFO", "DocumentSnapshot added with ID: " + documentReference.getId());
                                            } else {
                                                Log.w("WARN", "Error adding document", task.getException());
                                            }
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("WARN", "Error adding document", e);
                        }
                    });

        }

    }

    public String genRandomTitle() {
        String title = art[random.nextInt(art.length)];

        title = title.concat(" ").concat(noun[random.nextInt(noun.length)])
                     .concat(" ").concat(adj[random.nextInt(adj.length)])
                     .concat(" ").concat(verb[random.nextInt(verb.length)])
                     .concat(" ").concat(adv[random.nextInt(adv.length)]);

        return title;
    }

    public List<DocumentReference> getAllUsers() {

        final List<DocumentReference> users = new ArrayList<>();

        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                users.add(document.getReference());
                            }

                        } else {
                            Log.w("WARN", "Error getting documents.", task.getException());

                        }
                    }
                });
        return users;

    }

    public DocumentReference genRandomAuthor() {

        final List<DocumentReference> users = getAllUsers();

        return users.get(random.nextInt(users.size()));
    }

    public String genRandomContent() {
        int max = random.nextInt(30)+5;
        String content = "";

        for (int i = 0; i < max; i++) {
            content = content.concat(" ").concat(noun[random.nextInt(noun.length)])
                    .concat(" ").concat(adj[random.nextInt(adj.length)])
                    .concat(" ").concat(verb[random.nextInt(verb.length)])
                    .concat(" ").concat(adv[random.nextInt(adv.length)])
                    .concat(". ");

        }

        return content;
    }

    public List<DocumentReference> genRandomComments(int n, DocumentReference post) {
        final List<DocumentReference> comments = new ArrayList<>();

        String content;
        DocumentReference author;
        ArrayList<DocumentReference> likes, dislikes;

        Comment comment;

        // generate n comments into post
        for (int i = 0; i < n; i++) {

            author = genRandomAuthor();
            content = genRandomContent();
            likes = genRandomLikes();
            dislikes = genRandomDislikes();

            comment = new Comment(author, content, post, likes, dislikes);

            db.collection("comments").add(comment)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            comments.add(documentReference);
                            Log.d("INFO", "DocumentSnapshot added with ID: " + documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("INFO", "Error adding document", e);
                        }
                    });

        }
        // Just in case
        System.gc();
        return comments;
    }

    public ArrayList<DocumentReference> genRandomLikes() {
        // get all users
        List<DocumentReference> users = getAllUsers();
        List<DocumentReference> likes = new ArrayList<>();

        for (int i = 0; i < users.size(); i++) { if (this.random.nextBoolean()) { likes.add(users.get(i)); } }
        return new ArrayList<>(likes);
    }

    public ArrayList<DocumentReference> genRandomDislikes() {
        // get all users
        List<DocumentReference> users = getAllUsers();
        List<DocumentReference> dislikes = new ArrayList<>();

        for (int i = 0; i < users.size(); i++) { if (this.random.nextBoolean()) { dislikes.add(users.get(i)); } }
        return new ArrayList<>(dislikes);
    }
}
