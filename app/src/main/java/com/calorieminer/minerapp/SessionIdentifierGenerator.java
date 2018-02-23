package com.calorieminer.minerapp;

import java.math.BigInteger;
import java.security.SecureRandom;

class SessionIdentifierGenerator {

    private SecureRandom random = new SecureRandom();

    String nextSessionId() {
        return new BigInteger(130, random).toString(32);
    }
}
