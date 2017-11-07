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
 *
 */

package com.aspc.samplejobsync;

import org.apache.commons.logging.Log;
import com.aspc.remote.rest.ReST;
import com.aspc.remote.rest.Response;
import com.aspc.remote.rest.Status;
import com.aspc.remote.util.misc.CLogger;
import javax.annotation.Nonnegative;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author parminder
 */
public class ScanJob {

    private final String remoteURL;
    private final String username;
    private final String password;

    private static final Log LOGGER = CLogger.getLog(ScanJob.class.toString());//#LOGGER-NOPMD

    public ScanJob(String remoteURL, String username, String password) {
        this.remoteURL = remoteURL;
        this.username = username;
        this.password = password;
    }

    public long process(final @Nonnegative long since) {
        return scan(since);
    }

    private long scan(final @Nonnegative long since) {
        long nextSince = since;
        try {
            String call = "/ReST/v6/sync/Job";
            while (true) {
                ReST.Builder b = ReST.builder(remoteURL + call)
                        .setAuthorization(username, password)
                        .setParameter("since", since)
                        .setParameter("block", "2 minute");

                JSONObject json = b.getResponseAndCheck().getContentAsJSON();

                JSONArray jobArray = json.getJSONArray("results");

                int len = jobArray.length();
                for (int pos = 0; pos < len; pos++) {
                    JSONObject job = jobArray.getJSONObject(pos);

                    processJob(job.getString("_href"));
                }

                if (json.has("next")) {
                    call = json.getString("next");
                } else {
                    nextSince = json.getLong("since");
                    break;
                }
            }
        } catch (Exception e) {
            try {
                LOGGER.warn("load all failed", e);
                Thread.sleep(60000);
            } catch (InterruptedException ex) {
                LOGGER.warn("Could not sleep", ex);
            }
        }
        return nextSince;
    }

    private JSONObject processJob(final String call) throws Exception {
        JSONObject json;
        Response r = ReST
                .builder(remoteURL + call)
                .setAuthorization(username, password)
                .getResponse();

        if (r.status == Status.C404_ERROR_NOT_FOUND) {
            return null;
        }
        r.checkStatus();

        json = r.getContentAsJSON();

        // DO STUFF 
        LOGGER.info(json.toString(2));

        return json;
    }

}
