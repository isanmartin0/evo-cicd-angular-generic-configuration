#!/usr/bin/groovy
import com.evobanco.AngularUtils
import com.evobanco.AngularConstants

def runAngularGenericJenkinsfile() {

    AngularUtils utils = new AngularUtils()

    def angularNPMRepositoryURL = 'https://digitalservices.evobanco.com/artifactory/api/npm/angular-npm-repo/'
    def angularNPMLocalRepositoryURL = 'https://digitalservices.evobanco.com/artifactory/api/npm/angular-npm-local/'
    def angularGenericLocalRepositoryURL = 'https://digitalservices.evobanco.com/artifactory/angular-generic-local/'
    def artifactoryURL = 'https://digitalservices.evobanco.com/artifactory/'
    def artifactoryRepository = ''

    def openshiftURL = 'https://openshift.grupoevo.corp:8443'
    def openshiftCredential = 'openshift'
    def registry = '172.20.253.34'
    def artifactoryCredential = 'artifactory-token'
    //def artifactoryNPMAuthCredential = 'artifactory-npm-auth'
    //def artifactoryNPMEmailAuthCredential = 'artifactory-npm-email-auth'
    def artifactoryNPMAuthCredential = 'prueba-artifactory-npm-auth'
    def artifactoryNPMEmailAuthCredential = 'prueba-artifactory-npm-email-auth'
    def jenkinsNamespace = 'cicd'
    def params
    String envLabel
    String branchName
    String branchNameHY
    String branchType


    //Parallel project configuration (PPC) properties
    def branchPPC = 'master'
    String credentialsIdPPCDefault = '4b18ea85-c50b-40f4-9a81-e89e44e20178' //credentials of the parallel configuration project
    def credentialsIdPPC
    def relativeTargetDirPPC = '/tmp/configs/PPC/'
    def isPPCJenkinsFile = false
    def isPPCJenkinsYaml = false
    def isPPCOpenshiftTemplate = false
    def jenkinsFilePathPPC = relativeTargetDirPPC + 'Jenkinsfile'
    def jenkinsYamlPathPPC = relativeTargetDirPPC + 'Jenkins.yml'
    def openshiftNginxTemplatePathPPC = relativeTargetDirPPC + 'kube/nginx_template.yaml'
    def jenknsFilePipelinePPC


    //Generic project configuration properties
    def gitDefaultProjectConfigurationPath='https://github.com/isanmartin0/evo-cicd-angular-generic-configuration'
    def relativeTargetDirGenericPGC = '/tmp/configs/generic/'
    def branchGenericPGC = 'master'
    def credentialsIdGenericPGC = '4b18ea85-c50b-40f4-9a81-e89e44e20178' //credentials of the generic configuration project
    def jenkinsYamlGenericPath = relativeTargetDirGenericPGC + 'Jenkins.yml'


    def packageJSON
    String projectURL
    String packageName
    String packageVersion
    String packageTag
    String packageTarball
    String packageViewTarball
    boolean isScopedPackage = false
    String packageScope


    //int maxOldBuildsToKeep = 0
    //int daysOldBuildsToKeep = 0

    //Taurus parameters
    def taurus_test_base_path = 'taurus'
    def acceptance_test_path = '/acceptance_test/'
    def performance_test_path = '/performance_test/'
    def smoke_test_path = '/smoke_test/'
    def security_test_path = '/security_test/'


    def openshift_route_hostname = ''
    def openshift_route_hostname_with_protocol = ''

    //Parameters Angular
    Boolean installGloballyAngularCli = false
    int image_stream_nodejs_version_default = 8
    def angularCliVersion_default = "1.7.4"
    def lintingFlags_default = "--format prose --force true"
    def unitTestingFlags_default = "--browsers ChromeHeadles --watch=false --code-coverage"
    def e2eTestingFlags_default = ""
    def buildFlags_default = "--aot=false"
    def buildEnvironmentFlag_default = "--env="

    def angularCliLocalPath = "node_modules/@angular/cli/bin/"

    def build_from_registry_url = 'https://github.com/isanmartin0/s2i-angular-container.git'
    def build_from_artifact_branch = 'master'

    def nodeJS_6_installation = "Node-6.11.3"
    def nodeJS_8_installation = "Node-8.9.4"
    def nodeJS_10_installation = "Node-10.8.0"

    def nodeJS_6_installation_angularCliVersion_default = "1.0.0"
    def nodeJS_8_installation_angularCliVersion_default = "1.7.4"
    def nodeJS_10_installation_angularCliVersion_default = "6.1.2"

    def nodeJS_6_installation_angularJson_filename = ".angular-cli.json"
    def nodeJS_8_installation_angularJson_filename = ".angular-cli.json"
    def nodeJS_10_installation_angularJson_filename = "angular.json"

    def nodeJS_6_installation_build_flags = "--aot=false"
    def nodeJS_8_installation_build_flags = "--aot=false"
    def nodeJS_10_installation_build_flags = "--build-optimizer"

    def nodeJS_6_installation_build_environment_flag = "--env="
    def nodeJS_8_installation_build_environment_flag = "--env="
    def nodeJS_10_installation_build_environment_flag = "--configuration="

    def nodeJS_6_installation_linting_flags = "--format prose --force true"
    def nodeJS_8_installation_linting_flags = "--format prose --force true"
    def nodeJS_10_installation_linting_flags = "--format prose --force true"

    def nodeJS_6_installation_unit_testing_flags = "--browsers ChromeHeadless --watch=false --code-coverage"
    def nodeJS_8_installation_unit_testing_flags = "--browsers ChromeHeadless --watch=false --code-coverage"
    def nodeJS_10_installation_unit_testing_flags = "--browsers ChromeHeadless --watch=false --code-coverage"

    def nodeJS_6_installation_e2e_testing_flags = ""
    def nodeJS_8_installation_e2e_testing_flags = ""
    def nodeJS_10_installation_e2e_testing_flags = ""

    def nodeJS_pipeline_installation = ""
    int image_stream_nodejs_version = image_stream_nodejs_version_default
    def angularCliVersion = angularCliVersion_default
    def lintingFlags = lintingFlags_default
    def unitTestingFlags = unitTestingFlags_default
    def e2eTestingFlags = e2eTestingFlags_default
    def buildFlags = buildFlags_default
    def buildEnvironmentFlag = buildEnvironmentFlag_default
    def buildEnvironment

    def buildDefaultOutputPath = ["dist/"]
    def buildOutputPath = ''

    def sonarProjectPath = "sonar-project.properties"
    def sonarQubeServer = 'sonarqube'
    def sonarScannerHome = 'SonarQube Scanner 3.1.0'
    def karmaSonarQubeReporterDefaultVersion = "1.2.0"

    def angularJSONFileName = ''
    def angularJSON

    echo "BEGIN ANGULAR GENERIC CONFIGURATION PROJECT (PGC)"


    node('openshift37-nodejs10-chrome') {

        echo 'Pipeline begin timestamp... '
        sh 'date'

        stage('Checkout') {
            echo 'Getting source code...'
            checkout scm
            projectURL = scm.userRemoteConfigs[0].url
            echo "Source code hosted in: ${projectURL}"
        }


        try {
            def credentialsIdPPCArray = scm.userRemoteConfigs.credentialsId
            credentialsIdPPC = credentialsIdPPCArray.first()
            echo "Using credentialsIdPPCDefault value for access to Parallel Project Configuration (PPC)"

        } catch (exc) {
            echo 'There is an error on retrieving credentialsId of multibranch configuration'
            def exc_message = exc.message
            echo "${exc_message}"

            credentialsIdPPC = credentialsIdPPCDefault
        }

        echo "credentialsIdPPC: ${credentialsIdPPC}"

        stage('Detect Angular Parallel project configuration (PPC)') {

            packageJSON = readJSON file: 'package.json'

            echo "package.json content:"
            echo "${packageJSON}"

            isScopedPackage = utils.isScopedPackage(packageJSON.name)
            echo "isScopedPackage: ${isScopedPackage}"

            if (isScopedPackage) {
                packageScope = utils.getPackageScope(packageJSON.name)
                echo "packageScope: ${packageScope}"
            }

            packageName = utils.getProject(packageJSON.name)
            packageVersion = packageJSON.version
            packageTag = utils.getPackageTag(packageJSON.name, packageVersion)
            packageTarball = utils.getPackageTarball(packageJSON.name, packageVersion)
            packageViewTarball = utils.getPackageViewTarball(packageJSON.name, packageVersion)


            echo "packageName: ${packageName} \n" +
                "packageVersion: ${packageVersion} \n" +
                "packageTag: ${packageTag} \n" +
                "packageTarball: ${packageTarball} \n" +
                "packageViewTarball: ${packageViewTarball}"


            try {
                def parallelConfigurationProject = utils.getParallelConfigurationProjectURL(projectURL)

                echo "Angular parallel configuration project ${parallelConfigurationProject} searching"

                retry (3)
                        {
                            checkout([$class                           : 'GitSCM',
                                      branches                         : [[name: branchPPC]],
                                      doGenerateSubmoduleConfigurations: false,
                                      extensions                       : [[$class           : 'RelativeTargetDirectory',
                                                                           relativeTargetDir: relativeTargetDirPPC]],
                                      submoduleCfg                     : [],
                                      userRemoteConfigs                : [[credentialsId: credentialsIdPPC,
                                                                           url          : parallelConfigurationProject]]])
                        }

                echo "Angular Parallel configuration project ${parallelConfigurationProject} exits"

                // Jenkinsfile
                isPPCJenkinsFile = fileExists jenkinsFilePathPPC

                if (isPPCJenkinsFile) {
                    echo "Angular Parallel configuration project Jenkinsfile... FOUND"
                } else {
                    echo "Angular Parallel configuration project Jenkinsfile... NOT FOUND"
                }


                // Jenkins.yml
                isPPCJenkinsYaml = fileExists jenkinsYamlPathPPC

                if (isPPCJenkinsYaml) {
                    echo "Angular Parallel configuration project Jenkins.yml... FOUND"
                } else {
                    echo "Angular Parallel configuration project Jenkins.yml... NOT FOUND"
                }

                // Openshift template (template.yaml)


                if (isPPCJenkinsYaml) {
                    //Take parameters of the parallel project configuration (PPC)
                    params = readYaml  file: jenkinsYamlPathPPC
                    echo "Using Jenkins.yml from Angular parallel project configuration (PPC)"

                    //The template is provided by parallel project configuration (PPC)
                    params.openshift.templatePath = relativeTargetDirPPC + params.openshift.templatePath

                    echo "params.openshift.templatePath: ${params.openshift.templatePath}"

                    isPPCOpenshiftTemplate = fileExists params.openshift.templatePath
                } else {
                    isPPCOpenshiftTemplate = fileExists openshiftNginxTemplatePathPPC
                }


                if (isPPCOpenshiftTemplate) {
                    echo "Angular Parallel configuration project Openshift template... FOUND"
                } else {
                    echo "Angular Parallel configuration project Openshift template... NOT FOUND"
                }


                echo "isPPCJenkinsFile : ${isPPCJenkinsFile}"
                echo "isPPCJenkinsYaml : ${isPPCJenkinsYaml}"
                echo "isPPCOpenshiftTemplate : ${isPPCOpenshiftTemplate}"

            }
            catch (exc) {
                echo 'There is an error on retrieving Angular parallel project configuration'
                def exc_message = exc.message
                echo "${exc_message}"
            }
        }


        if (isPPCJenkinsFile) {

            stage('Switch to Angular parallel configuration project Jenkinsfile') {

                echo "Loading Jenkinsfile from Angular Parallel Configuration Project (PPC)"

                jenknsFilePipelinePPC = load jenkinsFilePathPPC

                echo "Jenkinsfile from Angular Parallel Configuration Project (PPC) loaded"

                echo "Executing Jenkinsfile from Angular Parallel Configuration Project (PPC)"

                jenknsFilePipelinePPC.runAngularPPCJenkinsfile()
            }


        } else {
            echo "Executing Jenkinsfile from Angular Generic Configuration Project (PGC)"

            stage('Load Angular pipeline configuration') {

                if (isPPCJenkinsYaml && isPPCOpenshiftTemplate) {
                    //The generic pipeline will use Jenkins.yml and template of the parallel project configuration

                    //Take parameters of the parallel project configuration (PPC)
                    params = readYaml  file: jenkinsYamlPathPPC
                    echo "Using Jenkins.yml from Angular parallel project configuration (PPC)"

                    //The template is provided by parallel project configuration (PPC)
                    params.openshift.templatePath = relativeTargetDirPPC + params.openshift.templatePath
                    echo "Template provided by Angular parallel project configuration (PPC)"

                    assert params.openshift.templatePath?.trim()

                    echo "params.openshift.templatePath: ${params.openshift.templatePath}"

                } else {
                    //The Angular generic pipeline will useAngular generic Jenkins.yml or Angular generic Openshift template
                    //We need load this elements

                    echo "Angular generic configuration project loading"

                    retry (3) {
                        checkout([$class                           : 'GitSCM',
                                  branches                         : [[name: branchGenericPGC]],
                                  doGenerateSubmoduleConfigurations: false,
                                  extensions                       : [[$class           : 'RelativeTargetDirectory',
                                                                       relativeTargetDir: relativeTargetDirGenericPGC]],
                                  submoduleCfg                     : [],
                                  userRemoteConfigs                : [[credentialsId: credentialsIdGenericPGC,
                                                                       url          : gitDefaultProjectConfigurationPath]]])
                    }

                    echo "Angular generic configuration project loaded"


                    if (isPPCJenkinsYaml) {
                        //Take parameters of the parallel project configuration (PPC)
                        params = readYaml  file: jenkinsYamlPathPPC
                        echo "Using Jenkins.yml from Angular parallel project configuration (PPC)"
                    } else {
                        //Take the generic parameters
                        params = readYaml  file: jenkinsYamlGenericPath
                        echo "Using Jenkins.yml from Angular generic project"
                    }

                    echo "Jenkins.yml loaded\n" +
                            "------------------\n" +
                            "${params}"

                    if (isPPCOpenshiftTemplate) {
                        //The template is provided by parallel project configuration (PPC)
                        params.openshift.templatePath = relativeTargetDirPPC + params.openshift.templatePath
                        echo "Template provided by Angular parallel project configuration (PPC)"
                    } else {
                        //The tamplate is provided by generic configuration
                        params.openshift.templatePath = relativeTargetDirGenericPGC + params.openshift.templatePath
                        echo "Template provided by Angular generic configuration project"
                    }

                    assert params.openshift.templatePath?.trim()

                    echo "params.openshift.templatePath: ${params.openshift.templatePath}"
                }

            }




            stage('NodeJS initialization') {
                echo 'Node initializing...'

                /*************************************************************
                 ************* IMAGE STREAM TAG NODE VERSION *****************
                 *************************************************************/
                echo "params.imageStreamNodejsVersion: ${params.imageStreamNodejsVersion}"
                String imageStreamNodejsVersionParam = params.imageStreamNodejsVersion
                if (imageStreamNodejsVersionParam != null && imageStreamNodejsVersionParam.isInteger()) {
                    image_stream_nodejs_version = imageStreamNodejsVersionParam as Integer
                }

                if (image_stream_nodejs_version >= 10) {
                    nodeJS_pipeline_installation = nodeJS_10_installation
                    angularCliVersion = nodeJS_10_installation_angularCliVersion_default
                    lintingFlags = nodeJS_10_installation_linting_flags
                    unitTestingFlags = nodeJS_10_installation_unit_testing_flags
                    e2eTestingFlags = nodeJS_10_installation_e2e_testing_flags
                    buildFlags = nodeJS_10_installation_build_flags
                    buildEnvironmentFlag = nodeJS_10_installation_build_environment_flag
                    angularJSONFileName = nodeJS_10_installation_angularJson_filename
                } else if (image_stream_nodejs_version >= 8) {
                    nodeJS_pipeline_installation = nodeJS_8_installation
                    angularCliVersion = nodeJS_8_installation_angularCliVersion_default
                    lintingFlags = nodeJS_8_installation_linting_flags
                    unitTestingFlags = nodeJS_8_installation_unit_testing_flags
                    e2eTestingFlags = nodeJS_8_installation_e2e_testing_flags
                    buildFlags = nodeJS_8_installation_build_flags
                    buildEnvironmentFlag = nodeJS_8_installation_build_environment_flag
                    angularJSONFileName = nodeJS_8_installation_angularJson_filename
                } else if (image_stream_nodejs_version >= 6) {
                    nodeJS_pipeline_installation = nodeJS_6_installation
                    angularCliVersion = nodeJS_6_installation_angularCliVersion_default
                    lintingFlags = nodeJS_6_installation_linting_flags
                    unitTestingFlags = nodeJS_6_installation_unit_testing_flags
                    e2eTestingFlags = nodeJS_6_installation_e2e_testing_flags
                    buildFlags = nodeJS_6_installation_build_flags
                    buildEnvironmentFlag = nodeJS_6_installation_build_environment_flag
                    angularJSONFileName = nodeJS_6_installation_angularJson_filename
                } else {
                    utils = null
                    currentBuild.result = AngularConstants.FAILURE_BUILD_RESULT
                    throw new hudson.AbortException("Error setting NodeJS version") as Throwable
                }

                echo "Assigning NodeJS installation: ${nodeJS_pipeline_installation}"
                echo "Assigning default @angular/cli version: ${angularCliVersion}"
                echo "Assigning default unit testing flags: ${unitTestingFlags}"
                echo "Assigning default e2e testing flags: ${e2eTestingFlags}"
                echo "Assigning default build flags: ${buildFlags}"
                echo "Assigning default build environment flag: ${buildEnvironmentFlag}"
                echo "Assigning default angular json file: ${angularJSONFileName}"

                def node = tool name: "${nodeJS_pipeline_installation}", type: 'jenkins.plugins.nodejs.tools.NodeJSInstallation'
                env.PATH = "${node}/bin:${env.PATH}"

                echo 'Node version:'
                sh "node -v"

                echo 'NPM version:'
                sh "npm -v"

                angularJSON = readJSON file: "${angularJSONFileName}"

                echo "${angularJSONFileName} content:"
                echo "${angularJSON}"

            }

            stage('Branch type detection') {
                echo "Prepare stage (PGC)"

                angularSetDisplayName()

                echo "${currentBuild.displayName}"

                branchName = utils.getBranch()
                echo "We are on branch ${branchName}"
                branchType = utils.getBranchType(branchName)
                echo "This branch is a ${branchType} branch"
                branchNameHY = branchName.replace("/", "-").replace(".", "-").replace("_","-")
                echo "Branch name processed: ${branchName}"
            }

            stage ('Openshift environment') {
                switch (branchType) {
                    case 'feature':
                        echo "Detect feature type branch"
                        envLabel="dev"
                        if (params.ngBuild.buildEnvironments.environmentFeature) {
                            buildEnvironment = "${params.ngBuild.buildEnvironments.environmentFeature}"
                        }
                        break
                    case 'develop':
                        echo "Detect develop type branch"
                        envLabel="dev"
                        if (params.ngBuild.buildEnvironments.environmentDevelop) {
                            buildEnvironment = "${params.ngBuild.buildEnvironments.environmentDevelop}"
                        }
                        break
                    case 'release':
                        echo "Detect release type branch"
                        envLabel="uat"
                        if (params.ngBuild.buildEnvironments.environmentRelease) {
                            buildEnvironment = "${params.ngBuild.buildEnvironments.environmentRelease}"
                        }
                        break
                    case 'master':
                        echo "Detect master type branch"
                        envLabel="pro"
                        if (params.ngBuild.buildEnvironments.environmentMaster) {
                            buildEnvironment = "${params.ngBuild.buildEnvironments.environmentMaster}"
                        }
                        break
                    case 'hotfix':
                        echo "Detect hotfix type branch"
                        envLabel="uat"
                        if (params.ngBuild.buildEnvironments.environmentHotfix) {
                            buildEnvironment = "${params.ngBuild.buildEnvironments.environmentHotfix}"
                        }
                        break
                }
                echo "Environment selected: ${envLabel}"
            }


            withCredentials([string(credentialsId: "${artifactoryNPMAuthCredential}", variable: 'ARTIFACTORY_NPM_AUTH'), string(credentialsId: "${artifactoryNPMEmailAuthCredential}", variable: 'ARTIFACTORY_NPM_EMAIL_AUTH')]) {
                withEnv(["NPM_AUTH=${ARTIFACTORY_NPM_AUTH}", "NPM_AUTH_EMAIL=${ARTIFACTORY_NPM_EMAIL_AUTH}"]) {
                    withNPM(npmrcConfig: 'my-custom-npmrc') {
                        //All branches will do these stages
                        echo "params.angularCli.installGloballyAngularCli: ${params.angularCli.installGloballyAngularCli}"

                        if (params.angularCli.installGloballyAngularCli) {
                            installGloballyAngularCli = params.angularCli.installGloballyAngularCli.toBoolean()
                        }

                        if (installGloballyAngularCli) {
                            stage('Install globally @angular/cli') {
                                angularInstallGloballyAngularCli {
                                    installAngularCliSpecificVersion = params.angularCli.installAngularCliSpecificVersion
                                    angularCliDefaultVersion = angularCliVersion
                                    angularCliSpecificVersion = params.angularCli.angularCliVersion
                                    theNodeJS_pipeline_installation = nodeJS_pipeline_installation
                                }
                            }
                        } else {
                            echo "Skipping globally @angular/cli installation..."
                        }


                        stage('Configure Artifactory NPM Registry') {
                            echo 'Setting Artifactory NPM registry'
                            sh "npm config set registry ${angularNPMRepositoryURL} "

                            sh "npm config get registry"
                        }


                        stage('Install packages') {
                            angularInstallDependencies {
                                removeSourcePackageLock = params.removeSourcePackageLock
                            }
                        }


                        if (branchType in params.testing.predeploy.unitTesting) {

                            stage('Karma-sonarqube-reporter installation') {

                                angularInstallKarmaSonarQubeReporter {
                                    useKarmaSonarQubeReporterSpecificVersion = params.testing.predeploy.useKarmaSonarQubeReporterSpecificVersion
                                    theKarmaSonarQubeReporterDefaultVersion = karmaSonarQubeReporterDefaultVersion
                                    theKarmaSonarQubeReporterSpecificVersion = params.testing.predeploy.karmaSonarQubeReporterSpecificVersion
                                }

                            }
                        }


                        stage ('Show installed packages') {
                            angularShowInstalledDependencies {
                                showGlobalInstalledDependencies = params.showInstalledDependencies.showGlobalInstalledDependencies
                                showGlobalInstalledDependenciesDepthLimit = params.showInstalledDependencies.showGlobalInstalledDependenciesDepthLimit
                                showGlobalInstalledDependenciesDepth = params.showInstalledDependencies.showGlobalInstalledDependenciesDepth
                                showLocalInstalledDependencies = params.showInstalledDependencies.showLocalInstalledDependencies
                                showLocalInstalledDependenciesDepthLimit = params.showInstalledDependencies.showLocalInstalledDependenciesDepthLimit
                                showLocalInstalledDependenciesDepth = params.showInstalledDependencies.showLocalInstalledDependenciesDepth
                                showLocalInstalledDependenciesOnlyType = params.showInstalledDependencies.showLocalInstalledDependenciesOnlyType
                                showLocalInstalledDependenciesType = params.showInstalledDependencies.showLocalInstalledDependenciesType
                            }
                        }


                        stage('Get ng version') {
                            angularGetNGVersion {
                                theAngularCliLocalPath = angularCliLocalPath
                                theInstallGloballyAngularCli = installGloballyAngularCli
                            }
                        }

                        if (branchType in params.testing.predeploy.linting) {

                            stage('Linting') {
                                angularExecuteLinting {
                                    useLintingFlags = params.testing.predeploy.useLintingFlags
                                    theLintingDefaultFlags = lintingFlags
                                    theLintingFlags = params.testing.predeploy.lintingFlags
                                    theAngularCliLocalPath = angularCliLocalPath
                                    theInstallGloballyAngularCli = installGloballyAngularCli
                                }
                            }

                        } else {
                            echo "Skipping linting..."
                        }

                        if (branchType in params.testing.predeploy.unitTesting) {

                            stage('Unit Testing') {
                                angularExecuteUnitTesting {
                                    useUnitTestingFlags = params.testing.predeploy.useUnitTestingFlags
                                    theUnitTestingDefaultFlags = unitTestingFlags
                                    theUnitTestingFlags = params.testing.predeploy.unitTestingFlags
                                    useUnitTestingKarmaConfigurationFileSpecificPath = params.testing.predeploy.useUnitTestingKarmaConfigurationFileSpecificPath
                                    theUnitTestingKarmaConfigurationFileSpecificPath = params.testing.predeploy.unitTestingKarmaConfigurationFileSpecificPath
                                    theAngularCliLocalPath = angularCliLocalPath
                                    theInstallGloballyAngularCli = installGloballyAngularCli
                                }
                            }
                        } else {
                            echo "Skipping unit tests..."
                        }


                        if (branchType in params.testing.predeploy.e2eTesting) {
                            stage('End to end (e2e) Testing') {
                                angularExecuteE2ETesting {
                                    useE2ETestingFlags = params.testing.predeploy.useE2ETestingFlags
                                    theE2ETestingDefaultFlags = e2eTestingFlags
                                    theE2ETestingFlags = params.testing.predeploy.e2eTestingFlags
                                    useE2ETestingProtractorConfigurationFileSpecificPath = params.testing.predeploy.useE2ETestingProtractorConfigurationFileSpecificPath
                                    theE2ETestingProtractorConfigurationFileSpecificPath = params.testing.predeploy.e2eTestingProtractorConfigurationFileSpecificPath
                                    theAngularCliLocalPath = angularCliLocalPath
                                    theInstallGloballyAngularCli = installGloballyAngularCli
                                }
                            }
                        } else {
                            echo "Skipping e2e tests..."
                        }



                        if (branchType in params.testing.predeploy.sonarQube) {
                            stage('SonarQube') {
                                angularExecuteSonarQubeAnalisis {
                                    theSonarProjectPath = sonarProjectPath
                                    thePackageName = packageName
                                    theBranchNameHY = branchNameHY
                                    theSonarQubeServer = sonarQubeServer
                                    theScannerHome = sonarScannerHome
                                    theSonarSources = params.testing.predeploy.sonarQubeAnalisis.sonarSources
                                    theSonarExclusions = params.testing.predeploy.sonarQubeAnalisis.sonarExclusions
                                    theSonarTests = params.testing.predeploy.sonarQubeAnalisis.sonarTests
                                    theSonarTestsInclusions = params.testing.predeploy.sonarQubeAnalisis.sonarTestInclusions
                                    theSonarTypescriptExclusions = params.testing.predeploy.sonarQubeAnalisis.sonarTypescriptExclusions
                                    theSonarTestExecutionReportPath = params.testing.predeploy.sonarQubeAnalisis.sonarTestExecutionReportPath
                                    theSonarCoverageReportPath = params.testing.predeploy.sonarQubeAnalisis.sonarCoverageReportPath
                                }
                            }
                        } else {
                            echo "Skipping Running SonarQube..."
                        }


                        stage('Build Angular application') {

                            def isProdFlag = false
                            if (branchType in params.ngBuild.flagProdBranches) {
                                isProdFlag = true
                            }
                            angularBuildAngularApplication {
                                theIsProdFlag = isProdFlag
                                theBuildEnvirnomentFlag = buildEnvironmentFlag
                                theBuildEnvironment = buildEnvironment
                                useBuildFlags = params.ngBuild.useBuildFlags
                                theBuildDefaultFlags = buildFlags
                                theBuildFlags = params.ngBuild.buildFlags
                                theAngularCliLocalPath = angularCliLocalPath
                                theInstallGloballyAngularCli = installGloballyAngularCli
                            }
                        }


                        stage('Create tarball') {
                            buildOutputPath = angularCreateTarball {
                                                thePackageJSON = packageJSON
                                                theBuildDefaultOutputPath = buildDefaultOutputPath
                                                useSpecificOutputPath = params.ngBuild.useSpecificOutputPath
                                                theBuildSpecificOutputPath = params.ngBuild.buildSpecificOutputPath
                            }
                        }


                        stage ('Check tarball creation') {
                            angularCheckTarballCreation {
                                thePackageTarball = packageTarball
                            }
                        }


                        if (branchType in params.npmRegistryPublish) {
                            stage('Artifact NPM Registry Publish') {
                                angularNPMRegistryPublish {
                                    thePackageTarball = packageTarball
                                    theAngularNPMLocalRepositoryURL = angularNPMLocalRepositoryURL
                                }

                                artifactoryRepository = angularNPMLocalRepositoryURL
                            }
                        } else {
                            stage('Artifact Generic Registry Publish') {
                                angularGenericRegistryPublish {
                                    artCredentialsId = artifactoryCredential
                                    theArtifactoryURL = artifactoryURL
                                    theAngularGenericLocalRepositoryURL = angularGenericLocalRepositoryURL
                                    thePackageName = packageName
                                    thePackageTarball = packageTarball
                                }

                                artifactoryRepository = angularGenericLocalRepositoryURL
                            }
                        }


                        if (branchName == 'master') {
                            stage('Establish Artifactory registry type') {
                                angularConfigureNPMRepository {
                                    theAngularNPMRepositoryURL = angularNPMRepositoryURL
                                }

                                artifactoryRepository = angularNPMLocalRepositoryURL
                            }


                            stage('Check published package on NPM registry') {
                                angularCheckPublishedPackage {
                                    thePackageTag = packageTag
                                    thePackageViewTarball = packageViewTarball
                                }
                            }

                            stage('Set Build Output Path') {
                                buildOutputPath = angularSetBuildOutputPath {
                                    theBuildDefaultOutputPath = buildDefaultOutputPath
                                    useSpecificOutputPath = params.ngBuild.useSpecificOutputPath
                                    theBuildSpecificOutputPath = params.ngBuild.buildSpecificOutputPath
                                }
                            }

                        }


                        stage('Setting S2I project') {

                            echo "Setting source code to build from URL (build from registry package)"
                            echo "Source URL: ${projectURL} --> ${build_from_registry_url}"
                            projectURL = build_from_registry_url
                            echo "new projectURL: ${projectURL}"
                            echo "Setting source code to build from branch (build from registry package)"
                            echo "Source branch: ${branchName} --> ${build_from_artifact_branch}"
                            branchName = build_from_artifact_branch
                            echo "new branchName: ${branchName}"

                        }
                    } // withNPM
                } // withEnv
            } // withCredentials


            stage('OpenShift Build') {
                /*********************************************
                 ************* NGINX VERSION *****************
                 *********************************************/
                echo "params.nginxVersion: ${params.nginxVersion}"
                def theNginxVerxion = params.nginxVersion

                echo "theNginxVerxion: ${theNginxVerxion}"


                /**********************************************************
                 ************* OPENSHIFT PROJECT CREATION *****************
                 **********************************************************/

                echo "Building image on OpenShift..."

                angularOpenshiftCheckAndCreateProject {
                    oseCredential = openshiftCredential
                    cloudURL = openshiftURL
                    environment = envLabel
                    jenkinsNS = jenkinsNamespace
                    artCredential = artifactoryCredential
                    template = params.openshift.templatePath
                    branchHY = branchNameHY
                    branch_type = branchType
                    dockerRegistry = registry
                    sourceRepositoryURL = projectURL
                    sourceRepositoryBranch = branchName
                    package_name = packageName
                    package_tarball = packageTarball
                    artifactoryRepo = artifactoryRepository
                    contextDir = ''
                    nginxVersion = theNginxVerxion
                    build_output_path = buildOutputPath
                }


                /**************************************************************
                 ************* ENVIRONMENT VARIABLES CREATION *****************
                 **************************************************************/
                retry(3) {
                    angularOpenshiftEnvironmentVariables {
                        branchHY = branchNameHY
                        branch_type = branchType
                        theEnvironmentVariablesMap = params.environmentVariables
                    }

                    sleep(10)
                }


                angularOpenshiftBuildProject {
                    repoUrl = angularNPMRepositoryURL
                    branchHY = branchNameHY
                    branch_type = branchType
                }

            } // end stage

        } // end isPPCJenkinsFile

    } // end of node

    if (!isPPCJenkinsFile) {
        def deploy = 'Yes'

        if (branchType in params.confirmDeploy) {
            node {
                try {
                    stage('Decide on Deploying') {
                        deploy = angularTimeoutConfirmMessage {
                            theTimeoutConfirm = params.timeoutConfirmDeploy
                            theTimeoutConfirmTime = params.timeoutConfirmDeployTime
                            theTimeoutConfirmUnit = params.timeoutConfirmDeployUnit
                            theMessage = 'Waiting for user approval'
                            theChoiceName = 'Continue and deploy?'
                            theChoices = 'No\nYes'
                            theChoiceDescription = 'Choose "Yes" if you want to deploy this build'
                        }
                    }
                } catch (err) {
                    def user = err.getCauses()[0].getUser()
                    utils = null
                    currentBuild.result = AngularConstants.FAILURE_BUILD_RESULT
                    if ('SYSTEM'.equals(user.toString())) { //timeout
                        throw new hudson.AbortException("Timeout on confirm deploy") as Throwable
                    } else {
                        throw new hudson.AbortException("There is an error on confirm deploy") as Throwable
                    }
                }
            }
        }

        if (deploy == 'Yes') {
            node {
                checkout scm
                stage('OpenShift Deploy') {
                    echo "Deploying on OpenShift..."

                    openshift_route_hostname = angularOpenshiftDeployProject {
                        branchHY = branchNameHY
                        branch_type = branchType
                    }

                    openshift_route_hostname_with_protocol = utils.getRouteHostnameWithProtocol(openshift_route_hostname, false)
                }
            }

            echo "Openshift route hostname: ${openshift_route_hostname}"
            echo "Openshift route hostname (with protocol): ${openshift_route_hostname_with_protocol}"

            angularExecuteAllPerformanceTests {
                theBranchType = branchType
                theTimeoutConfirmPostDeployTests = params.testing.postdeploy.timeoutConfirmPostDeployTests
                theTimeoutConfirmPostDeployTestsTime = params.testing.postdeploy.timeoutConfirmPostDeployTestsTime
                theTimeoutConfirmPostDeployTestsUnit = params.testing.postdeploy.timeoutConfirmPostDeployTestsUnit
                theSmokeTestingBranches = params.testing.postdeploy.smokeTesting
                theAcceptanceTestingBranches = params.testing.postdeploy.acceptanceTesting
                theSecurityTestingBranches = params.testing.postdeploy.securityTesting
                thePerformanceTestingBranches = params.testing.postdeploy.performanceTesting
                errorOnPostDeployTestsUnstableResult = params.jenkins.errorOnPostDeployTestsUnstableResult
                theTaurusTestBasePath = taurus_test_base_path
                theSmokeTestPath = smoke_test_path
                theAcceptanceTestPath = acceptance_test_path
                theSecurityTestPath = security_test_path
                thePerformanceTestPath = performance_test_path
                theOpenshiftRouteHostnameWithProtocol = openshift_route_hostname_with_protocol
            }
        } else {
            //User doesn't want to deploy
            //Failed status
            utils = null
            currentBuild.result = AngularConstants.FAILURE_BUILD_RESULT
            throw new hudson.AbortException("The deploy on Openshift hasn't been confirmed") as Throwable
        }


        stage('Notification') {
            echo "Sending Notifications..."
        }

        stage('Remove old builds') {
            angularRemoveOldBuilds {
                maxOldBuildsToKeep = params.jenkins.maxOldBuildsToKeep
                daysOldBuildsToKeep = params.jenkins.daysOldBuildsToKeep
            }
        }
    }

    node {

        echo "END ANGULARGENERIC CONFIGURATION PROJECT (PGC)"

        echo 'Pipeline end timestamp... '
        sh 'date'

        echo "Current build duration: ${currentBuild.durationString.replace(' and counting', '')}"

    }


    utils = null

} //end of method

return this
