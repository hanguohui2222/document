/**
 * Copyright (C) 2010-2011 Diego Torres Milano
 */
package com.amigo.widgetdemol.test;

import android.test.ActivityInstrumentationTestCase2;

import com.amigo.widgetdemol.GnForceTouchWindow;

public class GnForceTouchWindowTest extends
        ActivityInstrumentationTestCase2<GnForceTouchWindow> {

    private GnForceTouchWindow mActivity;

    public GnForceTouchWindowTest() {
        this("TemperatureConverterActivityTests");
    }

    public GnForceTouchWindowTest(String name) {
        super(GnForceTouchWindow.class);
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
