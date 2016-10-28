package net.steamcrafted.loadtoast;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 * Created by Wannes2 on 23/04/2015.
 */
public class LoadToast {

    private String mText = "";
    private LoadToastView mView;
    private ViewGroup mParentView;
    private int mTranslationY = 0;
    private boolean mShowCalled = false;
    private boolean mToastCanceled = false;
    private boolean mInflated = false;
    private boolean mVisible = false;


    public LoadToast(Context context){
        mView = new LoadToastView(context);
        mParentView = (ViewGroup) ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        mParentView.addView(mView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ViewHelper.setAlpha(mView, 0);
        mParentView.postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewHelper.setTranslationX(mView, (mParentView.getWidth() - mView.getWidth()) / 2);
                ViewHelper.setTranslationY(mView, -mView.getHeight() + mTranslationY);
                mInflated = true;
                if(!mToastCanceled && mShowCalled) show();
            }
        },1);

        mParentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                checkZPosition();
            }
        });
    }

    public LoadToast setTranslationY(int pixels){
        mTranslationY = pixels;
        return this;
    }

    public LoadToast setText(String message){
        mText = message;
        mView.setText(mText);
        return this;
    }

    public LoadToast setTextColor(int color){
        mView.setTextColor(color);
        return this;
    }

    public LoadToast setBackgroundColor(int color){
        mView.setBackgroundColor(color);
        return this;
    }

    public LoadToast setProgressColor(int color){
        mView.setProgressColor(color);
        return this;
    }

    LoadToast showToast(final CompletionCallback callback){
        if(!mInflated){
            mShowCalled = true;
            return this;
        }
        mView.show();
        ViewHelper.setTranslationX(mView, (mParentView.getWidth() - mView.getWidth()) / 2);
        ViewHelper.setAlpha(mView, 0f);
        ViewHelper.setTranslationY(mView, -mView.getHeight() + mTranslationY);
        //mView.setVisibility(View.VISIBLE);
        ViewPropertyAnimator.animate(mView).alpha(1f).translationY(25 + mTranslationY)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if(callback != null) callback.onViewAnimationCompleted(mVisible);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        if(callback != null) callback.onViewAnimationCompleted(mVisible);
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .setDuration(300).setStartDelay(0).start();

        mVisible = true;
        checkZPosition();

        return this;
    }

    public LoadToast show(){
        return showToast(null);
    }

    public LoadToast show(CompletionCallback callback){
        return showToast(callback);
    }

    private void done(boolean success, CompletionCallback callback){
        if(!mInflated){
            mToastCanceled = true;
            return;
        }
        if(success) mView.success();
        else mView.error();

        slideUp(callback);
    }

    public void success(CompletionCallback callback){
        done(true, callback);
    }

    public void error(CompletionCallback callback){
        done(false, callback);
    }

    public void success(){
        done(true, null);
    }

    public void error(){
        done(false, null);
    }

    private void checkZPosition(){
        // If the toast isn't visible, no point in updating all the views
        if(!mVisible) return;

        int pos = mParentView.indexOfChild(mView);
        int count = mParentView.getChildCount();
        if(pos != count-1){
            ((ViewGroup) mView.getParent()).removeView(mView);
            mParentView.requestLayout();
            mParentView.addView(mView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    private void slideUp(final CompletionCallback callback){
        ViewPropertyAnimator.animate(mView).setStartDelay(1000).alpha(0f)
                .translationY(-mView.getHeight() + mTranslationY)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if(callback != null) callback.onViewAnimationCompleted(mVisible);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        if(callback != null) callback.onViewAnimationCompleted(mVisible);
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .setDuration(300)
                .start();

        mVisible = false;
    }

    public interface CompletionCallback{
        void onViewAnimationCompleted(boolean isVisible);
    }
}
