package com.company; /* On my honor, I have neither given nor received unauthorized aid on this assignment */
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.ArrayList;

 public class ARMsim {
     static int pc=64,offset;
     static int[] regis=new int[32];
     static ArrayList<opcode> lines=new ArrayList<opcode>();
     static ArrayList<Integer> mem=new ArrayList<Integer>();

     public static void main(String[] args) throws Exception{

         BufferedWriter decoded = new BufferedWriter(new FileWriter("disassembly.txt"));
         BufferedWriter simulation = new BufferedWriter(new FileWriter("simulation.txt"));

         BufferedReader inp = new BufferedReader(new FileReader(args[0]));
         String line=inp.readLine();

         //op code
         while(!line.substring(0,3).equals("101")){
             opcode op=decode(line);
             lines.add(op);
             decoded.write(line+"\t"+pc+"\t"+op+"\n");
             line=inp.readLine();
             pc+=4;
         }

         //dummy
         decoded.write(line+"\t"+pc+"\tDUMMY\n");
         lines.add(new opcode("DUMMY",5,5,0,0,0));
         line=inp.readLine();
         offset=pc+4;

         //variables
         while(line!=null){
             pc+=4;
             mem.add(convert(line));
             decoded.write(line+"\t"+pc+"\t"+(int)Long.parseLong(line,2)+"\n");
             line=inp.readLine();
         }
         //execute
         pc=64;
         int cycle=1,index=0;
         while(index<lines.size()){
             simulation.write("--------------------\n");
             simulation.write("Cycle "+cycle+":\t"+pc+"\t"+lines.get(index)+"\n\n");

             //run code
             execute(lines.get(index));
             pc+=4;
             index=(pc-64)/4;
             cycle++;
             //print
             simulation.write("Registers\n");
             for(int x=0;x<4;x++){
                 if(x<2) simulation.write("X0"+x*8+":");
                 else simulation.write("X"+x*8+":");
                 for(int y=0;y<8;y++)
                     simulation.write("\t"+regis[y+x*8]);
                 simulation.newLine();
             }
             simulation.write("\nData\n");
             for(int x=0;x<mem.size()/8.0+.5;x++){
                 simulation.write(offset+32*x+":\t");
                 for(int y=0;y<8 && y+x*8<mem.size();y++)
                     if(y<7)simulation.write(mem.get(y+x*8)+"\t");
                     else simulation.write(mem.get(y+x*8)+"");
                 simulation.newLine();

             }
             if(index<lines.size()) simulation.newLine();
         }
         inp.close();
         decoded.close();
         simulation.close();
     }

     static class opcode {
         String op;
         int type,id,r1,r2,r3;

         public opcode(String o, int t,int i, int a, int b, int c){
             op=o.replace("X31", "XZR");
             type=t;
             id=i;
             r1=a;
             r2=b;
             r3=c;
         }
         public String toString(){
             return op;
         }
     }
     static opcode decode(String line){//fix zero regis
         if(line.substring(0,3).equals("001")){
             int r1 = (int)Long.parseLong(line.substring(8,13), 2);
             int r2 = convert(line.substring(13));

             if(line.substring(3,8).equals("10000"))
                 return new opcode("CBZ X"+r1+", #"+r2,1,1,r1,r2,0);//cbz 1
             else
                 return new opcode("CBNZ X"+r1+", #"+r2,1,2,r1,r2,0);//cbnz 2
         }
         else if(line.substring(0,3).equals("010")){
             int r1 = (int)Long.parseLong(line.substring(10,15), 2);
             int r2 = (int)Long.parseLong(line.substring(15,20), 2);
             int r3 = convert(line.substring(20));

             if(line.substring(3,10).equals("1000000"))
                 return new opcode("ORRI X"+r1+", X"+r2+", #"+r3,2,1,r1,r2,r3);//orri 1
             else if(line.substring(3,10).equals("1000001"))
                 return new opcode("EORI X"+r1+", X"+r2+", #"+r3,2,2,r1,r2,r3);//eorr 2
             else if(line.substring(3,10).equals("1000010"))
                 return new opcode("ADDI X"+r1+", X"+r2+", #"+r3,2,3,r1,r2,r3);//addi 3
             else if(line.substring(3,10).equals("1000011"))
                 return new opcode("SUBI X"+r1+", X"+r2+", #"+r3,2,4,r1,r2,r3);//subi 4
             else
                 return new opcode("ANDI X"+r1+", X"+r2+", #"+r3,2,5,r1,r2,r3);//andi 5

         }
         else if(line.substring(0,3).equals("011")){
             int r1 = (int)Long.parseLong(line.substring(11,16), 2);
             int r2 = (int)Long.parseLong(line.substring(16,21), 2);
             int r3 = (int)Long.parseLong(line.substring(21,26), 2);

             if(line.substring(3,11).equals("10100000"))
                 return new opcode("EOR X"+r1+", X"+r2+", X"+r3,3,1,r1,r2,r3);//eor 1
             else if(line.substring(3,11).equals("10100010"))
                 return new opcode("ADD X"+r1+", X"+r2+", X"+r3,3,2,r1,r2,r3);//add 2
             else if(line.substring(3,11).equals("10100011"))
                 return new opcode("SUB X"+r1+", X"+r2+", X"+r3,3,3,r1,r2,r3);//sub 3
             else if(line.substring(3,11).equals("10100100"))
                 return new opcode("AND X"+r1+", X"+r2+", X"+r3,3,4,r1,r2,r3);//and 4
             else if(line.substring(3,11).equals("10100101"))
                 return new opcode("ORR X"+r1+", X"+r2+", X"+r3,3,5,r1,r2,r3);//orr 5
             else if(line.substring(3,11).equals("10100110"))
                 return new opcode("LSR X"+r1+", X"+r2+", X"+r3,3,6,r1,r2,r3);//lsr 6
             else
                 return new opcode("LSL X"+r1+", X"+r2+", X"+r3,3,7,r1,r2,r3);//lsl 7
         }
         else{
             int r1 = (int)Long.parseLong(line.substring(11,16), 2);
             int r2 = (int)Long.parseLong(line.substring(16,21), 2);
             int r3 = (int)Long.parseLong(line.substring(21), 2);

             if(line.substring(3,11).equals("10101010"))
                 return new opcode("LDUR X"+r1+", [X"+r2+", #"+r3+"]",4,1,r1,r2,r3);//ldur 1
             else
                 return new opcode("STUR X"+r1+", [X"+r2+", #"+r3+"]",4,2,r1,r2,r3);//stur 2

         }
     }
     public static int convert(String s) {
         if(s.charAt(0)=='1'){
             int x = Integer.parseInt(s.replace("0", " ").replace("1", "0").replace(" ", "1"), 2);
             x = (x + 1) * -1;
             return x;
         }
         return Integer.parseInt(s, 2);
     }
     public static void execute(opcode op){
         if(op.type==1){
             if(op.id==1){
                 if(regis[op.r1]==0)
                     pc+=op.r2*4-4;
             }
             else
             if(regis[op.r1]!=0)
                 pc+=op.r2*4-4;
         }
         else if(op.type==2){
             if(op.id==1)
                 regis[op.r1]=regis[op.r2]|op.r3;
             else if(op.id==2)
                 regis[op.r1]=regis[op.r2]^op.r3;
             else if(op.id==3)
                 regis[op.r1]=regis[op.r2]+op.r3;
             else if(op.id==4)
                 regis[op.r1]=regis[op.r2]-op.r3;
             else
                 regis[op.r1]=regis[op.r2]&op.r3;
         }
         else if(op.type==3){
             if(op.id==1)
                 regis[op.r1]=regis[op.r2]^regis[op.r3];
             else if(op.id==2)
                 regis[op.r1]=regis[op.r2]+regis[op.r3];
             else if(op.id==3)
                 regis[op.r1]=regis[op.r2]-regis[op.r3];
             else if(op.id==4)
                 regis[op.r1]=regis[op.r2]|regis[op.r3];
             else if(op.id==5)
                 regis[op.r1]=regis[op.r2]>>>regis[op.r3];
             else
                 regis[op.r1]=regis[op.r2]<<regis[op.r3];
         }
         else if(op.type==4){
             if(op.id==1)
                 regis[op.r1]=mem.get(regis[op.r2]/4+(op.r3-offset)/4);
             else
                 mem.set(regis[op.r2]/4+(op.r3-offset)/4,regis[op.r1]);
         }
     }
 }