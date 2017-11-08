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

import com.aspc.remote.application.AppCmdLine;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.FileUtil;
import com.aspc.remote.util.misc.StringUtilities;
import java.io.File;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.json.JSONObject;

/**
 * <h1>SampleSyncApp</h1>
 *
 * The SampleSyncApp program implements an application 
 * that simply gets configuration from JSON file which 
 * calls SampleSync class.
 *
 * @author parminder
 */
public class SampleSyncApp extends AppCmdLine {

    private static final Log LOGGER = CLogger.getLog("src.main.java.com.aspc.SampleSyncApp");//#LOGGER-NOPMD

    private JSONObject config;

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
        
        String host = config.getString("host");
        String username = config.getString("username");
        String password = config.getString("password");
        String since = config.optString("since");

        SampleSync sampleSync = new SampleSync(host, username, password);
        sampleSync.scan(since);
    }

    /**
     * The main for the program
     *
     * @param args The command line arguments
     * @throws Exception a serious problem.
     */
    public static void main(String[] args) throws Exception {
        new SampleSyncApp().execute(args);
    }
}
