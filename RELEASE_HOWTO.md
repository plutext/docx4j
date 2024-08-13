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

+ docx4j-ImportXHTML

+ Enterprise Ed. 

+ .NET dist



TODO: make toolchain UTF-8 filename safe ie git, zip, unzip

 
---------- 

Merge changes from 8 or 11 as appropriate.  If creating a patch (of changes in 11_1_2):

    git diff master VERSION_11_1_2 > patchfile

may need to run dos2unix on source file(s), then its

    patch -p1 --dry-run  < patchfile

Pull request can be merged into the correct branch by:
1. appending .patch to the URL in GitHub
2. git am < blah.patch
(they are automatically committed)

Update CHANGELOG.md, README.md with release info.

    http://www.jukie.net/bart/blog/pimping-out-git-log
        
(refer to CHANGELOG.md to see what rnumber to start at)  

    git lg b6c12c8..HEAD > stuff.txt  

Update pom.xml with target version number (must NOT be -SNAPSHOT for nexus-staging-maven-plugin )
Update <tag> in scm element.  (Can it just be deleted?)

Check sub-modules are using <version>${revision}</version> (ie that the 2 Maven commits from last time have been reverted)

Update build.xml so it has the same version as pom.xml (ie without  -SNAPSHOT)

Check jar versions in pom.xml, build.xml

Check everything is committed (though nexus-staging-maven-plugin doesn't seem to care)

Update Getting Started as necessary (inc HTML and PDF versions)

mvn clean

Run JarCheck on result of mvn install to check its compiled for 1.6 (run it on all jars in dist)

(Jar not clean? avoid mixing mvn and eclipse (test?) output)

git commit / push upstream
(uses git-remote-https, if you want to force a particular network connection)

-------------

Note for Java 11:  Maven Central requires Javadoc.

But org.slf4j is a multi-release jar, and the maven javadoc plugin can't handle it under Java 11: https://bugs.openjdk.java.net/browse/JDK-8222309
				 
So we have to build with Java 12 or later (currently 17) :-

$ sudo archlinux-java set java-17-openjdk

(running this also sets JAVA_HOME)

for import-XHTML:
    export JAVA_HOME=/usr/lib/jvm/java-8-openjdk
    mvn install -P jdk-8-config

Use eclipse/java-2022-06
(cf docx4j 8 / Java 8: use eclipse 2019-12)

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

New release process (docx4j 11.4.9 and later, which uses nexus-staging-maven-plugin instead of maven-deploy-plugin based on https://central.sonatype.org/publish/publish-maven/ and working with gpg 2.2.40 (first listed signature is jason@plutext.org)

    mvn clean deploy -P release

in deploy, prompt for passphrase is the *other* one [e..]

That should complete with something like:

		Uploaded to ossrh: https://oss.sonatype.org:443/service/local/staging/deployByRepositoryId/orgdocx4j-1095/org/docx4j/docx4j-core-tests/11.4.9/docx4j-core-tests-11.4.9.pom (2.7 kB at 1.5 kB/s)
		[INFO]  * Upload of locally staged artifacts finished.
		[INFO]  * Closing staging repository with ID "orgdocx4j-1095".

		Waiting for operation to complete...
		..........

		[INFO] Remote staged 1 repositories, finished with success.
		[INFO] Remote staging repositories are being released...

		Waiting for operation to complete...
		.........

		[INFO] Remote staging repositories released.
		[INFO] ------------------------------------------------------------------------
		[INFO] Reactor Summary for docx4j parent 11.4.9:
		[INFO]
		[INFO] docx4j parent ...................................... SUCCESS [  6.737 s]
		[INFO] docx4j-openxml-objects ............................. SUCCESS [ 14.698 s]
		[INFO] docx4j-openxml-objects-pml ......................... SUCCESS [  4.891 s]
		[INFO] docx4j-openxml-objects-sml ......................... SUCCESS [  5.554 s]
		[INFO] docx4j-core ........................................ SUCCESS [ 12.495 s]
		[INFO] docx4j-JAXB-ReferenceImpl .......................... SUCCESS [  3.642 s]
		[INFO] docx4j-samples-resources ........................... SUCCESS [  2.389 s]
		[INFO] docx4j-core-tests .................................. SUCCESS [  4.713 s]
		[INFO] docx4j-diffx ....................................... SUCCESS [  4.096 s]
		[INFO] docx4j-docx-anon ................................... SUCCESS [  3.227 s]
		[INFO] docx4j-export-fo ................................... SUCCESS [  3.740 s]
		[INFO] docx4j-conversion-via-microsoft-graph .............. SUCCESS [  2.839 s]
		[INFO] docx4j-documents4j-remote .......................... SUCCESS [  3.503 s]
		[INFO] docx4j-documents4j-local ........................... SUCCESS [  3.386 s]
		[INFO] docx4j-JAXB-MOXy ................................... SUCCESS [  3.149 s]
		[INFO] docx4j-samples-docx4j .............................. SUCCESS [  0.476 s]
		[INFO] docx4j-samples-docx-diffx .......................... SUCCESS [  0.316 s]
		[INFO] docx4j-samples-docx-export-fo ...................... SUCCESS [  0.445 s]
		[INFO] docx4j-samples-pptx4j .............................. SUCCESS [  0.347 s]
		[INFO] docx4j-samples-xlsx4j .............................. SUCCESS [  0.340 s]
		[INFO] docx4j-samples-glox4j .............................. SUCCESS [  0.317 s]
		[INFO] docx4j-samples-conversion-via-microsoft-graph ...... SUCCESS [  0.431 s]
		[INFO] docx4j-samples-documents4j-remote .................. SUCCESS [  0.356 s]
		[INFO] docx4j-samples-documents4j-local ................... SUCCESS [  0.504 s]
		[INFO] docx4j-bundle ...................................... SUCCESS [  3.399 s]
		[INFO] docx4j-legacy-service .............................. SUCCESS [06:13 min]
		[INFO] ------------------------------------------------------------------------
		[INFO] BUILD SUCCESS
		[INFO] ------------------------------------------------------------------------
		[INFO] Total time:  07:40 min

ie no need to Login to the Nexus UI at https://oss.sonatype.org/index.html#welcome anymore,
or to manually close then release :-)

If the remote operation is skipped, it might be because your last module had skip in it? (eg docx4j-ImportXHTML)

This new release process does not add a tag; easiest to do that in SmartGit.

Repeat above for -ImportXHTML 

Run ant release (requires docx4j, -ImportXHTML  to be in maven)

 ant release  -buildfile etc/build.xml
    
Ideally you'd commit the branch with the actual released version number in the pom,
then checkout -b an incremented version number,
and in that branch do -SNAPSHOT.

Here, also do:  git push -u origin [the incremented version number]  <--------- set up to track remote branch
(can do that in SmartGit)

Switch branch if necessary, eg:

    git checkout master
    
and

$ sudo archlinux-java set java-8-openjdk

or 

$ sudo archlinux-java set java-14-adoptopenjdk


Update pom.xml to incremented-SNAPSHOT


----

Put in /docx4j dir, for example

	scp *2.1.zip  ubuntu@docx4java.org:/home/ubuntu/docx4j-8.2.1/


Update downloads.html
Announce release in docx4j forum
Update news  (includes link to release announcement)

Update the default branch in GitHub (Settings > General > Default Branch > Switch)

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
  
-----

The infernal:-

grep -rli '<nature>org.fusesource.ide.project.RiderProjectNature</nature>' * | xargs -i@ sed -i 's/<nature>org.fusesource.ide.project.RiderProjectNature<\/nature>//g' @



