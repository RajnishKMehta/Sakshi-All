# Room KSP Compilation Error on JDK 21

When compiling the project using JDK 21 and latest KSP/Room, we encountered a crash during KSP processing:

```
> Task :sakshi-vault:kspDebugKotlin FAILED
e: [ksp] java.lang.IllegalStateException: unexpected jvm signature V

Execution failed for task ':sakshi-vault:kspDebugKotlin'.
> A failure occurred while executing com.google.devtools.ksp.gradle.KspAAWorkerAction
   > unexpected jvm signature V

Caused by: java.lang.IllegalStateException: unexpected jvm signature V
	at androidx.room.compiler.processing.javac.kotlin.JvmDescriptorUtilsKt.typeNameFromJvmSignature(JvmDescriptorUtils.kt:105)
	at androidx.room.compiler.processing.ksp.KSTypeJavaPoetExtKt.asJTypeName(KSTypeJavaPoetExt.kt:110)
    ...
```

This error seems to be caused by Room parsing a function returning `void` (JVM signature `V`) but incorrectly handling it in `ksp` for `VaultDatabase`. We switched to Java 21 toolchains, applied the appropriate `ksp` plugins for Room 2.6.1, and installed `build-tools 37.0.0` to resolve AIDL incompatibilities. However, this Room KSP error is currently blocking successful code generation.
