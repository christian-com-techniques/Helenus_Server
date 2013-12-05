JAVAC = javac
JAVACFLAGS = -d bin/ -Xlint:unchecked


all : clean classfiles



clean : 
	-@rm bin/*


classfiles:
	$(JAVAC) $(JAVACFLAGS) src/*.java
