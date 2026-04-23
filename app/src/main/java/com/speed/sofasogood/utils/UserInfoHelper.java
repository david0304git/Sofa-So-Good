package com.speed.sofasogood.utils;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.speed.sofasogood.R;
import com.speed.sofasogood.activities.LoginActivity;

public class UserInfoHelper {

    private boolean expanded = false;

    public void setup(Activity activity) {
        View container = activity.findViewById(R.id.userInfoContainer);
        if (container == null) return;

        ImageButton btnUserInfo = activity.findViewById(R.id.btnUserInfo);
        TextView tvUserEmail = activity.findViewById(R.id.tvUserEmail);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Logged in: show avatar, tap to expand email
            btnUserInfo.setImageResource(R.drawable.ic_avatar);
            tvUserEmail.setText(user.getEmail());

            btnUserInfo.setOnClickListener(v -> {
                if (!expanded) {
                    tvUserEmail.setVisibility(View.VISIBLE);
                    tvUserEmail.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    int targetWidth = tvUserEmail.getMeasuredWidth();
                    tvUserEmail.getLayoutParams().width = 0;
                    tvUserEmail.requestLayout();
                    ValueAnimator anim = ValueAnimator.ofInt(0, targetWidth);
                    anim.setDuration(300);
                    anim.addUpdateListener(a -> {
                        tvUserEmail.getLayoutParams().width = (int) a.getAnimatedValue();
                        tvUserEmail.requestLayout();
                    });
                    anim.start();
                    expanded = true;
                } else {
                    int currentWidth = tvUserEmail.getWidth();
                    ValueAnimator anim = ValueAnimator.ofInt(currentWidth, 0);
                    anim.setDuration(200);
                    anim.addUpdateListener(a -> {
                        tvUserEmail.getLayoutParams().width = (int) a.getAnimatedValue();
                        tvUserEmail.requestLayout();
                    });
                    anim.addListener(new android.animation.AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(android.animation.Animator animation) {
                            tvUserEmail.setVisibility(View.GONE);
                        }
                    });
                    anim.start();
                    expanded = false;
                }
            });
        } else {
            // Not logged in: show login icon, tap to go to login
            btnUserInfo.setImageResource(R.drawable.ic_login);
            tvUserEmail.setVisibility(View.GONE);

            btnUserInfo.setOnClickListener(v ->
                    activity.startActivity(new Intent(activity, LoginActivity.class)));
        }
    }
}
