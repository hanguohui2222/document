/**
 * Copyright (C) 2010-2011 Diego Torres Milano
 */
package com.amigo.widgetdemol.test;

import android.test.ActivityInstrumentationTestCase2;

import com.amigo.widgetdemol.AmigoExpandableListViewDemo;

public class AmigoExpandableListViewDemoTest extends
        ActivityInstrumentationTestCase2<AmigoExpandableListViewDemo> {

    private AmigoExpandableListViewDemo mActivity;

    public AmigoExpandableListViewDemoTest() {
        this("TemperatureConverterActivityTests");
    }

    public AmigoExpandableListViewDemoTest(String name) {
        super(AmigoExpandableListViewDemo.class);
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
