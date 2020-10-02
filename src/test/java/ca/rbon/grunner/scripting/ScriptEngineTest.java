package ca.rbon.grunner.scripting;

import org.junit.Test;

import javax.script.ScriptException;

public class ScriptEngineTest {

  static final String VALID_GROOVY = "(1..<1000).findAll({ 3 % 3 == 0 || 5 % 5 == 0 }).sum()";
  static final String INVALID_GROOVY = "(((.sum(";

  @Test
  public void acceptCompileValidGroovy() throws ScriptException {
    var eng = new ScriptMachine();
    eng.compile(VALID_GROOVY);
  }

  @Test
  public void acceptEvalValidGroovy() throws ScriptException {
    var eng = new ScriptMachine();
    eng.eval(VALID_GROOVY);
  }

  @Test(expected = ScriptException.class)
  public void rejectCompileInvalidGroovy() throws ScriptException {
    var eng = new ScriptMachine();
    eng.compile(INVALID_GROOVY);
  }

  @Test(expected = ScriptException.class)
  public void rejectEvalInvalidGroovy() throws ScriptException {
    var eng = new ScriptMachine();
    eng.eval(INVALID_GROOVY);
  }
}