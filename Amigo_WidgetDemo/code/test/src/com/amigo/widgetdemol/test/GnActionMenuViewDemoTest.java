/**
 * Copyright (C) 2010-2011 Diego Torres Milano
 */
package com.amigo.widgetdemol.test;

import android.test.ActivityInstrumentationTestCase2;

import com.amigo.widgetdemol.GnActionMenuViewDemo;

public class GnActionMenuViewDemoTest extends
        ActivityInstrumentationTestCase2<GnActionMenuViewDemo> {

    private GnActionMenuViewDemo mActivity;

    public GnActionMenuViewDemoTest() {
        this("TemperatureConverterActivityTests");
    }

    public GnActionMenuViewDemoTest(String name) {
        super(GnActionMenuViewDemo.class);
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
