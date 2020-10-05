package ca.rbon.grunner.scripting;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.script.ScriptException;
import org.junit.Ignore;
import org.junit.Test;

public class ScriptEngineTest {

  static final String VALID_GROOVY = "(1..<1000).findAll({ 3 % 3 == 0 || 5 % 5 == 0 }).sum()";
  static final String INVALID_GROOVY = "(((.sum(";
  static final String EVIL_GROOVY = "new ServerSocket(6666)";

  @Test
  public void acceptCompileValidGroovy() throws ScriptException {
    var eng = new ScriptMachine();
    eng.compile(VALID_GROOVY);
  }

  @Test
  public void acceptEvalValidGroovy() throws Exception {
    var eng = new ScriptMachine();
    eng.eval(VALID_GROOVY);
  }

  @Test(expected = ScriptException.class)
  public void rejectCompileInvalidGroovy() throws ScriptException {
    var eng = new ScriptMachine();
    eng.compile(INVALID_GROOVY);
  }

  @Test(expected = ScriptException.class)
  public void rejectEvalInvalidGroovy() throws Exception {
    var eng = new ScriptMachine();
    eng.eval(INVALID_GROOVY);
  }

  @Ignore("Need to find a safe way to validate that security manager will deny permissions to scripts")
  @Test(expected = SecurityException.class)
  public void rejectEvalEvilGroovy() throws Exception {
    var eng = new ScriptMachine();
    SecurityManager mockSecurity = mock(SecurityManager.class);
    try {
      ProxySecurityManager.enable(mockSecurity);
      eng.eval(EVIL_GROOVY);
    } finally {
      ProxySecurityManager.disable();
    }
    verify(mockSecurity).checkPermission(any());
  }

}
