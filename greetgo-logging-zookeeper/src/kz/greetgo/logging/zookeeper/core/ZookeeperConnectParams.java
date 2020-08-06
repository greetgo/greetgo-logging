package kz.greetgo.logging.zookeeper.core;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class ZookeeperConnectParams {

  private IntSupplier sessionTimeoutMillis = () -> 3000;
  private IntSupplier connectTimeoutMillis = () -> 25000;
  private IntSupplier maxRetries = () -> 3;
  private IntSupplier sleepBetweenRetriesMillis = () -> 100;
  private Supplier<String> connectStr = null;

  private void check() {
    if (connectStr == null) {
      throw new RuntimeException("1H2c3x3vAb :: Not specified connectStr. Value example: `localhost:2181`");
    }
  }

  public ZookeeperConnectParams maxRetries(IntSupplier maxRetries) {
    this.maxRetries = Objects.requireNonNull(maxRetries);
    return this;
  }

  public ZookeeperConnectParams maxRetries(int maxRetries) {
    this.maxRetries = () -> maxRetries;
    return this;
  }

  public ZookeeperConnectParams sleepBetweenRetriesMillis(IntSupplier sleepBetweenRetriesMillis) {
    this.sleepBetweenRetriesMillis = Objects.requireNonNull(sleepBetweenRetriesMillis);
    return this;
  }

  public ZookeeperConnectParams sleepBetweenRetriesMillis(int sleepBetweenRetriesMillis) {
    this.sleepBetweenRetriesMillis = () -> sleepBetweenRetriesMillis;
    return this;
  }

  public ZookeeperConnectParams connectTimeoutMillis(IntSupplier connectTimeoutMillis) {
    this.connectTimeoutMillis = Objects.requireNonNull(connectTimeoutMillis);
    return this;
  }

  public ZookeeperConnectParams connectTimeoutMillis(int connectTimeoutMillis) {
    this.connectTimeoutMillis = () -> connectTimeoutMillis;
    return this;
  }

  public ZookeeperConnectParams sessionTimeoutMillis(IntSupplier sessionTimeoutMillis) {
    this.sessionTimeoutMillis = Objects.requireNonNull(sessionTimeoutMillis);
    return this;
  }

  public ZookeeperConnectParams sessionTimeoutMillis(int sessionTimeoutMillis) {
    this.sessionTimeoutMillis = () -> sessionTimeoutMillis;
    return this;
  }

  public ZookeeperConnectParams connectStr(Supplier<String> connectStr) {
    this.connectStr = Objects.requireNonNull(connectStr);
    return this;
  }

  public ZookeeperConnectParams connectStr(String connectStr) {
    Objects.requireNonNull(connectStr);
    this.connectStr = () -> connectStr;
    return this;
  }

  public CuratorFramework createClient() {
    check();

    var retryPolicy = new RetryNTimes(maxRetries.getAsInt(),
                                      sleepBetweenRetriesMillis.getAsInt());

    return CuratorFrameworkFactory.newClient(connectStr.get(),
                                             sessionTimeoutMillis.getAsInt(),
                                             connectTimeoutMillis.getAsInt(),
                                             retryPolicy);
  }
}
