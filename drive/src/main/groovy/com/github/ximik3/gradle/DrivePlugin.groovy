package com.github.ximik3.gradle

import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Android gradle plugin for uploading .apk files to Google Drive
 * Created by volodymyr.kukhar on 10/24/16.
 */
class DrivePlugin implements Plugin<Project> {

    public static final String GOOGLE_DRIVE_GROUP = "Google Drive"

    void apply(Project project) {
        def log = project.logger

        def hasAppPlugin = project.plugins.find { p -> p instanceof AppPlugin }
        if (!hasAppPlugin) {
            throw new IllegalStateException("The 'com.android.application' plugin is required.")
        }

        def extension = project.extensions.create('deploy', DrivePluginExtension)
//        def extension = project.extensions.create('drive', DrivePluginExtension)

        project.android.applicationVariants.all { variant ->
            if (variant.buildType.isDebuggable()||variant.buildType.name.contains("debug")) {
                log.debug("Skipping debuggable build type ${variant.buildType.name}.")
                return
            }

            def buildTypeName = variant.buildType.name.capitalize()
//            println "DrivePlugin Show buildTypeName : " + buildTypeName
            def hasFlavors = !variant.flavorName.isEmpty()
//            println "DrivePlugin Show applicationVariants hasFlavors : " + hasFlavors
//            println "DrivePlugin Show applicationVariants full  : " + hasFlavors ? variant.flavorName.capitalize() : ''
            def artifactIdSuffix = hasFlavors ? variant.flavorName.replace('_', '-').capitalize() : ''
//            println "DrivePlugin Show applicationVariants artifactIdSuffix  : " + artifactIdSuffix
            def productFlavorNames = variant.productFlavors.collect { it.name.capitalize() }
            if (productFlavorNames.isEmpty()) {
                productFlavorNames = [""]
            }
//            println "DrivePlugin Show applicationVariants productFlavorNames  : " + productFlavorNames
            def productFlavorName = productFlavorNames.join('')
//            def flavor = StringUtils.uncapitalize(productFlavorName)

            def variationName = "${productFlavorName}${buildTypeName}"
//            println "DrivePlugin Show applicationVariants variationName  : " + variationName
            def uploadApkTaskName = "deploy_${variationName}_apk"

            // Create and configure upload task for each variant
            def publishApkTask = project.tasks.create(uploadApkTaskName, DriveUploadTask)
            publishApkTask.extension = extension
            publishApkTask.variant = variant
            publishApkTask.description = "Uploads the APK for the ${variationName} build to Google Drive"
            publishApkTask.group = GOOGLE_DRIVE_GROUP
        }
    }
}
