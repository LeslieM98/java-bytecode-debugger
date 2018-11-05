import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;



public class OpStackViewer{

    private List<String> instructions;
    private int instructionCounter;

    private Pair<Integer, String> prevInstr;

    private List<Pair<Type, String>> operandStack;
    private HashMap<String, Integer> labels;

    public static void main(String[] args) {
        OpStackViewer instance = new OpStackViewer();
        instance.start(args);
    }

    public OpStackViewer(){
        operandStack = new ArrayList<>();
        instructionCounter = 0;
        prevInstr = null;
    }


    public void start(String[] args) {
        Path p = Paths.get("test.j"); // TODO: remmove hardcoded val

        try{
            instructions = Files.readAllLines(p);
        }catch(Exception e){ 
            System.exit(1);
        }

        scanLabels();

        Scanner in = new Scanner(System.in);
        String read = "1";
        while(!read.equals("q") && instructionCounter < instructions.size()){

            showInstructions();
            read = in.nextLine();
            switch (read) {
                case "n":
                    execute();
                    break;
                case "p":
                    printStack();
                case "r":
                    executeRemaining();
                default:
                    if(read.contains("j")){
                        execute(Integer.parseInt(read.replace('j', ' ').trim()));
                    }
                    break;
            }
        }
    }

    private void scanLabels(){
        labels = new HashMap<>();
        int pos;
        String name;
        int line = 0;
        for (String x : instructions) {
            String tmp = x.trim();
            line++;
            pos = tmp.indexOf(':');
            if(pos < 0) continue;

            name = tmp.substring(0, pos);
            labels.put(name, line);
        }
    }

    private static final String LINE = "--------------";

    private void showInstructions(){
        StringBuilder sb = new StringBuilder();
        if(prevInstr != null){
            sb.append("prev instr: " + prevInstr.getRight() + " (" + prevInstr.getLeft() + ")");
        } else {
            sb.append("prev instr: " + " ()");
        }
        System.out.println(sb.toString());

        sb = new StringBuilder();
        sb.append("curr instr: " + instructions.get(instructionCounter) + " (" + instructionCounter + ")");
        System.out.println(sb.toString());
        System.out.println(LINE);
    }

    private void execute() {
        prevInstr = new Pair<Integer,String>(instructionCounter, instructions.get(instructionCounter));

        execute(instructions.get(instructionCounter));
        instructionCounter++;
    }

    private void execute(int ic) {
        while(instructionCounter != ic){
            execute();
        }
    }

    private void executeRemaining() {
        while(instructionCounter < instructions.size()){
            execute();
        }
    }

    private void execute(String instr) {
        if(instr.contains("ifeq")){
            ifeq(instr);
        } else if(instr.contains("getstatic")){
            getstatic(instr);
        } else if(instr.contains("invokevirtual")){
            invokevirtual(instr);
        } else if(instr.contains("ifne")){
            ifne(instr);
        } else if(instr.contains("dcmpg")){
            dcmpg(instr);
        } else if(instr.contains("dconst")){
            dconst(instr);
        } else if(instr.contains("goto")){
            gotoLabel(instr);
        } else if(instr.contains("iand")){
            iand(instr);
        } else if(instr.contains("ior")){
            ior(instr);
        } else if(instr.contains("iflt")){
            iflt(instr);
        } else if(instr.contains("ifgt")){
            ifgt(instr);
        } else if(instr.contains("ifle")){
            ifle(instr);
        } else if(instr.contains("ifge")){
            ifge(instr);
        } else if(instr.contains("ldc2_w")){
            ldc2_w(instr);
        }
    }

    private Pair<Type, String> pop(){
        int lastindex = operandStack.size()-1;
        Pair<Type, String> tmp = operandStack.get(lastindex);
        operandStack.remove(lastindex);
        return tmp;
    }

    private Pair<Type, String> pop(Type expected) {
        Pair<Type, String> tmp = pop();

        if(tmp.getLeft() != expected){
            throw new IllegalStateException("Expected " + expected.toString() + " but was " + tmp.getLeft().toString() + " (" + instructionCounter + ")");
        }

        return tmp;
    }

    private void push(Pair<Type, String> o){
        operandStack.add(o);
    }

    private void print(String s){
        // TODO: implement
    }

    private char typeToChar(Type t){
        char c;
        switch (t) {
            case INT:
                c = 'I';
                break;
            case LONG:
                c = 'J';
                break;
            case FLOAT:
                c = 'F';
                break;
            case DOUBLE:
                c = 'D';
                break;
            case BYTE:
                c = 'B';
                break;
            case SHORT:
                c = 'S';
                break;
            case CHAR:
                c = 'C';
                break;
            case BOOLEAN:
                c = 'Z';
                break;
            case REFERENCE:
                c = 'a';
                break;
            case VOID:
                c = 'V';
                break;
            default:
                c = 0;
                break;
        }
        return c;
    }

    private void printStack(){
        Pair<Type, String> tmp;
        StringBuilder sb;
        for (int i = operandStack.size()-1; i >= 0; i--) {
            sb = new StringBuilder();
            tmp = operandStack.get(i);
            sb.append(typeToChar(tmp.getLeft()))
                .append(":")
                .append(tmp.getRight());
            
            System.out.println(sb.toString());
        }

        System.out.println(LINE);
    }








    // Actual instructions

    private void ifeq(String instr) {
        Pair<Type, String> tmp;
        tmp = pop(Type.INT);

        int val = Integer.parseInt(tmp.getRight());
        String lbl = instr.substring(4).trim();

        if(val == 0){
            instructionCounter = labels.get(lbl);
        }
    }

    private void getstatic(String instr) {
        push(new Pair<>(Type.REFERENCE, ""));
    }

    private void invokevirtual(String instr) {
        Pair<Type, String> tmp = pop(Type.REFERENCE);

        tmp = pop(Type.DOUBLE);

        print(tmp.getRight());
    }

    private void ifne(String instr) {
        Pair<Type, String> tmp;
        tmp = pop(Type.INT);

        int val = Integer.parseInt(tmp.getRight());
        String lbl = instr.substring(4).trim();

        if(val != 0){
            instructionCounter = labels.get(lbl);
        }
    }

    private void dcmpg(String instr) {
        Pair<Type, String> op1, op2;

        op1 = pop(Type.DOUBLE);
        op2 = pop(Type.DOUBLE);

        Double val1, val2;
        val1 = Double.parseDouble(op1.getRight());
        val2 = Double.parseDouble(op2.getRight());

        push(new Pair<>(Type.INT, "" + val1.compareTo(val2)));
    }

    private void dconst(String instr) {
        String val = instr.substring(6).trim();

        push(new Pair<>(Type.DOUBLE, val));
    }

    private void gotoLabel(String instr) {
        String lbl = instr.substring(4).trim();

        instructionCounter = labels.get(lbl);
    }

    private void iand(String instr) {
        Pair<Type, String> tmp = pop(Type.INT);
        int val1, val2;

        val1 = Integer.parseInt(tmp.getRight());
        
        tmp = pop(Type.INT);
        val2 = Integer.parseInt(tmp.getRight());

        int result = val1 & val2;
        push(new Pair<>(Type.INT, "" + result));
    }
    
    private void ior(String instr) {
        Pair<Type, String> tmp = pop(Type.INT);
        int val1, val2;

        val1 = Integer.parseInt(tmp.getRight());
        
        tmp = pop(Type.INT);
        val2 = Integer.parseInt(tmp.getRight());

        int result = val1 | val2;
        push(new Pair<>(Type.INT, "" + result));
    }

    private void iflt(String instr) {
        Pair<Type, String> tmp = pop(Type.INT);

        int val = Integer.parseInt(tmp.getRight());
        String lbl = instr.substring(4).trim();
        if(val < 0){
            instructionCounter = labels.get(lbl);
        }
    }

    private void ifgt(String instr) {
        Pair<Type, String> tmp = pop(Type.INT);

        int val = Integer.parseInt(tmp.getRight());
        String lbl = instr.substring(4).trim();
        if(val > 0){
            instructionCounter = labels.get(lbl);
        }
    }

    private void ifle(String instr) {
        Pair<Type, String> tmp = pop(Type.INT);

        int val = Integer.parseInt(tmp.getRight());
        String lbl = instr.substring(4).trim();
        if(val <= 0){
            instructionCounter = labels.get(lbl);
        }
    }

    private void ifge(String instr) {
        Pair<Type, String> tmp = pop(Type.INT);

        int val = Integer.parseInt(tmp.getRight());
        String lbl = instr.substring(4).trim();
        if(val >= 0){
            instructionCounter = labels.get(lbl);
        }
    }

    private void ldc2_w(String instr) {
        String val = instr.substring(6).trim();

        push(new Pair(Type.DOUBLE, val));
    }
}