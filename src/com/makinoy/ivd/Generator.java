package com.makinoy.ivd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.*;

public class Generator {
	
	public static void main(String[] args) throws IOException {
		String sampleClass = "com/makinoy/ivd/InvokeSample";
		byte[] bytes = generate(sampleClass);
		File ofile = new File("./bin/" + sampleClass + ".class");
		FileOutputStream fos = new FileOutputStream(ofile);
		fos.write(bytes);
		fos.close();
	}
	
	public static byte[] generate(String invokerClassName) {

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		
		cw.visit(Opcodes.V1_7,
				Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, // public class
				invokerClassName,
				null,
				"java/lang/Object", // extends Object
				null);
		
		addConstructor(cw);
		
		addInvokeMain("com/makinoy/test/S",
				"com/makinoy/test/C",
				"dynamicMethod",
				"(Lcom/makinoy/test/S;)V",
				cw);
		
		return cw.toByteArray();
	}

	private static void addConstructor(ClassWriter cw) {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
		mv.visitInsn(RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}
	
	private static void addInvokeMain(
			String receiverClassName,
			String concreteClassName,
			String dynamicMethodName,
			String targetMethodDesc,
			ClassWriter cw) {
		
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC,
				"main",
				"([Ljava/lang/String;)V",
				null,
				null);
		
		mv.visitCode();
		
		Handle bootstrap = createBootstrapHandle(
				"com/makinoy/ivd/Linker", 
				"bootstrap");
		
		Label startLabel = new Label();
		mv.visitLabel(startLabel);
		
		mv.visitTypeInsn(Opcodes.NEW, concreteClassName);
		mv.visitInsn(Opcodes.DUP);
		mv.visitMethodInsn(INVOKESPECIAL, concreteClassName, "<init>", "()V");
		mv.visitVarInsn(ASTORE, 1);
		mv.visitVarInsn(ALOAD, 1);
		
		mv.visitInvokeDynamicInsn(dynamicMethodName, targetMethodDesc, bootstrap);
		
		mv.visitInsn(RETURN);
		
		Label endLabel = new Label();
		mv.visitLabel(endLabel);
		
		String desc = "L" + receiverClassName + ";";
		mv.visitLocalVariable("c", desc, null, startLabel, endLabel, 1);
		mv.visitMaxs(2, 2);
		mv.visitEnd();
	}

	private static Handle createBootstrapHandle(String dynamicLinkClassName,
			String bootstrapMethodName) {
		Handle bootstrap = new Handle(H_INVOKESTATIC,
				dynamicLinkClassName, // linker
				bootstrapMethodName,  // bootstrap
				createBootstrapMethodDescriptor());
		return bootstrap;
	}

	// (MethodHandles.Lookup, String, MethodType):CallSite
	private static String createBootstrapMethodDescriptor() {
		MethodType mt = MethodType.methodType(CallSite.class,
				MethodHandles.Lookup.class,
				String.class,
				MethodType.class);
		return mt.toMethodDescriptorString();
	}
}
