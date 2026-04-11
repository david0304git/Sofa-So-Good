package com.speed.sofasogood.auth;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.speed.sofasogood.models.UserProfile;
import com.speed.sofasogood.utils.AppConstants;

public class AuthManager {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public AuthManager() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public void logout() {
        auth.signOut();
    }

    public Task<?> registerUser(
            @NonNull String email,
            @NonNull String password,
            @NonNull String playerName
    ) {
        return auth.createUserWithEmailAndPassword(email, password)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    FirebaseUser user = auth.getCurrentUser();
                    if (user == null) {
                        throw new IllegalStateException("User created but current user is null");
                    }

                    UserProfile profile = new UserProfile(
                            user.getUid(),
                            email,
                            playerName
                    );

                    return db.collection(AppConstants.USERS_COLLECTION)
                            .document(user.getUid())
                            .set(profile);
                });
    }

    public Task<?> loginUser(@NonNull String email, @NonNull String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

    public Task<Void> sendPasswordReset(@NonNull String email) {
        return auth.sendPasswordResetEmail(email);
    }

    public Task<com.google.firebase.firestore.DocumentSnapshot> getMyProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("No logged-in user");
        }

        return db.collection(AppConstants.USERS_COLLECTION)
                .document(user.getUid())
                .get();
    }

    public Task<Void> updatePlayerName(@NonNull String playerName) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("No logged-in user");
        }

        return db.collection(AppConstants.USERS_COLLECTION)
                .document(user.getUid())
                .update("playerName", playerName);
    }
}