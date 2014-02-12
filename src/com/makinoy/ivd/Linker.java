package com.makinoy.ivd;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

import com.makinoy.test.C;

public class Linker {
	
	public static CallSite bootstrap(
			Lookup lookup,
			String dynamicMethodName,
			MethodType type) throws NoSuchMethodException, IllegalAccessException {
		
		Class<?> receiverType = type.parameterType(0);
		
		System.out.println("Caller:" + lookup.toString());
		System.out.println("Dynamic Method Name:" + dynamicMethodName);
		System.out.println("Receiver Type:" + receiverType);
		
		MethodHandle target = lookup.findVirtual(C.class, "m$o", MethodType.methodType(void.class));
		
		return new ConstantCallSite(target.asType(type));
	}
}
