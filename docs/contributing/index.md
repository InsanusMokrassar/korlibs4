---
layout: default
title: Contributing
css-icon: fab fa-github
priority: 1000
---

{% include toc_include.md max_level=2 %}

## Code

All new development happens in the `korlibs4` repository:

<https://github.com/korlibs/korlibs4>

You can fork the repository, make changes and create a PR proposing them.

### Building

You can build `korlibs4` with:

```kotlin
git clone https://github.com/korlibs/korlibs4.git
cd korlibs4
./gradlew publishToMavenLocal
```

This publishes all the libraries with the version `999.0.0.999` in the `~/.m2` folder.

To make compilation faster, you can add `org.gradle.parallel=true` to your `~/.gradle/gradle.properties` file (crete it if it doesn't exist).

## Documentation

Did you find something **wrong**, **misleading** or **confusing**? Or just want to add more content to the documentation?

In order to make it easier to contribute, each page of the documentation contains a `Suggest Change / Typo`,
so you can propose a change.

<img src="/contributing/suggest_change.png" style="border: 2px solid black;" />

All the documentation is hosted at github using github pages and [jekyll](https://jekyllrb.com/):

<https://github.com/korlibs/korlibs4/tree/main/docs>

