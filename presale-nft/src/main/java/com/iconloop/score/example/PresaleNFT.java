/*
 * Copyright 2020 ICONLOOP Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iconloop.score.example;

import com.iconloop.score.util.EnumerableIntMap;
import com.iconloop.score.token.irc3.IRC3Basic;
import com.iconloop.score.util.IntSet;

import score.Address;
import score.Context;
import score.DictDB;
import score.VarDB;
import score.ArrayDB;
import score.BranchDB;

// import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Payable;

import java.math.BigInteger;
import java.util.List;

// import java.time.Instant;

public class PresaleNFT extends IRC3Basic {
    private final VarDB<BigInteger> start = Context.newVarDB("start", BigInteger.class);
    private final VarDB<BigInteger> end = Context.newVarDB("end", BigInteger.class);
    private final VarDB<BigInteger> tokenAmount = Context.newVarDB("tokenAmount", BigInteger.class);
    private final VarDB<BigInteger> ownerBalance = Context.newVarDB("ownerBalance", BigInteger.class);
    private final EnumerableIntMap<String> data = new EnumerableIntMap<>("data", String.class);
    private final EnumerableIntMap<BigInteger> price = new EnumerableIntMap<>("price", BigInteger.class);
    private final ArrayDB<Address> whitesList = Context.newArrayDB("whitesList", Address.class);
    private final DictDB<Address, IntSet> holdTokens = Context.newDictDB("holdTokens", IntSet.class);
    private final BranchDB<BigInteger, ArrayDB<Address>> tokenOwners = Context.newBranchDB("tokenOwners",
            Address.class);
    private final EnumerableIntMap<String> tokenURLs = new EnumerableIntMap<>("tokenURL", String.class);
    private final VarDB<BigInteger> temp = Context.newVarDB("temp", BigInteger.class);

    public PresaleNFT(String _name, String _symbol) {
        super(_name, _symbol);
        this.start.set(BigInteger.ZERO);
        this.end.set(BigInteger.ZERO);
        this.tokenAmount.set(BigInteger.ZERO);
        this.ownerBalance.set(BigInteger.ZERO);
    }

    @External(readonly = true)
    public Address getOwner() {
        return Context.getOwner();
    }

    @External(readonly = true)
    public BigInteger getStartTime() {
        return start.get();
    }

    @External(readonly = true)
    public BigInteger getEndTime() {
        return end.get();
    }

    @External(readonly = true)
    public BigInteger getBalance() {
        return ownerBalance.get();
    }

    @External(readonly = true)
    public BigInteger getDataAmount() {
        return BigInteger.valueOf(data.length());
    }

    @External(readonly = true)
    public BigInteger total() {
        return this.tokenAmount.get();
    }

    @External(readonly = true)
    public List<Address> getWhiteList() {
        int len = this.whitesList.size();
        Address[] array = new Address[len];
        for (int i = 0; i < len; i++) {
            array[i] = this.whitesList.get(i);
        }
        return List.of(array);
    }

    @External(readonly = true)
    public List<String> getData() {
        int len = this.data.length();
        String[] array = new String[len];
        for (int i = 0; i < len; i++) {
            var _data = data.get(BigInteger.valueOf(i));
            var _price = price.get(BigInteger.valueOf(i));
            array[i] = "{\"url\":\"" + _data + "\",\"price\":" + _price + ", \"id\":" + i + "}";
        }
        return List.of(array);
    }

    @External(readonly = true)
    public String getDataAtIndex(BigInteger _index) {
        return data.getOrThrow(_index, "Non exist index");
    }

    @External(readonly = true)
    public int getHoldingTokenLengthOfCaller(Address address) {
        var tokens = this.holdTokens.get(address);
        Context.require(tokens != null, "This address didn't mint any token before.");
        return tokens.length();
    }

    @External(readonly = true)
    public List<String> getTokensOfCaller(Address address) {
        // var caller = Context.getCaller();
        var tokens = this.holdTokens.get(address);
        Context.require(tokens != null, "This address didn't mint any token before.");
        int len = tokens.length();
        String[] array = new String[len];
        for (int i = 0; i < len; i++) {
            var _data = tokenURLs.get(tokens.at(i));
            array[i] = "{\"url\":\"" + _data + ", \"id\":" + i + "}";
        }
        return List.of(array);
    }

    @External(readonly = true)
    public String getGeneralData() {
        var owner = Context.getOwner();
        var startAt = start.get();
        var endAt = end.get();
        var dataAmount = BigInteger.valueOf(data.length());
        var mintAmount = tokenAmount.get();
        return "{\"owner\":\"" + owner
                + "\", \"startAt\":" + startAt
                + ", \"endAt\":" + endAt
                + ", \"dataAmount\":" + dataAmount
                + ", \"mintAmount\":" + mintAmount + "}";
    }

    @External
    public void setPresaleTime(BigInteger _startTime, BigInteger _endTime) {
        onlyOwner();
        Context.require(_startTime.compareTo(_endTime) < 0, "The start time have to larger than the end time");
        this.start.set(_startTime);
        this.end.set(_endTime);
    }

    @External
    public void addToken(String _dataURL, BigInteger _price) {
        // Update data URL and Price.
        onlyOwner();
        Context.require(_dataURL != null, "Data URL should not be null");
        Context.require(_price.compareTo(BigInteger.ZERO) > 0, "Price should not be less than 0");
        this.data.set(BigInteger.valueOf(this.data.length()), _dataURL);
        this.price.set(BigInteger.valueOf(this.price.length()), _price);
    }

    @External
    public void registerWhiteUser() {
        var caller = Context.getCaller();
        Context.require(!_isWhiteUser(caller), "This address have register before");
        this.whitesList.add(caller);
    }

    @External
    @Payable
    public void mintDataID(BigInteger _id) {
        var caller = Context.getCaller();
        // var time = BigInteger.valueOf(Context.getTransactionTimestamp());
        var value = Context.getValue();
        // Context.require(_startedPresale(time), "Cannot mint token. The presale is
        // notstarted. Come back later");
        // Context.require(!_endedPresale(time), "Cannot mint token. The presale is
        // ended");
        Context.require(!_tokenIsMinted(_id, caller),
                "Cannot mint token. This token is minted by this address before.");
        Context.require(_isWhiteUser(caller), "Cannot mint token. User is not in the white list");
        Context.require(price.get(_id).equals(value), "Value should equals to price");
        // Get value and deposit it
        this.temp.set(value);
        ownerBalance.set(this.ownerBalance.get().add(value));

        // Increase tokenAmount
        tokenAmount.set(tokenAmount.get().add(BigInteger.ONE));

        // Add token to the tokens list of the caller
        var tokens = holdTokens.get(caller);
        if (tokens == null) {
            tokens = new IntSet(tokenAmount.get().toString());
            holdTokens.set(caller, tokens);
        } else {
            tokens.add(tokenAmount.get());
        }

        // Add address to the owners list of this token
        tokenOwners.at(_id).add(caller);

        // Mint a new token with an ID is tokenAmount
        super._mint(caller, tokenAmount.get());

        // Map tokenId to URL
        tokenURLs.set(tokenAmount.get(), data.get(_id));

        // Trigger event
        // Mint(tokenAmount.get(), caller);
    }

    private boolean _tokenIsMinted(BigInteger _dataId, Address _address) {
        var tokens = tokenOwners.at(_dataId);
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).equals(_address)) {
                return true;
            }
        }
        return false;
    }

    private boolean _isWhiteUser(Address address) {
        for (int i = 0; i < this.whitesList.size(); i++) {
            if (address.equals(this.whitesList.get(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean _startedPresale(BigInteger now) {
        return now.compareTo(start.get()) > 0;
    }

    private boolean _endedPresale(BigInteger now) {
        return now.compareTo(end.get()) > 0;
    }

    private void onlyOwner() {
        Context.require(Context.getOwner().equals(Context.getCaller()), "This method is only called by the owner");
    }

    // @EventLog(indexed = 3)
    // public void Mint(BigInteger _tokenId, Address _owner) {
    // }
}