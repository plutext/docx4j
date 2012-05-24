Docx4j release process
======================

[Do this process for a release candidate first]

Check everything is committed

Update Getting Started as necessary (inc HTML and PDF versions)

Check jar versions in pom.xml, build.xml

Update README.txt with release info.

    http://www.jukie.net/bart/blog/pimping-out-git-log
    
(refer to README.txt to see what rnumber to start at)    

Update pom.xml with target version number

git commit
git push upstream

----

Start up the Git Bash session and go to your project directory.

Windows users, you need to start up an SSH agent to provide your passkey when needed by the release process.

To do this, in your Git Shell type :
    1eval `ssh-agent`  //pay attention to the back tick quotes here
which should return a piece of text like Agent pid xyz. This command starts the agent and sets up a couple of 
environment variables relating to the SSH agent. If you type env | grep SSH you will see the environment variables :
   1$ env | grep SSH
   2SSH_AGENT_PID=1234
   3SSH_AUTH_SOCK=/tmp/ssh-abcdef1234/agent.5678

Windows users will need to manually create the directory c:/tmp/ssh-abcdef1234/agent.5678 otherwise you
get and error saying could not open a connection to your authentication agent.

Create the new directory and then add your key to the agent using the following syntax which assumes your 
Github RSA key is in the c:\.ssh\ directory. If it isn’t then just substitute the directory for your key.

   1$ssh-add "/c/.ssh/id_rsa"

then per https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide

mvn release:clean

mvn release:prepare

mvn release:perform

perform breaks (1) because target/generated-sources is already under version control (why? get rid of this)

so have to mv target out of the way, svn update, then copy current contents into it

perform breaks (2) because it run in wrong dir, so cd target/checkout/docx4j and manually run it:

$ mvn deploy --no-plugin-updates -Psonatype-oss-release -f pom.xml

(This runs on the branch you've created in previous steps. All the previous steps do is create that branch, and change trunk's pom to whatever next version will be)

Uploading: https://oss.sonatype.org/service/local/staging/deploy/maven2//org/docx4j/docx4j/2.7.1/docx4j-2.7.1.jar
5691K uploaded  (docx4j-2.7.1.jar)

Then release it:

1. close the staging repository:

   1. Login to the Nexus UI, https://oss.sonatype.org/index.html#welcome
   2. Go to Staging Repositories page.
   3. Select a staging repository.
   4. Click the Close button.

2. when you are sure the closed staging repository has no problem, click the Release button.

--------

The mvn generated jar is currently bigger (5.6MB), because it contains unnecessary docx.

1111267 Fri Oct 14 15:03:28 EST 2011 jaxb-binder-issue.docx

TODO: fix this

-------

ANT_OPTS="-Xmx512m -XX:MaxPermSize=256m" ant dist

    (Remove the jaxb jars from dist dir)

    Create docx4j-x.x.x.tar.gz (ant dist, rename the jar as well)

----

Announce release in docx4j forum