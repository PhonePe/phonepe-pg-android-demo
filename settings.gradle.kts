import java.net.URI

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        //PhonePe SDK
        maven {
            url  = URI("https://phonepe.mycloudrepo.io/public/repositories/phonepe-intentsdk-snapshots")
//            url  = URI("https://phonepe.mycloudrepo.io/public/repositories/phonepe-intentsdk-android")
        }
    }
}

rootProject.name = "PhonePe Options"
include(":app")