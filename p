[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO] 
[INFO] datavault                                                          [pom]
[INFO] datavault-common                                                   [jar]
[INFO] datavault-webapp                                                   [jar]
[INFO] datavault-broker                                                   [jar]
[INFO] datavault-worker                                                   [jar]
[INFO] 
[INFO] ------------------------< datavault:datavault >-------------------------
[INFO] Building datavault 0.0.1-SNAPSHOT                                  [1/5]
[INFO] --------------------------------[ pom ]---------------------------------
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for datavault 0.0.1-SNAPSHOT:
[INFO] 
[INFO] datavault .......................................... FAILURE [  0.018 s]
[INFO] datavault-common ................................... SKIPPED
[INFO] datavault-webapp ................................... SKIPPED
[INFO] datavault-broker ................................... SKIPPED
[INFO] datavault-worker ................................... SKIPPED
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  0.122 s
[INFO] Finished at: 2024-03-27T16:18:21Z
[INFO] ------------------------------------------------------------------------
[ERROR] Unknown lifecycle phase "datavault-webapp". You must specify a valid lifecycle phase or a goal in the format <plugin-prefix>:<goal> or <plugin-group-id>:<plugin-artifact-id>[:<plugin-version>]:<goal>. Available lifecycle phases are: validate, initialize, generate-sources, process-sources, generate-resources, process-resources, compile, process-classes, generate-test-sources, process-test-sources, generate-test-resources, process-test-resources, test-compile, process-test-classes, test, prepare-package, package, pre-integration-test, integration-test, post-integration-test, verify, install, deploy, pre-clean, clean, post-clean, pre-site, site, post-site, site-deploy. -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/LifecyclePhaseNotFoundException
