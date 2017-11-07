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

 @TODO look at the msDelta program 
 */
package com.aspc.samplejobsync;

import com.aspc.remote.application.AppCmdLine;
import com.aspc.remote.database.InvalidDataException;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.FileUtil;
import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.util.misc.TimeUtil;
import java.io.File;
import java.util.Date;
import javax.annotation.Nonnull;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author parminder
 */
public class App extends AppCmdLine {

    private static final Log LOGGER = CLogger.getLog(App.class.toString());//#LOGGER-NOPMD

    private JSONObject config;

    private String remoteURL;

    @Override
    protected void addExtraOptions(final Options options) {
        super.addExtraOptions(options);
        Option configOption = new Option("c", true, "Config file");
        options.addOption(configOption);
    }

    /**
     * handle the command line args
     *
     * @param line the command line
     * @throws Exception a serious problem
     */
    @Override
    public void handleCommandLine(final CommandLine line) throws Exception {
        super.handleCommandLine(line);
        String configFileName = line.getOptionValue("c");

        if (StringUtilities.isBlank(configFileName)) {
            throw new Exception("configuration file is mandatory");
        }

        File f = new File(configFileName);
        if (f.canRead() == false || f.isFile() == false) {
            throw new Exception("not a valid file: " + f);
        }

        config = new JSONObject(FileUtil.readFile(f));
    }

    @Override
    public void process() throws Exception {
        
        remoteURL = config.getString("remoteURL");
        
        String sinceString = "";
        try {
            sinceString = config.getString("since");
        } catch (JSONException je) {
            LOGGER.info(je.getMessage() + " Default since: last 24 hours");
        }
        
        ScanJob scanJob = new ScanJob(remoteURL, config.getString("username"), config.getString("password"));
        
        long since = getDate(sinceString).getTime();

        while (true) {
            since = scanJob.process(since);
        }
    }
    
    /*
     * Accepted formats for date/timestamp string.
     * 
     * long    :- 1430549369303,
     * date    :- 2016-11-06T08:49:37Z,
     * strings :- -7 Days, -1 day, -5 Hours, -15 seconds, -20 Secs,
     *            -5 Mins & -1 Minute
     */
    public Date getDate(final @Nonnull String sinceString) throws InvalidDataException {

        Date since;

        if (StringUtilities.notBlank(sinceString)) {

            Date now = new Date();

            if (sinceString.startsWith("-")) {
                since = TimeUtil.subtractDurationFromDate(now, sinceString.substring(1), null);
            } else {
                try {
                    since = TimeUtil.parseUserTime(sinceString, null);
                } catch (InvalidDataException invalidDataException) {
                    throw new InvalidDataException(invalidDataException.getMessage());
                }
            }

            if (since == null) {
                throw new InvalidDataException("'since' is null");
            } else if (since.after(now)) {
                throw new InvalidDataException("'since' cannot be after now");
            }

        } else {
            since = TimeUtil.addDurationToDate(new Date(), "-24", null);
        }

        return since;
    }

    
    /**
     * The main for the program
     *
     * @param args The command line arguments
     * @throws Exception a serious problem.
     */
    public static void main(String[] args) throws Exception {
        new App().execute(args);
    }
}
