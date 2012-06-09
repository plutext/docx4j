Docx4j release process
======================

[Do this process for a release candidate first]

Check everything is committed

Update Getting Started as necessary (inc HTML and PDF versions)

Check jar versions in pom.xml, build.xml

Run JarCheck on result of mvn install to check its compiled for 1.5

Update README.txt with release info.

http://www.jukie.net/bart/blog/pimping-out-git-log
    
(refer to README.txt to see what rnumber to start at)    

Update pom.xml with target version number (must still be -SNAPSHOT)

    git commit / push upstream

Start up the Git Bash session and go to your project directory.

Windows users, you need to start up an SSH agent to provide your passkey when needed by the release process.

To do this, in your Git Shell type :

    1eval `ssh-agent`  //pay attention to the back tick quotes here
    
which should return a piece of text like Agent pid xyz. This command starts the agent and sets up a couple of 
environment variables relating to the SSH agent. 

If you type env | grep SSH you will see the environment variables :

   1$ env | grep SSH
   2SSH_AGENT_PID=1234
   3SSH_AUTH_SOCK=/tmp/ssh-abcdef1234/agent.5678

Windows users will need to manually create the directory c:/tmp/ssh-abcdef1234/agent.5678 otherwise you
get and error saying could not open a connection to your authentication agent.

Create the new directory and then add your key to the agent using the following syntax which assumes your 
Github RSA key is in the c:\.ssh\ directory. If it isn’t then just substitute the directory for your key.

$ ssh-add ~/.ssh/id_rsa
Enter passphrase for /c/Users/jharrop/.ssh/id_rsa: [the github 2 one]

then per https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide

mvn release:clean

mvn release:prepare

in prepare, prompt for passphrase is the *other* one 
(if you bugger it up, do git reset --hard, and start again with clean!)

release:prepare ends with:

		---
		[INFO] Checking in modified POMs...
		[INFO] Executing: cmd.exe /X /C "git add -- pom.xml"
		[INFO] Working directory: c:\Users\jharrop\git\plutext\docx4jGREAT
		[INFO] Executing: cmd.exe /X /C "git status"
		[INFO] Working directory: c:\Users\jharrop\git\plutext\docx4jGREAT
		[INFO] Executing: cmd.exe /X /C "git commit --verbose -F C:\Users\jharrop\AppDat
		a\Local\Temp\maven-scm-1479963909.commit pom.xml"
		[INFO] Working directory: c:\Users\jharrop\git\plutext\docx4jGREAT
		[INFO] Executing: cmd.exe /X /C "git symbolic-ref HEAD"
		[INFO] Working directory: c:\Users\jharrop\git\plutext\docx4jGREAT
		[INFO] Executing: cmd.exe /X /C "git push git@github.com:plutext/docx4j.git mast
		er:master"
		[INFO] Working directory: c:\Users\jharrop\git\plutext\docx4jGREAT
		[INFO] Tagging release with the label docx4j-2.8.0...
		[INFO] Executing: cmd.exe /X /C "git tag -F C:\Users\jharrop\AppData\Local\Temp\
		maven-scm-1814557282.commit docx4j-2.8.0"
		[INFO] Working directory: c:\users\jharrop\git\plutext\docx4jgreat
		[INFO] Executing: cmd.exe /X /C "git push git@github.com:plutext/docx4j.git docx
		4j-2.8.0"
		[INFO] Working directory: c:\users\jharrop\git\plutext\docx4jgreat
		[INFO] Executing: cmd.exe /X /C "git ls-files"
		[INFO] Working directory: c:\users\jharrop\git\plutext\docx4jgreat
		[INFO] Transforming 'docx4j'...
		[INFO] Not removing release POMs
		[INFO] Checking in modified POMs...
		[INFO] Executing: cmd.exe /X /C "git add -- pom.xml"
		[INFO] Working directory: c:\Users\jharrop\git\plutext\docx4jGREAT
		[INFO] Executing: cmd.exe /X /C "git status"
		[INFO] Working directory: c:\Users\jharrop\git\plutext\docx4jGREAT
		[INFO] Executing: cmd.exe /X /C "git commit --verbose -F C:\Users\jharrop\AppDat
		a\Local\Temp\maven-scm-651007931.commit pom.xml"
		[INFO] Working directory: c:\Users\jharrop\git\plutext\docx4jGREAT
		[INFO] Executing: cmd.exe /X /C "git symbolic-ref HEAD"
		[INFO] Working directory: c:\Users\jharrop\git\plutext\docx4jGREAT
		[INFO] Executing: cmd.exe /X /C "git push git@github.com:plutext/docx4j.git mast
		er:master"
		[INFO] Working directory: c:\Users\jharrop\git\plutext\docx4jGREAT
		[INFO] Release preparation complete.
		[INFO] ------------------------------------------------------------------------
		[INFO] BUILD SUCCESS
		[INFO] ------------------------------------------------------------------------
		[INFO] Total time: 2:46.951s
		[INFO] Finished at: Thu May 24 15:42:00 EST 2012
		[INFO] Final Memory: 8M/244M
		[INFO] ------------------------------------------------------------------------

and gives you release.properties:

		jharrop@JHARROP-M4600 ~/git/plutext/docx4jGREAT (master)
		$ cat release.properties
		#release configuration
		#Thu May 24 15:42:00 EST 2012
		project.dev.org.docx4j\:docx4j=2.8.1-SNAPSHOT
		scm.tag=docx4j-2.8.0
		project.scm.org.docx4j\:docx4j.tag=HEAD
		scm.url=scm\:git|git@github.com\:plutext/docx4j.git
		pushChanges=true
		preparationGoals=clean verify
		project.scm.org.docx4j\:docx4j.url=http\://svn.sonatype.org/spice/tags/oss-paren
		t-7/docx4j
		project.scm.org.docx4j\:docx4j.developerConnection=scm\:git|git@github.com\:plut
		ext/docx4j.git
		project.rel.org.docx4j\:docx4j=2.8.0
		remoteTagging=true
		scm.commentPrefix=[maven-release-plugin]
		project.scm.org.docx4j\:docx4j.connection=scm\:svn\:http\://svn.sonatype.org/spi
		ce/tags/oss-parent-7/docx4j
		exec.additionalArguments=-Psonatype-oss-release
		completedPhase=end-release

NB: it says that before you've done release:perform!!

If you need to start again for any reason, delete the tag it added:

	git tag -d docx4j-2.8.0
	git push origin :refs/tags/docx4j-2.8.0

and change the version back in your pom (and commit)
and delete release.properties

-----------

You can't do:

	$ mvn release:perform -X -DlocalCheckout=true

since it says it

	don't handle protocol 'git@github.com:file'

You can'so just do:

	$ mvn release:perform -X 

and be patient .. it may look like nothing is happening 
(frozen checking out... and no network traffic), but have faith....

enter code signing password again

.. then its upload to oss.sonatype.org

    [INFO] --- maven-gpg-plugin:1.1:sign (sign-artifacts) @ docx4j ---

    [INFO] --- maven-install-plugin:2.3.1:install (default-install) @ docx4j ---

    [INFO] Installing c:\Users\jharrop\git\plutext\docx4jGREAT\target\checkout\t
arget\docx4j-2.8.0.jar to C:\Users\jharrop\.m2\repository\org\docx4j\docx4j\2.8.
0\docx4j-2.8.0.jar

etc

    [INFO] --- maven-deploy-plugin:2.5:deploy (default-deploy) @ docx4j ---

    Uploading: https://oss.sonatype.org/service/local/staging/deploy/maven2/org/
docx4j/docx4j/2.8.0/docx4j-2.8.0.jar
    Uploaded: https://oss.sonatype.org/service/local/staging/deploy/maven2/org/d
ocx4j/docx4j/2.8.0/docx4j-2.8.0.jar (3735 KB at 31.7 KB/sec)

    Uploading: https://oss.sonatype.org/service/local/staging/deploy/maven2/org/
docx4j/docx4j/2.8.0/docx4j-2.8.0-sources.jar
    Uploaded: https://oss.sonatype.org/service/local/staging/deploy/maven2/org/d
ocx4j/docx4j/2.8.0/docx4j-2.8.0-sources.jar (4042 KB at 84.5 KB/sec)

    Uploading: https://oss.sonatype.org/service/local/staging/deploy/maven2/org/
docx4j/docx4j/2.8.0/docx4j-2.8.0-javadoc.jar
    16061 KB 
.. at 74.5 KB/sec

Then release it - see https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide

1. close the staging repository:

   1. Login to the Nexus UI, https://oss.sonatype.org/index.html#welcome
   2. Go to Staging Repositories page.
   3. Select a staging repository.
   4. Click the Close button.

2. when you are sure the closed staging repository has no problem, click the Release button.


-------


ANT_OPTS="-Xmx512m -XX:MaxPermSize=256m" ant dist

(Remove the jaxb jars from dist dir)

but for consistency, use the docx4j jar maven made.

Put Getting Started in the dist dir

Create docx4j-x.x.x.zip (ant dist, rename the jar as well)

Update build.xml so it has the same version as pom.xml

----

Announce release in docx4j forum