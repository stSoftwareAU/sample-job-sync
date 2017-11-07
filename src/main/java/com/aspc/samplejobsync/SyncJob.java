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

import com.aspc.remote.database.InvalidDataException;
import org.apache.commons.logging.Log;
import com.aspc.remote.rest.ReST;
import com.aspc.remote.rest.Response;
import com.aspc.remote.rest.Status;
import com.aspc.remote.rest.errors.ReSTException;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.util.misc.TimeUtil;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * <h1>SyncJob</h1>
 *
 * The SyncJob program implements an application that simply make ReST call to
 * Job class and log the results.
 *
 * @author parminder
 */
public class SyncJob {

    private final JSONObject config;
    private String remoteURL;
    private String username;
    private String password;
    private String callURL = "/ReST/v6/sync/Job";

    private static final Log LOGGER = CLogger.getLog(SyncJob.class.toString());//#LOGGER-NOPMD

    public SyncJob(JSONObject config) {
        this.config = config;
    }

    public void process() throws InvalidDataException, ReSTException, IOException, FileNotFoundException, Exception {

        remoteURL = config.getString("remoteURL");
        username = config.getString("username");
        password = config.getString("password");

        String sinceString = "";
        if (config.has("since")) {
            sinceString = config.getString("since");
        }

        boolean runOnce = false;
        if (config.has("runonce")) {
            runOnce = config.getBoolean("runonce");
        }

        long since = calculateSince(sinceString);

        if (runOnce) {
            syncOnce(since);
        } else {
            while (true) {
                since = sync(since);
            }
        }
    }

    private long sync(final @Nonnegative long since) {
        long nextSince = since;
        try {
            while (true) {
                ReST.Builder b = ReST.builder(remoteURL + callURL)
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
                    callURL = json.getString("next");
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

    private void syncOnce(final @Nonnegative long since) throws MalformedURLException, InvalidDataException, FileNotFoundException, ReSTException, IOException, Exception {

        ReST.Builder b = ReST.builder(remoteURL + callURL)
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

    /*
     * Accepted formats for date/timestamp string.
     * 
     * long    :- 1430549369303,
     * date    :- 2016-11-06T08:49:37Z,
     * strings :- -7 Days, -1 day, -5 Hours, -15 seconds, -20 Secs,
     *            -5 Mins & -1 Minute
     */
    public Long calculateSince(final @Nonnull String sinceString) throws InvalidDataException {

        Date since;
        Date now = new Date();

        if (StringUtilities.notBlank(sinceString)) {

            if (sinceString.startsWith("-")) {
                since = TimeUtil.subtractDurationFromDate(now, sinceString.substring(1), null);
            } else {
                try {
                    since = TimeUtil.parseUserTime(sinceString, null);
                } catch (InvalidDataException invalidDataException) {
                    throw new InvalidDataException(invalidDataException.getMessage());
                }
            }

        } else {
            since = TimeUtil.addDurationToDate(new Date(), "-24", null);
        }

        if (since == null) {
            throw new InvalidDataException("'since' is null");
        } else if (since.after(now)) {
            throw new InvalidDataException("'since' cannot be after now");
        }

        return since.getTime();
    }

}
