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
package com.aspc;

import org.apache.commons.logging.Log;
import com.aspc.remote.rest.ReST;
import com.aspc.remote.rest.Response;
import com.aspc.remote.rest.Status;
import com.aspc.remote.rest.errors.ReSTException;
import com.aspc.remote.util.misc.CLogger;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * <h1>SampleSync</h1>
 *
 * The SampleSync program implements an application that simply make ReST call
 * to Job class and log the results.
 *
 * @author parminder
 */
public class SampleSync {

    private final String host;
    private final String username;
    private final String password;

    private static final Log LOGGER = CLogger.getLog("src.main.java.com.aspc.SampleSync");//#LOGGER-NOPMD

    public SampleSync(final @Nonnull String host, final @Nonnull String username, final @Nonnull String password) {
        this.host = host;
        this.username = username;
        this.password = password;
    }

    /**
     * Loop forever scanning for new or changed jobs.
     *
     * @param scanFrom from what time should we scan
     *
     * @throws Exception a non temporary issue has occurred.
     */
    @SuppressWarnings("SleepWhileInLoop")
    public void scan(final @Nonnegative String scanFrom) throws Exception {

        String since = scanFrom;
        while (true) {

            try {
                since = process(since);
            } catch (ReSTException re) {
                switch (re.status) {
                    case C500_SERVER_INTERNAL_ERROR:
                    case C503_SERVICE_UNAVAILABLE:
                    case C521_WEB_SERVER_IS_DOWN:
                    case C599_TIMED_OUT_SERVER_NETWORK_CONNECT:
                        LOGGER.warn("scan failed, retrying", re);
                        Thread.sleep((long) (120000 * Math.random()) + 1);

                    default:
                        LOGGER.error("Permanent issue", re);
                        throw re;
                }
            }
        }
    }

    /**
     *
     * @param since the time to scan from.
     *
     * @return the last transaction time of any record on the server.
     *
     * @throws Exception a issue as occurred.
     */
    public @Nonnull String process(final @Nonnull String since) throws Exception {

        long nextSince;
        String callURL = "/ReST/v6/sync/Job";

        while (true) {

            ReST.Builder b = ReST.builder(host + callURL)
                    .setAuthorization(username, password)
                    .setParameter("since", since)
                    .setParameter("block", "2 minute");

            JSONObject json = b.getResponseAndCheck().getContentAsJSON();

            JSONArray resultsArray = json.getJSONArray("results");

            int arrayLength = resultsArray.length();

            for (int pos = 0; pos < arrayLength; pos++) {

                JSONObject transaction = resultsArray.getJSONObject(pos);

                processJob(transaction.getString("_href"));
            }

            if (json.has("next")) {
                callURL = json.getString("next");
            } else {
                nextSince = json.getLong("since");
                break;
            }
        }
        assert nextSince > 0;
        return Long.toString(nextSince);

    }

    /**
     *
     * @param call ReST URL
     *
     * @throws Exception
     */
    private void processJob(final @Nonnull String call) throws Exception {

        JSONObject json;
        Response r = ReST
                .builder(host + call)
                .setAuthorization(username, password)
                .getResponse();

        if (r.status == Status.C404_ERROR_NOT_FOUND) {
            LOGGER.info(r.status);
        }
        r.checkStatus();

        json = r.getContentAsJSON();

        // DO STUFF 
        LOGGER.info(json.toString(2));
    }

}
