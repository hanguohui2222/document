/**
 * Copyright (C) 2010-2011 Diego Torres Milano
 */
package com.amigo.widgetdemol.test;

import android.test.ActivityInstrumentationTestCase2;

import com.amigo.widgetdemol.GnButtonWindow;

public class GnButtonWindowTest extends
        ActivityInstrumentationTestCase2<GnButtonWindow> {

    private GnButtonWindow mActivity;

    public GnButtonWindowTest() {
        this("TemperatureConverterActivityTests");
    }

    public GnButtonWindowTest(String name) {
        super(GnButtonWindow.class);
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
