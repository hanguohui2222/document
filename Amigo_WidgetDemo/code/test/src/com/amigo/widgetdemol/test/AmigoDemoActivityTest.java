/**
 * Copyright (C) 2010-2011 Diego Torres Milano
 */
package com.amigo.widgetdemol.test;

import static android.test.ViewAsserts.assertLeftAligned;
import static android.test.ViewAsserts.assertOnScreen;
import static android.test.ViewAsserts.assertRightAligned;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.LinearLayout;

import com.amigo.widgetdemol.AmigoDemoActivity;

public class AmigoDemoActivityTest extends
        ActivityInstrumentationTestCase2<AmigoDemoActivity> {

    private AmigoDemoActivity mActivity;

    public AmigoDemoActivityTest() {
        this("TemperatureConverterActivityTests");
    }

    public AmigoDemoActivityTest(String name) {
        super(AmigoDemoActivity.class);
        setName(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }

    public final void testPreconditions() {
        assertNotNull(mActivity);
    }
}
