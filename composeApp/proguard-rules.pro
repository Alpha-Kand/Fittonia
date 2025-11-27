-dontobfuscate
-optimizationpasses 5
-dontwarn org.gradle.api.Plugin

-keep class * extends android.app.Activity
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}

-assumenosideeffects class java.io.PrintStream {
     public void println(%);
     public void println(**);
 }
