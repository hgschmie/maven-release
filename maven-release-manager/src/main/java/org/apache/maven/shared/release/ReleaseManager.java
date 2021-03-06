package org.apache.maven.shared.release;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.settings.Settings;
import org.apache.maven.shared.release.config.ReleaseDescriptor;

import java.util.List;

/**
 * Release management classes.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public interface ReleaseManager
{
    /**
     * The Plexus role.
     */
    String ROLE = ReleaseManager.class.getName();

    /**
     * Prepare a release.
     *
     * @param releaseDescriptor the configuration to pass to the preparation steps
     * @param settings          the settings.xml configuration
     * @param reactorProjects   the reactor projects
     * @throws ReleaseExecutionException if there is a problem performing the release
     * @throws ReleaseFailureException   if there is a problem performing the release
     */
    void prepare( ReleaseDescriptor releaseDescriptor, Settings settings, List reactorProjects )
        throws ReleaseExecutionException, ReleaseFailureException;

    /**
     * Prepare a release.
     *
     * @param releaseDescriptor the configuration to pass to the preparation steps
     * @param settings          the settings.xml configuration
     * @param reactorProjects   the reactor projects
     * @param resume            resume a previous release, if the properties file exists
     * @param dryRun            do not commit any changes to the file system or SCM
     * @throws ReleaseExecutionException if there is a problem performing the release
     * @throws ReleaseFailureException   if there is a problem performing the release
     */
    void prepare( ReleaseDescriptor releaseDescriptor, Settings settings, List reactorProjects, boolean resume,
                  boolean dryRun )
        throws ReleaseExecutionException, ReleaseFailureException;

    /**
     * Prepare a release.
     *
     * @param releaseDescriptor the configuration to pass to the preparation steps
     * @param settings          the settings.xml configuration
     * @param reactorProjects   the reactor projects
     * @param resume            resume a previous release, if the properties file exists
     * @param dryRun            do not commit any changes to the file system or SCM
     * @param listener          the listener
     * @throws ReleaseExecutionException if there is a problem performing the release
     * @throws ReleaseFailureException   if there is a problem performing the release
     */
    void prepare( ReleaseDescriptor releaseDescriptor, Settings settings, List reactorProjects, boolean resume,
                  boolean dryRun, ReleaseManagerListener listener )
        throws ReleaseExecutionException, ReleaseFailureException;

    ReleaseResult prepareWithResult( ReleaseDescriptor releaseDescriptor, Settings settings, List reactorProjects,
                                     boolean resume, boolean dryRun, ReleaseManagerListener listener );

    /**
     * Perform a release.
     *
     * @param releaseDescriptor the configuration to use for release
     * @param settings          the settings.xml configuration
     * @param reactorProjects   the reactor projects
     * @throws ReleaseExecutionException if there is a problem performing the release
     * @throws ReleaseFailureException   if there is a problem performing the release
     */
    void perform( ReleaseDescriptor releaseDescriptor, Settings settings, List reactorProjects )
        throws ReleaseExecutionException, ReleaseFailureException;

    /**
     * Perform a release.
     *
     * @param releaseDescriptor the configuration to use for release
     * @param settings          the settings.xml configuration
     * @param reactorProjects   the reactor projects
     * @param listener          the listener
     * @throws ReleaseExecutionException if there is a problem performing the release
     * @throws ReleaseFailureException   if there is a problem performing the release
     */
    void perform( ReleaseDescriptor releaseDescriptor, Settings settings, List reactorProjects,
                  ReleaseManagerListener listener )
        throws ReleaseExecutionException, ReleaseFailureException;

    ReleaseResult performWithResult( ReleaseDescriptor releaseDescriptor, Settings settings, List reactorProjects,
                                     ReleaseManagerListener listener );

    /**
     * Clean a release.
     *
     * @param releaseDescriptor the configuration to use for release
     * @param reactorProjects   the reactor projects
     */
    void clean( ReleaseDescriptor releaseDescriptor, ReleaseManagerListener listener, List reactorProjects );

    /**
     * Rollback changes made by the previous release
     *
     * @param releaseDescriptor the configuration to use for release
     * @param settings          the settings.xml configuration
     * @param reactorProjects   the reactor projects
     * @throws ReleaseExecutionException if there is a problem during release rollback
     * @throws ReleaseFailureException   if there is a problem during release rollback
     */
    void rollback( ReleaseDescriptor releaseDescriptor, Settings settings, List reactorProjects )
        throws ReleaseExecutionException, ReleaseFailureException;

    /**
     * Rollback changes made by the previous release
     *
     * @param releaseDescriptor the configuration to use for release
     * @param settings          the settings.xml configuration
     * @param reactorProjects   the reactor projects
     * @param listener          the listener
     * @throws ReleaseExecutionException if there is a problem during release rollback
     * @throws ReleaseFailureException   if there is a problem during release rollback
     */
    void rollback( ReleaseDescriptor releaseDescriptor, Settings settings, List reactorProjects,
                   ReleaseManagerListener listener )
        throws ReleaseExecutionException, ReleaseFailureException;

    /**
     * Branch a project
     *
     * @param releaseDescriptor the configuration to use for release
     * @param settings          the settings.xml configuration
     * @param reactorProjects   the reactor projects
     * @param dryRun            do not commit any changes to the file system or SCM
     * @throws ReleaseExecutionException if there is a problem during release rollback
     * @throws ReleaseFailureException   if there is a problem during release rollback
     */
    void branch( ReleaseDescriptor releaseDescriptor, Settings settings, List reactorProjects, boolean dryRun )
        throws ReleaseExecutionException, ReleaseFailureException;

    /**
     * Branch a project
     *
     * @param releaseDescriptor the configuration to use for release
     * @param settings          the settings.xml configuration
     * @param reactorProjects   the reactor projects
     * @param dryRun            do not commit any changes to the file system or SCM
     * @param listener          the listener
     * @throws ReleaseExecutionException if there is a problem during release rollback
     * @throws ReleaseFailureException   if there is a problem during release rollback
     */
    void branch( ReleaseDescriptor releaseDescriptor, Settings settings, List reactorProjects, boolean dryRun,
                 ReleaseManagerListener listener )
        throws ReleaseExecutionException, ReleaseFailureException;
}
