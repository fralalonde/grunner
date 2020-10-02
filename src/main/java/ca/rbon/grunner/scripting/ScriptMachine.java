package ca.rbon.grunner.scripting;

import org.springframework.stereotype.Component;

import javax.script.*;

@Component
public class ScriptMachine {

  final Compilable compEngine;

  public ScriptMachine() {
    ScriptEngineManager factory = new ScriptEngineManager();
    var scriptEngine = factory.getEngineByName("groovy");
    compEngine = (Compilable) scriptEngine;
  }

  public Object eval(String script) throws ScriptException {
    var compiled = compile(script);
    return compiled.eval();
  }

  public CompiledScript compile(String script) throws ScriptException {
    return compEngine.compile(script);
  }

}
