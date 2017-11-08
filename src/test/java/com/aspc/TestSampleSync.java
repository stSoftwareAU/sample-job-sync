/*
 *  Copyright (c) 2001-2004 ASP Converters Pty Ltd.
 *
 *  www.stSoftware.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */

package com.aspc;

import com.aspc.remote.database.InvalidDataException;
import com.aspc.remote.rest.errors.NotAuthorizedException;
import org.junit.Test;

/**
 * <h1>TestSampleSync</h1>
 *
 * The TestSampleSync test runs SampleSyncApp.
 *
 * @author parminder
 */
public class TestSampleSync {

    @Test
    public void testSampleSync() throws InvalidDataException, Exception {
        
        String host = "https://demo2.jobtrack.com.au";
        String username = "admin";
        String password = "admin";
        String since = "-100 Days"; //can also use "2017-07-10T08:49:37Z";

        SampleSync sampleSync = new SampleSync(host, username, password);
        sampleSync.process(since);
        
    }
    
    @Test
    public void testUnauthorisedUser() throws InvalidDataException, Exception {
        
        String host = "https://demo2.jobtrack.com.au";
        String username = "abc";
        String password = "xyz";
        String since = "2017-07-10T08:49:37Z";

        SampleSync sampleSync = new SampleSync(host, username, password);
        try {
            sampleSync.process(since);
        } catch (NotAuthorizedException aex) {
            
        }
        
    }

}
