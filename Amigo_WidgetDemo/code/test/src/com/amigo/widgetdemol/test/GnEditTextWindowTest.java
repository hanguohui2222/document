/**
 * Copyright (C) 2010-2011 Diego Torres Milano
 */
package com.amigo.widgetdemol.test;

import android.test.ActivityInstrumentationTestCase2;

import com.amigo.widgetdemol.GnEditTextWindow;

public class GnEditTextWindowTest extends
        ActivityInstrumentationTestCase2<GnEditTextWindow> {

    private GnEditTextWindow mActivity;

    public GnEditTextWindowTest() {
        this("TemperatureConverterActivityTests");
    }

    public GnEditTextWindowTest(String name) {
        super(GnEditTextWindow.class);
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
