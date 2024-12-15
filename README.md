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

## Replace Conditional with Polymorphism
برای اعمال این بازآرایی تابع semanticAction در CodeGeneration را هدف قرار دادیم. این تابع با توجه به یک مقدار int func توابع مختلف تولید کد را صدا میزند. برای رفع این موضوع، ما یک واسط به نام SematicAction به صورت زیر تعریف کردیم.
```java
public interface SemanticAction {
    void execute(CodeGenerator cg, Token next);
}
```
سپس به ازای هر یک از توابع صدا شده در semanticAction یک کلاس تعریف کردیم که از SemanticAction ارث میبرد و تابع execute آن دارای محتویات جابجا شده از تابع موجود در کلاس CodeGeneration است. بعنوان مثال کلاس CheckIdSemanticAction بازآرایی شده‌ی checkId بصورت زیر است:
```java
class CheckIDSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<String> symbolStack = cg.getSymbolStack();
        Stack<Address> ss = cg.getSs();

        symbolStack.pop();
        if (ss.peek().varType == varType.Non) {
            //TODO : error
        }
    }
}
```
سپس یک array حاوی SemanticActionهای مختلف در CodeGenerator قرار دادیم و مقادیر شماره‌ی تابع را به عنوان اندیس‌شان قرار دادیم. به صورت زیر:
```java
public class CodeGenerator {
    // other stuff

    private SemanticAction[] actionMap = new SemanticAction[34];

    public CodeGenerator() {
        symbolTable = new SymbolTable(memory);
        
        actionMap[0] = new ReturnSemanticAction();
        actionMap[1] = new CheckIDSemanticAction();
        actionMap[2] = new PidSemanticAction();
        actionMap[3] = new FpidSemanticAction();
        actionMap[4] = new KpidSemanticAction();
        // ...
        actionMap[29] = new MethodReturnSemanticAction();
        actionMap[30] = new DefParamSemanticAction();
        actionMap[31] = new LastTypeBoolSemanticAction();
        actionMap[32] = new LastTypeIntSemanticAction();
        actionMap[33] = new DefMainSemanticAction();
    }

    // more other stuff
}
```
در نهایت تابع semanticAction بصورت زیر شد:
```java
public void semanticFunction(int func, Token next) {
    Log.print("codegenerator : " + func);
    actionMap[func].execute(this, next);
}
```

## Seperate Query from Modifier
برای اعمال این بازآرایی تابع getTemp() در Memory را که هم عمل Modification انجام میداد و هم مقدار قبلی را باز میگرداند به دو تابع getTempIndex() و addTempIndex() تغییر میدهیم و هر جا از آن استفاده شده بود، با توابه جدید بروزرسانی میکنیم.

```java
public class Memory {
    // other stuff

    public int getTempIndex() {
        return lastTempIndex;
    }

    public void addTempIndex() {
        lastTempIndex += tempSize;
    }

    // more other stuff
}
```

## Self-Encapsulate Field
این موضوع در چندین کلاس رعایت نشده است. یکی از نمونه‌‌های آن در کلاس Memory است که متغیر private با نام codeBlock آن در داخل کلاس بصورت direct access استفاده شده‌ است که ما بصورت زیر این کلاس را تغییر دادیم.

```java
public class Memory {
    @Getter
    @Setter
    private ArrayList<_3AddressCode> codeBlock;
    private int lastTempIndex;
    private int lastDataAddress;
    private final int stratTempMemoryAddress = 500;
    private final int stratDataMemoryAddress = 200;
    private final int dataSize = 4;
    private final int tempSize = 4;

    public Memory() {
        setCodeBlock(new ArrayList<_3AddressCode>());
        lastTempIndex = stratTempMemoryAddress;
        lastDataAddress = stratDataMemoryAddress;
    }

    public int getTempIndex() {
        return lastTempIndex;
    }

    public void addTempIndex() {
        lastTempIndex += tempSize;
    }

    public int getDateAddress() {
        lastDataAddress += dataSize;
        return lastDataAddress - dataSize;
    }

    public int saveMemory() {
        getCodeBlock().add(new _3AddressCode());
        return getCodeBlock().size() - 1;
    }

    public void add3AddressCode(Operation op, Address opr1, Address opr2, Address opr3) {
        getCodeBlock().add(new _3AddressCode(op, opr1, opr2, opr3));
    }

    public void add3AddressCode(int i, Operation op, Address opr1, Address opr2, Address opr3) {
        getCodeBlock().remove(i);
        getCodeBlock().add(i, new _3AddressCode(op, opr1, opr2, opr3));
    }

    public int getCurrentCodeBlockAddress() {
        return getCodeBlock().size();
    }

    public void pintCodeBlock() {
        System.out.println("Code Block");
        for (int i = 0; i < getCodeBlock().size(); i++) {
            System.out.println(i + " : " + getCodeBlock().get(i).toString());
        }
    }
}
```

## Additional Refactoring 1: Encapsulate Collection
این تکنیک بازآرایی بدین معناست که داده-ساختارهای از نوع collection نباید تنها یک getter و setter ساده داشته باشند بلکه getter آنها باید یک کپی immutable برگرداند و همچنین بجای setter از توابع ریزدانه‌ای add و remove استفاده کنیم. ما این تکنیک را برای متغیر rules در کلاس Parser اعمال کردیم. (۴ تابع به این کلاس افزودیم و بجای دسترسی مستقیم به rules و تغییر محتویات آن، از این توابع استفاده کردیم.)
```java
public void clearRules() {
    this.rules = new ArrayList<Rule>();
}

public void addRule(Rule rule) {
    this.rules.add(rule);
}

public void removeRule(Rule rule) {
    this.rules.remove(rule);
}

public List<Rule> getRules() {
    return Collections.unmodifiableList(this.rules);
}
```

## Additional Refactoring 2: Replace Exception with Test
این تکنیک به این صورت است که برای افزایش خوانایی کد بجای استفاده از try/catch برای مقداردهی یک متغیر یا برگرداندن یک مقدار خاص، از if استفاده کنیم. ما برای این تکنیک، try/catch موجود در کلاس Rule را که عدد را از رشته استخراج میکند به ضکل زیر تبدیل کردیم.
```diff
- try {
-     semanticAction = Integer.parseInt(stringRule.substring(index + 1));
- } catch (NumberFormatException ex) {
-     semanticAction = 0;
- }
+ String actionPart = stringRule.substring(index + 1);
+ if (actionPart.matches("\\d+")) {
+     semanticAction = Integer.parseInt(actionPart);
+ } else {
+     semanticAction = 0;
+ }
```

## سوالات نهایی
### سوال ۱
- کد تمیز: کدی است که به راحتی خوانده، فهمیده و نگهداری می‌شود. این کد از قواعد مشخص، نام‌گذاری مناسب و مستندسازی کافی استفاده می‌کند تا خوانایی و کار تیمی را بهبود ببخشد.
- بدهی فنی: بدهی فنی به هزینه‌ای اشاره دارد که به دلیل کدنویسی سریع و غیراستاندارد (معمولا برای سرعت بخشیدن به تحویل پروژه) ایجاد می‌شود و نیازمند بازنویسی یا بهبود در آینده است.
- بوی بد کد: به علائم یا نشانه‌هایی در کد گفته می‌شود که احتمال وجود مشکلات طراحی یا ضعف‌هایی در کد را نشان می‌دهند، مانند توابع خیلی طولانی یا استفاده نادرست از الگوها. این بوها به معنی اشتباه نیستند، اما به بازبینی و بهبود نیاز دارند.

### سوال ۲
1. Bloaters (متورم‌ها)  
   کدهایی هستند که با گذر زمان بسیار بزرگ و غیرقابل مدیریت می‌شوند. این موارد شامل:
   - Long Method (متد طولانی): متدهایی که بسیار طولانی شده‌اند و خوانایی و نگهداری آن دشوار است.  
   - Large Class (کلاس بزرگ): کلاس‌هایی که وظایف زیادی دارند و باید به بخش‌های کوچک‌تر تقسیم شوند.  
   - Primitive Obsession: استفاده بیش‌ازحد از انواع داده‌های اولیه به‌جای کلاس‌های مشخص.  
   - Long Parameter List (فهرست پارامتر طولانی): متدهایی که تعداد زیادی پارامتر دارند.  
   - Data Clumps: گروه‌هایی از داده که همیشه با هم ظاهر می‌شوند و می‌توان آن‌ها را در یک کلاس قرار داد.
2. Object-Orientation Abusers (سوء‌استفاده‌کنندگان از شی‌گرایی)  
   این کدها نشان‌دهنده استفاده نادرست یا ناقص از اصول برنامه‌نویسی شی‌گرا هستند. موارد شامل:  
   - Switch Statements (دستورات Switch): استفاده زیاد از دستورات شرطی به‌جای استفاده از پلی‌مورفیسم.  
   - Refused Bequest: کلاس فرزند از برخی قابلیت‌های ارث‌بری استفاده نمی‌کند.  
   - Alternative Classes with Different Interfaces: کلاس‌هایی که کار مشابهی انجام می‌دهند ولی رابط‌های متفاوت دارند.  
   - Temporary Field: فیلدهایی که در بعضی موارد استفاده می‌شوند و در دیگر قسمت‌ها بی‌استفاده‌اند.
3. Change Preventers (مانع تغییر)  
   این بوها نشان می‌دهند که یک تغییر کوچک نیاز به تغییرات زیادی در قسمت‌های دیگر کد دارد. این موارد شامل:  
   - Divergent Change (تغییر واگرا): کلاس‌هایی که برای تغییرات مختلف نیاز به اصلاحات متفاوت دارند.  
   - Shotgun Surgery: تغییر در یک قسمت نیاز به تغییر در بسیاری از کلاس‌های دیگر دارد.  
   - Parallel Inheritance Hierarchies: ایجاد سلسله مراتب ارث‌بری مشابه و موازی که نگهداری را دشوار می‌کند.
4. Dispensables (بیهوده‌ها)  
   شامل کدهایی است که وجودشان بی‌فایده است و حذف آن‌ها کد را تمیزتر می‌کند:  
   - Duplicate Code (کد تکراری): تکرار یک کد در بخش‌های مختلف برنامه.  
   - Lazy Class (کلاس تنبل): کلاس‌هایی که کار خاصی انجام نمی‌دهند.  
   - Dead Code (کد مرده): کدهایی که هرگز اجرا نمی‌شوند.  
   - Comments (کامنت‌ها): توضیح اضافی و غیرضروری که به‌جای اصلاح کد نوشته شده‌اند.  
   - Speculative Generality: کدهایی که برای آینده نوشته می‌شوند اما استفاده‌ای ندارند.
5. Couplers (وابستگی‌ها)  
   کدهایی که وابستگی بیش‌ازحدی بین کلاس‌ها ایجاد می‌کنند. این موارد شامل:  
   - Feature Envy: متدی که بیشتر به داده‌های کلاس دیگری وابسته است تا داده‌های کلاس خودش.  
   - Inappropriate Intimacy: کلاس‌ها به جزییات داخلی یکدیگر بیش از حد دسترسی دارند.  
   - Message Chains: کدهایی که چندین فراخوانی زنجیره‌ای ایجاد می‌کنند.  
   - Middle Man: کلاس‌هایی که تنها مسئول انتقال درخواست‌ها هستند و کار دیگری انجام نمی‌دهند.

### سوال ۳
- سوال ۳.۱. بوی بد Lazy Class در دسته‌بندی Dispensables (بیهوده‌ها) قرار می‌گیرد.
- سوال ۳.۲. 
   برای حذف Lazy Class می‌توان از بازآرایی‌های زیر استفاده کرد:  
   - Inline Class: ادغام کلاس تنبل در کلاس دیگری که بیشترین استفاده از ویژگی‌های آن کلاس را دارد.  
   - Collapse Hierarchy: اگر کلاس بخشی از یک سلسله مراتب ارث‌بری باشد و عملکرد خاصی نداشته باشد، آن را با کلاس والد ادغام می‌کنیم.
- سوال ۳.۳. در شرایطی که کلاس ممکن است در آینده توسعه یابد و وظایف بیشتری به آن اضافه شود یا در طراحی فعلی نقش ساختاری خاصی دارد (مانند کلاس‌های فریم‌ورک‌ها که هنوز نیاز به تکامل دارند)، می‌توان Lazy Class را موقتاً نادیده گرفت.

### سوال ۴
1. استفاده از عبارات سوییچ بجای مفاهیمی مانند Polyumorphism [Link](https://github.com/bigsheykh/Convert_UML_to_ANSI_C/blob/51b0fa79d4797d2a9afc3f27e0f57984b537431a/src/com/project/phase2CodeGeneration/Phase2CodeFileManipulator.java#L451)
2. کد و متغیر‌های. بلااستفاده و به اصطلاح مرده مثلا [Link](https://github.com/bigsheykh/Convert_UML_to_ANSI_C/blob/51b0fa79d4797d2a9afc3f27e0f57984b537431a/src/com/project/classBaseUML/ClassMethod.java#L31)
3. کد تکراری در [Link1](https://github.com/bigsheykh/Convert_UML_to_ANSI_C/blob/51b0fa79d4797d2a9afc3f27e0f57984b537431a/src/com/project/classBaseUML/ClassConstructor.java#L89) و [Link2](https://github.com/bigsheykh/Convert_UML_to_ANSI_C/blob/51b0fa79d4797d2a9afc3f27e0f57984b537431a/src/com/project/classBaseUML/ClassMethod.java#L124). این قسمت را میتوان با یک تابع extractAttributes جایگزین کرد.
4. Primitive Obsession در بعضی جاها مانند [Link](https://github.com/bigsheykh/Convert_UML_to_ANSI_C/blob/51b0fa79d4797d2a9afc3f27e0f57984b537431a/src/com/project/classBaseUML/ClassMethod.java#L166) خوانایی کد را خیلی کاهش داده و اگر مقدار بازگردانده شده‌ی این تابع بصورت کلاس تعریف میشد خواناتر و قابل. تغییر خواهد بود.
5. کلاس بزرگ یا Data: [Link](https://github.com/bigsheykh/Convert_UML_to_ANSI_C/blob/master/src/com/project/lexicalAnalyzer/CLanguageTokens.java). این کلاس میتواند توسط Enum جایگزین شود.
6. استفاده از کامنت برای توضیح کد ناخوانا [Link](https://github.com/bigsheykh/Convert_UML_to_ANSI_C/blob/51b0fa79d4797d2a9afc3f27e0f57984b537431a/src/com/project/diagramGUI/GUIList.java#L96)
7. Refused Bequest در [Link](https://github.com/bigsheykh/Convert_UML_to_ANSI_C/blob/51b0fa79d4797d2a9afc3f27e0f57984b537431a/src/com/project/classBaseUML/ClassDiagram.java#L95)
8. توابع و کلاس‌های طولانی
9. تغییرات واگرا یا Divergent Change
10. Shotgun Surgery

### سوال ۵
پلاگین formatter ابزاری است که کد ما را بر اساس قواعد و استانداردهای مشخصی قالب‌بندی می‌کند. این ابزار به‌طور خودکار فاصله‌ها، تورفتگی‌ها، محل قرارگیری پرانتزها و سایر عناصر ساختاری کد را تنظیم می‌کند.

مزایا:
- خوانایی بهتر: کد تمیزتر و منسجم‌تر می‌شود و توسعه‌دهندگان دیگر به‌راحتی آن را درک می‌کنند.
- کاهش اشتباهات: جلوگیری از بروز خطاهای ناشی از فرمت نادرست کد.
- سازگاری تیمی: همه اعضای تیم از یک استاندارد واحد برای نوشتن کد استفاده می‌کنند.

رابطه با بازآرایی کد: 
- اغلب اولین گام برای بهبود ساختار کد Formatter است، و بازآرایی‌ها بعد از آن انجام می شوند تا منطق کد بهبود یابد و بوهای بد برطرف شوند.
