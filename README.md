# MiniJava
Mini-Java is a subset of Java. MiniJava compiler implement a compiler for the Mini-java
programming language.

# Rules of MiniJava
```
Goal --> Source EOF
Source --> ClassDeclarations MainClass
MainClass --> class Identifier { public static void main() { VarDeclarations Statements}}
ClassDeclarations --> ClassDeclaration ClassDeclarations | lambda
ClassDeclaration --> class Identifier Extension { FieldDeclarations MethodDeclarations }
Extension --> extends Identifier | lambda
FieldDeclarations --> FieldDeclaration FieldDeclarations | lambda
FieldDeclaration --> static Type Identifier ;
VarDeclarations --> VarDeclaration VarDeclarations | lambda
VarDeclaration --> Type Identifier ;
MethodDeclarations --> MethodDeclaration MethodDeclarations | lambda
MethodDeclaration --> public static Type Identifier ( Parameters ) { VarDeclarations Statements return GenExpression ; }
Parameters --> Type Identifier Parameter | lambda
Parameter --> , Type Identifier Parameter | lambda
Type --> boolean | int
Statements --> Statements Statement | lambda
Statement --> { Statements } | if ( GenExpression ) Statement else Statement | while ( GenExpression ) Statement | System.out.println ( GenExpression ) ; | Identifier = GenExpression ;
GenExpression --> Expression | RelExpression
Expression --> Expression + Term | Expression - Term | Term
Term --> Term * Factor | Factor
Factor --> ( Expression ) | Identifier | Identifier . Identifier | Identifier . Identifier ( Arguments ) | true | false | Integer
RelExpression --> RelExpression && RelTerm | RelTerm
RelTerm --> Expression == Expression | Expression < Expression
Arguments --> GenExpression Argument | lambda
Argument --> , GenExpression Argument | lambda
Identifier --> <IDENTIFIER_LITERAL>
Integer --> <INTEGER_LITERAL>
```

## 1st Facade
از آنجا که کلاس Parser تنها از دو تابع CodeGenerator استفاده میکند، میتوان این دو تابع را بصورت یک واسط Facade به نام ParserCodeGeneratorFacade جدا کرد.
```java
package MiniJava.parser;

import MiniJava.codeGenerator.CodeGenerator;
import MiniJava.scanner.token.Token;

public class ParserCodeGeneratorFacade {
    private final CodeGenerator cg;

    public ParserCodeGeneratorFacade() {
        this.cg = new CodeGenerator();
    }

    public void semanticFunction(int func, Token next) {
        cg.semanticFunction(func, next);
    }

    public void printMemory() {
        cg.printMemory();
    }
}
```

## 2nd Facade
از آنجا که کلاس SymbolTable تنها از یک تابع Memory استفاده میکند، میتوان این تابع را بصورت یک واسط Facade به نام SymbolTableMemoryFacade جدا کرد.
```java
package MiniJava.semantic.symbol;

import MiniJava.codeGenerator.Memory;

public class SymbolTableMemoryFacade {
    private Memory mem;

    public SymbolTableMemoryFacade(Memory mem) {
        this.mem = mem;
    }

    public int getDateAddress() {
        return mem.getDateAddress();
    }
}
```