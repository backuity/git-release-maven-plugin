import java.nio.file.Files

new File(basedir, ".gitignore").write("build.log")

repository = Files.createTempDirectory("git-release")

new AntBuilder().replace(
        file: new File(basedir, "pom.xml"),
        token: "tmp-dir-to-setup",
        value: repository.toUri())

println "git init".execute(null, basedir).text
println "git add -A".execute(null, basedir).text
println "git commit -m init".execute(null, basedir).text