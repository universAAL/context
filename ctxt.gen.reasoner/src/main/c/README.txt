If you want to build the solution in Visual Studio, you may follow
the following instructions:

Create a new project within a new solution in your Visual Studio
and add persona_sr_notifier.c as the only source file to it. Then,
configure the project to create a dll for Win32 platform analog to
the following hints:

1. Compiler

- AdditionalIncludeDirectories:
  C:\Program Files\MySQL\MySQL Server 5.1\include
  C:\Program Files\Microsoft SDKs\Windows\v6.0A\Include

- PreprocessorDefinitions="HAVE_DLOPEN;_WIN32"

2. Linker

- AdditionalLibraryDirectories:
  C:\Program Files\MySQL\MySQL Server 5.1\lib
  C:\Program Files\Microsoft SDKs\Windows\v6.0A\Lib
  
- AdditionalDependencies="WSock32.Lib"

Then build the solution and copy the created dll to 

    C:\Program Files\MySQL\MySQL Server 5.1\lib\plugin

Create the 'plugin' directory, if it does not exist.
The dll file name must be 'persona_sr_notifier.dll'; change file
name, if necessary.
