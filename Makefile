#
# Makefile to build the jltools source to source compiler
# includes a makefile in each package to handle building of respective 
# packages
#


# set up some reasonable defaults (for building in CUCS)
JC 			= javac
JAVA			= java
JAR			= jar

JAVADOC_MAIN		= com.sun.tools.javadoc.Main
JAVADOC_DOCLET		= -doclet iContract.doclet.Standard

CUP_RUNTIME		= /home/nks/lib
CLASSPATH		= $(SOURCE):/usr/local/jdk1.2.1/jre/lib/rt.jar:$(CUP_RUNTIME)
CLASSPATHFLAG		= -classpath $(CLASSPATH)
JC_FLAGS 		= $(CLASSPATHFLAG)

JAR_FILE		= jltools.jar
JAR_FLAGS		= cf 

JAVADOC_OUTPUT		= ./javadoc
JAVADOC_FLAGS		= -mx40m -ms40m -classpath /home/nks/lib/iDoclet.jar:/usr/local/jdk1.2.1/lib/tools.jar:$(CLASSPATH) 

SOURCE			= .
PERSONAL_MAKEFILE	= Makefile.personal

# allow users to overload the above by overriding the above settings.
-include $(PERSONAL_MAKEFILE)

# let the included makefiles know that we called them. otherwise, they'll try to 
# call here.
CALLED_FROM_PARENT 	= true

#implicit rule to build class files
%.class: %.java
	$(JC) $(JC_FLAGS) $<

#everything:
all: util lex parse types ast visit ext/op frontend runtime main

#include all of our package makefiles. they give us what class files are in each.
include jltools/util/Makefile
include jltools/lex/Makefile
include jltools/parse/Makefile
include jltools/ast/Makefile 
include jltools/types/Makefile
include jltools/visit/Makefile
include jltools/ext/op/Makefile
include jltools/frontend/Makefile
include jltools/main/Makefile
include jltools/runtime/Makefile

#other targets:
util: $(UTIL_TARGET) 

types: util $(TYPES_TARGET)

lex: util types $(LEX_TARGET)

parse: util lex $(PARSE_TARGET)

ast: util types lex $(AST_TARGET)

visit: util types ast $(VISIT_TARGET)

ext/op: util types ast $(EXTOP_TARGET)

frontend: util types lex parse ast visit $(FRONTEND_TARGET)

runtime: $(RUNTIME_TARGET)

main: frontend runtime $(MAIN_TARGET)

#clean: (just delete the class files)
clean: 
	rm -f jltools/util/*.class
	rm -f jltools/lex/*.class
	rm -f jltools/parse/*.class
	rm -f jltools/ast/*.class
	rm -f jltools/types/*.class
	rm -f jltools/visit/*.class
	rm -f jltools/ext/op/*.class
	rm -f jltools/frontend/*.class
	rm -f jltools/main/*.class
	rm -f jltools/runtime/*.class
	rm -f jltools/util/*.html
	rm -f jltools/lex/*.html
	rm -f jltools/parse/*.html
	rm -f jltools/ast/*.html
	rm -f jltools/types/*.html
	rm -f jltools/visit/*.html
	rm -f jltools/ext/op/*.html
	rm -f jltools/frontend/*.html
	rm -f jltools/main/*.html
	rm -f jltools/runtime/*.html


# Delete class files as well as the grammar files, so that we can regenerate 
# them. Also delete the javadoc & jar file, if they exist.
clobber superclean: clean
	rm -f jltools/parse/Grm.java
	rm -f jltools/parse/sym.java
	rm -f $(JAR_FILE)
	rm -rf $(JAVADOC_OUTPUT)

# create a jar file
jar: all
	$(JAR) $(JAR_FLAGS) $(JAR_FILE) jltools/*/*.class

javadoc: FORCE
	-mkdir -p $(JAVADOC_OUTPUT)
	$(JAVA) $(JAVADOC_FLAGS) $(JAVADOC_MAIN) -d $(JAVADOC_OUTPUT) \
		-classpath $(CLASSPATH) $(JAVADOC_DOCLET) -package\
			jltools.ast      \
			jltools.lex      \
			jltools.frontend \
			jltools.runtime  \
			jltools.parse    \
			jltools.types    \
			jltools.util     \
			jltools.visit    \
			jltools.main     \
			jltools.ext.op 

FORCE:

classpath: # type "eval `make classpath`" to set classpath
	@echo setenv CLASSPATH "$(CLASSPATH)"
