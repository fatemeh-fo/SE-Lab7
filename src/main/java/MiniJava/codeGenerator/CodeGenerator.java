package MiniJava.codeGenerator;

import MiniJava.Log.Log;
import MiniJava.errorHandler.ErrorHandler;
import MiniJava.scanner.token.Token;
import MiniJava.semantic.symbol.Symbol;
import MiniJava.semantic.symbol.SymbolTable;
import MiniJava.semantic.symbol.SymbolType;

import MiniJava.codeGenerator.SemanticAction.*;

import lombok.Getter;

import java.util.Stack;
import java.util.HashMap;

/**
 * Created by Alireza on 6/27/2015.
 */
public class CodeGenerator {
    @Getter
    private Memory memory = new Memory();
    @Getter
    private Stack<Address> ss = new Stack<Address>();
    @Getter
    private Stack<String> symbolStack = new Stack<>();
    @Getter
    private Stack<String> callStack = new Stack<>();
    @Getter
    private SymbolTable symbolTable;
    private SemanticAction[] actionMap = new SemanticAction[34];

    public CodeGenerator() {
        symbolTable = new SymbolTable(memory);

        actionMap[0] = new ReturnSemanticAction();
        actionMap[1] = new CheckIDSemanticAction();
        actionMap[2] = new PidSemanticAction();
        actionMap[3] = new FpidSemanticAction();
        actionMap[4] = new KpidSemanticAction();
        actionMap[5] = new IntpidSemanticAction();
        actionMap[6] = new StartCallSemanticAction();
        actionMap[7] = new CallSemanticAction();
        actionMap[8] = new ArgSemanticAction();
        actionMap[9] = new AssignSemanticAction();
        actionMap[10] = new AddSemanticAction();
        actionMap[11] = new SubSemanticAction();
        actionMap[12] = new MultSemanticAction();
        actionMap[13] = new LabelSemanticAction();
        actionMap[14] = new SaveSemanticAction();
        actionMap[15] = new WhileSemanticAction();
        actionMap[16] = new JpfSaveSemanticAction();
        actionMap[17] = new JpHereSemanticAction();
        actionMap[18] = new PrintSemanticAction();
        actionMap[19] = new EqualSemanticAction();
        actionMap[20] = new LessThanSemanticAction();
        actionMap[21] = new AndSemanticAction();
        actionMap[22] = new NotSemanticAction();
        actionMap[23] = new DefClassSemanticAction();
        actionMap[24] = new DefMethodSemanticAction();
        actionMap[25] = new PopClassSemanticAction();
        actionMap[26] = new ExtendSemanticAction();
        actionMap[27] = new DefFieldSemanticAction();
        actionMap[28] = new DefVarSemanticAction();
        actionMap[29] = new MethodReturnSemanticAction();
        actionMap[30] = new DefParamSemanticAction();
        actionMap[31] = new LastTypeBoolSemanticAction();
        actionMap[32] = new LastTypeIntSemanticAction();
        actionMap[33] = new DefMainSemanticAction();
    }

    public void printMemory() {
        memory.pintCodeBlock();
    }

    public void semanticFunction(int func, Token next) {
        Log.print("codegenerator : " + func);
        actionMap[func].execute(this, next);
    }

//    public void spid(Token next){
//        symbolStack.push(next.value);
//    }
}
