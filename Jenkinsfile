#!groovy
build('cm-dudoser', 'java-maven') {
    checkoutRepo()
    loadBuildUtils()

    def javaServicePipeline
    runStage('load JavaService pipeline') {
        javaServicePipeline = load("build_utils/jenkins_lib/pipeJavaService.groovy")
    }

    def serviceName = env.REPO_NAME
    def mvnArgs = '-DjvmArgs="-Xmx256m" -DskipDtrack=true'
    def useJava11 = true

    javaServicePipeline(serviceName, useJava11, mvnArgs)
}
