# Compiler
A Compiler built for Design of Compilers class  
Anthony Gandini Spring 2023  
  
Project 1 is in the Lexer branch  
Project 2 is in the Parser branch  
Project 3 is in the Semantic Analysis branch  
Project 4 is in the main branch  
  
Usage Intructions:  
Compiler.java is the entry point  
from the root directory of the project  
javac ./src/*.java  
java -cp ./src Compiler (input file)  
  
the output of each program is printed and can be found in ./src/IO/output.txt  
output is copied to clipboard when verbose mode is on  
  
the output can be tested [Here](https://www.labouseur.com/commondocs/operating-systems/SvegOS/public_html/index.html)

### Language Grammar Used
https://www.labouseur.com/courses/compilers/grammar.pdf

### The Assignments
[Project 1: Lexer](https://www.labouseur.com/courses/compilers/project1.pdf)  
[Project 2: Parser](https://www.labouseur.com/courses/compilers/project2.pdf)  
[Project 3: Semantic Analysis](https://www.labouseur.com/courses/compilers/project3.pdf)  
[Project 4: Code Generation](https://www.labouseur.com/courses/compilers/project4.pdf)  
  
## Note for Project 2  
I worked on a GUI to display the CST but opted to not include it  
because I was unable to get it to work properly before the deadline.  
I currently have no plans to finish it.
  
## Note for Project 4  
I have done some code optimization in conditionals:  
A conditional contatining 2 constants will be evaluated at compile time  
    this will either omit branch code or omit the branch entirely  
Conditionals containing a string constant and a variable can be partially evaluated at compile time  
    I check if the string is in the heap, if it is not then the block is omitted
    if the string is in the heap then normal code gen is done  