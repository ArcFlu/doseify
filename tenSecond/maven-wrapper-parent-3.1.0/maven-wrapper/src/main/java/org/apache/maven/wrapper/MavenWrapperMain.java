package org.apache.maven.wrapper;

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

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.wrapper.cli.CommandLineParser;
import org.apache.maven.wrapper.cli.SystemPropertiesCommandLineConverter;

/**
 * Main entry point for the Maven Wrapper, delegating wrapper execution to {@link WrapperExecutor}.
 *
 * @author Hans Dockter
 */
public class MavenWrapperMain
{
    private static final String POM_PROPERTIES = "/META-INF/maven/org.apache.maven.wrapper/"
        + "maven-wrapper/pom.properties";

    public static final String DEFAULT_MAVEN_USER_HOME = System.getProperty( "user.home" ) + "/.m2";

    public static final String MAVEN_USER_HOME_PROPERTY_KEY = "maven.user.home";

    public static final String MAVEN_USER_HOME_ENV_KEY = "MAVEN_USER_HOME";

    public static final String MVNW_VERBOSE = "MVNW_VERBOSE";

    public static final String MVNW_USERNAME = "MVNW_USERNAME";

    public static final String MVNW_PASSWORD = "MVNW_PASSWORD";

    public static final String MVNW_REPOURL = "MVNW_REPOURL";

    public static void main( String[] args )
        throws Exception
    {
        File wrapperJar = wrapperJar();
        File propertiesFile = wrapperProperties( wrapperJar );
        File rootDir = rootDir( wrapperJar );

        String wrapperVersion = wrapperVersion();
        Logger.info( "Apache Maven Wrapper " + wrapperVersion );

        Properties systemProperties = System.getProperties();
        systemProperties.putAll( parseSystemPropertiesFromArgs( args ) );

        addSystemProperties( rootDir );

        WrapperExecutor wrapperExecutor = WrapperExecutor.forWrapperPropertiesFile( propertiesFile, System.out );
        wrapperExecutor.execute( args, new Installer( new DefaultDownloader( "mvnw", wrapperVersion ),
                                                      new PathAssembler( mavenUserHome() ) ),
                                 new BootstrapMainStarter() );
    }

    private static Map<String, String> parseSystemPropertiesFromArgs( String[] args )
    {
        SystemPropertiesCommandLineConverter converter = new SystemPropertiesCommandLineConverter();
        CommandLineParser commandLineParser = new CommandLineParser();
        converter.configure( commandLineParser );
        commandLineParser.allowUnknownOptions();
        return converter.convert( commandLineParser.parse( args ) );
    }

    private static void addSystemProperties( File rootDir )
    {
        System.getProperties().putAll( SystemPropertiesHandler.getSystemProperties( new File( mavenUserHome(),
                                                                                              "maven.properties" ) ) );
        System.getProperties().putAll( SystemPropertiesHandler.getSystemProperties( new File( rootDir,
                                                                                              "maven.properties" ) ) );
    }

    private static File rootDir( File wrapperJar )
    {
        return wrapperJar.getParentFile().getParentFile().getParentFile();
    }

    private static File wrapperProperties( File wrapperJar )
    {
        return new File( wrapperJar.getParent(), wrapperJar.getName().replaceFirst( "\\.jar$", ".properties" ) );
    }

    private static File wrapperJar()
    {
        URI location;
        try
        {
            location = MavenWrapperMain.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        }
        catch ( URISyntaxException e )
        {
            throw new RuntimeException( e );
        }
        if ( !location.getScheme().equals( "file" ) )
        {
            throw new RuntimeException( String.format( "Cannot determine classpath for wrapper Jar from codebase '%s'.",
                                                       location ) );
        }
        return new File( location.getPath() );
    }

    static String wrapperVersion()
    {
        try ( InputStream resourceAsStream = MavenWrapperMain.class.getResourceAsStream( POM_PROPERTIES ) )
        {
            if ( resourceAsStream == null )
            {
                throw new IllegalStateException( POM_PROPERTIES + " not found." );
            }
            Properties mavenProperties = new Properties();
            mavenProperties.load( resourceAsStream );
            String version = mavenProperties.getProperty( "version" );
            if ( version == null )
            {
                throw new NullPointerException( "No version specified in " + POM_PROPERTIES );
            }
            return version;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Could not determine wrapper version.", e );
        }
    }

    private static File mavenUserHome()
    {
        String mavenUserHome = System.getProperty( MAVEN_USER_HOME_PROPERTY_KEY );
        if ( mavenUserHome != null )
        {
            return new File( mavenUserHome );
        }

        mavenUserHome = System.getenv( MAVEN_USER_HOME_ENV_KEY );

        return new File( mavenUserHome == null ? DEFAULT_MAVEN_USER_HOME : mavenUserHome );
    }
}
