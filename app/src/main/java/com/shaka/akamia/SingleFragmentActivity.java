/*
 * Copyright (c) 2015, Shaka LLC
 * All rights reserved.
 *
 * Program:     SingleFragmentActivity
 * Purpose:     This activity inherited from Fragment Activity
 * Created by:  John Hou
 * Created on:  7/13/2015
 */
package com.shaka.akamia;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;


public abstract class SingleFragmentActivity extends AppCompatActivity {
    protected abstract Fragment createFragment();

    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        // Get fragment manager itself
        FragmentManager fm = getSupportFragmentManager();

        // Give it a fragment to manager
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);

        // If there is no fragment in the fragment list
        if (fragment == null) {
            // create a fragment
            fragment = createFragment();

            // create and commit a fragment transaction
            // Note: Fragment Transactions are used to add, remove, attach, detach, or replace
            //       fragments in the fragment list.
            fm.beginTransaction()
                    .add(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }
}
