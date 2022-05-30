package me.sample.utils;


import me.sample.exception.InvalidUUIDException;

import java.util.Random;
import java.util.UUID;

public class UUIDHelper {

    public static UUID fromString(String name) {
        try {
            return UUID.fromString(name);
        } catch (IllegalArgumentException e) {
            throw new InvalidUUIDException(name);
        }
    }

    public static String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
}
