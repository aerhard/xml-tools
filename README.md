# XML Tools

XML Tools meta project including

- xml-tools-server: a simple socket server providing XML validation and autocomplete suggestions with Jing, Xerces and Saxon
- https://github.com/aerhard/jing-trang as a git submodule
- https://github.com/aerhard/linter-autocomplete-jing as a git submodule

## Setup

* clone the GitHub repository from https://github.com/aerhard/xml-tools
* run `git submodules update --init` to get the submodules
* if not present, install npm, a recent Java Development Kit (JDK), Maven (http://maven.apache.org/download.cgi) and Ant (http://ant.apache.org/bindownload.cgi)
* run `install` from the project root

## Updating the `jing-suggest` dependency in `xml-tools-server`

run `./build-jing-suggest` from the project root

## Deploying the XML Tools Server to the linter-autocomplete-jing project

run `./build` from the project root
