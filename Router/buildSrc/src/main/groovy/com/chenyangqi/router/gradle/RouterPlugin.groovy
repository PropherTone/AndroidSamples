package com.chenyangqi.router.gradle

import com.android.build.gradle.AppExtension
import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.xml.crypto.dsig.Transform

class RouterPlugin implements Plugin<Project> {

    //注入插件逻辑
    @Override
    void apply(Project project) {
        //注册 Transform
//        if (project.plugins.hasPlugin(AppPlugin)) {
//            AppExtension appExtension = project.extensions.getByType(AppExtension)
//            Transform transform = new RouterMappingTransform()
//            appExtension.registerTransform(transform)
//        }
        //注册扩展参数
        project.getExtensions().create("router", RouterExtension)

        //1、自动帮用户传递路劲参数到注解处理器中
        if (project.extensions.findByName("kapt") != null) {
            project.extensions.findByName("kapt").arguments {
                arg("root_project_dir", project.rootProject.projectDir.absolutePath)
                println("step1:传递参数到注解处理器中")
            }
        }

        //2、实现旧的构建产物自动清理
        project.clean.doFirst {
            File routerMappingDir = new File(project.rootProject.projectDir, "router_mapping")
            if (routerMappingDir.exists()) {
                routerMappingDir.deleteDir()
                println("step2：已删除旧的router_mapping dir")
            }
        }

        println("int RouterPlugin,apply from ${project.name}")

        //当配置阶段结束时获取设置的wikiDir
        project.afterEvaluate {
            RouterExtension extension = project["router"]
            println("设置的保存wikiDir路径：${extension.wikiDir}")

            // 3. 在javac任务 (compileDebugJavaWithJavac) 后，汇总生成文档
            project.tasks.findAll { task ->
                task.name.startsWith('compile') &&
                        task.name.endsWith('JavaWithJavac')
            }.each { task ->
                task.doLast {
                    File routerMappingDir = new File(project.rootProject.projectDir, "router_mapping")
                    if (!routerMappingDir.exists()) {
                        return
                    }
                    File[] allChildFiles = routerMappingDir.listFiles()
                    if (allChildFiles.length < 1) {
                        return
                    }
                    StringBuilder markdownBuilder = new StringBuilder()
                    markdownBuilder.append("# 页面文档\n\n")
                    allChildFiles.each { child ->
                        if (child.name.endsWith(".json")) {
                            JsonSlurper jsonSlurper = new JsonSlurper()
                            def content = jsonSlurper.parse(child)
                            content.each { innerContent ->
                                def url = innerContent['url']
                                def description = innerContent['description']
                                def realPath = innerContent['realPath']
                                markdownBuilder.append("## $description \n")
                                markdownBuilder.append("- url: $url \n")
                                markdownBuilder.append("- realPath: $realPath \n\n")
                            }
                        }
                    }
                    File wikiFileDir = new File(extension.wikiDir)
                    if (!wikiFileDir.exists()) {
                        wikiFileDir.mkdir()
                    }
                    File wikiFile = new File(wikiFileDir, "页面文档.md")
                    if (wikiFile.exists()) {
                        wikiFile.delete()
                    }
                    wikiFile.write(markdownBuilder.toString())
                }
            }
        }
    }
}