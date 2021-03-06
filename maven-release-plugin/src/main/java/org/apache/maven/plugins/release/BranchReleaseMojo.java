package org.apache.maven.plugins.release;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.ReleaseFailureException;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.codehaus.plexus.util.StringUtils;

/**
 * Branch a project in SCM.
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @aggregator
 * @goal branch
 */
public class BranchReleaseMojo
    extends AbstractReleaseMojo
{
    /**
     * The branch name to use.
     *
     * @parameter expression="${branchName}"
     */
    private String branchName;

    /**
     * Whether to update versions in the branch.
     *
     * @parameter expression="${updateBranchVersions}" default-value="false"
     */
    private boolean updateBranchVersions;

    /**
     * Whether to update versions in the working copy.
     *
     * @parameter expression="${updateWorkingCopyVersions}" default-value="true"
     */
    private boolean updateWorkingCopyVersions;

    /**
     * Whether to update versions to SNAPSHOT in the branch.
     *
     * @parameter expression="${updateVersionsToSnapshot}" default-value="true"
     */
    private boolean updateVersionsToSnapshot;

    /**
     * Whether to use "edit" mode on the SCM, to lock the file for editing during SCM operations.
     *
     * @parameter expression="${useEditMode}" default-value="false"
     */
    private boolean useEditMode;

    /**
     * Whether to update dependencies version to the next development version.
     *
     * @parameter expression="${updateDependencies}" default-value="true"
     */
    private boolean updateDependencies;

    /**
     * Whether to automatically assign submodules the parent version.  If set to false,
     * the user will be prompted for the version of each submodules.
     *
     * @parameter expression="${autoVersionSubmodules}" default-value="false"
     */
    private boolean autoVersionSubmodules;

    /**
     * Dry run: don't checkin or tag anything in the scm repository, or modify the checkout.
     * Running <code>mvn -DdryRun=true release:prepare</code> is useful in order to check that modifications to
     * poms and scm operations (only listed on the console) are working as expected.
     * Modified POMs are written alongside the originals without modifying them.
     *
     * @parameter expression="${dryRun}" default-value="false"
     */
    private boolean dryRun;

    /**
     * Whether to add a schema to the POM if it was previously missing on release.
     *
     * @parameter expression="${addSchema}" default-value="true"
     */
    private boolean addSchema;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        super.execute();

        if ( StringUtils.isEmpty( branchName ) )
        {
            throw new MojoExecutionException( "The branch name is required." );
        }

        ReleaseDescriptor config = createReleaseDescriptor();
        config.setAddSchema( addSchema );
        config.setScmUseEditMode( useEditMode );
        config.setUpdateDependencies( updateDependencies );
        config.setAutoVersionSubmodules( autoVersionSubmodules );
        config.setScmReleaseLabel( branchName );
        config.setBranchCreation( true );
        config.setUpdateBranchVersions( updateBranchVersions );
        config.setUpdateWorkingCopyVersions( updateWorkingCopyVersions );
        config.setUpdateVersionsToSnapshot( updateVersionsToSnapshot );

        try
        {
            releaseManager.branch( config, settings, reactorProjects, dryRun );
        }
        catch ( ReleaseExecutionException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( ReleaseFailureException e )
        {
            e.printStackTrace();
            throw new MojoFailureException( e.getMessage() );
        }
    }
}
