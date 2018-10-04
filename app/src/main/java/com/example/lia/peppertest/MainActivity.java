package com.example.lia.peppertest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.DiscussBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.Discuss;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.Topic;

public class MainActivity extends AppCompatActivity implements RobotLifecycleCallbacks {
    private static final String TAG = "MainActivity";
    // Store the Discuss action.
    private Discuss discuss;
    // Store the Animate action.
    private Animate animate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
    }

    @Override
    protected void onDestroy() {
        // Unregister all the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {

        say(qiContext, "Hello human!");
        startDiscussion(qiContext);
        startAnimation(qiContext, R.raw.elephant_a001);
        startAnimation(qiContext, R.raw.dance);
    }

    @Override
    public void onRobotFocusLost() {
        // The robot focus is lost.

        // Remove the on started listener from the discuss action.
        if (discuss != null) {
            discuss.setOnStartedListener(null);
        }

        // Remove the on started listener from the animate action.
        if (animate != null) {
            animate.setOnStartedListener(null);
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }



    private void say(QiContext qiContext, String msg) {
        // Create a new say action.
        Say say = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText(msg) // Set the text to say.
                .build(); // Build the say action.

        // Execute the action.
        say.run();
    }

    private void startDiscussion(QiContext qiContext) {
        // Create a topic.
        Topic topic = TopicBuilder.with(qiContext) // Create the builder using the QiContext.
                .withResource(R.raw.greetings) // Set the topic resource.
                .build(); // Build the topic.

        // Create a new discuss action.
        discuss = DiscussBuilder.with(qiContext) // Create the builder using the QiContext.
                .withTopic(topic) // Add the topic.
                .build(); // Build the discuss action.

        // Set an on started listener to the discuss action.
        discuss.setOnStartedListener(new Discuss.OnStartedListener() {
            @Override
            public void onStarted() {
                Log.i(TAG, "Discussion started.");
            }
        });

        // Run the discuss action asynchronously.
        Future<String> discussFuture = discuss.async().run();

        // Add a consumer to the action execution.
        discussFuture.thenConsume(new Consumer<Future<String>>() {
            @Override
            public void consume(Future<String> future) throws Throwable {
                if (future.hasError()) {
                    Log.e(TAG, "Discussion finished with error.", future.getError());
                }
            }
        });
    }

    private void startAnimation(QiContext qiContext, int anim) {
        // Create an animation.
        Animation animation = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(anim) // Set the animation resource.
                .build(); // Build the animation.

        // Create an animate action.
        animate = AnimateBuilder.with(qiContext) // Create the builder with the context.
                .withAnimation(animation) // Set the animation.
                .build(); // Build the animate action.

        // Run the animate action asynchronously.
        Future<Void> animateFuture = animate.async().run();

        // Set an on started listener to the animate action.
        animate.setOnStartedListener(new Animate.OnStartedListener() {
            @Override
            public void onStarted() {
                Log.i(TAG, "Animation started.");
            }
        });

        // Add a consumer to the action execution.
        animateFuture.thenConsume(new Consumer<Future<Void>>() {
            @Override
            public void consume(Future<Void> future) throws Throwable {
                if (future.isSuccess()) {
                    Log.i(TAG, "Animation finished with success.");
                } else if (future.hasError()) {
                    Log.e(TAG, "Animation finished with error.", future.getError());
                }
            }
        });
    }
}
