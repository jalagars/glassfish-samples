pipeline {
   agent any
   tools {
      maven "Maven 3.6.3"
      jdk "default"
   }
   environment {
      gitrepo = "https://github.com/jalagars/glassfish-samples.git"
      artifactoryCredential = 'JAY_AWS_ARTIFACTORY_SERVER'
      artifactoryServerUrl = 'http://ec2-3-92-243-157.compute-1.amazonaws.com:8082/artifactory'
  }
   //added comment
   stages {
      stage('Git') {
         steps {
            // Get some code from a GitHub repository
            git "${env.gitrepo}"
         }
      }
       stage ('Artifactory configuration') {
            steps {
                rtServer (
                    id: "JAY_AWS_ARTIFACTORY_SERVER",
                    url: artifactoryServerUrl,
                    credentialsId: artifactoryCredential
                )

                rtMavenDeployer (
                    id: "MAVEN_DEPLOYER",
                    serverId: "JAY_AWS_ARTIFACTORY_SERVER",
                    releaseRepo: "libs-release-local",
                    snapshotRepo: "libs-snapshot-local"
                )

                rtMavenResolver (
                    id: "MAVEN_RESOLVER",
                    serverId: "JAY_AWS_ARTIFACTORY_SERVER",
                    releaseRepo: "libs-release",
                    snapshotRepo: "libs-snapshot"
                )
            }
        }
        stage ('Exec Maven') {
            steps {
                rtMavenRun (
                    tool: 'Maven 3.6.3', // Tool name from Jenkins configuration
                    pom: './ws/javaee8/jsf/dateTimeConverter/pom.xml',
                    goals: 'clean install',
                    deployerId: "MAVEN_DEPLOYER",
                    resolverId: "MAVEN_RESOLVER"
                )
            }
        }
        stage ('Publish build info to Artifactory') {
            steps {
                rtPublishBuildInfo (
                    serverId: "JAY_AWS_ARTIFACTORY_SERVER"
                )
            }
        }
   }
}
