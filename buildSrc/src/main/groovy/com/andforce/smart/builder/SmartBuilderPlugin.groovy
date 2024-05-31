package com.andforce.smart.builder

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

class SmartBuilderPlugin implements Plugin<Project> {

    def ENABLE_LOG = false

    @Override
    void apply(Project project) {

        // 在根目录创建文件
        def file = project.file("plugin_log.txt")
        if (!ENABLE_LOG) {
            file.delete()
        }

        if (file.exists()) {
            file.write("")
        }

        def full_sdk = project.rootProject.file('full_sdk.jar')

        project.dependencies {
            //https://www.jianshu.com/p/415fb00b4332
            delegate.compileOnly(project.files(full_sdk.absolutePath))
            //delegate.add("compileOnly", 'com.smartisanos:UnlimitedSDK:1.0.0')
            //delegate.compileOnly('com.smartisanos:UnlimitedSDK:1.0.0-SNAPSHOT')

        }

        project.afterEvaluate {

            //https://stackoverflow.com/questions/42058626/how-to-get-newest-snapshot-of-a-dependency-without-changing-version-in-gradle
            // cacheChangingModulesFor
            project.configurations.all{
                resolutionStrategy.cacheChangingModulesFor 0, 'seconds'
            }

            //https://stackoverflow.com/questions/46628910/saving-gradle-dependencies-to-a-directory
            project.configurations.compileOnly.setCanBeResolved(true)

            def UnlimitedSDK = full_sdk.absolutePath
            println "UnlimitedSDK Path: " + UnlimitedSDK

            // 判断project是Application还是Library
            def isApp = project.plugins.hasPlugin('com.android.application')
            def android = isApp ? project.extensions.getByType(AppExtension) : project.extensions.getByType(LibraryExtension)

            android.aaptOptions.additionalParameters('-I', '' + UnlimitedSDK)


            project.tasks.withType(JavaCompile){ javaCompile ->
                javaCompile.options.compilerArgs.add('-Xbootclasspath/p:' + UnlimitedSDK)
            }

            project.rootProject.file(".idea/modules/" + project.name + "/").listFiles().each {
                if (!it.name.contains(".unitTest.iml") && !it.name.contains(".androidTest.iml")) {
                    if (ENABLE_LOG) {
                        file.append('===>>  ' + it.absolutePath + '\n')
                    }

                    changeSDKOrder(it, file)
                }
            }
        }
    }

    private void changeSDKOrder(imlFile, file) {
        try {
            def parsedXml = (new XmlParser()).parse(imlFile)

            def allChildren = parsedXml.children()
            for (int i = 0; i < allChildren.size(); i++) {
                Object component = allChildren.get(i)
                if (ENABLE_LOG) {
                    file.append('===>>  ' + component.name() + '\n')
                }
                if (component.name() != 'component') {
                    continue
                }

                List orderEntry = component.get("orderEntry")
                def sdkNode = orderEntry.find { it.'@type' == 'jdk'}
                if (sdkNode != null) {
                    if (ENABLE_LOG) {
                        file.append('===>> remove sdk node: ' + sdkNode + '\n')
                    }
                    component.remove(sdkNode)

                    if (ENABLE_LOG) {
                        file.append('===>> append sdk node: ' + sdkNode + '\n')
                    }
                    component.append(sdkNode)
                    break
                }
            }

            if (ENABLE_LOG) {
                file.append('===>>  保存修改\n')
            }
            def xml = new FileOutputStream(imlFile)
            groovy.xml.XmlUtil.serialize(parsedXml, xml)
            xml.flush()
            xml.close()

            if (ENABLE_LOG) {
                file.append('===>>  删除多余的空行\n')
            }
            // 删除imlFile中的空行
            def lines = imlFile.readLines()
            imlFile.withWriter { writer ->
                lines.each { line ->
                    if (line.trim() != '') {
                        writer.writeLine(line)
                    }
                }
            }

        } catch (FileNotFoundException e) {
            // nop, iml not found
            if (ENABLE_LOG) {
                file.append('===>>  ' + e + '\n')
            }
        }
    }
}
