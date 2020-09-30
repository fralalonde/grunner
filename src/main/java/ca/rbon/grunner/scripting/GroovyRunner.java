package ca.rbon.grunner.scripting;

import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

@Component
public class GroovyRunner {

    final ScriptEngine scriptEngine;

    public GroovyRunner() {
        ScriptEngineManager factory = new ScriptEngineManager();
        scriptEngine = factory.getEngineByName("groovy");
    }

    public Object run(String script) throws ScriptException {
        return scriptEngine.eval(script);
    }


}
