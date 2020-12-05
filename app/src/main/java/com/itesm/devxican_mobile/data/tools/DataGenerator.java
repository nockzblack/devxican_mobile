package com.itesm.devxican_mobile.data.tools;

import android.util.Log;

import androidx.annotation.NonNull;

import com.itesm.devxican_mobile.data.Post;
import com.example.devxican_mobile.data.Comment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class DataGenerator {
    public Random random;
    FirebaseFirestore db;
    List<String> comments;

    String[] art = {"El", "La", "Lo"};
    String[] noun = {"programa", "crash", "booteo", "pc", "lap", "mochila", "mouse", "HDD", "SDD", "red neuronal", "RTX", "stack", "stack overflow"};
    String[] verb = {"crasheo", "encendio", "apago", "murio", "congelo", "reinicio", "descargo", "subir", "implmentar", "tardo"};
    String[] adj = {"verde", "azul", "actualizado", "facil", "imposible"};
    String[] adv = {"rapido", "lento", "eficientemente", "increiblemente"};



    public DataGenerator(long seed) throws IOException {
        this.random = new Random(seed);
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();
    }

    public void generatePosts(int limit) throws IOException {

        String title , author, content, uid;
        Post post;

        for (int i = 0; i < limit; i++) {

            String[] info = genRandomAuthor().split(" ");
            author = info[0];
            uid = info[1];

            title = genRandomTitle();
            content = genRandomContent();


            post = new Post(author, title, content);
            // generate and upload random comments

            db.collection("posts").add(post)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(final DocumentReference documentReference) {
                            Log.d("INFO", "DocumentSnapshot added with ID: " + documentReference.getId());

                            int n = random.nextInt(10);
                            try {
                                List<String> comments = genRandomComments(n, documentReference.getId());
                                db.collection("posts").document(documentReference.getId())
                                        .update("comments", (Object) comments).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("COMMENTS", "Document with id: "+ documentReference.getId() + "successfully updated.");
                                        } else {
                                            Log.wtf("ERROR", "Error: " + task.getException());
                                        }
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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

    public String genRandomTitle() throws IOException {
        String title = art[random.nextInt(art.length)];

        title = title.concat(" ").concat(noun[random.nextInt(noun.length)])
                     .concat(" ").concat(adj[random.nextInt(adj.length)])
                     .concat(" ").concat(verb[random.nextInt(verb.length)])
                     .concat(" ").concat(adv[random.nextInt(adv.length)]);

        return title;
    }

    public String genRandomAuthor() throws IOException {
        final String[] res = {"Fulanito", "90S8LgrwFqWLeQQNuUI6jghmrYn2"};

        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> mails, uid;
                            mails = new ArrayList<>();
                            uid = new ArrayList<>();
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Log.d("INFO", document.getId() + " => " + document.getData());

                                uid.add((String) document.getId());
                                mails.add((String) document.get("email"));
                            }

                            int n = random.nextInt(mails.size());

                            res[0] = mails.get(n);
                            res[1] = uid.get(n);
                        } else {
                            Log.w("WARN", "Error getting documents.", task.getException());

                        }
                    }
                });

        return res[0].concat(" ").concat(res[1]);

    }

    public String genRandomDate() {
        return Integer.toString(random.nextInt(31)+1)
                .concat("-").concat(Integer.toString(random.nextInt(12)+1))
                .concat("-").concat(Integer.toString(random.nextInt(10)+2010));
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

    public List<String> genRandomComments(int n, String postId) throws IOException {
        comments = new ArrayList<>();


        String author, content, uid;
        List<String> likedBy, dislikedBy;

        Comment comment;


        for (int i = 0; i < n; i++) {

            String[] info = genRandomAuthor().split(" ");
            author = info[0];
            uid = info[1];

            content = genRandomContent();
            likedBy = genRandomLikes();
            dislikedBy = genRandomDislikes();

            comment = new Comment(author, uid, postId, content, likedBy, dislikedBy);

            /*
            doc.put("author", author);
            doc.put("description", content);
            doc.put("date", date);
            doc.put("likes", likes);
            doc.put("dislikes", dislikes);
            */

            db.collection("comments").add(comment)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            comments.add(documentReference.getId());
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

    public List<String> genRandomLikes() {

        return new ArrayList<String>();
    }

    public List<String> genRandomDislikes() {
        return new ArrayList<String>();
    }

    private int genRandomLikes(int n) {
        if (n == 0) {
            return random.nextInt();
        } else {
            return random.nextInt() + genRandomLikes(n-1);
        }
    }


}
