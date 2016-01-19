package edu.atilim.acma.design.io;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import edu.atilim.acma.design.Design;
import edu.atilim.acma.util.Log;
import edu.atilim.acma.util.Pair;
import edu.atilim.acma.design.Accessibility;
import edu.atilim.acma.design.Field;
import edu.atilim.acma.design.Method;
import edu.atilim.acma.design.Method.Parameter;
import edu.atilim.acma.design.Node;
import edu.atilim.acma.design.Package;
import edu.atilim.acma.design.Type;

public class WriteClass {
    private static Design design;

    public WriteClass(Design d)
     {
        design = d;
    }

    public void writeStage()
     {
        ZipFile zip = null;
        try { zip = new ZipFile(design.toString());}
         catch (Exception e) {}

        if (zip == null)
            throw new RuntimeException("ZIP file could not be read.");

        List<ZipEntry> readers = read(zip);
        int i = 0;

        for (Type t: design.getTypes())
         {
            ClassReader cr = null;
            try {
                cr = new ClassReader(zip.getInputStream(readers.get(i)));
            } catch (IOException e) {
                e.printStackTrace();}

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            cr.accept(cw, 0);

            BufferedOutputStream stream = null;
            try {
                FileOutputStream f = new FileOutputStream(new File(t.getSimpleName() + ".class"));
                stream = new BufferedOutputStream(f);
                stream.write(cw.toByteArray());
            } catch (Exception e) {
                Log.severe("Could not write to bytecode file!");
            } finally {
                if (stream != null) {
                    try { stream.flush(); stream.close();
                    } catch (IOException e) {}
                }
            }
            i++;
        }

            try {
            zip.close();}
            catch (IOException e) {
            e.printStackTrace();}
    }

    public void Info(List<ZipEntry> readers)
     {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter("INFO.txt", true));
            bw.write(design.toString() + "\r\n");
            bw.write(design.getTag() + "\r\n\n");

            for (Package p: design.getPackages())
             {
                bw.write(p.getName() + "\r\n");
                for (Type t: p.getTypes())
                 {
                    bw.write("\r\t" + t.getName() + "\r\n");
                }
            }

            bw.write("\r\n");
            for (ZipEntry r: readers)
                bw.write(r.getName() + "\r\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try { bw.close(); } catch (Exception e) {}
        }
    }

    public void afterBase()
     {
        // Any parameters that haven't been read in are given default values.
        for (Type t: design.getTypes())
         {
            ClassWriter cw = new ClassWriter(0);
            String[] interfaces = new String[t.getInterfaces().size()];
            int counter1 = 0;

            for (Type i: t.getInterfaces())
             {
                interfaces[counter1] = i.getName();
                counter1++;
            }

            if (t.getParentType() != null)
                cw.visitInnerClass(t.getName(), t.getParentType().getName(), t.getSimpleName(), getSecurity(t) + getFlags(t));
            // Version is set to V1_5 and signature set to null by default.
            else if ((t.isRootType()) || (t.getSuperType() == null))
                cw.visit(Opcodes.V1_5, getSecurity(t) + getFlags(t), t.getName(), null, "java/lang/Object", interfaces);
             else
                cw.visit(Opcodes.V1_5, getSecurity(t) + getFlags(t), t.getName(), null, t.getSuperType().getName(), interfaces);

            for (Field f: t.getFields())
             {
                // The field type isn't read in.
                cw.visitField(getSecurity(f) + getFlags(f), f.getName(), f.getFieldType(), null, null).visitEnd();
            }

            for (Method m: t.getMethods())
             {
//				Type [] types = new Type[m.getParameters().size()];
//				int counter2 = 0;
//
//				for (Parameter p: m.getParameters())
//				{
//					types[counter2] = p.getType();
//					counter2++;
//				}

                // The parameter descriptor isn't read in.
                MethodVisitor mv = cw.visitMethod(getSecurity(m) + getFlags(m), m.getName(), "(Ljava/lang/Object;)I", null, null);
                mv.visitCode();

                for (Field af: m.getAccessedFields())
                    // The opcode and field type isn't read in.
                    //mv.visitFieldInsn(Opcodes.GETFIELD, af.getOwnerType().getName(), af.getName(), "I");
                    mv.
                visitFieldInsn(Opcodes.GETFIELD, "pkg/Bean", af.getName(), "I");

                for (Method cm: m.getCalledMethods())
                    // The opcode and field type isn't read in.
                    mv.
                visitMethodInsn(Opcodes.INVOKESTATIC, cm.getOwnerType().getName(), cm.getName(), "I");

                for (Type it: m.getInstantiatedTypes())
                    // The opcode isn't read in.
                    mv.
                visitTypeInsn(Opcodes.NEW, it.getName());

                //mv.visitLocalVariable();
                //mv.visitVarInsn();
                mv.visitEnd();
            }

            cw.visitEnd();
            write(cw, t.getName());
        }
    }

    public void write(ClassWriter cw, String type)
     {
        String name = design.toString();
        int lastDash = name.lastIndexOf("\\");
        int lastDot = name.lastIndexOf(".");
        if ((lastDash >= 0) && (lastDot >= 0))
            name = name.substring(lastDash + 1, lastDot);
        name = ".\\data\\refactored\\" + name + "\\";
        String name2 = type;
        int lastDot2 = name2.lastIndexOf(".");
        String file = name2.substring(lastDot2 + 1);
        if (lastDot2 >= 0)
            name2 = name2.substring(0, lastDot2);
        name2 = name2.replace(".", "\\");

        name = name + name2;
        file = file + ".class";


        BufferedOutputStream stream = null;
        try {
            File theDir = new File(name);
            theDir.mkdirs();
            File theFile = new File(theDir, file);
            stream = new BufferedOutputStream(new FileOutputStream(theFile));
            stream.write(cw.toByteArray());
        } catch (Exception e) {
            Log.severe("Could not write to bytecode file!");
        } finally {
            if (stream != null) {
                try { stream.flush(); stream.close();
                } catch (IOException e) {}
            }
        }
    }

    public void info(String output)
     {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter("DETAILS.txt", true));
            bw.write(output + "\r\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try { bw.close(); } catch (Exception e) {}
        }
    }

    private int getSecurity(Node n)
     {
        if (n.getAccess() == Accessibility.PUBLIC)
            return Opcodes.ACC_PUBLIC;
         else if (n.getAccess() == Accessibility.PROTECTED)
            return Opcodes.ACC_PROTECTED;
         else if (n.getAccess() == Accessibility.PRIVATE)
            return Opcodes.ACC_PRIVATE;
         else
            return 0;
    }

    private int getFlags(Type t)
     {
        int value = 0;

        if (t.isInterface())
            value += Opcodes.ACC_INTERFACE;
        if (t.isAnnotation())
            value += Opcodes.ACC_ANNOTATION;
        if (t.isAbstract())
            value += Opcodes.ACC_ABSTRACT;
        if (t.isFinal())
            value += Opcodes.ACC_FINAL;
        if (t.isStatic())
            value += Opcodes.ACC_STATIC;

        return value;
    }

    private int getFlags(Node n)
     {
        int value = 0;

        if (n.isAbstract())
            value += Opcodes.ACC_ABSTRACT;
        if (n.isFinal())
            value += Opcodes.ACC_FINAL;
        if (n.isStatic())
            value += Opcodes.ACC_STATIC;

        return value;
    }

    public List<ZipEntry> read(ZipFile zip) {
        List<ZipEntry> readers = new ArrayList<ZipEntry>();

        for (Enumeration<? extends  ZipEntry> e = zip.entries(); e.hasMoreElements();)
         {
            ZipEntry ze = e.nextElement();

            if (ze.isDirectory() || !ze.getName().endsWith(".class"))
                continue;
            readers.add(ze);
        }

        return readers;
    }
}
