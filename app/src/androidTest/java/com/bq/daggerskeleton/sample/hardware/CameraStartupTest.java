package com.bq.daggerskeleton.sample.hardware;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.bq.daggerskeleton.R;
import com.bq.daggerskeleton.common.MainActivity;
import com.bq.daggerskeleton.sample.hardware.session.SessionState;
import com.bq.daggerskeleton.sample.hardware.session.SessionStore;
import com.bq.daggerskeleton.util.FluxUtil;
import com.bq.daggerskeleton.util.Repeat;
import com.bq.daggerskeleton.util.RepeatRule;
import com.bq.daggerskeleton.util.RxCountingResourceRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
public class CameraStartupTest {

   @Rule
   public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);
   @Rule public RxCountingResourceRule countingRule = new RxCountingResourceRule("cameraOpen");
   @Rule public RepeatRule repeatRule = new RepeatRule();

   @Test
   public void camera_open_under_300ms() {
      countingRule.increment();

      SessionStore store = FluxUtil.findStore(activityRule.getActivity(), SessionStore.class);
      store.flowable()
            .startWith(store.state())
            .filter(s -> s.status == SessionState.Status.READY)
            .timeout(300, TimeUnit.MILLISECONDS)
            .take(1)
            .doOnTerminate(countingRule::decrement)
            .test();

      onView(withId(R.id.espresso_hook_view))
            .check((view, noViewFoundException) -> {
               assertEquals(SessionState.Status.READY, store.state().status);
            });
   }

   @LargeTest
   @Repeat(5)
   public void open_camera_many_times() {
      camera_open_under_300ms();
      activityRule.getActivity().finish();
      activityRule.launchActivity(null);
   }

}