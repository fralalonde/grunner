package ca.rbon.grunner.scripting;

import java.security.*;
import java.security.cert.Certificate;
import javax.script.*;
import org.springframework.stereotype.Component;

/**
 * Handles compilation and evaluation of scripts.
 */
@Component
public class ScriptMachine {

  final Compilable compEngine;

  public ScriptMachine() {
    ScriptEngineManager factory = new ScriptEngineManager();
    var scriptEngine = factory.getEngineByName("groovy");
    compEngine = (Compilable) scriptEngine;
  }

  public Object eval(String script) throws Exception {
    var compiled = compile(script);
    return sandbox(compiled::eval);
  }

  public CompiledScript compile(String script) throws ScriptException {
    return compEngine.compile(script);
  }

  /**
   * Sandboxing is a best effort and is COMPLETELY UNTESTED (see README for
   * warnings and such)
   */
  private static <T> T sandbox(PrivilegedExceptionAction<T> action) throws ScriptException, PrivilegedActionException {
    Permissions perms = new Permissions();
    ProtectionDomain domain = new ProtectionDomain(new CodeSource(null, (Certificate[]) null), perms);
    AccessControlContext context = new AccessControlContext(new ProtectionDomain[] { domain });
    try {
      return AccessController.doPrivileged(action, context);
    } catch (PrivilegedActionException e) {
      var cause = e.getCause();
      if (cause instanceof ScriptException) {
        throw (ScriptException) cause;
      }
      throw e;
    }
  }

}
