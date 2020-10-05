package ca.rbon.grunner.scripting;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * FIXME Not pretty, but trying to use this still makes more tests fail than
 * expected. Why?
 */
public class ProxySecurityManager extends SecurityManager {

  static SecurityManager parent;

  static AtomicInteger refCount = new AtomicInteger(0);

  private ThreadLocal<SecurityManager> secman = ThreadLocal.withInitial(() -> parent);

  static synchronized void enable(SecurityManager mockSec) {
    ProxySecurityManager proxy;
    if (refCount.incrementAndGet() == 1) {
      parent = System.getSecurityManager();
      proxy = new ProxySecurityManager();
      System.setSecurityManager(proxy);
    } else {
      // already installed
      proxy = (ProxySecurityManager) System.getSecurityManager();
    }
    proxy.secman.set(mockSec);
  }

  static synchronized void disable() {
    var proxy = (ProxySecurityManager) System.getSecurityManager();
    if (refCount.decrementAndGet() == 0) {
      // final user quit, uninstalling proxy
      System.setSecurityManager(parent);
    } else {
      // still required by other threads, just reestablish parent
      proxy.secman.set(parent);
    }
  }

}