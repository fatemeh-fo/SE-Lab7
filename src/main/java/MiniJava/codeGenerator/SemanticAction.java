package MiniJava.codeGenerator;

import java.util.Stack;

import MiniJava.errorHandler.ErrorHandler;
import MiniJava.scanner.token.Token;
import MiniJava.semantic.symbol.Symbol;
import MiniJava.semantic.symbol.SymbolTable;
import MiniJava.semantic.symbol.SymbolType;

public interface SemanticAction {
    void execute(CodeGenerator cg, Token next);
}

class ReturnSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {}
}

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

class PidSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<String> symbolStack = cg.getSymbolStack();
        SymbolTable symbolTable = cg.getSymbolTable();
        Stack<Address> ss = cg.getSs();

        if (symbolStack.size() > 1) {
            String methodName = symbolStack.pop();
            String className = symbolStack.pop();
            try {

                Symbol s = symbolTable.get(className, methodName, next.value);
                varType t = varType.Int;
                switch (s.type) {
                    case Bool:
                        t = varType.Bool;
                        break;
                    case Int:
                        t = varType.Int;
                        break;
                }
                ss.push(new Address(s.address, t));


            } catch (Exception e) {
                ss.push(new Address(0, varType.Non));
            }
            symbolStack.push(className);
            symbolStack.push(methodName);
        } else {
            ss.push(new Address(0, varType.Non));
        }
        symbolStack.push(next.value);
    }
}

class FpidSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<String> symbolStack = cg.getSymbolStack();
        SymbolTable symbolTable = cg.getSymbolTable();
        Stack<Address> ss = cg.getSs();
        
        ss.pop();
        ss.pop();

        Symbol s = symbolTable.get(symbolStack.pop(), symbolStack.pop());
        varType t = varType.Int;
        switch (s.type) {
            case Bool:
                t = varType.Bool;
                break;
            case Int:
                t = varType.Int;
                break;
        }
        ss.push(new Address(s.address, t));
    }
}

class KpidSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        SymbolTable symbolTable = cg.getSymbolTable();
        Stack<Address> ss = cg.getSs();
        ss.push(symbolTable.get(next.value));
    }
}

class IntpidSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<Address> ss = cg.getSs();
        ss.push(new Address(Integer.parseInt(next.value), varType.Int, TypeAddress.Imidiate));
    }
}

class StartCallSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        SymbolTable symbolTable = cg.getSymbolTable();
        Stack<String> symbolStack = cg.getSymbolStack();
        Stack<Address> ss = cg.getSs();
        Stack<String> callStack = cg.getCallStack();

        //TODO: method ok
        ss.pop();
        ss.pop();
        String methodName = symbolStack.pop();
        String className = symbolStack.pop();
        symbolTable.startCall(className, methodName);
        callStack.push(className);
        callStack.push(methodName);

        //symbolStack.push(methodName);    
    }
}

class CallSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        SymbolTable symbolTable = cg.getSymbolTable();
        Stack<Address> ss = cg.getSs();
        Stack<String> callStack = cg.getCallStack();
        Memory memory = cg.getMemory();
        
        //TODO: method ok
        String methodName = callStack.pop();
        String className = callStack.pop();
        try {
            symbolTable.getNextParam(className, methodName);
            ErrorHandler.printError("The few argument pass for method");
        } catch (IndexOutOfBoundsException e) {
        }
        varType t = varType.Int;
        switch (symbolTable.getMethodReturnType(className, methodName)) {
            case Int:
                t = varType.Int;
                break;
            case Bool:
                t = varType.Bool;
                break;
        }
        Address temp = new Address(memory.getTempIndex(), t);
        memory.addTempIndex();
        ss.push(temp);
        memory.add3AddressCode(Operation.ASSIGN, new Address(temp.num, varType.Address, TypeAddress.Imidiate), new Address(symbolTable.getMethodReturnAddress(className, methodName), varType.Address), null);
        memory.add3AddressCode(Operation.ASSIGN, new Address(memory.getCurrentCodeBlockAddress() + 2, varType.Address, TypeAddress.Imidiate), new Address(symbolTable.getMethodCallerAddress(className, methodName), varType.Address), null);
        memory.add3AddressCode(Operation.JP, new Address(symbolTable.getMethodAddress(className, methodName), varType.Address), null, null);

        //symbolStack.pop();
    }
}

class ArgSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        SymbolTable symbolTable = cg.getSymbolTable();
        Stack<Address> ss = cg.getSs();
        Stack<String> callStack = cg.getCallStack();
        Memory memory = cg.getMemory();

        //TODO: method ok

        String methodName = callStack.pop();
//        String className = symbolStack.pop();
        try {
            Symbol s = symbolTable.getNextParam(callStack.peek(), methodName);
            varType t = varType.Int;
            switch (s.type) {
                case Bool:
                    t = varType.Bool;
                    break;
                case Int:
                    t = varType.Int;
                    break;
            }
            Address param = ss.pop();
            if (param.varType != t) {
                ErrorHandler.printError("The argument type isn't match");
            }
            memory.add3AddressCode(Operation.ASSIGN, param, new Address(s.address, t), null);

//        symbolStack.push(className);

        } catch (IndexOutOfBoundsException e) {
            ErrorHandler.printError("Too many arguments pass for method");
        }
        callStack.push(methodName);
    }
}

class AssignSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<Address> ss = cg.getSs();
        Memory memory = cg.getMemory();

        Address s1 = ss.pop();
        Address s2 = ss.pop();
//        try {
        if (s1.varType != s2.varType) {
            ErrorHandler.printError("The type of operands in assign is different ");
        }
//        }catch (NullPointerException d)
//        {
//            d.printStackTrace();
//        }
        memory.add3AddressCode(Operation.ASSIGN, s1, s2, null);
    }
}

class AddSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<Address> ss = cg.getSs();
        Memory memory = cg.getMemory();

        Address temp = new Address(memory.getTempIndex(), varType.Int);
        memory.addTempIndex();
        Address s2 = ss.pop();
        Address s1 = ss.pop();

        if (s1.varType != varType.Int || s2.varType != varType.Int) {
            ErrorHandler.printError("In add two operands must be integer");
        }
        memory.add3AddressCode(Operation.ADD, s1, s2, temp);
        ss.push(temp);
    }
}

class SubSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<Address> ss = cg.getSs();
        Memory memory = cg.getMemory();

        Address temp = new Address(memory.getTempIndex(), varType.Int);
        memory.addTempIndex();
        Address s2 = ss.pop();
        Address s1 = ss.pop();
        if (s1.varType != varType.Int || s2.varType != varType.Int) {
            ErrorHandler.printError("In sub two operands must be integer");
        }
        memory.add3AddressCode(Operation.SUB, s1, s2, temp);
        ss.push(temp);
    }
}

class MultSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<Address> ss = cg.getSs();
        Memory memory = cg.getMemory();

        Address temp = new Address(memory.getTempIndex(), varType.Int);
        memory.addTempIndex();
        Address s2 = ss.pop();
        Address s1 = ss.pop();
        if (s1.varType != varType.Int || s2.varType != varType.Int) {
            ErrorHandler.printError("In mult two operands must be integer");
        }
        memory.add3AddressCode(Operation.MULT, s1, s2, temp);
//        memory.saveMemory();
        ss.push(temp);
    }
}

class LabelSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<Address> ss = cg.getSs();
        Memory memory = cg.getMemory();

        ss.push(new Address(memory.getCurrentCodeBlockAddress(), varType.Address));
    }
}

class SaveSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<Address> ss = cg.getSs();
        Memory memory = cg.getMemory();
        
        ss.push(new Address(memory.saveMemory(), varType.Address));
    }
}

class WhileSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<Address> ss = cg.getSs();
        Memory memory = cg.getMemory();

        memory.add3AddressCode(ss.pop().num, Operation.JPF, ss.pop(), new Address(memory.getCurrentCodeBlockAddress() + 1, varType.Address), null);
        memory.add3AddressCode(Operation.JP, ss.pop(), null, null);
    }
}

class JpfSaveSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<Address> ss = cg.getSs();
        Memory memory = cg.getMemory();

        Address save = new Address(memory.saveMemory(), varType.Address);
        memory.add3AddressCode(ss.pop().num, Operation.JPF, ss.pop(), new Address(memory.getCurrentCodeBlockAddress(), varType.Address), null);
        ss.push(save);
    }
}

class JpHereSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<Address> ss = cg.getSs();
        Memory memory = cg.getMemory();

        memory.add3AddressCode(ss.pop().num, Operation.JP, new Address(memory.getCurrentCodeBlockAddress(), varType.Address), null, null);
    }
}

class PrintSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<Address> ss = cg.getSs();
        Memory memory = cg.getMemory();

        memory.add3AddressCode(Operation.PRINT, ss.pop(), null, null);
    }
}

class EqualSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<Address> ss = cg.getSs();
        Memory memory = cg.getMemory();
        
        Address temp = new Address(memory.getTempIndex(), varType.Bool);
        memory.addTempIndex();
        Address s2 = ss.pop();
        Address s1 = ss.pop();
        if (s1.varType != s2.varType) {
            ErrorHandler.printError("The type of operands in equal operator is different");
        }
        memory.add3AddressCode(Operation.EQ, s1, s2, temp);
        ss.push(temp);
    }
}

class LessThanSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<Address> ss = cg.getSs();
        Memory memory = cg.getMemory();

        Address temp = new Address(memory.getTempIndex(), varType.Bool);
        memory.addTempIndex();
        Address s2 = ss.pop();
        Address s1 = ss.pop();
        if (s1.varType != varType.Int || s2.varType != varType.Int) {
            ErrorHandler.printError("The type of operands in less than operator is different");
        }
        memory.add3AddressCode(Operation.LT, s1, s2, temp);
        ss.push(temp);
    }
}

class AndSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<Address> ss = cg.getSs();
        Memory memory = cg.getMemory();

        Address temp = new Address(memory.getTempIndex(), varType.Bool);
        memory.addTempIndex();
        Address s2 = ss.pop();
        Address s1 = ss.pop();
        if (s1.varType != varType.Bool || s2.varType != varType.Bool) {
            ErrorHandler.printError("In and operator the operands must be boolean");
        }
        memory.add3AddressCode(Operation.AND, s1, s2, temp);
        ss.push(temp);
    }
}

class NotSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<Address> ss = cg.getSs();
        Memory memory = cg.getMemory();

        Address temp = new Address(memory.getTempIndex(), varType.Bool);
        memory.addTempIndex();
        Address s2 = ss.pop();
        Address s1 = ss.pop();
        if (s1.varType != varType.Bool) {
            ErrorHandler.printError("In not operator the operand must be boolean");
        }
        memory.add3AddressCode(Operation.NOT, s1, s2, temp);
        ss.push(temp);
    }
}

class DefClassSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<Address> ss = cg.getSs();
        Stack<String> symbolStack = cg.getSymbolStack();
        SymbolTable symbolTable = cg.getSymbolTable();

        ss.pop();
        symbolTable.addClass(symbolStack.peek());
    }
}

class DefMethodSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<Address> ss = cg.getSs();
        Stack<String> symbolStack = cg.getSymbolStack();
        SymbolTable symbolTable = cg.getSymbolTable();
        Memory memory = cg.getMemory();

        ss.pop();
        String methodName = symbolStack.pop();
        String className = symbolStack.pop();

        symbolTable.addMethod(className, methodName, memory.getCurrentCodeBlockAddress());

        symbolStack.push(className);
        symbolStack.push(methodName);
    }
}

class PopClassSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<String> symbolStack = cg.getSymbolStack();
        symbolStack.pop();
    }
}

class ExtendSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<Address> ss = cg.getSs();
        Stack<String> symbolStack = cg.getSymbolStack();
        SymbolTable symbolTable = cg.getSymbolTable();

        ss.pop();
        symbolTable.setSuperClass(symbolStack.pop(), symbolStack.peek());
    }
}

class DefFieldSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<Address> ss = cg.getSs();
        Stack<String> symbolStack = cg.getSymbolStack();
        SymbolTable symbolTable = cg.getSymbolTable();

        ss.pop();
        symbolTable.addField(symbolStack.pop(), symbolStack.peek());
    }
}

class DefVarSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<Address> ss = cg.getSs();
        Stack<String> symbolStack = cg.getSymbolStack();
        SymbolTable symbolTable = cg.getSymbolTable();

        ss.pop();

        String var = symbolStack.pop();
        String methodName = symbolStack.pop();
        String className = symbolStack.pop();

        symbolTable.addMethodLocalVariable(className, methodName, var);

        symbolStack.push(className);
        symbolStack.push(methodName);
    }
}

class MethodReturnSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<Address> ss = cg.getSs();
        Stack<String> symbolStack = cg.getSymbolStack();
        SymbolTable symbolTable = cg.getSymbolTable();
        Memory memory = cg.getMemory();

        //TODO : call ok

        String methodName = symbolStack.pop();
        Address s = ss.pop();
        SymbolType t = symbolTable.getMethodReturnType(symbolStack.peek(), methodName);
        varType temp = varType.Int;
        switch (t) {
            case Int:
                break;
            case Bool:
                temp = varType.Bool;
        }
        if (s.varType != temp) {
            ErrorHandler.printError("The type of method and return address was not match");
        }
        memory.add3AddressCode(Operation.ASSIGN, s, new Address(symbolTable.getMethodReturnAddress(symbolStack.peek(), methodName), varType.Address, TypeAddress.Indirect), null);
        memory.add3AddressCode(Operation.JP, new Address(symbolTable.getMethodCallerAddress(symbolStack.peek(), methodName), varType.Address), null, null);

        //symbolStack.pop();
    }
}

class DefParamSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<Address> ss = cg.getSs();
        Stack<String> symbolStack = cg.getSymbolStack();
        SymbolTable symbolTable = cg.getSymbolTable();

        //TODO : call Ok
        ss.pop();
        String param = symbolStack.pop();
        String methodName = symbolStack.pop();
        String className = symbolStack.pop();

        symbolTable.addMethodParameter(className, methodName, param);

        symbolStack.push(className);
        symbolStack.push(methodName);
    }
}

class LastTypeBoolSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        SymbolTable symbolTable = cg.getSymbolTable();
        symbolTable.setLastType(SymbolType.Bool);
    }
}

class LastTypeIntSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        SymbolTable symbolTable = cg.getSymbolTable();
        symbolTable.setLastType(SymbolType.Int);
    }
}

class DefMainSemanticAction implements SemanticAction {
    @Override
    public void execute(CodeGenerator cg, Token next) {
        Stack<Address> ss = cg.getSs();
        Stack<String> symbolStack = cg.getSymbolStack();
        SymbolTable symbolTable = cg.getSymbolTable();
        Memory memory = cg.getMemory();

        //ss.pop();
        memory.add3AddressCode(ss.pop().num, Operation.JP, new Address(memory.getCurrentCodeBlockAddress(), varType.Address), null, null);
        String methodName = "main";
        String className = symbolStack.pop();

        symbolTable.addMethod(className, methodName, memory.getCurrentCodeBlockAddress());

        symbolStack.push(className);
        symbolStack.push(methodName);
    }
}