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

import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.env.ReleaseEnvironment;
import org.apache.maven.shared.release.policy.PolicyException;
import org.apache.maven.shared.release.policy.version.VersionPolicy;
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest;
import org.apache.maven.shared.release.policy.version.VersionPolicyResult;
import org.apache.maven.shared.release.scm.ReleaseScmRepositoryException;
import org.apache.maven.shared.release.scm.ScmRepositoryConfigurator;
import org.apache.maven.shared.release.util.ReleaseUtil;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.Interpolator;
import org.codehaus.plexus.interpolation.PrefixAwareRecursionInterceptor;
import org.codehaus.plexus.interpolation.PrefixedPropertiesValueSource;
import org.codehaus.plexus.interpolation.RecursionInterceptor;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.codehaus.plexus.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Input any variables that were not yet configured.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class InputVariablesPhase
    extends AbstractReleasePhase
{
    /**
     * Component used to prompt for input.
     */
    @Requirement
    private Prompter prompter;

    /**
     * Whether this is a branch or a tag operation.
     */
    private boolean branchOperation;

    /**
     * Component used for custom or default version policy
     */
    private Map<String, VersionPolicy> versionPolicies;

    /**
     * Tool that gets a configured SCM repository from release configuration.
     */
    @Requirement
    private ScmRepositoryConfigurator scmRepositoryConfigurator;

    void setPrompter( Prompter prompter )
    {
        this.prompter = prompter;
    }

    boolean isBranchOperation()
    {
        return branchOperation;
    }

    protected ScmProvider getScmProvider( ReleaseDescriptor releaseDescriptor, ReleaseEnvironment releaseEnvironment )
        throws ReleaseScmRepositoryException, ReleaseExecutionException
    {
        try
        {
            ScmRepository repository =
                scmRepositoryConfigurator.getConfiguredRepository( releaseDescriptor,
                                                                   releaseEnvironment.getSettings() );

            return scmRepositoryConfigurator.getRepositoryProvider( repository );
        }
        catch ( ScmRepositoryException e )
        {
            throw new ReleaseScmRepositoryException(
                e.getMessage() + " for URL: " + releaseDescriptor.getScmSourceUrl(), e.getValidationMessages() );
        }
        catch ( NoSuchScmProviderException e )
        {
            throw new ReleaseExecutionException( "Unable to configure SCM repository: " + e.getMessage(), e );
        }
    }

    public ReleaseResult execute( ReleaseDescriptor releaseDescriptor, ReleaseEnvironment releaseEnvironment,
                                  List<MavenProject> reactorProjects )
        throws ReleaseExecutionException
    {
        ReleaseResult result = new ReleaseResult();

        // get the root project
        MavenProject project = ReleaseUtil.getRootProject( reactorProjects );

        String tag = releaseDescriptor.getScmReleaseLabel();

        // Must get default version from mapped versions, as the project will be the incorrect snapshot
        String key = ArtifactUtils.versionlessKey( project.getGroupId(), project.getArtifactId() );
        String releaseVersion = (String) releaseDescriptor.getReleaseVersions().get( key );

        if ( releaseVersion == null )
        {
            throw new ReleaseExecutionException( "Project tag cannot be selected if version is not yet mapped" );
        }

        String scmTagNameFormat = releaseDescriptor.getScmTagNameFormat();
        String defaultTag;

        if ( scmTagNameFormat != null )
        {
            Interpolator interpolator = new StringSearchInterpolator( "@{", "}" );
            List<String> possiblePrefixes = java.util.Arrays.asList( "project", "pom" );
            Properties values = new Properties();
            values.setProperty( "artifactId", project.getArtifactId() );
            values.setProperty( "groupId", project.getGroupId() );
            values.setProperty( "version", releaseVersion );
            interpolator.addValueSource( new PrefixedPropertiesValueSource( possiblePrefixes, values, true ) );
            RecursionInterceptor recursionInterceptor = new PrefixAwareRecursionInterceptor( possiblePrefixes );
            try
            {
                defaultTag = interpolator.interpolate( scmTagNameFormat, recursionInterceptor );
            }
            catch ( InterpolationException e )
            {
                throw new ReleaseExecutionException(
                    "Could not interpolate specified tag name format: " + scmTagNameFormat, e );
            }
        }
        else
        {
            defaultTag = project.getArtifactId() + "-" + releaseVersion;
        }

        ScmProvider provider = null;
        try
        {
            provider = getScmProvider( releaseDescriptor, releaseEnvironment );
        }
        catch ( ReleaseScmRepositoryException e )
        {
            throw new ReleaseExecutionException(
                "No scm provider can be found for url: " + releaseDescriptor.getScmSourceUrl(), e );
        }

        defaultTag = provider.sanitizeTagName( defaultTag );

        String policyTag = null;

        try
        {
            policyTag = resolveTagOrBranchVersion( tag, releaseVersion, releaseDescriptor.getProjectVersionPolicyId() );
        }
        catch ( VersionParseException e )
        {
            if ( !releaseDescriptor.isInteractive() )
            {
                throw new ReleaseExecutionException( "Error parsing version: " + e.getMessage(), e );
            }
        }
        catch ( PolicyException e )
        {
            throw new ReleaseExecutionException( e.getMessage(), e );
        }

        tag = StringUtils.isEmpty( policyTag ) ? tag : policyTag;

        if ( releaseDescriptor.isInteractive() )
        {
            String interactiveTag = null;
            try
            {
                if ( branchOperation )
                {
                    interactiveTag = prompter.prompt( "What is the branch name for \"" + project.getName() + "\"? ("
                            + project.getGroupId() + ":" + project.getArtifactId() + ")" );

                    if ( StringUtils.isEmpty( interactiveTag ) )
                    {
                        throw new ReleaseExecutionException( "No branch name was given." );
                    }
                }
                else
                {
                    interactiveTag = prompter.prompt( "What is the SCM release tag or label for \"" + project.getName()
                            + "\"? (" + project.getGroupId() + ":" + project.getArtifactId() + ")", defaultTag );
                }
            }
            catch ( PrompterException e )
            {
                throw new ReleaseExecutionException( "Error reading version from input handler: " + e.getMessage(), e );
            }

            tag = StringUtils.isEmpty( interactiveTag ) ? tag : interactiveTag;
        }

        if ( tag == null )
        {
            if ( branchOperation )
            {
                throw new ReleaseExecutionException( "No branch name was given." );
            }
            else
            {
                tag = defaultTag;
            }
        }

        releaseDescriptor.setScmReleaseLabel( tag );

        result.setResultCode( ReleaseResult.SUCCESS );

        return result;
    }

    private String resolveTagOrBranchVersion( String defaultTag, String projectVersion, String policyId )
        throws PolicyException, VersionParseException
    {
        VersionPolicy policy = versionPolicies.get( policyId );
        if ( policy == null )
        {
            throw new PolicyException( "Policy '" + policyId + "' is unknown, available: " + versionPolicies.keySet() );
        }

        VersionPolicyRequest request = new VersionPolicyRequest()
            .setVersion( projectVersion )
            .setProposal( defaultTag );

        VersionPolicyResult result = policy.getBranchOrTagVersion( request );
        if ( result == null || result.getVersion() == null )
        {
            return defaultTag;
        }
        else
        {
            return result.getVersion();
        }
    }

    public ReleaseResult simulate( ReleaseDescriptor releaseDescriptor, ReleaseEnvironment releaseEnvironment,
                                   List<MavenProject> reactorProjects )
        throws ReleaseExecutionException
    {
        ReleaseResult result = new ReleaseResult();

        // It makes no modifications, so simulate is the same as execute
        execute( releaseDescriptor, releaseEnvironment, reactorProjects );

        result.setResultCode( ReleaseResult.SUCCESS );

        return result;
    }

}
