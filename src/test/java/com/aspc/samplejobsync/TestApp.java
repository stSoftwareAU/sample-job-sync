package com.aspc.samplejobsync;

import com.aspc.remote.database.InvalidDataException;
import com.aspc.remote.util.misc.TimeUtil;
import java.util.Date;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.Assert;
import org.junit.Test;

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
/**
 *
 * @author parminder
 */
public class TestApp {

    @Test
    public void testScanJob() throws InvalidDataException {

        ScanJob scanJob = new ScanJob("https://demo2.jobtrack.com.au", "admin", "admin");
        Date since = TimeUtil.addDurationToDate(new Date(), "-24", null);

        if (since == null) {
            Assert.fail();
        } else {

            long sinceLong = since.getTime();
            long nextSince = scanJob.process(sinceLong);
            Assert.assertTrue(nextSince > sinceLong);
        }

    }

}
