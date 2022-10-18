// package com.iconloop.score.example;

import score.Address;
import score.Context;
import score.DictDB;
import score.annotation.External;
import score.annotation.Payable;

import java.math.BigInteger;

public class SimpleBank {
  private final DictDB<Address, BigInteger> balances;

  public SimpleBank(
    // BigInteger _fund
    ) {
    // Context.require(_fund.compareTo(BigInteger.ZERO) >=0);
    // this.balances = Context.newDictDB();
    this.balances = Context.newDictDB("balances", BigInteger.class);
  }

  @External
  @Payable
  public BigInteger deposit() {
    Address caller = Context.getCaller();
    BigInteger value = Context.getValue();
    this.balances.set(caller, value);
    return this.balances.get(caller);
  }

  @External(readonly=true)
  public BigInteger getBalance(Address _address) {
    return this.balances.get(_address);
  }
}