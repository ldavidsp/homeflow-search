[![Download](https://jitpack.io/v/ldavidsp/homeflow-search.svg)](https://jitpack.io/#ldavidsp/homeflow-search)
![GitHub repo size in bytes](https://img.shields.io/github/repo-size/ldavidsp/homeflow-search.svg)
![GitHub issues](https://img.shields.io/github/issues/ldavidsp/homeflow-search.svg)
![GitHub top language](https://img.shields.io/github/languages/top/ldavidsp/homeflow-search.svg)
![visitors](https://visitor-badge.laobi.icu/badge?page_id=homeflow-search.readme)

Homeflow Search View Android Kotlin
=====

## Installation

**Step 1.** Add the [JitPack](https://jitpack.io/#ldavidsp/homeflow-search/1.0.1) repository to your build file. Add it in your root `/build.gradle` at the end of repositories:

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

**If gradle is >= 7.2**:. Add the dependency in /settings.gradle:
```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

**Step 2.** Add the dependency in `/app/build.gradle` :

```gradle
dependencies {
    ...
    implementation 'com.github.ldavidsp:homeflow-search:v2.0.4'
}
```

## Usage
To open the search view on your app, add the following code to the end of your layout:
```xml
<com.homeflow.search.HomeflowSearchView
    android:id="@+id/search_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```
