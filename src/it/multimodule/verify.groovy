pom = new File( basedir, 'pom.xml' )
project = new XmlSlurper().parse( pom )
assert '0.2.1-SNAPSHOT' == project.version.text()

moduleA = new File(basedir,'module-a/pom.xml')
projectA = new XmlSlurper().parse(moduleA)
assert '0.2.1-SNAPSHOT' == projectA.parent.version.text()

moduleB = new File(basedir,'module-b/pom.xml')
projectB = new XmlSlurper().parse(moduleB)
assert '0.2.1-SNAPSHOT' == projectB.parent.version.text()

println "git checkout 0.2.0".execute(null, basedir).text

pom = new File( basedir, 'pom.xml' )
project = new XmlSlurper().parse( pom )
assert '0.2.0' == project.version.text()

moduleA = new File(basedir,'module-a/pom.xml')
projectA = new XmlSlurper().parse(moduleA)
assert '0.2.0' == projectA.parent.version.text()

moduleB = new File(basedir,'module-b/pom.xml')
projectB = new XmlSlurper().parse(moduleB)
assert '0.2.0' == projectB.parent.version.text()
