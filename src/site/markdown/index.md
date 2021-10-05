# Overview

The Apache Sling HTL Maven Plugin provides support for validating HTML Template Language scripts from projects during
build time, reporting issues like:

* syntax errors;
* expression warnings (e.g. missing required display contexts, sensible attributes with dynamic values, etc.);
* incorrect usage of block elements.

It is compatible with [m2eclipse](https://www.eclipse.org/m2e/documentation/m2e-making-maven-plugins-compat.html) and supports executing its goals during Eclipse incremental builds.

## Goals
The HTL Maven Plugin has only one goal:

* [htl:validate](validate-mojo.html) is bound to the generate-sources phase and is used to validate/transpile HTL scripts.


## Usage
General instructions on how to use the HTL Maven Plugin can be found on the [usage](usage.html) page.

In case you still have questions regarding the plugin's usage feel free to contact the Apache Sling Development List. The posts to the
mailing list are archived and could already contain the answer to your question as part of an older thread. Hence, it is also worth
browsing/searching the mail archive.

If you feel like the plugin is missing a feature or has a defect, you can fill a feature request or bug report in our issue tracker. When
creating a new issue, please provide a comprehensive description of your concern. Especially for fixing bugs it is crucial that the
developers can reproduce your problem. For this reason, entire debug logs, POMs or most preferably little demo projects attached to the
issue are very much appreciated. Of course, patches are welcome, too. Contributors can check out the project from our source repository and
will find supplementary information in the guide to helping with Maven.

