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

package com.aspc.samplejobsync;

import com.aspc.remote.database.InvalidDataException;
import com.aspc.remote.util.misc.TimeUtil;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author parminder
 */
public class TestScanJob {

    @Test
    public void testScanJob() throws InvalidDataException {

        ScanJob scanJob = new ScanJob("https://demo2.jobtrack.com.au", "admin", "admin");
        Date since = TimeUtil.addDurationToDate(new Date(), "-2000", null);

        if (since == null) {
            Assert.fail();
        } else {

            long sinceLong = since.getTime();
            long nextSince = scanJob.process(sinceLong);
            Assert.assertTrue(nextSince > sinceLong);
        }

    }

}
