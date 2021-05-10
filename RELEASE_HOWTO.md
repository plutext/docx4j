Docx4j release process
======================

Do this process for a beta then a release candidate first; 
 beta & RC should be pushed to maven repo on GitHub - see notes in pom.
 Create beta from docx4j-ImportXHTML, since beta can include that stuff.
 But don't bother putting that in maven repo.
 Use mvn to build docx4j-ImportXHTML, but then ant to gather the jars 

In docx4j pom:

<!-- Uncomment to deploy to GitHub.  MUST Comment out for real release
<distributionManagement>
:
 
 
When it comes to the actual release, follow the below for:

+ docx4j

+ xhtmlrenderer
+ docx4j-ImportXHTML
+ docx4j-export-FO

+ docx4j-MOXy

+ Enterprise Ed. 

+ .NET dist



TODO: make toolchain UTF-8 filename safe ie git, zip, unzip

 
---------- 

Merge changes from 8 or 11 as appropriate.  If creating a patch (of changes in 11_1_2):

    git diff master VERSION_11_1_2 > patchfile

may need to run dos2unix on source file(s), then its

    patch -p1 --dry-run  < patchfile


Update CHANGELOG.md, README.md with release info.

    http://www.jukie.net/bart/blog/pimping-out-git-log
        
(refer to CHANGELOG.md to see what rnumber to start at)  

    git lg b6c12c8..HEAD > stuff.txt  

Update pom.xml with target version number (must still be -SNAPSHOT)

Check sub-modules are using <version>${revision}</version> (ie that the 2 Maven commits from last time have been reverted)

Update build.xml so it has the same version as pom.xml (but without  -SNAPSHOT) 

Check everything is committed

Update Getting Started as necessary (inc HTML and PDF versions)

Check jar versions in pom.xml, build.xml

mvn clean

Run JarCheck on result of mvn install to check its compiled for 1.6 (run it on all jars in dist)

(Jar not clean? avoid mixing mvn and eclipse (test?) output)

git commit / push upstream
(uses git-remote-https, if you want to force a particular network connection)

-------------

Note for Java 11:  Maven Central requires Javadoc.

But org.slf4j is a multi-release jar, and the maven javadoc plugin can't handle it under Java 11: https://bugs.openjdk.java.net/browse/JDK-8222309
				 
So we have to build with Java 12 or later (currently 14) :- 

$ sudo archlinux-java set java-14-adoptopenjdk

for import-XHTML:
    export JAVA_HOME=/usr/lib/jvm/java-8-openjdk
    mvn install -P jdk-8-config

-------------

Start up the Git Bash session and go to your project directory.

Windows users, you need to start up an SSH agent to provide your passkey when needed by the release process.

To do this, in your Git Shell type :

    eval `ssh-agent`  //pay attention to the back tick quotes here
    
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

--------------

Linux

eval "$(ssh-agent -s)"

ssh-add ~/.ssh/id_rsa
Enter passphrase for ... .ssh/id_rsa: [the github 2 one]

---------------

This command prompt can be used to do what follows for the 4 projects.  ie the above only needs to be done once :-)

then per https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide

mvn release:clean

mvn release:prepare

in prepare, prompt for passphrase is the *other* one [e..]
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

Easiest is to discard all changes, and if necessary revert and commit any commits it made.

You'll still need to delete any tag as per above.


-----------

You can't do:

	$ mvn release:perform -X -DlocalCheckout=true

since it says it

	don't handle protocol 'git@github.com:file'

so just do:

	$ mvn release:perform -X 

(don't need to worry about presence of bin-testOutput dir etc)

.. be patient .. it may look like nothing is happening 
(frozen checking out... and no network traffic), but have faith....

enter code signing password again [ie e..]

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

Revert and commit (most recent first) the 2 commits which change the version number in all the poms
(Swap sub-modules back to <version>${revision}</version>)
then manually update the version number in parent pom (it needs to be the released version, since build.xml copies the pom.xml)

Repeat above for -ImportXHTML 

Run ant release (requires docx4j, -ImportXHTML  to be in maven)

 ant release  -buildfile etc/build.xml

Switch branch if necessary, eg:

    git checkout master
    
and

$ sudo archlinux-java set java-8-openjdk

or 

$ sudo archlinux-java set java-14-adoptopenjdk


Upodate pom.xml to -SNAPSHOT

----

Put in /docx4j dir, for example

	scp *2.1.zip  ubuntu@docx4java.org:/home/ubuntu/docx4j-8.2.1/


Update downloads.html
Announce release in docx4j forum
Update news  (includes link to release announcement)

----

.NET releases

Nuget publish procedure:
(see also HOWTO_update.txt on M4600)

Create the dll:
0.  you'll need slf4j-api.dll (use the version in nuget, or update it first: IKVM needs to use the version end-users will be using, or they'll get TypeInitializationException).
    should just be 1.7.5.4
1.  get branch:  git checkout tags/docx4j-6.0.1 -b docx4j-6.0.1
2.  mvn install (to ensure deps are present, and since it is only mvn which writes docx4j version)
4.	ant dist.NET to create the DLL, strong named since that's useful for VSTO

C:\Program Files (x86)\Microsoft SDKs\Windows\v7.0A\Bin\sn -v can be used to check
	
docx4.NET in Visual Studio:	
0.  git clone https://github.com/plutext/docx4j.NET.git
1.	open that in Visual Studio, remove reference to existing DLL; copy/add the new one
2.	update docx4j.properties (don't need that in -ImportXHTML nuspec, since it is pulled in automatically)
3.  build (issues doing this with VS Community 2017 on Yoga; use VS 2010 on M4600 VM )
4.	test it works
5.  update nuget deps?

NuGet Package Explorer:
6.	open the existing .nuspec file (in NuGet Package Explorer application, v4.1 or later required, I'm using 4.4.46, but that mangles @src attribute on save, so you'll need to fix it)
7.	update the version number etc, then save it
8.	publish (key is in user profiles doc; i left the append 'api/v2/package' option ticked)
9.  save new .nuspec (save metadata as..) if you edited in NuGet Package Explorer 
10.  push to GitHub

Procedure for -ImportXHTML is similar,
  but copy the docx4j.dll into it first.

TODO: review which version of .NET to target (see howto file)  
  


