package org.apache.maven.shared.release.phase;

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

import static org.junit.Assert.*;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.release.PlexusJUnit4TestCase;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.env.DefaultReleaseEnvironment;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.junit.Test;

/**
 * Test the variable input phase.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class BranchInputVariablesPhaseTest
    extends PlexusJUnit4TestCase
{
    private InputVariablesPhase phase;

    public void setUp()
        throws Exception
    {
        super.setUp();
        phase = (InputVariablesPhase) lookup( ReleasePhase.ROLE, "branch-input-variables" );
    }

    @Test
    public void testInputVariablesInteractive()
        throws Exception
    {
        // prepare
        Prompter mockPrompter = mock( Prompter.class );
        when( mockPrompter.prompt( isA( String.class ) ) ).thenReturn( "tag-value", "simulated-tag-value" );
        phase.setPrompter( mockPrompter );

        List<MavenProject> reactorProjects = Collections.singletonList( createProject( "artifactId", "1.0" ) );

        ReleaseDescriptor releaseDescriptor = new ReleaseDescriptor();
        releaseDescriptor.mapReleaseVersion( "groupId:artifactId", "1.0" );
        releaseDescriptor.setScmSourceUrl( "scm:svn:file://localhost/tmp/scm-repo" );

        // execute
        phase.execute( releaseDescriptor, new DefaultReleaseEnvironment(), reactorProjects );

        // verify
        assertEquals( "Check tag", "tag-value", releaseDescriptor.getScmReleaseLabel() );

        // prepare
        releaseDescriptor = new ReleaseDescriptor();
        releaseDescriptor.mapReleaseVersion( "groupId:artifactId", "1.0" );
        releaseDescriptor.setScmSourceUrl( "scm:svn:file://localhost/tmp/scm-repo" );

        // execute
        phase.simulate( releaseDescriptor, new DefaultReleaseEnvironment(), reactorProjects );

        // verify
        assertEquals( "Check tag", "simulated-tag-value", releaseDescriptor.getScmReleaseLabel() );

        verify( mockPrompter, times( 2 ) ).prompt( isA( String.class ) );
        verifyNoMoreInteractions( mockPrompter );
    }

    @Test
    public void testUnmappedVersion()
        throws Exception
    {
        List<MavenProject> reactorProjects = Collections.singletonList( createProject( "artifactId", "1.0" ) );

        ReleaseDescriptor releaseDescriptor = new ReleaseDescriptor();

        try
        {
            phase.execute( releaseDescriptor, new DefaultReleaseEnvironment(), reactorProjects );

            fail( "Expected an exception" );
        }
        catch ( ReleaseExecutionException e )
        {
            assertNull( "check no cause", e.getCause() );
        }

        releaseDescriptor = new ReleaseDescriptor();

        try
        {
            phase.simulate( releaseDescriptor, new DefaultReleaseEnvironment(), reactorProjects );

            fail( "Expected an exception" );
        }
        catch ( ReleaseExecutionException e )
        {
            assertNull( "check no cause", e.getCause() );
        }
    }

    @Test
    public void testInputVariablesNonInteractiveConfigured()
        throws Exception
    {
        // prepare
        Prompter mockPrompter = mock( Prompter.class );
        phase.setPrompter( mockPrompter );

        List<MavenProject> reactorProjects = Collections.singletonList( createProject( "artifactId", "1.0" ) );

        ReleaseDescriptor releaseDescriptor = new ReleaseDescriptor();
        releaseDescriptor.mapReleaseVersion( "groupId:artifactId", "1.0" );
        releaseDescriptor.setInteractive( false );
        releaseDescriptor.setScmReleaseLabel( "tag-value" );
        releaseDescriptor.setScmSourceUrl( "scm:svn:file://localhost/tmp/scm-repo" );

        // execute
        phase.execute( releaseDescriptor, new DefaultReleaseEnvironment(), reactorProjects );

        // verify
        assertEquals( "Check tag", "tag-value", releaseDescriptor.getScmReleaseLabel() );

        // prepare
        releaseDescriptor = new ReleaseDescriptor();
        releaseDescriptor.mapReleaseVersion( "groupId:artifactId", "1.0" );
        releaseDescriptor.setInteractive( false );
        releaseDescriptor.setScmReleaseLabel( "simulated-tag-value" );
        releaseDescriptor.setScmSourceUrl( "scm:svn:file://localhost/tmp/scm-repo" );

        // execute
        phase.simulate( releaseDescriptor, new DefaultReleaseEnvironment(), reactorProjects );

        // verify
        assertEquals( "Check tag", "simulated-tag-value", releaseDescriptor.getScmReleaseLabel() );

        // never use prompter
        verifyNoMoreInteractions( mockPrompter );
    }

    @Test
    public void testInputVariablesInteractiveConfigured()
        throws Exception
    {
        // prepare
        Prompter mockPrompter = mock( Prompter.class );
        phase.setPrompter( mockPrompter );

        List<MavenProject> reactorProjects = Collections.singletonList( createProject( "artifactId", "1.0" ) );

        ReleaseDescriptor releaseDescriptor = new ReleaseDescriptor();
        releaseDescriptor.mapReleaseVersion( "groupId:artifactId", "1.0" );
        releaseDescriptor.setScmReleaseLabel( "tag-value" );
        releaseDescriptor.setScmSourceUrl( "scm:svn:file://localhost/tmp/scm-repo" );

        // execute
        phase.execute( releaseDescriptor, new DefaultReleaseEnvironment(), reactorProjects );

        // verify
        assertEquals( "Check tag", "tag-value", releaseDescriptor.getScmReleaseLabel() );

        // prepare
        releaseDescriptor = new ReleaseDescriptor();
        releaseDescriptor.mapReleaseVersion( "groupId:artifactId", "1.0" );
        releaseDescriptor.setScmReleaseLabel( "simulated-tag-value" );
        releaseDescriptor.setScmSourceUrl( "scm:svn:file://localhost/tmp/scm-repo" );

        // execute
        phase.simulate( releaseDescriptor, new DefaultReleaseEnvironment(), reactorProjects );

        // verify
        assertEquals( "Check tag", "simulated-tag-value", releaseDescriptor.getScmReleaseLabel() );

        // never use prompter
        verifyNoMoreInteractions( mockPrompter );
    }

    @Test
    public void testPrompterException()
        throws Exception
    {
        // prepare
        Prompter mockPrompter = mock( Prompter.class );
        when( mockPrompter.prompt( isA( String.class ),
                                   isA( String.class ) ) ).thenThrow( new PrompterException( "..." ) );
        phase.setPrompter( mockPrompter );

        List<MavenProject> reactorProjects = Collections.singletonList( createProject( "artifactId", "1.0" ) );

        ReleaseDescriptor releaseDescriptor = new ReleaseDescriptor();
        releaseDescriptor.mapReleaseVersion( "groupId:artifactId", "1.0" );
        releaseDescriptor.setScmSourceUrl( "scm:svn:file://localhost/tmp/scm-repo" );

        // execute
        try
        {
            phase.execute( releaseDescriptor, new DefaultReleaseEnvironment(), reactorProjects );

            fail( "Expected an exception" );
        }
        catch ( ReleaseExecutionException e )
        {
            assertEquals( "No branch name was given.", e.getMessage() );
        }

        // prepare
        releaseDescriptor = new ReleaseDescriptor();
        releaseDescriptor.mapReleaseVersion( "groupId:artifactId", "1.0" );
        releaseDescriptor.setScmSourceUrl( "scm:svn:file://localhost/tmp/scm-repo" );

        // execute
        try
        {
            phase.simulate( releaseDescriptor, new DefaultReleaseEnvironment(), reactorProjects );

            fail( "Expected an exception" );
        }
        catch ( ReleaseExecutionException e )
        {
            assertEquals( "No branch name was given.", e.getMessage() );
        }

        // verify
        verify( mockPrompter, times( 2 ) ).prompt( isA( String.class ) );
        verifyNoMoreInteractions( mockPrompter );
    }

    @Test
    public void testBranchOperation()
        throws Exception
    {
        assertTrue( phase.isBranchOperation() );
    }

    @Test
    public void testEmptyBranchName()
        throws Exception
    {
        // prepare
        Prompter mockPrompter = mock( Prompter.class );
        phase.setPrompter( mockPrompter );

        List<MavenProject> reactorProjects = Collections.singletonList( createProject( "artifactId", "1.0" ) );

        ReleaseDescriptor releaseDescriptor = new ReleaseDescriptor();
        releaseDescriptor.setInteractive( false );
        releaseDescriptor.setScmReleaseLabel( null );
        releaseDescriptor.mapReleaseVersion( "groupId:artifactId", "1.0" );
        releaseDescriptor.setScmSourceUrl( "scm:svn:file://localhost/tmp/scm-repo" );

        // execute
        try
        {
            phase.execute( releaseDescriptor, new DefaultReleaseEnvironment(), reactorProjects );

            fail( "Expected an exception" );
        }
        catch ( ReleaseExecutionException e )
        {
            assertEquals( "No branch name was given.", e.getMessage() );
        }

        // prepare
        releaseDescriptor = new ReleaseDescriptor();
        releaseDescriptor.setInteractive( false );
        releaseDescriptor.setScmReleaseLabel( null );
        releaseDescriptor.mapReleaseVersion( "groupId:artifactId", "1.0" );
        releaseDescriptor.setScmSourceUrl( "scm:svn:file://localhost/tmp/scm-repo" );

        // execute
        try
        {
            phase.simulate( releaseDescriptor, new DefaultReleaseEnvironment(), reactorProjects );

            fail( "Expected an exception" );
        }
        catch ( ReleaseExecutionException e )
        {
            assertEquals( "No branch name was given.", e.getMessage() );
        }

        // never use prompter
        verifyNoMoreInteractions( mockPrompter );
    }

    private static MavenProject createProject( String artifactId, String version )
    {
        Model model = new Model();
        model.setGroupId( "groupId" );
        model.setArtifactId( artifactId );
        model.setVersion( version );
        return new MavenProject( model );
    }
}
